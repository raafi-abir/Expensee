package com.expensee.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.provider.Settings
import com.expensee.data.AppDatabase
import com.expensee.data.model.BudgetEntity
import com.expensee.data.model.CategoryEntity
import com.expensee.data.model.DeletedRecordEntity
import com.expensee.data.model.SavingsGoalEntity
import com.expensee.data.model.SubscriptionEntity
import com.expensee.data.model.TransactionEntity
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.UserRecoverableAuthException
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

@OptIn(FlowPreview::class)
class DriveSyncManager private constructor(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val database = AppDatabase.getInstance(context)
    private val driveApi = DriveApiService()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val payloadAdapter = moshi.adapter(ExpenseeSyncPayload::class.java)

    private val syncRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val deviceId: String by lazy {
        try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                ?: UUID.randomUUID().toString()
        } catch (e: Exception) {
            UUID.randomUUID().toString()
        }
    }

    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    init {
        // Debounce rapid database mutations (e.g. typing or batch adding transactions)
        scope.launch {
            syncRequests
                .debounce(1500L)
                .collect {
                    performSyncInternal()
                }
        }

        registerNetworkListener()
    }

    private fun registerNetworkListener() {
        try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            if (connectivityManager != null) {
                val request = NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build()

                networkCallback = object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        triggerSync()
                    }
                }
                connectivityManager.registerNetworkCallback(request, networkCallback!!)
            }
        } catch (e: Exception) {
            // Permission or system failure; sync will still trigger on app actions
        }
    }

    fun triggerSync() {
        scope.launch {
            syncRequests.emit(Unit)
        }
    }

    fun isOnline(): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
            val activeNetwork = cm.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun performSyncInternal() = withContext(Dispatchers.IO) {
        val userProfileDao = database.userProfileDao()
        val profile = userProfileDao.getProfileDirect() ?: return@withContext

        // Only sync if user connected a Google account and enabled sync
        if (!profile.isCloudSyncEnabled || profile.googleAccountEmail.isNullOrBlank()) {
            return@withContext
        }

        val accountEmail = profile.googleAccountEmail

        if (!isOnline()) {
            userProfileDao.updateSyncStatus("OFFLINE")
            return@withContext
        }

        userProfileDao.updateSyncStatus("SYNCING")

        try {
            // 1. Acquire Google Drive OAuth token for appDataFolder scope
            val token = acquireOAuthToken(accountEmail)
            if (token == null) {
                userProfileDao.updateSyncStatus("AUTH_REQUIRED")
                return@withContext
            }

            // 2. Check if a sync file exists in Drive appDataFolder
            val existingFileId = driveApi.findSyncFileId(token)

            var remotePayload: ExpenseeSyncPayload? = null
            if (existingFileId != null) {
                val rawDownloaded = driveApi.downloadSyncFile(token, existingFileId)
                if (!rawDownloaded.isNullOrBlank()) {
                    val decryptedJson = SyncSecurityManager.decryptPayload(rawDownloaded)
                    remotePayload = try {
                        payloadAdapter.fromJson(decryptedJson)
                    } catch (e: Exception) {
                        null
                    }
                }
            }

            // 3. Two-Way Non-Destructive Merge
            mergeRemoteChangesToLocal(remotePayload)

            // 4. Construct updated payload from local database
            val updatedPayload = buildLocalSyncPayload()
            val jsonToUpload = payloadAdapter.toJson(updatedPayload)
            val encryptedPayload = SyncSecurityManager.encryptPayload(jsonToUpload)

            // 5. Upload to Drive appDataFolder
            if (existingFileId != null) {
                driveApi.updateSyncFile(token, existingFileId, encryptedPayload)
            } else {
                driveApi.uploadNewSyncFile(token, encryptedPayload)
            }

            // 6. Update local status
            userProfileDao.updateSyncSuccess(System.currentTimeMillis())

        } catch (e: UserRecoverableAuthException) {
            userProfileDao.updateSyncStatus("AUTH_REQUIRED")
        } catch (e: DriveAuthException) {
            userProfileDao.updateSyncStatus("AUTH_REQUIRED")
        } catch (e: Exception) {
            userProfileDao.updateSyncStatus("ERROR")
        }
    }

    private suspend fun acquireOAuthToken(accountEmail: String): String? = withContext(Dispatchers.IO) {
        try {
            GoogleAuthUtil.getToken(
                context,
                accountEmail,
                "oauth2:${DriveApiService.DRIVE_APP_DATA_SCOPE}"
            )
        } catch (e: UserRecoverableAuthException) {
            // Requires user consent or re-auth
            throw e
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun mergeRemoteChangesToLocal(remote: ExpenseeSyncPayload?) = withContext(Dispatchers.IO) {
        if (remote == null) return@withContext

        val transactionDao = database.transactionDao()
        val categoryDao = database.categoryDao()
        val budgetDao = database.budgetDao()
        val savingsGoalDao = database.savingsGoalDao()
        val subscriptionDao = database.subscriptionDao()
        val deletedDao = database.deletedRecordDao()

        val allCategories: List<CategoryEntity> = categoryDao.getAllCategoriesDirect()
        val categoryMap: Map<String, CategoryEntity> = allCategories.associateBy { it.name.lowercase() }

        // Local deleted records list to avoid re-inserting deleted items
        val locallyDeletedIds = deletedDao.getAllDeletedSyncIds().toSet()

        // 1. Merge Transactions
        val localTransactions = transactionDao.getAllTransactionsDirect()
        val localTxMap = localTransactions.associateBy { it.syncId }

        for (remoteTx in remote.transactions) {
            if (remoteTx.syncId.isBlank() || locallyDeletedIds.contains(remoteTx.syncId)) continue

            val localTx = localTxMap[remoteTx.syncId]
            if (remoteTx.isDeleted) {
                if (localTx != null) {
                    transactionDao.deleteTransaction(localTx)
                    deletedDao.recordDeleted(DeletedRecordEntity(remoteTx.syncId, "TRANSACTION"))
                }
            } else if (localTx == null) {
                // New transaction from another device
                val cat = categoryMap[remoteTx.categoryName.lowercase()]
                    ?: allCategories.firstOrNull { it.type == remoteTx.type }
                    ?: allCategories.firstOrNull()

                if (cat != null) {
                    transactionDao.insertTransaction(
                        TransactionEntity(
                            id = 0,
                            syncId = remoteTx.syncId,
                            amount = remoteTx.amount,
                            type = remoteTx.type,
                            categoryId = cat.id,
                            date = remoteTx.date,
                            note = remoteTx.note,
                            paymentMethod = remoteTx.paymentMethod,
                            createdAt = remoteTx.createdAt,
                            updatedAt = remoteTx.updatedAt
                        )
                    )
                }
            } else if (remoteTx.updatedAt > localTx.updatedAt) {
                // Remote transaction has newer edits
                val cat = categoryMap[remoteTx.categoryName.lowercase()]
                    ?: categoryDao.getCategoryById(localTx.categoryId)
                    ?: allCategories.firstOrNull()

                transactionDao.updateTransaction(
                    localTx.copy(
                        amount = remoteTx.amount,
                        type = remoteTx.type,
                        categoryId = cat?.id ?: localTx.categoryId,
                        date = remoteTx.date,
                        note = remoteTx.note,
                        paymentMethod = remoteTx.paymentMethod,
                        updatedAt = remoteTx.updatedAt
                    )
                )
            }
        }

        // 2. Merge Budgets
        val localBudgets = budgetDao.getAllBudgetsDirect()
        val localBudgetMap = localBudgets.associateBy { it.syncId }

        for (remoteBudget in remote.budgets) {
            if (remoteBudget.syncId.isBlank() || locallyDeletedIds.contains(remoteBudget.syncId)) continue

            val localBudget = localBudgetMap[remoteBudget.syncId]
            if (remoteBudget.isDeleted) {
                if (localBudget != null) {
                    budgetDao.deleteBudget(localBudget)
                    deletedDao.recordDeleted(DeletedRecordEntity(remoteBudget.syncId, "BUDGET"))
                }
            } else if (localBudget == null) {
                val catId = remoteBudget.categoryName?.let { cName: String -> categoryMap[cName.lowercase()]?.id }
                budgetDao.insertBudget(
                    BudgetEntity(
                        id = 0,
                        syncId = remoteBudget.syncId,
                        categoryId = catId,
                        amount = remoteBudget.amount,
                        month = remoteBudget.month,
                        year = remoteBudget.year,
                        createdAt = remoteBudget.createdAt,
                        updatedAt = remoteBudget.updatedAt
                    )
                )
            } else if (remoteBudget.updatedAt > localBudget.updatedAt) {
                val catId = remoteBudget.categoryName?.let { cName: String -> categoryMap[cName.lowercase()]?.id }
                budgetDao.updateBudget(
                    localBudget.copy(
                        categoryId = catId,
                        amount = remoteBudget.amount,
                        month = remoteBudget.month,
                        year = remoteBudget.year,
                        updatedAt = remoteBudget.updatedAt
                    )
                )
            }
        }

        // 3. Merge Savings Goals
        val localGoals = savingsGoalDao.getAllGoalsDirect()
        val localGoalMap = localGoals.associateBy { it.syncId }

        for (remoteGoal in remote.goals) {
            if (remoteGoal.syncId.isBlank() || locallyDeletedIds.contains(remoteGoal.syncId)) continue

            val localGoal = localGoalMap[remoteGoal.syncId]
            if (remoteGoal.isDeleted) {
                if (localGoal != null) {
                    savingsGoalDao.deleteGoal(localGoal)
                    deletedDao.recordDeleted(DeletedRecordEntity(remoteGoal.syncId, "GOAL"))
                }
            } else if (localGoal == null) {
                savingsGoalDao.insertGoal(
                    SavingsGoalEntity(
                        id = 0,
                        syncId = remoteGoal.syncId,
                        name = remoteGoal.name,
                        targetAmount = remoteGoal.targetAmount,
                        currentAmount = remoteGoal.currentAmount,
                        deadline = remoteGoal.deadline,
                        iconName = remoteGoal.iconName,
                        createdAt = remoteGoal.createdAt,
                        updatedAt = remoteGoal.updatedAt
                    )
                )
            } else if (remoteGoal.updatedAt > localGoal.updatedAt) {
                savingsGoalDao.updateGoal(
                    localGoal.copy(
                        name = remoteGoal.name,
                        targetAmount = remoteGoal.targetAmount,
                        currentAmount = remoteGoal.currentAmount,
                        deadline = remoteGoal.deadline,
                        iconName = remoteGoal.iconName,
                        updatedAt = remoteGoal.updatedAt
                    )
                )
            }
        }

        // 4. Merge Subscriptions
        val localSubs = subscriptionDao.getAllSubscriptionsDirect()
        val localSubMap = localSubs.associateBy { it.syncId }

        for (remoteSub in remote.subscriptions) {
            if (remoteSub.syncId.isBlank() || locallyDeletedIds.contains(remoteSub.syncId)) continue

            val localSub = localSubMap[remoteSub.syncId]
            if (remoteSub.isDeleted) {
                if (localSub != null) {
                    subscriptionDao.deleteSubscription(localSub)
                    deletedDao.recordDeleted(DeletedRecordEntity(remoteSub.syncId, "SUBSCRIPTION"))
                }
            } else if (localSub == null) {
                val catId = remoteSub.categoryName?.let { cName: String -> categoryMap[cName.lowercase()]?.id }
                subscriptionDao.insertSubscription(
                    SubscriptionEntity(
                        id = 0,
                        syncId = remoteSub.syncId,
                        name = remoteSub.name,
                        amount = remoteSub.amount,
                        billingCycle = remoteSub.billingCycle,
                        nextPaymentDate = remoteSub.nextPaymentDate,
                        categoryId = catId,
                        active = remoteSub.active,
                        createdAt = remoteSub.createdAt,
                        updatedAt = remoteSub.updatedAt
                    )
                )
            } else if (remoteSub.updatedAt > localSub.updatedAt) {
                val catId = remoteSub.categoryName?.let { cName: String -> categoryMap[cName.lowercase()]?.id }
                subscriptionDao.updateSubscription(
                    localSub.copy(
                        name = remoteSub.name,
                        amount = remoteSub.amount,
                        billingCycle = remoteSub.billingCycle,
                        nextPaymentDate = remoteSub.nextPaymentDate,
                        categoryId = catId,
                        active = remoteSub.active,
                        updatedAt = remoteSub.updatedAt
                    )
                )
            }
        }
    }

    private suspend fun buildLocalSyncPayload(): ExpenseeSyncPayload = withContext(Dispatchers.IO) {
        val profile = database.userProfileDao().getProfileDirect()
        val allCats: List<CategoryEntity> = database.categoryDao().getAllCategoriesDirect()
        val categories: Map<Long, CategoryEntity> = allCats.associateBy { it.id }
        val transactions = database.transactionDao().getAllTransactionsDirect()
        val budgets = database.budgetDao().getAllBudgetsDirect()
        val goals = database.savingsGoalDao().getAllGoalsDirect()
        val subscriptions = database.subscriptionDao().getAllSubscriptionsDirect()
        val deletedRecords = database.deletedRecordDao().getAllDeleted()

        val syncTxs = transactions.map { tx ->
            SyncTransaction(
                syncId = tx.syncId.ifBlank { UUID.randomUUID().toString() },
                amount = tx.amount,
                type = tx.type,
                categoryName = categories[tx.categoryId]?.name ?: "Other",
                date = tx.date,
                note = tx.note,
                paymentMethod = tx.paymentMethod,
                createdAt = tx.createdAt,
                updatedAt = tx.updatedAt,
                isDeleted = false
            )
        } + deletedRecords.filter { it.entityType == "TRANSACTION" }.map { del ->
            SyncTransaction(
                syncId = del.syncId,
                amount = 0.0,
                type = "EXPENSE",
                categoryName = "",
                date = 0L,
                createdAt = del.deletedAt,
                updatedAt = del.deletedAt,
                isDeleted = true
            )
        }

        val syncBudgets = budgets.map { b ->
            SyncBudget(
                syncId = b.syncId.ifBlank { UUID.randomUUID().toString() },
                categoryName = b.categoryId?.let { cid: Long -> categories[cid]?.name },
                amount = b.amount,
                month = b.month,
                year = b.year,
                createdAt = b.createdAt,
                updatedAt = b.updatedAt,
                isDeleted = false
            )
        } + deletedRecords.filter { it.entityType == "BUDGET" }.map { del ->
            SyncBudget(
                syncId = del.syncId,
                amount = 0.0,
                month = 1,
                year = 2026,
                createdAt = del.deletedAt,
                updatedAt = del.deletedAt,
                isDeleted = true
            )
        }

        val syncGoals = goals.map { g ->
            SyncSavingsGoal(
                syncId = g.syncId.ifBlank { UUID.randomUUID().toString() },
                name = g.name,
                targetAmount = g.targetAmount,
                currentAmount = g.currentAmount,
                deadline = g.deadline,
                iconName = g.iconName,
                createdAt = g.createdAt,
                updatedAt = g.updatedAt,
                isDeleted = false
            )
        } + deletedRecords.filter { it.entityType == "GOAL" }.map { del ->
            SyncSavingsGoal(
                syncId = del.syncId,
                name = "",
                targetAmount = 0.0,
                currentAmount = 0.0,
                createdAt = del.deletedAt,
                updatedAt = del.deletedAt,
                isDeleted = true
            )
        }

        val syncSubs = subscriptions.map { s ->
            SyncSubscription(
                syncId = s.syncId.ifBlank { UUID.randomUUID().toString() },
                name = s.name,
                amount = s.amount,
                billingCycle = s.billingCycle,
                nextPaymentDate = s.nextPaymentDate,
                categoryName = s.categoryId?.let { cid: Long -> categories[cid]?.name },
                active = s.active,
                createdAt = s.createdAt,
                updatedAt = s.updatedAt,
                isDeleted = false
            )
        } + deletedRecords.filter { it.entityType == "SUBSCRIPTION" }.map { del ->
            SyncSubscription(
                syncId = del.syncId,
                name = "",
                amount = 0.0,
                billingCycle = "MONTHLY",
                nextPaymentDate = 0L,
                active = false,
                createdAt = del.deletedAt,
                updatedAt = del.deletedAt,
                isDeleted = true
            )
        }

        ExpenseeSyncPayload(
            formatVersion = 1,
            clientDeviceId = deviceId,
            exportedAt = System.currentTimeMillis(),
            userProfile = profile?.let {
                SyncUserProfile(
                    name = it.name,
                    currencyCode = it.currencyCode,
                    currencySymbol = it.currencySymbol,
                    monthlyBudgetLimit = it.monthlyBudgetLimit,
                    monthlyIncome = it.monthlyIncome,
                    themeMode = it.themeMode,
                    updatedAt = it.updatedAt
                )
            },
            transactions = syncTxs,
            budgets = syncBudgets,
            goals = syncGoals,
            subscriptions = syncSubs
        )
    }

    companion object {
        @Volatile
        private var instance: DriveSyncManager? = null

        fun getInstance(context: Context): DriveSyncManager {
            return instance ?: synchronized(this) {
                instance ?: DriveSyncManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
