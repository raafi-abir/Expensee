package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionEntity
import com.example.domain.FinancialEngine
import com.example.ui.AppTab
import com.example.ui.DashboardUiState
import com.example.ui.components.CategoryIconHelper
import com.example.ui.components.MonthYearPickerSheet
import com.example.ui.theme.AppTheme
import com.example.ui.theme.BrandGraphite
import com.example.ui.theme.DashboardAshHeader
import com.example.ui.theme.ModernCardBg
import com.example.ui.theme.ModernCardBorder
import com.example.ui.theme.ModernDarkInsightBg
import com.example.ui.theme.ModernExpenseRed
import com.example.ui.theme.ModernExpenseRedBg
import com.example.ui.theme.ModernIncomeBlue
import com.example.ui.theme.ModernIncomeBlueBg
import com.example.ui.theme.ModernPageBg
import com.example.ui.theme.ModernTextDark
import com.example.ui.theme.ModernTextLight
import com.example.ui.theme.ModernTextSubtle
import com.example.ui.theme.AppDateFormatters
import com.example.ui.theme.tactilePress
import com.example.ui.theme.cardTactilePress
import com.example.ui.theme.buttonTactilePress
import com.example.ui.theme.AnimatedFinancialAmount
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith

private val DASHBOARD_MONTH_NAMES = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
)

