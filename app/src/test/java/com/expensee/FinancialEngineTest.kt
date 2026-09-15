package com.expensee

import com.expensee.data.model.TransactionEntity
import com.expensee.domain.BudgetState
import com.expensee.domain.FinancialEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FinancialEngineTest {

    private val sampleTransactions = listOf(
        TransactionEntity(
            id = 1,
            amount = 50000.0,
            type = "INCOME",
            categoryId = 1,
            date = 1000L,
            note = "Salary",
            paymentMethod = "Bank"
        ),
        TransactionEntity(
            id = 2,
            amount = 10000.0,
            type = "EXPENSE",
            categoryId = 2,
            date = 2000L,
            note = "Rent",
            paymentMethod = "Bank"
        ),
        TransactionEntity(
            id = 3,
            amount = 5000.0,
            type = "EXPENSE",
            categoryId = 3,
            date = 3000L,
            note = "Groceries",
            paymentMethod = "Card"
        ),
        TransactionEntity(
            id = 4,
            amount = 5000.0,
            type = "INCOME",
            categoryId = 4,
            date = 4000L,
            note = "Freelance",
            paymentMethod = "Cash"
        )
    )

    @Test
    fun testIncomeCalculation() {
        val income = FinancialEngine.calculateMonthlyIncome(sampleTransactions)
        assertEquals(55000.0, income, 0.001)
    }

    @Test
    fun testExpenseCalculation() {
        val expenses = FinancialEngine.calculateMonthlyExpenses(sampleTransactions)
        assertEquals(15000.0, expenses, 0.001)
    }

    @Test
    fun testAvailableBalance() {
        val balance = FinancialEngine.calculateAvailableBalance(55000.0, 15000.0)
        assertEquals(40000.0, balance, 0.001)
    }

    @Test
    fun testSavingsRate() {
        val savings = FinancialEngine.calculateSavings(55000.0, 15000.0)
        assertEquals(40000.0, savings, 0.001)

        val rate = FinancialEngine.calculateSavingsRate(55000.0, 15000.0)
        // 40000 / 55000 * 100 = 72.72%
        assertEquals(72.72, rate, 0.1)
    }

    @Test
    fun testBudgetCalculations() {
        // Within budget: 10,000 spent out of 20,000 (50%)
        val normalStatus = FinancialEngine.calculateBudgetStatus(10000.0, 20000.0)
        assertEquals(50.0, normalStatus.percentageUsed, 0.01)
        assertEquals(10000.0, normalStatus.remaining, 0.01)
        assertEquals(BudgetState.NORMAL, normalStatus.state)

        // Approaching budget: 18,000 spent out of 20,000 (90%)
        val approachingStatus = FinancialEngine.calculateBudgetStatus(18000.0, 20000.0)
        assertEquals(90.0, approachingStatus.percentageUsed, 0.01)
        assertEquals(BudgetState.APPROACHING, approachingStatus.state)

        // Exceeded budget: 25,000 spent out of 20,000 (125%)
        val exceededStatus = FinancialEngine.calculateBudgetStatus(25000.0, 20000.0)
        assertEquals(125.0, exceededStatus.percentageUsed, 0.01)
        assertEquals(0.0, exceededStatus.remaining, 0.01)
        assertEquals(BudgetState.EXCEEDED, exceededStatus.state)
    }

    @Test
    fun testCategoryTotals() {
        val totals = FinancialEngine.calculateCategoryTotals(sampleTransactions, "EXPENSE")
        assertEquals(10000.0, totals[2L] ?: 0.0, 0.01)
        assertEquals(5000.0, totals[3L] ?: 0.0, 0.01)
    }

    @Test
    fun testSmartInsights() {
        val catNames = mapOf(2L to "Rent", 3L to "Groceries")
        val insights = FinancialEngine.generateSmartInsights(
            transactions = sampleTransactions,
            previousMonthExpenses = 12000.0,
            budgetLimit = 20000.0,
            categoryNames = catNames,
            currencySymbol = "৳"
        )

        assertTrue(insights.isNotEmpty())
        // Should detect Rent as top expense
        val topExpenseInsight = insights.find { it.title.contains("Top Spending") }
        assertTrue(topExpenseInsight != null)
        assertTrue(topExpenseInsight!!.description.contains("Rent"))
    }

    @Test
    fun testIncomeSalaryCorrectionFlow() {
        // Step 1: Add income 50,000 and expense 10,000
        val txIncome = TransactionEntity(
            id = 1,
            amount = 50000.0,
            type = "INCOME",
            categoryId = 1,
            date = 1000L,
            note = "Monthly Income",
            paymentMethod = "Bank"
        )
        val txExpense = TransactionEntity(
            id = 2,
            amount = 10000.0,
            type = "EXPENSE",
            categoryId = 2,
            date = 2000L,
            note = "Rent",
            paymentMethod = "Bank"
        )
        var currentTxs = listOf(txIncome, txExpense)

        var totalIncome = FinancialEngine.calculateMonthlyIncome(currentTxs)
        var totalExpense = FinancialEngine.calculateMonthlyExpenses(currentTxs)
        var balance = FinancialEngine.calculateAvailableBalance(totalIncome, totalExpense)

        assertEquals(50000.0, totalIncome, 0.001)
        assertEquals(10000.0, totalExpense, 0.001)
        assertEquals(40000.0, balance, 0.001)
        assertEquals("$40,000", FinancialEngine.formatAmount(balance, "$"))

        // Step 2: Edit income 50,000 -> 40,000
        val editedIncome = txIncome.copy(amount = 40000.0)
        currentTxs = listOf(editedIncome, txExpense)

        totalIncome = FinancialEngine.calculateMonthlyIncome(currentTxs)
        totalExpense = FinancialEngine.calculateMonthlyExpenses(currentTxs)
        balance = FinancialEngine.calculateAvailableBalance(totalIncome, totalExpense)

        assertEquals(40000.0, totalIncome, 0.001)
        assertEquals(10000.0, totalExpense, 0.001)
        assertEquals(30000.0, balance, 0.001)
        assertEquals("$30,000", FinancialEngine.formatAmount(balance, "$"))

        // Step 3: Delete the income
        currentTxs = listOf(txExpense)

        totalIncome = FinancialEngine.calculateMonthlyIncome(currentTxs)
        totalExpense = FinancialEngine.calculateMonthlyExpenses(currentTxs)
        balance = FinancialEngine.calculateAvailableBalance(totalIncome, totalExpense)

        assertEquals(0.0, totalIncome, 0.001)
        assertEquals(10000.0, totalExpense, 0.001)
        assertEquals(-10000.0, balance, 0.001)
        assertEquals("-$10,000", FinancialEngine.formatAmount(balance, "$"))
    }

    @Test
    fun testFinancialEdgeCases() {
        // Zero transactions
        val emptyTxs = emptyList<TransactionEntity>()
        val zeroIncome = FinancialEngine.calculateMonthlyIncome(emptyTxs)
        val zeroExpense = FinancialEngine.calculateMonthlyExpenses(emptyTxs)
        val zeroBalance = FinancialEngine.calculateAvailableBalance(zeroIncome, zeroExpense)
        val zeroSavingsRate = FinancialEngine.calculateSavingsRate(zeroIncome, zeroExpense)

        assertEquals(0.0, zeroIncome, 0.001)
        assertEquals(0.0, zeroExpense, 0.001)
        assertEquals(0.0, zeroBalance, 0.001)
        assertEquals(0.0, zeroSavingsRate, 0.001)
        assertTrue(!zeroSavingsRate.isNaN())
        assertTrue(!zeroSavingsRate.isInfinite())

        // Negative balance formatting
        assertEquals("-$10,000", FinancialEngine.formatAmount(-10000.0, "$"))
        assertEquals("-$10,000.50", FinancialEngine.formatAmount(-10000.50, "$", includeDecimals = true))

        // Large amounts
        assertEquals("$10,000,000", FinancialEngine.formatAmount(10000000.0, "$"))

        // Division by zero in budget status (budgetLimit = 0.0)
        val zeroBudgetStatus = FinancialEngine.calculateBudgetStatus(5000.0, 0.0)
        assertEquals(0.0, zeroBudgetStatus.percentageUsed, 0.001)
        assertEquals(BudgetState.NORMAL, zeroBudgetStatus.state)
        assertTrue(!zeroBudgetStatus.percentageUsed.isNaN())
        assertTrue(!zeroBudgetStatus.percentageUsed.isInfinite())

        // Division by zero in savings rate (income = 0.0, expense = 5000.0)
        val noIncomeRate = FinancialEngine.calculateSavingsRate(0.0, 5000.0)
        assertEquals(0.0, noIncomeRate, 0.001)
        assertTrue(!noIncomeRate.isNaN())
        assertTrue(!noIncomeRate.isInfinite())

        // Insights on empty transactions
        val emptyInsights = FinancialEngine.generateSmartInsights(
            transactions = emptyTxs,
            previousMonthExpenses = 0.0,
            budgetLimit = 0.0,
            categoryNames = emptyMap(),
            currencySymbol = "$"
        )
        assertTrue(emptyInsights.isEmpty())
    }
}
