package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionEntity
import com.example.domain.BudgetCalculation
import com.example.domain.BudgetState
import com.example.ui.DashboardUiState
import com.example.ui.screens.DashboardScreen
import com.example.ui.theme.FinanceTrackerTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun finance_tracker_dashboard_screenshot() {
    val sampleCategories = listOf(
      CategoryEntity(id = 1, name = "Food & Dining", iconName = "restaurant", colorHex = "#EF4444", type = "EXPENSE"),
      CategoryEntity(id = 2, name = "Groceries", iconName = "shopping_cart", colorHex = "#10B981", type = "EXPENSE"),
      CategoryEntity(id = 3, name = "Salary", iconName = "payments", colorHex = "#3B82F6", type = "INCOME")
    )

    val sampleTransactions = listOf(
      TransactionEntity(id = 1, amount = 45000.0, type = "INCOME", categoryId = 3, date = System.currentTimeMillis(), note = "Monthly Salary", paymentMethod = "Bank"),
      TransactionEntity(id = 2, amount = 1200.0, type = "EXPENSE", categoryId = 1, date = System.currentTimeMillis() - 3600000, note = "Dinner with team", paymentMethod = "Card"),
      TransactionEntity(id = 3, amount = 3500.0, type = "EXPENSE", categoryId = 2, date = System.currentTimeMillis() - 86400000, note = "Weekly market", paymentMethod = "Cash")
    )

    val testState = DashboardUiState(
      selectedMonth = 9,
      selectedYear = 2026,
      currencySymbol = "৳",
      currencyCode = "BDT",
      availableBalance = 40300.0,
      totalIncome = 45000.0,
      totalExpenses = 4700.0,
      totalSaved = 40300.0,
      savingsRate = 89.0,
      overallBudgetStatus = BudgetCalculation(
        spent = 4700.0,
        limit = 25000.0,
        remaining = 20300.0,
        percentageUsed = 18.8,
        state = BudgetState.NORMAL
      ),
      recentTransactions = sampleTransactions,
      categories = sampleCategories
    )

    composeTestRule.setContent {
      FinanceTrackerTheme(darkTheme = true) {
        DashboardScreen(
          state = testState,
          onPrevMonth = {},
          onNextMonth = {},
          onAddExpenseClick = {},
          onAddIncomeClick = {},
          onTransactionClick = {},
          onDeleteTransactionClick = {},
          onNavigateTab = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