@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectMonth: (month: Int, year: Int) -> Unit = { _, _ -> },
    onAddExpenseClick: () -> Unit,
    onAddIncomeClick: () -> Unit,
    onSetIncomeClick: () -> Unit = onAddIncomeClick,
    onOpenCustomizeProfile: () -> Unit = {},
    onTransactionClick: (TransactionEntity) -> Unit,
    onDeleteTransactionClick: (Long) -> Unit,
    onNavigateTab: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentMonthName = DASHBOARD_MONTH_NAMES.getOrElse(state.selectedMonth - 1) { "November" }

    var showMonthDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(if (AppTheme.colors.isDark) Color(0xFF000000) else ModernPageBg)
            .testTag("dashboard_screen")
    ) {
        // --- 1. Neutral Graphite Hero Header ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (AppTheme.colors.isDark) Color(0xFF262626) else DashboardAshHeader)
                    .testTag("hero_balance_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    // Header Bar: Profile Avatar, Month Pill, Notification Bell
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Profile Avatar (Initials)
                        val initials = state.userName.trim().take(1).uppercase().ifEmpty { "U" }
                        val avatarInteraction = remember { MutableInteractionSource() }
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.22f),
                            border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.45f)),
                            modifier = Modifier
                                .size(40.dp)
                                .buttonTactilePress(pressedScale = 0.92f, interactionSource = avatarInteraction)
                                .clickable(
                                    interactionSource = avatarInteraction,
                                    indication = null,
                                    onClick = { onNavigateTab(AppTab.MORE) }
                                )
                                .testTag("dashboard_profile_avatar")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = initials,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        // Center Month Selector Pill ("November 2025 v")
                        val monthPillInteraction = remember { MutableInteractionSource() }
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.20f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .buttonTactilePress(pressedScale = 0.95f, interactionSource = monthPillInteraction)
                                .clickable(
                                    interactionSource = monthPillInteraction,
                                    indication = null,
                                    onClick = { showMonthDialog = true }
                                )
                                .testTag("month_selector_pill")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "$currentMonthName ${state.selectedYear}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    modifier = Modifier.testTag("month_title_text")
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Change Month",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Right Notification Bell with Red Dot
                        val bellInteraction = remember { MutableInteractionSource() }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.20f))
                                .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                                .buttonTactilePress(pressedScale = 0.92f, interactionSource = bellInteraction)
                                .clickable(
                                    interactionSource = bellInteraction,
                                    indication = null,
                                    onClick = { showNotificationsDialog = true }
                                )
                                .testTag("dashboard_notification_bell"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            // Red Badge Dot
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .align(Alignment.TopEnd)
                                    .padding(top = 8.dp, end = 8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEF4444))
                                    .border(1.dp, Color.White, CircleShape)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Hero Balance Section
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Current Balance",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Normal,
                            color = Color.White.copy(alpha = 0.82f)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        val displayBalance = remember(state.availableBalance, state.currencySymbol) {
                            FinancialEngine.formatAmount(state.availableBalance, state.currencySymbol)
                        }

                        Box(
                            modifier = Modifier.clipToBounds(),
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedContent(
                                targetState = displayBalance,
                                transitionSpec = {
                                    (fadeIn(animationSpec = tween(200)) + slideInVertically(animationSpec = tween(200)) { it / 5 })
                                        .togetherWith(fadeOut(animationSpec = tween(160)) + slideOutVertically(animationSpec = tween(160)) { -it / 5 })
                                },
                                label = "available_balance_anim"
                            ) { balanceText ->
                                Text(
                                    text = balanceText,
                                    style = MaterialTheme.typography.displayMedium.copy(
                                        fontSize = 38.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color.White,
                                    modifier = Modifier.testTag("available_balance_text")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Data-Driven Trend Badge based on real transaction pattern
                        val report = state.patternReport
                        val trendText = when {
                            report != null && report.hasData && report.averageDailyBurnRate > 0 -> {
                                "~${FinancialEngine.formatAmount(report.averageDailyBurnRate, state.currencySymbol)}/day avg pace"
                            }
                            state.totalIncome > 0 && state.totalExpenses == 0.0 -> {
                                "+${FinancialEngine.formatAmount(state.totalIncome, state.currencySymbol)} net"
                            }
                            state.totalExpenses > 0 -> {
                                "-${FinancialEngine.formatAmount(state.totalExpenses, state.currencySymbol)} spent"
                            }
                            else -> "Zero balance · Ready to track"
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White.copy(alpha = 0.22f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.32f))
                        ) {
                            Text(
                                text = trendText,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(26.dp))
                }
            }
        }

        // --- 2. Lower Content Section (Pure Black in Dark Mode, Card Sheet in Light Mode) ---
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = if (AppTheme.colors.isDark) RectangleShape else RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = if (AppTheme.colors.isDark) Color(0xFF000000) else ModernCardBg,
                shadowElevation = if (AppTheme.colors.isDark) 0.dp else 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    // "Your Money" Row with "Details >"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Your Money",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ModernTextDark
                            )
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Info",
                                tint = ModernTextLight,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Text(
                            text = "Details ›",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = ModernTextSubtle,
                            modifier = Modifier.clickable { onNavigateTab(AppTab.ANALYTICS) }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Two Cards: Income & Expenses Side-by-Side
                    val incomeCardInteraction = remember { MutableInteractionSource() }
                    val expenseCardInteraction = remember { MutableInteractionSource() }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Income Card
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .cardTactilePress(pressedScale = 0.985f, interactionSource = incomeCardInteraction)
                                .clickable(
                                    interactionSource = incomeCardInteraction,
                                    indication = null,
                                    onClick = onAddIncomeClick
                                )
                                .testTag("quick_add_income_button"),
                            shape = RoundedCornerShape(20.dp),
                            color = ModernCardBg,
                            border = BorderStroke(1.dp, ModernCardBorder),
                            shadowElevation = 0.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(ModernIncomeBlueBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Savings,
                                            contentDescription = "Income",
                                            tint = ModernIncomeBlue,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Surface(
                                        onClick = onSetIncomeClick,
                                        shape = RoundedCornerShape(8.dp),
                                        color = ModernIncomeBlueBg
                                    ) {
                                        Text(
                                            text = "Manage",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = ModernIncomeBlue,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Income",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ModernTextSubtle
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                val incomeDisplay = "+${FinancialEngine.formatAmount(state.totalIncome, state.currencySymbol)}"
                                Box(modifier = Modifier.clipToBounds()) {
                                    AnimatedContent(
                                        targetState = incomeDisplay,
                                        transitionSpec = {
                                            (fadeIn(animationSpec = tween(200)) + slideInVertically(animationSpec = tween(200)) { it / 5 })
                                                .togetherWith(fadeOut(animationSpec = tween(160)) + slideOutVertically(animationSpec = tween(160)) { -it / 5 })
                                        },
                                        label = "income_display_anim"
                                    ) { incText ->
                                        Text(
                                            text = incText,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = ModernTextDark
                                        )
                                    }
                                }
                                if (state.monthlyIncomeTarget > 0) {
                                    Text(
                                        text = "Target: ${FinancialEngine.formatAmount(state.monthlyIncomeTarget, state.currencySymbol)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ModernIncomeBlue,
                                        fontSize = 11.sp
                                    )
                                } else if (state.totalIncome == 0.0) {
                                    Text(
                                        text = "Tap to add",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ModernTextSubtle,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        // Expenses Card
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .cardTactilePress(pressedScale = 0.985f, interactionSource = expenseCardInteraction)
                                .clickable(
                                    interactionSource = expenseCardInteraction,
                                    indication = null,
                                    onClick = onAddExpenseClick
                                )
                                .testTag("quick_add_expense_button"),
                            shape = RoundedCornerShape(20.dp),
                            color = ModernCardBg,
                            border = BorderStroke(1.dp, ModernCardBorder),
                            shadowElevation = 0.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(ModernExpenseRedBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                        contentDescription = "Expenses",
                                        tint = ModernExpenseRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Expenses",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ModernTextSubtle
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = ModernTextLight,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                val expenseDisplay = FinancialEngine.formatAmount(state.totalExpenses, state.currencySymbol)
                                Box(modifier = Modifier.clipToBounds()) {
                                    AnimatedContent(
                                        targetState = expenseDisplay,
                                        transitionSpec = {
                                            (fadeIn(animationSpec = tween(200)) + slideInVertically(animationSpec = tween(200)) { it / 5 })
                                                .togetherWith(fadeOut(animationSpec = tween(160)) + slideOutVertically(animationSpec = tween(160)) { -it / 5 })
                                        },
                                        label = "expense_display_anim"
                                    ) { expText ->
                                        Text(
                                            text = expText,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = ModernTextDark
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Learned Pattern & Velocity Banner (Real data-driven report link, replacing random insight banner)
                    val pattern = state.patternReport
                    if (pattern != null && pattern.hasData && pattern.totalExpenses > 0) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateTab(AppTab.ANALYTICS) },
                            shape = RoundedCornerShape(18.dp),
                            color = if (AppTheme.colors.isDark) Color(0xFF242424) else Color(0xFFF0F0F0),
                            border = BorderStroke(1.dp, if (AppTheme.colors.isDark) Color(0xFF333333) else Color(0xFFE0E0E0))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (AppTheme.colors.isDark) Color(0xFF333333) else BrandGraphite,
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                                contentDescription = "Pace",
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Column {
                                        Text(
                                            text = "Learned Pace: ~${FinancialEngine.formatAmount(pattern.averageDailyBurnRate, state.currencySymbol)}/day",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = ModernTextDark
                                        )
                                        Text(
                                            text = "Projected ~${FinancialEngine.formatAmount(pattern.estimatedMonthEndExpense, state.currencySymbol)} month-end",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ModernTextSubtle
                                        )
                                    }
                                }
                                Text(
                                    text = "Report ›",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (AppTheme.colors.isDark) Color(0xFFE5E5E5) else BrandGraphite
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Transactions Section Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Transactions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ModernTextDark
                        )
                        TextButton(
                            onClick = { onNavigateTab(AppTab.HISTORY) },
                            modifier = Modifier.testTag("see_all_transactions_button")
                        ) {
                            Text(
                                text = "See all",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (AppTheme.colors.isDark) Color(0xFFE5E5E5) else BrandGraphite,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Filter Subheader Row
                    val dateHeaderStr = remember { AppDateFormatters.formatFullDate(System.currentTimeMillis()) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dateHeaderStr,
                            style = MaterialTheme.typography.labelSmall,
                            color = ModernTextSubtle
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (AppTheme.colors.isDark) Color(0xFF262626) else Color(0xFFEBEBEB)
                            ) {
                                Text(
                                    text = "This Month",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (AppTheme.colors.isDark) Color(0xFFE5E5E5) else BrandGraphite,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            Text(
                                text = "Total ${FinancialEngine.formatAmount(state.totalExpenses, state.currencySymbol)}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = ModernTextDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // --- 3. Transactions Content ---
                    if (state.recentTransactions.isEmpty()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            color = AppTheme.colors.chipBg,
                            border = BorderStroke(1.dp, ModernCardBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (AppTheme.colors.isDark) Color(0xFF262626) else Color(0xFFEBEBEB),
                                    modifier = Modifier.size(46.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                            contentDescription = null,
                                            tint = if (AppTheme.colors.isDark) Color(0xFFE5E5E5) else BrandGraphite,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "No Transactions Recorded",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ModernTextDark
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tap + below to add expenses or income and start tracking.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ModernTextSubtle,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val categoryMap = remember(state.categories) {
                                state.categories.associateBy { it.id }
                            }
                            state.recentTransactions.forEach { tx ->
                                DashboardTransactionRowItem(
                                    tx = tx,
                                    category = categoryMap[tx.categoryId],
                                    currencySymbol = state.currencySymbol,
                                    onClick = { onTransactionClick(tx) }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (state.recentTransactions.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    if (showNotificationsDialog) {
        val overallBudgetLimit = state.overallBudgetStatus?.limit ?: 0.0
        val hasBudgetWarning = overallBudgetLimit > 0 && state.totalExpenses >= overallBudgetLimit
        AlertDialog(
            onDismissRequest = { showNotificationsDialog = false },
            title = { Text("Financial Alerts & Insights") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (hasBudgetWarning) {
                        Text(
                            text = "Budget Limit Alert: Your total spending has reached ${FinancialEngine.formatAmount(state.totalExpenses, state.currencySymbol)}, which exceeds or matches your limit of ${FinancialEngine.formatAmount(overallBudgetLimit, state.currencySymbol)}.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        Text(
                            text = "Spending is on track for $currentMonthName ${state.selectedYear}. You are within healthy parameters.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ModernTextDark
                        )
                    }
                    Text(
                        text = "Recent activity: ${state.recentTransactions.size} transactions recorded this month.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ModernTextSubtle
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showNotificationsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGraphite)
                ) {
                    Text("Got it", color = Color.White)
                }
            },
            containerColor = ModernCardBg,
            titleContentColor = ModernTextDark,
            textContentColor = ModernTextDark
        )
    }

    if (showMonthDialog) {
        MonthYearPickerSheet(
            selectedMonth = state.selectedMonth,
            selectedYear = state.selectedYear,
            onMonthYearSelected = { month, year ->
                onSelectMonth(month, year)
            },
            onDismiss = { showMonthDialog = false }
        )
    }
}

@Composable
private fun ShowcaseTransactionCard(
    title: String,
    subtitle: String,
    amount: String,
    subAmount: String,
    iconBg: Color,
    iconTint: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = ModernCardBg,
        border = BorderStroke(1.dp, ModernCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ModernTextDark
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = ModernTextSubtle
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = amount,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ModernExpenseRed
                )
                if (subAmount.isNotEmpty()) {
                    Text(
                        text = subAmount,
                        style = MaterialTheme.typography.labelSmall,
                        color = ModernTextLight
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardTransactionRowItem(
    tx: TransactionEntity,
    category: CategoryEntity?,
    currencySymbol: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isIncome = tx.type == "INCOME"
    val formattedAmount = remember(tx.amount, isIncome, currencySymbol) {
        (if (isIncome) "+" else "-") + FinancialEngine.formatAmount(tx.amount, currencySymbol)
    }
    val amountColor = if (isIncome) Color(0xFF10B981) else ModernExpenseRed
    val icon = remember(category?.iconName) {
        CategoryIconHelper.getIcon(category?.iconName ?: "credit_card")
    }
    val isDark = AppTheme.colors.isDark
    val iconBg = if (isIncome) ModernIncomeBlueBg else if (isDark) Color(0xFF262626) else Color(0xFFEBEBEB)
    val iconTint = if (isIncome) ModernIncomeBlue else if (isDark) Color(0xFFE5E5E5) else BrandGraphite
    val dateStr = remember(tx.date) { AppDateFormatters.formatShortDate(tx.date) }
    val txInteraction = remember { MutableInteractionSource() }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .cardTactilePress(pressedScale = 0.985f, interactionSource = txInteraction)
            .clickable(
                interactionSource = txInteraction,
                indication = null,
                onClick = onClick
            )
            .testTag("transaction_item_${tx.id}"),
        shape = RoundedCornerShape(18.dp),
        color = ModernCardBg,
        border = BorderStroke(1.dp, ModernCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = category?.name,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category?.name ?: "Expense",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ModernTextDark
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (tx.note.isNotBlank()) tx.note else "${tx.paymentMethod} • $dateStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = ModernTextSubtle,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formattedAmount,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )
                Text(
                    text = tx.paymentMethod,
                    style = MaterialTheme.typography.labelSmall,
                    color = ModernTextLight
                )
            }
        }
    }
}

