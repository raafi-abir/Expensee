package com.expensee.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.expensee.data.AppDatabase
import com.expensee.data.FinanceRepository
import com.expensee.data.model.BudgetEntity
import com.expensee.data.model.CategoryEntity
import com.expensee.data.model.SavingsGoalEntity
import com.expensee.data.model.SubscriptionEntity
import com.expensee.data.model.TransactionEntity
import com.expensee.data.model.UserProfileEntity
import com.expensee.domain.BudgetCalculation
import com.expensee.domain.FinancialEngine
import com.expensee.domain.FinancialInsight
import com.expensee.domain.FinancialPatternEngine
import com.expensee.domain.LearnedPatternReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.compose.runtime.Immutable
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Immutable
sealed interface StartupState {
    object Loading : StartupState
    object NeedsSetup : StartupState
    object Ready : StartupState
}

@Immutable
enum class AppTab {
    DASHBOARD,
    HISTORY,
    ANALYTICS,
    BUDGETS,
    MORE
}

@Immutable
data class DashboardUiState(
    val startupState: StartupState = StartupState.Loading,
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val currencySymbol: String = "$",
    val currencyCode: String = "USD",
    val userName: String = "User",
    val monthlyIncomeTarget: Double = 0.0,
    val monthlyBudgetTarget: Double = 0.0,
    val themeMode: String = "SYSTEM",
    val availableBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val totalSaved: Double = 0.0,
    val savingsRate: Double = 0.0,
    val overallBudgetStatus: BudgetCalculation? = null,
    val overallBudgetEntity: BudgetEntity? = null,
    val recentTransactions: List<TransactionEntity> = emptyList(),
    val monthlyTransactions: List<TransactionEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val categoryTotals: Map<Long, Double> = emptyMap(),
    val categoryBudgets: List<BudgetEntity> = emptyList(),
    val goals: List<SavingsGoalEntity> = emptyList(),
    val subscriptions: List<SubscriptionEntity> = emptyList(),
    val insights: List<FinancialInsight> = emptyList(),
    val patternReport: LearnedPatternReport? = null,
    val isOnboardingCompleted: Boolean = false,
    val userProfile: UserProfileEntity? = null,
    val googleAccountEmail: String? = null,
    val googleAccountName: String? = null,
    val isCloudSyncEnabled: Boolean = false,
    val lastSyncTimestamp: Long = 0L,
    val syncStatus: String = "IDLE",
    val currentTab: AppTab = AppTab.DASHBOARD
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinanceRepository
    private val syncManager = com.expensee.data.sync.DriveSyncManager.getInstance(application)

    init {
        val database = AppDatabase.getInstance(application)
        repository = FinanceRepository(database)
        viewModelScope.launch(Dispatchers.IO) {
            repository.ensureInitialized()
        }
    }

    val startupState: StateFlow<StartupState> = repository.userProfile
        .map { profile ->
            when {
                profile == null -> StartupState.Loading
                !profile.onboardingCompleted -> StartupState.NeedsSetup
                else -> StartupState.Ready
            }
        }
        .distinctUntilChanged()
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            StartupState.Loading
        )

    private val calendar = Calendar.getInstance()
    private val _selectedMonth = MutableStateFlow(calendar.get(Calendar.MONTH) + 1)
    val selectedMonth = _selectedMonth.asStateFlow()

    private val _selectedYear = MutableStateFlow(calendar.get(Calendar.YEAR))
    val selectedYear = _selectedYear.asStateFlow()

    private val _currentTab = MutableStateFlow(AppTab.DASHBOARD)
    val currentTab = _currentTab.asStateFlow()

    // History filter states
    val searchQuery = MutableStateFlow("")
    val filterType = MutableStateFlow("ALL") // ALL, EXPENSE, INCOME
    val filterCategoryId = MutableStateFlow<Long?>(null)
    val filterPaymentMethod = MutableStateFlow<String?>(null)

    // Reactive streams based on selected month/year
    private val selectedMonthYear = combine(_selectedMonth, _selectedYear) { m, y -> Pair(m, y) }.distinctUntilChanged()

    val monthlyTransactions: StateFlow<List<TransactionEntity>> = selectedMonthYear
        .flatMapLatest { (m, y) -> repository.getTransactionsForMonth(m, y) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val overallBudget: StateFlow<BudgetEntity?> = selectedMonthYear
        .flatMapLatest { (m, y) -> repository.getOverallBudget(m, y) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val monthlyBudgets: StateFlow<List<BudgetEntity>> = selectedMonthYear
        .flatMapLatest { (m, y) -> repository.getBudgetsForMonth(m, y) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val goals: StateFlow<List<SavingsGoalEntity>> = repository.allGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subscriptions: StateFlow<List<SubscriptionEntity>> = repository.allSubscriptions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Base financial calculation cached on background dispatcher (Dispatchers.Default).
    // Tab switching combines immediately with this cached computation without re-running
    // pattern analysis, mathematical summations, or date manipulations.
    private data class FinancialBaseState(
        val month: Int = 1,
        val year: Int = 2025,
        val symbol: String = "$",
        val code: String = "USD",
        val userName: String = "User",
        val monthlyIncomeTarget: Double = 0.0,
        val monthlyBudgetTarget: Double = 0.0,
        val themeMode: String = "SYSTEM",
        val availableBalance: Double = 0.0,
        val totalIncome: Double = 0.0,
        val totalExpenses: Double = 0.0,
        val totalSaved: Double = 0.0,
        val savingsRate: Double = 0.0,
        val overallBudgetStatus: BudgetCalculation? = null,
        val overallBudgetEntity: BudgetEntity? = null,
        val recentTransactions: List<TransactionEntity> = emptyList(),
        val monthlyTransactions: List<TransactionEntity> = emptyList(),
        val categories: List<CategoryEntity> = emptyList(),
        val categoryTotals: Map<Long, Double> = emptyMap(),
        val categoryBudgets: List<BudgetEntity> = emptyList(),
        val goals: List<SavingsGoalEntity> = emptyList(),
        val subscriptions: List<SubscriptionEntity> = emptyList(),
        val insights: List<FinancialInsight> = emptyList(),
        val patternReport: LearnedPatternReport? = null,
        val startupState: StartupState = StartupState.Loading,
        val isOnboardingCompleted: Boolean = false,
        val userProfile: UserProfileEntity? = null,
        val googleAccountEmail: String? = null,
        val googleAccountName: String? = null,
        val isCloudSyncEnabled: Boolean = false,
        val lastSyncTimestamp: Long = 0L,
        val syncStatus: String = "IDLE"
    )

    @Suppress("UNCHECKED_CAST")
    private val financialBase: Flow<FinancialBaseState> = combine(
        selectedMonthYear,
        monthlyTransactions,
        categories,
        userProfile,
        overallBudget,
        monthlyBudgets,
        goals,
        subscriptions
    ) { args ->
        val (month, year) = args[0] as Pair<Int, Int>
        val txs = args[1] as List<TransactionEntity>
        val cats = args[2] as List<CategoryEntity>
        val profile = args[3] as UserProfileEntity?
        val overallBgt = args[4] as BudgetEntity?
        val allBgts = args[5] as List<BudgetEntity>
        val allGoals = args[6] as List<SavingsGoalEntity>
        val allSubs = args[7] as List<SubscriptionEntity>

        val symbol = profile?.currencySymbol ?: "৳"
        val code = profile?.currencyCode ?: "BDT"
        val budgetLimit = overallBgt?.amount ?: (profile?.monthlyBudgetLimit ?: 0.0)

        val (income, expenses) = FinancialEngine.calculateIncomeAndExpenses(txs)
        val balance = FinancialEngine.calculateAvailableBalance(income, expenses)
        val saved = FinancialEngine.calculateSavings(income, expenses)
        val rate = FinancialEngine.calculateSavingsRate(income, expenses)
        val budgetStatus = if (budgetLimit > 0) FinancialEngine.calculateBudgetStatus(expenses, budgetLimit) else null
        val catTotals = FinancialEngine.calculateCategoryTotals(txs, "EXPENSE")

        val patternReport = FinancialPatternEngine.analyzeAndEstimate(
            transactions = txs,
            categories = cats,
            selectedMonth = month,
            selectedYear = year,
            budgetLimit = budgetLimit,
            monthlyIncomeTarget = profile?.monthlyIncome ?: 0.0,
            currencySymbol = symbol
        )

        FinancialBaseState(
            month = month,
            year = year,
            symbol = symbol,
            code = code,
            userName = profile?.name ?: "User",
            monthlyIncomeTarget = profile?.monthlyIncome ?: 0.0,
            monthlyBudgetTarget = budgetLimit,
            themeMode = profile?.themeMode ?: "SYSTEM",
            availableBalance = balance,
            totalIncome = income,
            totalExpenses = expenses,
            totalSaved = saved,
            savingsRate = rate,
            overallBudgetStatus = budgetStatus,
            overallBudgetEntity = overallBgt,
            recentTransactions = txs.take(6),
            monthlyTransactions = txs,
            categories = cats,
            categoryTotals = catTotals,
            categoryBudgets = allBgts.filter { it.categoryId != null },
            goals = allGoals,
            subscriptions = allSubs,
            insights = patternReport.patternInsights,
            patternReport = patternReport,
            startupState = when {
                profile == null -> StartupState.Loading
                !profile.onboardingCompleted -> StartupState.NeedsSetup
                else -> StartupState.Ready
            },
            isOnboardingCompleted = profile?.onboardingCompleted ?: false,
            userProfile = profile,
            googleAccountEmail = profile?.googleAccountEmail,
            googleAccountName = profile?.googleAccountName,
            isCloudSyncEnabled = profile?.isCloudSyncEnabled ?: false,
            lastSyncTimestamp = profile?.lastSyncTimestamp ?: 0L,
            syncStatus = profile?.syncStatus ?: "IDLE"
        )
    }.flowOn(Dispatchers.Default)

    // Decoupled Dashboard state: emits strictly when underlying financial data, profile, or month changes.
    // Does NOT re-emit upon tab changes, completely preventing unnecessary whole-screen recompositions during tab transitions.
    val dashboardState: StateFlow<DashboardUiState> = financialBase.map { base ->
        DashboardUiState(
            selectedMonth = base.month,
            selectedYear = base.year,
            currencySymbol = base.symbol,
            currencyCode = base.code,
            userName = base.userName,
            monthlyIncomeTarget = base.monthlyIncomeTarget,
            monthlyBudgetTarget = base.monthlyBudgetTarget,
            themeMode = base.themeMode,
            availableBalance = base.availableBalance,
            totalIncome = base.totalIncome,
            totalExpenses = base.totalExpenses,
            totalSaved = base.totalSaved,
            savingsRate = base.savingsRate,
            overallBudgetStatus = base.overallBudgetStatus,
            overallBudgetEntity = base.overallBudgetEntity,
            recentTransactions = base.recentTransactions,
            monthlyTransactions = base.monthlyTransactions,
            categories = base.categories,
            categoryTotals = base.categoryTotals,
            categoryBudgets = base.categoryBudgets,
            goals = base.goals,
            subscriptions = base.subscriptions,
            insights = base.insights,
            patternReport = base.patternReport,
            startupState = base.startupState,
            isOnboardingCompleted = base.isOnboardingCompleted,
            userProfile = base.userProfile,
            googleAccountEmail = base.googleAccountEmail,
            googleAccountName = base.googleAccountName,
            isCloudSyncEnabled = base.isCloudSyncEnabled,
            lastSyncTimestamp = base.lastSyncTimestamp,
            syncStatus = base.syncStatus,
            currentTab = _currentTab.value
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DashboardUiState()
    )

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun nextMonth() {
        if (_selectedMonth.value == 12) {
            _selectedMonth.value = 1
            _selectedYear.value += 1
        } else {
            _selectedMonth.value += 1
        }
    }

    fun prevMonth() {
        if (_selectedMonth.value == 1) {
            _selectedMonth.value = 12
            _selectedYear.value -= 1
        } else {
            _selectedMonth.value -= 1
        }
    }

    fun setMonthAndYear(month: Int, year: Int) {
        _selectedMonth.value = month.coerceIn(1, 12)
        _selectedYear.value = year
    }

    fun addTransaction(
        amount: Double,
        type: String,
        categoryId: Long,
        date: Long,
        note: String,
        paymentMethod: String
    ) {
        viewModelScope.launch {
            repository.insertTransaction(
                TransactionEntity(
                    amount = amount,
                    type = type,
                    categoryId = categoryId,
                    date = date,
                    note = note.trim(),
                    paymentMethod = paymentMethod
                )
            )
            syncManager.triggerSync()
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(transaction.copy(updatedAt = System.currentTimeMillis()))
            syncManager.triggerSync()
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
            syncManager.triggerSync()
        }
    }

    fun deleteBudget(id: Long) {
        viewModelScope.launch {
            repository.deleteBudget(id)
            syncManager.triggerSync()
        }
    }

    fun clearOverallBudget() {
        viewModelScope.launch {
            val bgt = repository.getOverallBudgetDirect(_selectedMonth.value, _selectedYear.value)
            if (bgt != null) {
                repository.deleteBudget(bgt.id)
            }
            val prof = repository.getUserProfileDirect()
            if (prof != null && prof.monthlyBudgetLimit > 0) {
                repository.updateProfile(prof.copy(monthlyBudgetLimit = 0.0, updatedAt = System.currentTimeMillis()))
            }
            syncManager.triggerSync()
        }
    }

    fun setOverallBudget(amount: Double) {
        viewModelScope.launch {
            repository.setOverallBudget(amount, _selectedMonth.value, _selectedYear.value)
            syncManager.triggerSync()
        }
    }

    fun setCategoryBudget(categoryId: Long, amount: Double) {
        viewModelScope.launch {
            repository.setCategoryBudget(categoryId, amount, _selectedMonth.value, _selectedYear.value)
            syncManager.triggerSync()
        }
    }

    fun addSavingsGoal(name: String, targetAmount: Double, initialAmount: Double = 0.0) {
        viewModelScope.launch {
            repository.insertGoal(
                SavingsGoalEntity(
                    name = name.trim(),
                    targetAmount = targetAmount,
                    currentAmount = initialAmount
                )
            )
            syncManager.triggerSync()
        }
    }

    fun adjustGoalFunds(goalId: Long, delta: Double) {
        viewModelScope.launch {
            repository.adjustGoalFunds(goalId, delta)
            syncManager.triggerSync()
        }
    }

    fun deleteGoal(id: Long) {
        viewModelScope.launch {
            repository.deleteGoal(id)
            syncManager.triggerSync()
        }
    }

    fun addSubscription(name: String, amount: Double, cycle: String = "MONTHLY", categoryId: Long? = null) {
        viewModelScope.launch {
            repository.insertSubscription(
                SubscriptionEntity(
                    name = name.trim(),
                    amount = amount,
                    billingCycle = cycle,
                    categoryId = categoryId
                )
            )
            syncManager.triggerSync()
        }
    }

    fun toggleSubscription(sub: SubscriptionEntity) {
        viewModelScope.launch {
            repository.updateSubscription(sub.copy(active = !sub.active, updatedAt = System.currentTimeMillis()))
            syncManager.triggerSync()
        }
    }

    fun deleteSubscription(id: Long) {
        viewModelScope.launch {
            repository.deleteSubscription(id)
            syncManager.triggerSync()
        }
    }

    fun updateCurrency(currencyCode: String, currencySymbol: String) {
        viewModelScope.launch {
            repository.updateCurrency(currencyCode, currencySymbol)
        }
    }

    fun completeOnboarding(
        name: String,
        currencyCode: String,
        currencySymbol: String,
        monthlyIncome: Double,
        monthlyBudget: Double,
        themeMode: String = "SYSTEM",
        seedInitialIncomeTx: Boolean = false,
        googleAccountEmail: String? = null,
        googleAccountName: String? = null,
        isCloudSyncEnabled: Boolean = false
    ) {
        viewModelScope.launch {
            repository.completeOnboarding(
                name = name,
                currencyCode = currencyCode,
                currencySymbol = currencySymbol,
                monthlyIncome = monthlyIncome,
                monthlyBudget = monthlyBudget,
                themeMode = themeMode,
                seedInitialIncomeTx = seedInitialIncomeTx,
                googleAccountEmail = googleAccountEmail,
                googleAccountName = googleAccountName,
                isCloudSyncEnabled = isCloudSyncEnabled
            )
            if (monthlyBudget > 0) {
                repository.setOverallBudget(monthlyBudget, _selectedMonth.value, _selectedYear.value)
            }
            if (isCloudSyncEnabled) {
                syncManager.triggerSync()
            }
        }
    }

    fun completeOnboarding(currencyCode: String, currencySymbol: String, monthlyBudget: Double) {
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

    fun triggerSyncNow() {
        syncManager.triggerSync()
    }

    fun connectGoogleAccount(email: String, displayName: String) {
        viewModelScope.launch {
            repository.updateGoogleAccountAndSync(email, displayName, true)
            syncManager.triggerSync()
        }
    }

    fun disconnectGoogleAccount() {
        viewModelScope.launch {
            repository.disconnectGoogleAccount()
        }
    }

    fun setCloudSyncEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setCloudSyncEnabled(enabled)
            if (enabled) {
                syncManager.triggerSync()
            }
        }
    }

    fun setMonthlyIncome(income: Double, recordAsTransaction: Boolean = false) {
        viewModelScope.launch {
            repository.updateMonthlyIncome(income)
            if (recordAsTransaction && income > 0) {
                val currentMonthTxs = repository.getTransactionsForMonthDirect(_selectedMonth.value, _selectedYear.value)
                val existingSalaryTx = currentMonthTxs.firstOrNull {
                    it.type == "INCOME" && (
                        it.note.contains("Monthly Income", ignoreCase = true) ||
                        it.note.contains("Salary", ignoreCase = true)
                    )
                }
                if (existingSalaryTx != null) {
                    repository.updateTransaction(
                        existingSalaryTx.copy(
                            amount = income,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                } else {
                    val cats = repository.allCategories.firstOrNull() ?: emptyList()
                    val salaryCat = cats.firstOrNull { it.type == "INCOME" && it.name.contains("Salary", ignoreCase = true) }
                        ?: cats.firstOrNull { it.type == "INCOME" }
                    val catId = salaryCat?.id ?: 1L
                    repository.insertTransaction(
                        TransactionEntity(
                            amount = income,
                            type = "INCOME",
                            categoryId = catId,
                            date = System.currentTimeMillis(),
                            note = "Monthly Income",
                            paymentMethod = "Bank"
                        )
                    )
                }
            }
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            repository.updateThemeMode(mode)
        }
    }

    fun updateProfileDetails(
        name: String,
        monthlyIncome: Double,
        monthlyBudget: Double,
        currencyCode: String,
        currencySymbol: String,
        themeMode: String
    ) {
        viewModelScope.launch {
            repository.updateProfileDetails(
                name = name,
                monthlyIncome = monthlyIncome,
                monthlyBudget = monthlyBudget,
                currencyCode = currencyCode,
                currencySymbol = currencySymbol,
                themeMode = themeMode
            )
            if (monthlyBudget > 0) {
                repository.setOverallBudget(monthlyBudget, _selectedMonth.value, _selectedYear.value)
            }
        }
    }

    fun reopenOnboarding() {
        viewModelScope.launch {
            repository.reopenOnboarding()
        }
    }

    fun resetData() {
        viewModelScope.launch {
            repository.resetDatabase()
        }
    }

    fun exportTransactionsCsv(transactions: List<TransactionEntity>, categories: List<CategoryEntity>): String {
        val catMap = categories.associate { it.id to it.name }
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val sb = StringBuilder()
        sb.append("ID,Date,Type,Category,Amount,Payment Method,Note\n")
        transactions.forEach { tx ->
            val dateStr = dateFormat.format(Date(tx.date))
            val catName = catMap[tx.categoryId] ?: "Unknown"
            val cleanNote = tx.note.replace(",", " ")
            sb.append("${tx.id},$dateStr,${tx.type},\"$catName\",${tx.amount},\"${tx.paymentMethod}\",\"$cleanNote\"\n")
        }
        return sb.toString()
    }
}
