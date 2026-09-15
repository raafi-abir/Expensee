package com.expensee.domain

import androidx.compose.runtime.Immutable
import com.expensee.data.model.CategoryEntity
import com.expensee.data.model.TransactionEntity
import java.util.Calendar
import kotlin.math.roundToInt

@Immutable
data class CategoryPatternItem(
    val categoryId: Long,
    val categoryName: String,
    val totalAmount: Double,
    val percentageOfTotal: Double,
    val transactionCount: Int,
    val averageTicketSize: Double,
    val colorHex: String
)

@Immutable
data class DayOfWeekPattern(
    val dayName: String,
    val dayOfWeek: Int,
    val totalAmount: Double,
    val transactionCount: Int,
    val averageAmount: Double
)

@Immutable
data class LearnedPatternReport(
    val hasData: Boolean,
    val totalExpenses: Double,
    val totalIncome: Double,
    val netBalance: Double,
    val daysElapsed: Int,
    val daysRemaining: Int,
    val daysInMonth: Int,
    // Projections & Estimations
    val averageDailyBurnRate: Double,
    val estimatedMonthEndExpense: Double,
    val estimatedMonthEndSavings: Double,
    val dailySafeToSpend: Double,
    val projectedBudgetOverage: Double?,
    val isProjectedToOverspend: Boolean,
    // Learned Behavioral Patterns
    val peakSpendingDay: DayOfWeekPattern?,
    val weekdayExpenseTotal: Double,
    val weekendExpenseTotal: Double,
    val weekdayPercentage: Int,
    val weekendPercentage: Int,
    val dominantPaymentMethod: String?,
    val paymentMethodDistribution: Map<String, Int>,
    val averageTransactionAmount: Double,
    val transactionFrequencyPerDay: Double,
    // Real Category breakdown
    val categoryBreakdown: List<CategoryPatternItem>,
    val topCategory: CategoryPatternItem?,
    // Generated Data-Driven Pattern Insights
    val patternInsights: List<FinancialInsight>
)

object FinancialPatternEngine {

    private val DAY_NAMES = arrayOf(
        "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
    )

