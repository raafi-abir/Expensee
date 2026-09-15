package com.expensee.data

import com.expensee.data.model.BudgetEntity
import com.expensee.data.model.CategoryEntity
import com.expensee.data.model.RecurringTransactionEntity
import com.expensee.data.model.SavingsGoalEntity
import com.expensee.data.model.SubscriptionEntity
import com.expensee.data.model.TransactionEntity
import com.expensee.data.model.UserProfileEntity
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class FinanceRepository(private val database: AppDatabase) {

    private val transactionDao = database.transactionDao()
    private val categoryDao = database.categoryDao()
    private val budgetDao = database.budgetDao()
    private val savingsGoalDao = database.savingsGoalDao()
    private val subscriptionDao = database.subscriptionDao()
    private val recurringDao = database.recurringTransactionDao()
    private val userProfileDao = database.userProfileDao()
    private val deletedRecordDao = database.deletedRecordDao()

    // Transactions
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()

    fun getTransactionsForMonth(month: Int, year: Int): Flow<List<TransactionEntity>> {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startEpoch = cal.timeInMillis

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val endEpoch = cal.timeInMillis

        return transactionDao.getTransactionsBetween(startEpoch, endEpoch)
    }

    suspend fun getTransactionsForMonthDirect(month: Int, year: Int): List<TransactionEntity> {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startEpoch = cal.timeInMillis

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val endEpoch = cal.timeInMillis

        return transactionDao.getTransactionsBetweenDirect(startEpoch, endEpoch)
    }

    suspend fun insertTransaction(transaction: TransactionEntity): Long {
        return transactionDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(id: Long) {
        val tx = transactionDao.getTransactionById(id)
        if (tx != null && tx.syncId.isNotBlank()) {
            deletedRecordDao.recordDeleted(com.expensee.data.model.DeletedRecordEntity(tx.syncId, "TRANSACTION"))
        }
        transactionDao.deleteById(id)
    }

    // Categories
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    suspend fun insertCategory(category: CategoryEntity): Long {
        return categoryDao.insertCategory(category)
    }

    suspend fun deleteCategory(category: CategoryEntity) {
        categoryDao.deleteCategory(category)
    }

    // Budgets
    fun getBudgetsForMonth(month: Int, year: Int): Flow<List<BudgetEntity>> {
        return budgetDao.getBudgetsForMonth(month, year)
    }

    fun getOverallBudget(month: Int, year: Int): Flow<BudgetEntity?> {
        return budgetDao.getOverallBudget(month, year)
    }

    suspend fun getOverallBudgetDirect(month: Int, year: Int): BudgetEntity? {
        return budgetDao.getOverallBudgetDirect(month, year)
    }

    suspend fun setOverallBudget(amount: Double, month: Int, year: Int) {
        val existing = budgetDao.getOverallBudgetDirect(month, year)
        if (existing != null) {
            budgetDao.updateBudget(existing.copy(amount = amount, updatedAt = System.currentTimeMillis()))
        } else {
            budgetDao.insertBudget(
                BudgetEntity(
                    categoryId = null,
                    amount = amount,
                    month = month,
                    year = year
                )
            )
        }
    }

    suspend fun setCategoryBudget(categoryId: Long, amount: Double, month: Int, year: Int) {
        val existing = budgetDao.getBudgetForCategory(month, year, categoryId)
        if (existing != null) {
            budgetDao.updateBudget(existing.copy(amount = amount, updatedAt = System.currentTimeMillis()))
        } else {
            budgetDao.insertBudget(
                BudgetEntity(
                    categoryId = categoryId,
                    amount = amount,
                    month = month,
                    year = year
                )
            )
        }
    }

    suspend fun deleteBudget(id: Long) {
        val all = budgetDao.getAllBudgetsDirect()
        val item = all.find { it.id == id }
        if (item != null && item.syncId.isNotBlank()) {
            deletedRecordDao.recordDeleted(com.expensee.data.model.DeletedRecordEntity(item.syncId, "BUDGET"))
        }
        budgetDao.deleteById(id)
    }

    // Savings Goals
    val allGoals: Flow<List<SavingsGoalEntity>> = savingsGoalDao.getAllGoals()

    suspend fun insertGoal(goal: SavingsGoalEntity): Long {
        return savingsGoalDao.insertGoal(goal)
    }

    suspend fun updateGoal(goal: SavingsGoalEntity) {
        savingsGoalDao.updateGoal(goal)
    }

    suspend fun adjustGoalFunds(goalId: Long, deltaAmount: Double) {
        val goal = savingsGoalDao.getGoalById(goalId) ?: return
        val newAmount = (goal.currentAmount + deltaAmount).coerceAtLeast(0.0)
        savingsGoalDao.updateGoal(goal.copy(currentAmount = newAmount, updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteGoal(id: Long) {
        val goal = savingsGoalDao.getGoalById(id)
        if (goal != null && goal.syncId.isNotBlank()) {
            deletedRecordDao.recordDeleted(com.expensee.data.model.DeletedRecordEntity(goal.syncId, "GOAL"))
        }
        savingsGoalDao.deleteById(id)
    }

    // Subscriptions
    val allSubscriptions: Flow<List<SubscriptionEntity>> = subscriptionDao.getAllSubscriptions()

    suspend fun insertSubscription(subscription: SubscriptionEntity): Long {
        return subscriptionDao.insertSubscription(subscription)
    }

    suspend fun updateSubscription(subscription: SubscriptionEntity) {
        subscriptionDao.updateSubscription(subscription)
    }

    suspend fun deleteSubscription(id: Long) {
        val all = subscriptionDao.getAllSubscriptionsDirect()
        val item = all.find { it.id == id }
        if (item != null && item.syncId.isNotBlank()) {
            deletedRecordDao.recordDeleted(com.expensee.data.model.DeletedRecordEntity(item.syncId, "SUBSCRIPTION"))
        }
        subscriptionDao.deleteById(id)
    }

    // Recurring
    val allRecurring: Flow<List<RecurringTransactionEntity>> = recurringDao.getAllRecurring()

    suspend fun insertRecurring(item: RecurringTransactionEntity): Long {
        return recurringDao.insertRecurring(item)
    }

    suspend fun deleteRecurring(id: Long) {
        recurringDao.deleteById(id)
    }

    // User Profile
    val userProfile: Flow<UserProfileEntity?> = userProfileDao.getUserProfile()

    suspend fun getUserProfileDirect(): UserProfileEntity? {
        return userProfileDao.getUserProfileDirect()
    }

    suspend fun updateProfile(profile: UserProfileEntity) {
        userProfileDao.insertOrUpdate(profile)
    }

    suspend fun updateCurrency(currencyCode: String, currencySymbol: String) {
        val current = userProfileDao.getUserProfileDirect() ?: UserProfileEntity()
        userProfileDao.insertOrUpdate(
            current.copy(
                currencyCode = currencyCode,
                currencySymbol = currencySymbol,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun completeOnboarding(
        currencyCode: String,
        currencySymbol: String,
        monthlyBudget: Double
    ) {
        completeOnboarding(
            name = "User",
            currencyCode = currencyCode,
            currencySymbol = currencySymbol,
            monthlyIncome = 0.0,
            monthlyBudget = monthlyBudget,
            themeMode = "SYSTEM",
            seedInitialIncomeTx = false
        )
    }

    suspend fun completeOnboarding(
        name: String,
        currencyCode: String,
        currencySymbol: String,
        monthlyIncome: Double,
        monthlyBudget: Double,
        themeMode: String,
        seedInitialIncomeTx: Boolean = false,
        googleAccountEmail: String? = null,
        googleAccountName: String? = null,
        isCloudSyncEnabled: Boolean = false
    ) {
        val current = userProfileDao.getUserProfileDirect() ?: UserProfileEntity()
        userProfileDao.insertOrUpdate(
            current.copy(
                name = name.ifBlank { "User" },
                currencyCode = currencyCode,
                currencySymbol = currencySymbol,
                monthlyIncome = monthlyIncome,
                monthlyBudgetLimit = monthlyBudget,
                themeMode = themeMode,
                onboardingCompleted = true,
                updatedAt = System.currentTimeMillis(),
                googleAccountEmail = googleAccountEmail,
                googleAccountName = googleAccountName,
                isCloudSyncEnabled = isCloudSyncEnabled
            )
        )

        if (seedInitialIncomeTx && monthlyIncome > 0) {
            val incomeCategories = categoryDao.getCategoriesByTypeDirect("INCOME")
            val salaryCatId = incomeCategories.firstOrNull { it.name.contains("Salary", ignoreCase = true) }?.id
                ?: incomeCategories.firstOrNull()?.id ?: 1L
            transactionDao.insertTransaction(
                TransactionEntity(
                    amount = monthlyIncome,
                    type = "INCOME",
                    categoryId = salaryCatId,
                    date = System.currentTimeMillis(),
                    note = "Monthly Income",
                    paymentMethod = "Bank"
                )
            )
        }
    }

    suspend fun updateMonthlyIncome(income: Double) {
        val current = userProfileDao.getUserProfileDirect() ?: UserProfileEntity()
        userProfileDao.insertOrUpdate(
            current.copy(
                monthlyIncome = income,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateThemeMode(themeMode: String) {
        val current = userProfileDao.getUserProfileDirect() ?: UserProfileEntity()
        userProfileDao.insertOrUpdate(
            current.copy(
                themeMode = themeMode,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateProfileDetails(
        name: String,
        monthlyIncome: Double,
        monthlyBudget: Double,
        currencyCode: String,
        currencySymbol: String,
        themeMode: String
    ) {
        val current = userProfileDao.getUserProfileDirect() ?: UserProfileEntity()
        userProfileDao.insertOrUpdate(
            current.copy(
                name = name.ifBlank { "Alex" },
                monthlyIncome = monthlyIncome,
                monthlyBudgetLimit = monthlyBudget,
                currencyCode = currencyCode,
                currencySymbol = currencySymbol,
                themeMode = themeMode,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateGoogleAccountAndSync(email: String, name: String, enabled: Boolean) {
        val current = userProfileDao.getUserProfileDirect() ?: UserProfileEntity()
        userProfileDao.insertOrUpdate(
            current.copy(
                googleAccountEmail = email,
                googleAccountName = name,
                isCloudSyncEnabled = enabled,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun setCloudSyncEnabled(enabled: Boolean) {
        val current = userProfileDao.getUserProfileDirect() ?: return
        userProfileDao.insertOrUpdate(
            current.copy(
                isCloudSyncEnabled = enabled,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun disconnectGoogleAccount() {
        val current = userProfileDao.getUserProfileDirect() ?: return
        userProfileDao.insertOrUpdate(
            current.copy(
                googleAccountEmail = null,
                googleAccountName = null,
                isCloudSyncEnabled = false,
                syncStatus = "IDLE",
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun ensureInitialized() {
        val profile = userProfileDao.getUserProfileDirect()
        if (profile == null) {
            userProfileDao.insertOrUpdate(
                UserProfileEntity(
                    id = 1,
                    name = "User",
                    email = "",
                    currencyCode = "USD",
                    currencySymbol = "$",
                    monthlyBudgetLimit = 0.0,
                    monthlyIncome = 0.0,
                    themeMode = "SYSTEM",
                    onboardingCompleted = false
                )
            )
        }
        if (categoryDao.getCategoryCount() == 0) {
            database.populateInitialData()
        }
    }

    suspend fun reopenOnboarding() {
        val current = userProfileDao.getUserProfileDirect() ?: UserProfileEntity()
        userProfileDao.insertOrUpdate(
            current.copy(
                onboardingCompleted = false,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun resetDatabase() {
        database.clearAllTables()
        database.populateInitialData()
    }
}
