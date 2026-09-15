package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.ui.graphics.graphicsLayer
import com.example.ui.theme.AppMotion
import com.example.ui.theme.buttonTactilePress
import com.example.ui.theme.cardTactilePress
import com.example.ui.theme.fabTactilePress
import com.example.ui.theme.tactilePress
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TransactionEntity
import com.example.ui.AppTab
import com.example.ui.FinanceViewModel
import com.example.ui.StartupState
import com.example.ui.components.AddTransactionSheet
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.CustomizeProfileSheet
import com.example.ui.components.SetIncomeSheet
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.BudgetsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SavingsAndGoalsScreen
import com.example.ui.screens.TransactionsHistoryScreen
import com.example.ui.theme.AppTheme
import com.example.ui.theme.BrandGraphite
import com.example.ui.theme.FinanceTrackerTheme
import com.example.ui.theme.ModernCardBg
import com.example.ui.theme.ModernPageBg
import com.example.ui.theme.ModernTextDark
import com.example.ui.theme.ModernTextLight
import com.example.ui.theme.ModernTextSubtle
import com.example.ui.theme.tactilePress
import androidx.compose.foundation.interaction.MutableInteractionSource

class MainActivity : ComponentActivity() {

    private val viewModel: FinanceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinanceApp(viewModel = viewModel)
        }
    }
}