    fun analyzeAndEstimate(
        transactions: List<TransactionEntity>,
        categories: List<CategoryEntity>,
        selectedMonth: Int,
        selectedYear: Int,
        budgetLimit: Double,
        monthlyIncomeTarget: Double,
        currencySymbol: String = "$"
    ): LearnedPatternReport {
        val cal = Calendar.getInstance()
        val currentYear = cal.get(Calendar.YEAR)
        val currentMonth = cal.get(Calendar.MONTH) + 1
        val currentDay = cal.get(Calendar.DAY_OF_MONTH)

        // Calculate days in the selected month
        val monthCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, selectedYear)
            set(Calendar.MONTH, selectedMonth - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val daysInMonth = monthCal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val isCurrentMonth = (selectedYear == currentYear && selectedMonth == currentMonth)
        val isPastMonth = (selectedYear < currentYear) || (selectedYear == currentYear && selectedMonth < currentMonth)

        val daysElapsed = when {
            isCurrentMonth -> currentDay.coerceIn(1, daysInMonth)
            isPastMonth -> daysInMonth
            else -> 1 // Future month
        }
        val daysRemaining = (daysInMonth - daysElapsed).coerceAtLeast(1)

        val expenseTransactions = ArrayList<TransactionEntity>(transactions.size)
        val incomeTransactions = ArrayList<TransactionEntity>()
        var totalExpenses = 0.0
        var totalIncome = 0.0

        for (tx in transactions) {
            if (tx.type == "EXPENSE") {
                expenseTransactions.add(tx)
                totalExpenses += tx.amount
            } else if (tx.type == "INCOME") {
                incomeTransactions.add(tx)
                totalIncome += tx.amount
            }
        }

        val netBalance = totalIncome - totalExpenses

        val categoryMap = categories.associateBy { it.id }

        // If no transactions exist, return pure zero baseline with educational insight
        if (expenseTransactions.isEmpty() && incomeTransactions.isEmpty()) {
            val dailySafe = if (budgetLimit > 0) budgetLimit / daysInMonth else 0.0
            return LearnedPatternReport(
                hasData = false,
                totalExpenses = 0.0,
                totalIncome = 0.0,
                netBalance = 0.0,
                daysElapsed = daysElapsed,
                daysRemaining = daysRemaining,
                daysInMonth = daysInMonth,
                averageDailyBurnRate = 0.0,
                estimatedMonthEndExpense = 0.0,
                estimatedMonthEndSavings = 0.0,
                dailySafeToSpend = dailySafe,
                projectedBudgetOverage = null,
                isProjectedToOverspend = false,
                peakSpendingDay = null,
                weekdayExpenseTotal = 0.0,
                weekendExpenseTotal = 0.0,
                weekdayPercentage = 0,
                weekendPercentage = 0,
                dominantPaymentMethod = null,
                paymentMethodDistribution = emptyMap(),
                averageTransactionAmount = 0.0,
                transactionFrequencyPerDay = 0.0,
                categoryBreakdown = emptyList(),
                topCategory = null,
                patternInsights = listOf(
                    FinancialInsight(
                        title = "Pattern Engine Ready",
                        description = "No transactions logged yet. As you add your expenses, the engine will compute burn velocity, peak spending days, and month-end projections.",
                        iconName = "insights",
                        isPositive = true
                    )
                )
            )
        }

        // --- 1. Estimations ---
        val averageDailyBurnRate = if (daysElapsed > 0) totalExpenses / daysElapsed else 0.0
        val estimatedMonthEndExpense = if (isPastMonth) {
            totalExpenses
        } else {
            (averageDailyBurnRate * daysInMonth).coerceAtLeast(totalExpenses)
        }

        val effectiveIncome = if (totalIncome > 0) totalIncome else monthlyIncomeTarget
        val estimatedMonthEndSavings = effectiveIncome - estimatedMonthEndExpense

        val remainingBudget = (budgetLimit - totalExpenses).coerceAtLeast(0.0)
        val dailySafeToSpend = if (budgetLimit > 0) remainingBudget / daysRemaining else 0.0

        val isProjectedToOverspend = budgetLimit > 0 && estimatedMonthEndExpense > budgetLimit
        val projectedBudgetOverage = if (isProjectedToOverspend) estimatedMonthEndExpense - budgetLimit else null

        // --- 2. Day-of-Week Spending Analysis (Single-pass with reusable Calendar) ---
        val txCal = Calendar.getInstance()
        val dayTotals = DoubleArray(8) // Calendar.SUNDAY(1)..Calendar.SATURDAY(7)
        val dayCounts = IntArray(8)
        var weekendExpenseTotal = 0.0

        for (tx in expenseTransactions) {
            txCal.timeInMillis = tx.date
            val dow = txCal.get(Calendar.DAY_OF_WEEK)
            if (dow in 1..7) {
                dayTotals[dow] += tx.amount
                dayCounts[dow]++
                if (dow == Calendar.SATURDAY || dow == Calendar.SUNDAY) {
                    weekendExpenseTotal += tx.amount
                }
            }
        }

        val dayOfWeekPatterns = (Calendar.SUNDAY..Calendar.SATURDAY).map { dayIndex ->
            val totalForDay = dayTotals[dayIndex]
            val count = dayCounts[dayIndex]
            val avg = if (count > 0) totalForDay / count else 0.0
            DayOfWeekPattern(
                dayName = DAY_NAMES[dayIndex - 1],
                dayOfWeek = dayIndex,
                totalAmount = totalForDay,
                transactionCount = count,
                averageAmount = avg
            )
        }

        val peakDay = dayOfWeekPatterns.filter { it.totalAmount > 0 }.maxByOrNull { it.totalAmount }

        val weekdayExpenseTotal = (totalExpenses - weekendExpenseTotal).coerceAtLeast(0.0)

        val weekdayPercentage = if (totalExpenses > 0) {
            ((weekdayExpenseTotal / totalExpenses) * 100).roundToInt()
        } else 0
        val weekendPercentage = if (totalExpenses > 0) (100 - weekdayPercentage) else 0

        // --- 3. Payment Method Distribution ---
        val methodGroups = expenseTransactions.groupBy { it.paymentMethod.ifBlank { "Other" } }
        val paymentMethodDist = mutableMapOf<String, Int>()
        methodGroups.forEach { (method, list) ->
            val sum = list.sumOf { it.amount }
            val pct = if (totalExpenses > 0) ((sum / totalExpenses) * 100).roundToInt() else 0
            paymentMethodDist[method] = pct
        }
        val dominantPaymentMethod = paymentMethodDist.maxByOrNull { it.value }?.key

        // --- 4. Transaction Frequency & Ticket Size ---
        val expenseCount = expenseTransactions.size
        val averageTransactionAmount = if (expenseCount > 0) totalExpenses / expenseCount else 0.0
        val transactionFrequencyPerDay = if (daysElapsed > 0) expenseCount.toDouble() / daysElapsed else 0.0

        // --- 5. Category Breakdown ---
        val categoryBreakdown = expenseTransactions
            .groupBy { it.categoryId }
            .map { (catId, txList) ->
                val cat = categoryMap[catId]
                val catTotal = txList.sumOf { it.amount }
                val pct = if (totalExpenses > 0) catTotal / totalExpenses else 0.0
                val count = txList.size
                CategoryPatternItem(
                    categoryId = catId,
                    categoryName = cat?.name ?: "Other",
                    totalAmount = catTotal,
                    percentageOfTotal = pct,
                    transactionCount = count,
                    averageTicketSize = if (count > 0) catTotal / count else 0.0,
                    colorHex = cat?.colorHex ?: "#64748B"
                )
            }
            .sortedByDescending { it.totalAmount }

        val topCategory = categoryBreakdown.firstOrNull()

        // --- 6. Deterministic Data-Driven Insights ---
        val insights = mutableListOf<FinancialInsight>()

        // Pace / Burn rate observation
        if (totalExpenses > 0) {
            insights.add(
                FinancialInsight(
                    title = "Spending Velocity",
                    description = "Averaging ${FinancialEngine.formatAmount(averageDailyBurnRate, currencySymbol)}/day over $daysElapsed days. Projected month-end spend is ~${FinancialEngine.formatAmount(estimatedMonthEndExpense, currencySymbol)}.",
                    iconName = "speed",
                    isPositive = !isProjectedToOverspend
                )
            )
        }

        // Budget Trajectory
        if (budgetLimit > 0) {
            if (isProjectedToOverspend && projectedBudgetOverage != null) {
                insights.add(
                    FinancialInsight(
                        title = "Pace Exceeds Budget",
                        description = "At your current velocity, you are projected to overshoot your ${FinancialEngine.formatAmount(budgetLimit, currencySymbol)} budget by ~${FinancialEngine.formatAmount(projectedBudgetOverage, currencySymbol)}.",
                        iconName = "warning",
                        isPositive = false
                    )
                )
            } else {
                insights.add(
                    FinancialInsight(
                        title = "Sustainable Trajectory",
                        description = "Your spending pace is within your ${FinancialEngine.formatAmount(budgetLimit, currencySymbol)} limit. Aim for ${FinancialEngine.formatAmount(dailySafeToSpend, currencySymbol)}/day for the remaining $daysRemaining days.",
                        iconName = "check_circle",
                        isPositive = true
                    )
                )
            }
        }

        // Day of week habit
        if (peakDay != null && peakDay.totalAmount > 0) {
            insights.add(
                FinancialInsight(
                    title = "Peak Day: ${peakDay.dayName}",
                    description = "${peakDay.dayName}s represent your largest spending day, with ${FinancialEngine.formatAmount(peakDay.totalAmount, currencySymbol)} spent across ${peakDay.transactionCount} transactions.",
                    iconName = "event",
                    isPositive = true
                )
            )
        }

        // Weekend vs weekday pattern
        if (totalExpenses > 0 && weekendPercentage >= 40) {
            insights.add(
                FinancialInsight(
                    title = "Weekend Spending Concentration",
                    description = "$weekendPercentage% of your expenses occur on weekends (${FinancialEngine.formatAmount(weekendExpenseTotal, currencySymbol)}), indicating leisure or bulk shopping patterns.",
                    iconName = "weekend",
                    isPositive = false
                )
            )
        }

        // Top category pattern
        if (topCategory != null && topCategory.totalAmount > 0) {
            val pct = (topCategory.percentageOfTotal * 100).roundToInt()
            insights.add(
                FinancialInsight(
                    title = "Dominant Habit: ${topCategory.categoryName}",
                    description = "${topCategory.categoryName} accounts for $pct% of your total expenses (${FinancialEngine.formatAmount(topCategory.totalAmount, currencySymbol)} across ${topCategory.transactionCount} transactions).",
                    iconName = "category",
                    isPositive = pct < 50
                )
            )
        }

        return LearnedPatternReport(
            hasData = true,
            totalExpenses = totalExpenses,
            totalIncome = totalIncome,
            netBalance = netBalance,
            daysElapsed = daysElapsed,
            daysRemaining = daysRemaining,
            daysInMonth = daysInMonth,
            averageDailyBurnRate = averageDailyBurnRate,
            estimatedMonthEndExpense = estimatedMonthEndExpense,
            estimatedMonthEndSavings = estimatedMonthEndSavings,
            dailySafeToSpend = dailySafeToSpend,
            projectedBudgetOverage = projectedBudgetOverage,
            isProjectedToOverspend = isProjectedToOverspend,
            peakSpendingDay = peakDay,
            weekdayExpenseTotal = weekdayExpenseTotal,
            weekendExpenseTotal = weekendExpenseTotal,
            weekdayPercentage = weekdayPercentage,
            weekendPercentage = weekendPercentage,
            dominantPaymentMethod = dominantPaymentMethod,
            paymentMethodDistribution = paymentMethodDist,
            averageTransactionAmount = averageTransactionAmount,
            transactionFrequencyPerDay = transactionFrequencyPerDay,
            categoryBreakdown = categoryBreakdown,
            topCategory = topCategory,
            patternInsights = insights
        )
    }
}
