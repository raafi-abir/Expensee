package com.expensee.domain

import androidx.compose.runtime.Immutable
import com.expensee.data.model.TransactionEntity
import java.text.DecimalFormat
import java.util.Calendar

enum class BudgetState {
    NORMAL,
    APPROACHING,
    EXCEEDED
}

@Immutable
data class BudgetCalculation(
    val limit: Double,
    val spent: Double,
    val remaining: Double,
    val percentageUsed: Double,
    val state: BudgetState
)

@Immutable
data class FinancialInsight(
    val title: String,
    val description: String,
    val iconName: String,
    val isPositive: Boolean
)

object FinancialEngine {

    private val numberFormat = ThreadLocal.withInitial { DecimalFormat("#,##0") }
    private val decimalFormat = ThreadLocal.withInitial { DecimalFormat("#,##0.00") }

    fun calculateIncomeAndExpenses(transactions: List<TransactionEntity>): Pair<Double, Double> {
        var inc = 0.0
        var exp = 0.0
        for (tx in transactions) {
            if (tx.type == "EXPENSE") {
                exp += tx.amount
            } else if (tx.type == "INCOME") {
                inc += tx.amount
            }
        }
        return inc to exp
    }

    fun calculateMonthlyIncome(transactions: List<TransactionEntity>): Double {
        var sum = 0.0
        for (tx in transactions) {
            if (tx.type == "INCOME") sum += tx.amount
        }
        return sum
    }

    fun calculateMonthlyExpenses(transactions: List<TransactionEntity>): Double {
        var sum = 0.0
        for (tx in transactions) {
            if (tx.type == "EXPENSE") sum += tx.amount
        }
        return sum
    }

    fun calculateAvailableBalance(income: Double, expenses: Double): Double {
        return income - expenses
    }

    fun calculateSavings(income: Double, expenses: Double): Double {
        return income - expenses
    }

    fun calculateSavingsRate(income: Double, expenses: Double): Double {
        if (income <= 0.0) return 0.0
        val savings = income - expenses
        val rate = (savings / income) * 100.0
        return when {
            rate.isNaN() || rate.isInfinite() -> 0.0
            else -> rate
        }
    }

    fun calculateCategoryTotals(
        transactions: List<TransactionEntity>,
        type: String = "EXPENSE"
    ): Map<Long, Double> {
        return transactions
            .filter { it.type == type }
            .groupBy { it.categoryId }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }

    fun calculateDailySpending(
        transactions: List<TransactionEntity>,
        daysInMonth: Int
    ): Map<Int, Double> {
        val result = mutableMapOf<Int, Double>()
        for (day in 1..daysInMonth) {
            result[day] = 0.0
        }
        val cal = Calendar.getInstance()
        for (trans in transactions) {
            if (trans.type == "EXPENSE") {
                cal.timeInMillis = trans.date
                val day = cal.get(Calendar.DAY_OF_MONTH)
                if (day in 1..daysInMonth) {
                    result[day] = (result[day] ?: 0.0) + trans.amount
                }
            }
        }
        return result
    }

    fun calculateBudgetStatus(spent: Double, budgetLimit: Double): BudgetCalculation {
        if (budgetLimit <= 0.0) {
            return BudgetCalculation(
                limit = 0.0,
                spent = spent,
                remaining = 0.0,
                percentageUsed = 0.0,
                state = BudgetState.NORMAL
            )
        }
        val percentage = (spent / budgetLimit) * 100.0
        val remaining = (budgetLimit - spent).coerceAtLeast(0.0)
        val state = when {
            percentage >= 100.0 -> BudgetState.EXCEEDED
            percentage >= 80.0 -> BudgetState.APPROACHING
            else -> BudgetState.NORMAL
        }
        return BudgetCalculation(
            limit = budgetLimit,
            spent = spent,
            remaining = remaining,
            percentageUsed = percentage,
            state = state
        )
    }

    fun formatAmount(amount: Double, symbol: String = "৳", includeDecimals: Boolean = false): String {
        val isNegative = amount < 0.0
        val absAmount = kotlin.math.abs(amount)
        val df = if (includeDecimals) decimalFormat.get() else numberFormat.get()
        val formattedNumber = df?.format(absAmount) ?: absAmount.toString()
        return if (isNegative) "-$symbol$formattedNumber" else "$symbol$formattedNumber"
    }