@Composable
fun FinanceApp(viewModel: FinanceViewModel) {
    val startupState by viewModel.startupState.collectAsStateWithLifecycle()
    val state by viewModel.dashboardState.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val monthlyTransactions by viewModel.monthlyTransactions.collectAsStateWithLifecycle()

    val isSystemDark = isSystemInDarkTheme()
    val useDarkTheme = when (state.themeMode.uppercase()) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemDark
    }

    FinanceTrackerTheme(darkTheme = useDarkTheme) {
        when (startupState) {
            StartupState.Loading -> {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("startup_loading_gate"),
                    color = ModernPageBg
                ) {
                    // Startup Gate: Neutral themed surface while reading persisted setup state.
                    // Absolutely NO OnboardingScreen or Welcome Setup is rendered during Loading.
                }
            }
            StartupState.NeedsSetup -> {
                OnboardingScreen(
                    onComplete = { name, code, symbol, income, budget, theme, seedIncome ->
                        viewModel.completeOnboarding(name, code, symbol, income, budget, theme, seedIncome)
                    }
                )
            }
            StartupState.Ready -> {
                var showAddSheet by remember { mutableStateOf(false) }
                var addSheetDefaultType by remember { mutableStateOf("EXPENSE") }
                var showSetIncomeSheet by remember { mutableStateOf(false) }
                var showCustomizeProfileSheet by remember { mutableStateOf(false) }
                var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
                var transactionToDeleteId by remember { mutableStateOf<Long?>(null) }

                val isDashboardDark = currentTab == AppTab.DASHBOARD && useDarkTheme
                Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("main_scaffold"),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = if (isDashboardDark) Color(0xFF000000) else MaterialTheme.colorScheme.background,
            bottomBar = {
                val bottomNavBg = if (isDashboardDark) Color(0xFF000000) else ModernCardBg
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            if (!isDashboardDark) {
                                drawLine(
                                    color = if (useDarkTheme) Color(0xFF2D293B) else Color(0xFFF1F2F6),
                                    start = Offset(0f, 0f),
                                    end = Offset(size.width, 0f),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }
                        }
                        .testTag("bottom_navigation_bar"),
                    color = bottomNavBg,
                    shadowElevation = if (isDashboardDark) 0.dp else 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ModernNavTabItem(
                            selected = currentTab == AppTab.DASHBOARD,
                            onClick = { viewModel.setTab(AppTab.DASHBOARD) },
                            icon = if (currentTab == AppTab.DASHBOARD) Icons.Filled.Home else Icons.Outlined.Home,
                            label = "Home",
                            modifier = Modifier.testTag("nav_tab_dashboard")
                        )

                        ModernNavTabItem(
                            selected = currentTab == AppTab.ANALYTICS,
                            onClick = { viewModel.setTab(AppTab.ANALYTICS) },
                            icon = if (currentTab == AppTab.ANALYTICS) Icons.Filled.BarChart else Icons.Outlined.BarChart,
                            label = "Report",
                            modifier = Modifier.testTag("nav_tab_analytics")
                        )

                        val isDark = AppTheme.colors.isDark
                        val fabInteraction = remember { MutableInteractionSource() }
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFFE5E5E5) else BrandGraphite)
                                .fabTactilePress(interactionSource = fabInteraction)
                                .clickable(
                                    interactionSource = fabInteraction,
                                    indication = null,
                                    onClick = {
                                        addSheetDefaultType = "EXPENSE"
                                        editingTransaction = null
                                        showAddSheet = true
                                    }
                                )
                                .testTag("main_add_fab"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Transaction",
                                tint = if (isDark) Color(0xFF171717) else Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        ModernNavTabItem(
                            selected = currentTab == AppTab.BUDGETS,
                            onClick = { viewModel.setTab(AppTab.BUDGETS) },
                            icon = if (currentTab == AppTab.BUDGETS) Icons.Filled.AccountBalanceWallet else Icons.Outlined.AccountBalanceWallet,
                            label = "Plan",
                            modifier = Modifier.testTag("nav_tab_budgets")
                        )

                        ModernNavTabItem(
                            selected = currentTab == AppTab.MORE,
                            onClick = { viewModel.setTab(AppTab.MORE) },
                            icon = if (currentTab == AppTab.MORE) Icons.Filled.Settings else Icons.Outlined.Settings,
                            label = "Settings",
                            modifier = Modifier.testTag("nav_tab_more")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding())
                    .clipToBounds()
            ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    val initialOrder = initialState.toTabOrder()
                    val targetOrder = targetState.toTabOrder()
                    val movingForward = targetOrder >= initialOrder

                    val slideSpec = AppMotion.ScreenSlideSpring
                    val scaleSpec = AppMotion.ScreenScaleSpring
                    val fadeSpec = AppMotion.ScreenFadeTween

                    if (movingForward) {
                        // Forward Navigation:
                        // Destination enters from the RIGHT (+width -> 0) with subtle spatial scale (0.96 -> 1.0)
                        // Current exits to the LEFT (0 -> -width) with subtle scale (1.0 -> 0.96)
                        (slideInHorizontally(animationSpec = slideSpec) { width -> width } +
                            scaleIn(initialScale = 0.96f, animationSpec = scaleSpec) +
                            fadeIn(animationSpec = fadeSpec))
                            .togetherWith(
                                slideOutHorizontally(animationSpec = slideSpec) { width -> -width } +
                                    scaleOut(targetScale = 0.96f, animationSpec = scaleSpec) +
                                    fadeOut(animationSpec = fadeSpec)
                            )
                    } else {
                        // Backward Navigation:
                        // Destination enters from the LEFT (-width -> 0) with subtle spatial scale (0.96 -> 1.0)
                        // Current exits to the RIGHT (0 -> +width) with subtle scale (1.0 -> 0.96)
                        (slideInHorizontally(animationSpec = slideSpec) { width -> -width } +
                            scaleIn(initialScale = 0.96f, animationSpec = scaleSpec) +
                            fadeIn(animationSpec = fadeSpec))
                            .togetherWith(
                                slideOutHorizontally(animationSpec = slideSpec) { width -> width } +
                                    scaleOut(targetScale = 0.96f, animationSpec = scaleSpec) +
                                    fadeOut(animationSpec = fadeSpec)
                            )
                    }
                },
                modifier = Modifier.fillMaxSize(),
                label = "main_tabs_slide_transition"
            ) { currentTab ->
                when (currentTab) {
                    AppTab.DASHBOARD -> {
                        DashboardScreen(
                            state = state,
                            onPrevMonth = { viewModel.prevMonth() },
                            onNextMonth = { viewModel.nextMonth() },
                            onSelectMonth = { m, y -> viewModel.setMonthAndYear(m, y) },
                            onAddExpenseClick = {
                                addSheetDefaultType = "EXPENSE"
                                editingTransaction = null
                                showAddSheet = true
                            },
                            onAddIncomeClick = {
                                addSheetDefaultType = "INCOME"
                                editingTransaction = null
                                showAddSheet = true
                            },
                            onSetIncomeClick = {
                                showSetIncomeSheet = true
                            },
                            onOpenCustomizeProfile = {
                                showCustomizeProfileSheet = true
                            },
                            onTransactionClick = { tx ->
                                editingTransaction = tx
                                showAddSheet = true
                            },
                            onDeleteTransactionClick = { id ->
                                transactionToDeleteId = id
                            },
                            onNavigateTab = { tab ->
                                viewModel.setTab(tab)
                            }
                        )
                    }

                    AppTab.HISTORY -> {
                        TransactionsHistoryScreen(
                            transactions = monthlyTransactions,
                            categories = categories,
                            selectedMonth = state.selectedMonth,
                            selectedYear = state.selectedYear,
                            currencySymbol = state.currencySymbol,
                            onPrevMonth = { viewModel.prevMonth() },
                            onNextMonth = { viewModel.nextMonth() },
                            onSelectMonth = { m, y -> viewModel.setMonthAndYear(m, y) },
                            onTransactionClick = { tx ->
                                editingTransaction = tx
                                showAddSheet = true
                            },
                            onDeleteTransactionClick = { id ->
                                transactionToDeleteId = id
                            }
                        )
                    }

                    AppTab.ANALYTICS -> {
                        AnalyticsScreen(
                            transactions = monthlyTransactions,
                            categories = categories,
                            insights = state.insights,
                            selectedMonth = state.selectedMonth,
                            selectedYear = state.selectedYear,
                            currencySymbol = state.currencySymbol,
                            onPrevMonth = { viewModel.prevMonth() },
                            onNextMonth = { viewModel.nextMonth() },
                            onSelectMonth = { m, y -> viewModel.setMonthAndYear(m, y) },
                            patternReport = state.patternReport
                        )
                    }

                    AppTab.BUDGETS -> {
                        BudgetsScreen(
                            overallBudget = state.overallBudgetEntity ?: state.overallBudgetStatus?.let {
                                com.example.data.model.BudgetEntity(
                                    amount = it.limit,
                                    month = state.selectedMonth,
                                    year = state.selectedYear
                                )
                            },
                            categoryBudgets = state.categoryBudgets,
                            transactions = monthlyTransactions,
                            categories = categories,
                            goals = state.goals,
                            selectedMonth = state.selectedMonth,
                            selectedYear = state.selectedYear,
                            currencySymbol = state.currencySymbol,
                            onPrevMonth = { viewModel.prevMonth() },
                            onNextMonth = { viewModel.nextMonth() },
                            onSelectMonth = { m, y -> viewModel.setMonthAndYear(m, y) },
                            onSaveOverallBudget = { amount -> viewModel.setOverallBudget(amount) },
                            onDeleteOverallBudget = { viewModel.clearOverallBudget() },
                            onSaveCategoryBudget = { catId, amount -> viewModel.setCategoryBudget(catId, amount) },
                            onDeleteBudget = { id -> viewModel.deleteBudget(id) },
                            onViewAllGoals = { viewModel.setTab(AppTab.MORE) }
                        )
                    }

                    AppTab.MORE -> {
                        SavingsAndGoalsScreen(
                            goals = state.goals,
                            subscriptions = state.subscriptions,
                            categories = categories,
                            transactions = monthlyTransactions,
                            currentCurrencyCode = state.currencyCode,
                            currentCurrencySymbol = state.currencySymbol,
                            userName = state.userName,
                            monthlyIncome = state.monthlyIncomeTarget,
                            monthlyBudget = state.monthlyBudgetTarget,
                            themeMode = state.themeMode,
                            onUpdateThemeMode = { mode -> viewModel.setThemeMode(mode) },
                            onAddGoal = { name, target, initial -> viewModel.addSavingsGoal(name, target, initial) },
                            onAdjustGoalFunds = { id, delta -> viewModel.adjustGoalFunds(id, delta) },
                            onDeleteGoal = { id -> viewModel.deleteGoal(id) },
                            onAddSubscription = { name, amount, cycle -> viewModel.addSubscription(name, amount, cycle) },
                            onToggleSubscription = { sub -> viewModel.toggleSubscription(sub) },
                            onDeleteSubscription = { id -> viewModel.deleteSubscription(id) },
                            onUpdateCurrency = { code, symbol -> viewModel.updateCurrency(code, symbol) },
                            onExportCsv = { viewModel.exportTransactionsCsv(monthlyTransactions, categories) },
                            onResetData = { viewModel.resetData() },
                            onOpenCustomizeProfile = { showCustomizeProfileSheet = true },
                            onReopenOnboarding = { viewModel.reopenOnboarding() }
                        )
                    }
                }
            }
            }
        }

        // Add / Edit Transaction BottomSheet
        if (showAddSheet) {
            AddTransactionSheet(
                categories = categories,
                currencySymbol = state.currencySymbol,
                initialTransaction = editingTransaction,
                defaultType = addSheetDefaultType,
                onSave = { amount, type, categoryId, date, note, paymentMethod ->
                    if (editingTransaction == null) {
                        viewModel.addTransaction(amount, type, categoryId, date, note, paymentMethod)
                    } else {
                        viewModel.updateTransaction(
                            editingTransaction!!.copy(
                                amount = amount,
                                type = type,
                                categoryId = categoryId,
                                date = date,
                                note = note,
                                paymentMethod = paymentMethod
                            )
                        )
                    }
                    showAddSheet = false
                    editingTransaction = null
                },
                onDismiss = {
                    showAddSheet = false
                    editingTransaction = null
                }
            )
        }

        // Set / Manage Income BottomSheet
        if (showSetIncomeSheet) {
            SetIncomeSheet(
                currentMonthlyIncome = state.monthlyIncomeTarget,
                currentMonthTotalIncome = state.totalIncome,
                incomeTransactions = monthlyTransactions.filter { it.type == "INCOME" },
                categories = categories,
                currencySymbol = state.currencySymbol,
                onSaveMonthlyIncome = { amount, seedInitialTx ->
                    viewModel.setMonthlyIncome(amount, seedInitialTx)
                    showSetIncomeSheet = false
                },
                onOpenAddIncomeTransaction = {
                    showSetIncomeSheet = false
                    addSheetDefaultType = "INCOME"
                    editingTransaction = null
                    showAddSheet = true
                },
                onEditIncomeTransaction = { tx ->
                    showSetIncomeSheet = false
                    editingTransaction = tx
                    showAddSheet = true
                },
                onDeleteIncomeTransaction = { id ->
                    transactionToDeleteId = id
                },
                onDismiss = { showSetIncomeSheet = false }
            )
        }

        // Customize Profile & Appearance BottomSheet
        if (showCustomizeProfileSheet) {
            CustomizeProfileSheet(
                initialName = state.userName,
                initialMonthlyIncome = state.monthlyIncomeTarget,
                initialMonthlyBudget = state.monthlyBudgetTarget,
                initialCurrencyCode = state.currencyCode,
                initialCurrencySymbol = state.currencySymbol,
                initialThemeMode = state.themeMode,
                onThemeSelected = { mode -> viewModel.setThemeMode(mode) },
                onSave = { name, income, budget, code, symbol, theme ->
                    viewModel.updateProfileDetails(name, income, budget, code, symbol, theme)
                },
                onDismiss = { showCustomizeProfileSheet = false }
            )
        }

                // Confirm Delete Dialog
                if (transactionToDeleteId != null) {
                    ConfirmDeleteDialog(
                        title = "Delete Transaction",
                        message = "Are you sure you want to delete this transaction? This will update your balances and budget calculations.",
                        onConfirm = {
                            transactionToDeleteId?.let { id ->
                                viewModel.deleteTransaction(id)
                            }
                            transactionToDeleteId = null
                        },
                        onDismiss = {
                            transactionToDeleteId = null
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernNavTabItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    val activeColor = AppTheme.colors.activeNavTint
    val inactiveColor = AppTheme.colors.inactiveNavTint
    val interactionSource = remember { MutableInteractionSource() }

    val animatedTint by animateColorAsState(
        targetValue = if (selected) activeColor else inactiveColor,
        animationSpec = tween(durationMillis = 200),
        label = "nav_item_tint"
    )

    val iconScaleState = animateFloatAsState(
        targetValue = if (selected) 1.08f else 1.0f,
        animationSpec = AppMotion.TouchSpring,
        label = "nav_item_scale"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .buttonTactilePress(pressedScale = 0.94f, interactionSource = interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = animatedTint,
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer {
                    val s = iconScaleState.value
                    scaleX = s
                    scaleY = s
                }
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = animatedTint,
            fontSize = 11.sp
        )
    }
}

/**
 * Spatial tab order for natural iOS-inspired directional transitions:
 * Home (0) -> Report (1) -> Plan (2) -> Settings (3)
 */
private fun AppTab.toTabOrder(): Int = when (this) {
    AppTab.DASHBOARD -> 0
    AppTab.ANALYTICS -> 1
    AppTab.BUDGETS -> 2
    AppTab.MORE -> 3
    AppTab.HISTORY -> 1
}