    fun generateSmartInsights(
        transactions: List<TransactionEntity>,
        previousMonthExpenses: Double,
        budgetLimit: Double,
        categoryNames: Map<Long, String>,
        currencySymbol: String = "৳"
    ): List<FinancialInsight> {
        val insights = mutableListOf<FinancialInsight>()
        val currentExpenses = calculateMonthlyExpenses(transactions)
        val currentIncome = calculateMonthlyIncome(transactions)
        val savings = calculateSavings(currentIncome, currentExpenses)
        val savingsRate = calculateSavingsRate(currentIncome, currentExpenses)

        // Top spending category
        val categoryTotals = calculateCategoryTotals(transactions, "EXPENSE")
        if (categoryTotals.isNotEmpty()) {
            val highestEntry = categoryTotals.maxByOrNull { it.value }
            if (highestEntry != null && highestEntry.value > 0) {
                val catName = categoryNames[highestEntry.key] ?: "Category"
                val percentOfTotal = if (currentExpenses > 0) (highestEntry.value / currentExpenses * 100).toInt() else 0
                insights.add(
                    FinancialInsight(
                        title = "Top Spending: $catName",
                        description = "$catName accounted for $percentOfTotal% of your expenses (${formatAmount(highestEntry.value, currencySymbol)}).",
                        iconName = "pie_chart",
                        isPositive = false
                    )
                )
            }
        }

        // Budget adherence
        if (budgetLimit > 0) {
            val status = calculateBudgetStatus(currentExpenses, budgetLimit)
            when (status.state) {
                BudgetState.EXCEEDED -> {
                    val overspent = currentExpenses - budgetLimit
                    insights.add(
                        FinancialInsight(
                            title = "Budget Limit Exceeded",
                            description = "You've surpassed your budget by ${formatAmount(overspent, currencySymbol)} (${status.percentageUsed.toInt()}% used).",
                            iconName = "warning",
                            isPositive = false
                        )
                    )
                }
                BudgetState.APPROACHING -> {
                    insights.add(
                        FinancialInsight(
                            title = "Approaching Budget Limit",
                            description = "You have used ${status.percentageUsed.toInt()}% of your budget. ${formatAmount(status.remaining, currencySymbol)} remaining.",
                            iconName = "info",
                            isPositive = false
                        )
                    )
                }
                BudgetState.NORMAL -> {
                    insights.add(
                        FinancialInsight(
                            title = "Budget On Track",
                            description = "You are within your monthly budget with ${formatAmount(status.remaining, currencySymbol)} remaining.",
                            iconName = "check_circle",
                            isPositive = true
                        )
                    )
                }
            }
        }

        // Month over month comparison
        if (previousMonthExpenses > 0 && currentExpenses > 0) {
            val diff = currentExpenses - previousMonthExpenses
            val diffPercent = kotlin.math.abs((diff / previousMonthExpenses) * 100).toInt()
            if (diff > 0) {
                insights.add(
                    FinancialInsight(
                        title = "Spending Trend",
                        description = "You spent $diffPercent% more compared to last month.",
                        iconName = "trending_up",
                        isPositive = false
                    )
                )
            } else if (diff < 0) {
                insights.add(
                    FinancialInsight(
                        title = "Reduced Spending",
                        description = "You spent $diffPercent% less compared to last month. Great job!",
                        iconName = "trending_down",
                        isPositive = true
                    )
                )
            }
        }

        // Savings rate
        if (currentIncome > 0) {
            if (savingsRate >= 30.0) {
                insights.add(
                    FinancialInsight(
                        title = "Healthy Savings Rate",
                        description = "You've saved ${savingsRate.toInt()}% (${formatAmount(savings, currencySymbol)}) of your earnings this month.",
                        iconName = "savings",
                        isPositive = true
                    )
                )
            } else if (savingsRate > 0) {
                insights.add(
                    FinancialInsight(
                        title = "Positive Savings",
                        description = "Savings rate is currently ${savingsRate.toInt()}%. Keep building your buffer.",
                        iconName = "savings",
                        isPositive = true
                    )
                )
            }
        }

        return insights
    }
}
