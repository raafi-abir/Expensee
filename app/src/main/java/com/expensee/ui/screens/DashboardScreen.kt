package com.expensee.ui.screens

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
import androidx.compose.ui.res.painterResource
import com.expensee.R
import com.expensee.ui.components.SeedlingIllustration
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
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
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ReceiptLong
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
import com.expensee.data.model.CategoryEntity
import com.expensee.data.model.TransactionEntity
import com.expensee.domain.FinancialEngine
import com.expensee.ui.AppTab
import com.expensee.ui.DashboardUiState
import com.expensee.ui.components.CategoryIconHelper
import com.expensee.ui.components.MonthYearPickerSheet
import com.expensee.ui.theme.AppTheme
import com.expensee.ui.theme.BrandGraphite
import com.expensee.ui.theme.DashboardAshHeader
import com.expensee.ui.theme.ModernCardBg
import com.expensee.ui.theme.ModernCardBorder
import com.expensee.ui.theme.ModernDarkInsightBg
import com.expensee.ui.theme.ModernExpenseRed
import com.expensee.ui.theme.ModernExpenseRedBg
import com.expensee.ui.theme.ModernIncomeBlue
import com.expensee.ui.theme.ModernIncomeBlueBg
import com.expensee.ui.theme.ModernPageBg
import com.expensee.ui.theme.ModernTextDark
import com.expensee.ui.theme.ModernTextLight
import com.expensee.ui.theme.ModernTextSubtle
import com.expensee.ui.theme.AppDateFormatters
import com.expensee.ui.theme.tactilePress
import com.expensee.ui.theme.cardTactilePress
import com.expensee.ui.theme.buttonTactilePress
import com.expensee.ui.theme.AnimatedFinancialAmount
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

    val isDark = AppTheme.colors.isDark

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDark) Color(0xFF121315) else Color(0xFFF7F6F2))
            .testTag("dashboard_screen")
    ) {
        // --- 1. Top Bar & Month Selector Header ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 10.dp, bottom = 6.dp)
            ) {
                // Top Row: Avatar, Brand Logo + Name, Notification Bell
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile Avatar
                    val initials = state.userName.trim().take(1).uppercase().ifEmpty { "A" }
                    val avatarInteraction = remember { MutableInteractionSource() }
                    Surface(
                        shape = CircleShape,
                        color = if (isDark) Color(0xFF42464D) else Color(0xFF8D929A),
                        modifier = Modifier
                            .size(42.dp)
                            .buttonTactilePress(pressedScale = 0.92f, interactionSource = avatarInteraction)
                            .clickable(
                                interactionSource = avatarInteraction,
                                indication = null,
                                onClick = onOpenCustomizeProfile
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

                    // Brand: Leaf icon + "Expensee"
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_expensee_leaf),
                            contentDescription = "Expensee",
                            tint = if (isDark) Color(0xFF4CAF50) else Color(0xFF2E7D32),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Expensee",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = if (isDark) Color(0xFFF3F4F6) else Color(0xFF18181B)
                        )
                    }

                    // Notification Bell with Red Badge Dot
                    val bellInteraction = remember { MutableInteractionSource() }
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF222428) else Color(0xFFFFFFFF))
                            .border(1.dp, if (isDark) Color(0xFF2F3238) else Color(0xFFE5E7EB), CircleShape)
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
                            tint = if (isDark) Color(0xFFF3F4F6) else Color(0xFF374151),
                            modifier = Modifier.size(20.dp)
                        )
                        // Red Badge Dot
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 9.dp, end = 9.dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444))
                                .border(1.dp, if (isDark) Color(0xFF222428) else Color(0xFFFFFFFF), CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Centered Month Selector Pill ("September 2026 v")
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    val monthPillInteraction = remember { MutableInteractionSource() }
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isDark) Color(0xFF222428) else Color(0xFFFFFFFF),
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF2F3238) else Color(0xFFE5E7EB)),
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
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarToday,
                                contentDescription = null,
                                tint = if (isDark) Color(0xFFD1D5DB) else Color(0xFF4B5563),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "$currentMonthName ${state.selectedYear}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = if (isDark) Color(0xFFF3F4F6) else Color(0xFF18181B),
                                modifier = Modifier.testTag("month_title_text")
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Change Month",
                                tint = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- 2. Current Balance Hero Card ---
        item {
            val balanceCardInteraction = remember { MutableInteractionSource() }
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .cardTactilePress(pressedScale = 0.99f, interactionSource = balanceCardInteraction)
                    .testTag("hero_balance_card"),
                shape = RoundedCornerShape(24.dp),
                color = if (isDark) Color(0xFF1A1D21) else Color(0xFFFFFFFF),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF282B30) else Color(0xFFE8E8E8)),
                shadowElevation = 0.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(24.dp))
                ) {
                    // Seedling Illustration in bottom/middle right with organic ambient wash
                    SeedlingIllustration(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(155.dp)
                            .align(Alignment.CenterEnd),
                        isDark = isDark
                    )

                    // Left Content
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(start = 22.dp, top = 20.dp, bottom = 20.dp, end = 120.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Current Balance",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp,
                                color = if (isDark) Color(0xFF9CA3AF) else Color(0xFF71717A)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            val displayBalance = remember(state.availableBalance, state.currencySymbol) {
                                FinancialEngine.formatAmount(state.availableBalance, state.currencySymbol)
                            }

                            Box(modifier = Modifier.clipToBounds()) {
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
                                        color = if (isDark) Color(0xFFFFFFFF) else Color(0xFF18181B),
                                        modifier = Modifier.testTag("available_balance_text")
                                    )
                                }
                            }
                        }

                        // Bottom Pill
                        val isTracking = state.totalIncome > 0 || state.totalExpenses > 0
                        val trackingChipInteraction = remember { MutableInteractionSource() }
                        Surface(
                            shape = CircleShape,
                            color = if (isDark) Color(0xFF282B30) else Color(0xFFF3F4F6),
                            modifier = Modifier
                                .buttonTactilePress(pressedScale = 0.95f, interactionSource = trackingChipInteraction)
                                .clickable(
                                    interactionSource = trackingChipInteraction,
                                    indication = null,
                                    onClick = { onNavigateTab(AppTab.ANALYTICS) }
                                )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = if (isTracking) "View breakdown" else "Start tracking today",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = if (isDark) Color(0xFFD1D5DB) else Color(0xFF374151)
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                    contentDescription = null,
                                    tint = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280),
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 3. "Your Money" Section ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp)
            ) {
                // Header Row: "Your Money" & "Details >"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Your Money",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = if (isDark) Color(0xFFF3F4F6) else Color(0xFF18181B)
                    )

                    Row(
                        modifier = Modifier.clickable { onNavigateTab(AppTab.ANALYTICS) },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Details",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = "Details",
                            tint = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Income & Expenses Cards Side-by-Side
                val incomeCardInteraction = remember { MutableInteractionSource() }
                val expenseCardInteraction = remember { MutableInteractionSource() }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Income Card (Left)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .cardTactilePress(pressedScale = 0.985f, interactionSource = incomeCardInteraction)
                            .clickable(
                                interactionSource = incomeCardInteraction,
                                indication = null,
                                onClick = onAddIncomeClick
                            )
                            .testTag("quick_add_income_button"),
                        shape = RoundedCornerShape(20.dp),
                        color = if (isDark) Color(0xFF172920) else Color(0xFFEAF5EC),
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF203C2C) else Color(0xFFDFEDE1)),
                        shadowElevation = 0.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Top Row: Down arrow icon + right chevron
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isDark) Color(0xFF224835) else Color(0xFFD0EBD5)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDownward,
                                        contentDescription = "Income",
                                        tint = if (isDark) Color(0xFF81C784) else Color(0xFF1E7E34),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                    contentDescription = null,
                                    tint = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280),
                                    modifier = Modifier.size(12.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Income",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 13.sp,
                                color = if (isDark) Color(0xFFD1D5DB) else Color(0xFF4B5563)
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            val incomeDisplay = FinancialEngine.formatAmount(state.totalIncome, state.currencySymbol)
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
                                        fontSize = 22.sp,
                                        color = if (isDark) Color(0xFFFFFFFF) else Color(0xFF111827)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = if (state.totalIncome == 0.0) "Tap to add" else "Tap to add more",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 11.sp,
                                color = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
                            )
                        }
                    }

                    // Expenses Card (Right)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .cardTactilePress(pressedScale = 0.985f, interactionSource = expenseCardInteraction)
                            .clickable(
                                interactionSource = expenseCardInteraction,
                                indication = null,
                                onClick = onAddExpenseClick
                            )
                            .testTag("quick_add_expense_button"),
                        shape = RoundedCornerShape(20.dp),
                        color = if (isDark) Color(0xFF2B191D) else Color(0xFFFDEEE9),
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF3F2329) else Color(0xFFFADCD3)),
                        shadowElevation = 0.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Top Row: Up arrow icon + right chevron
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isDark) Color(0xFF48232A) else Color(0xFFFBDCD5)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowUpward,
                                        contentDescription = "Expenses",
                                        tint = if (isDark) Color(0xFFE57373) else Color(0xFFD32F2F),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                    contentDescription = null,
                                    tint = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280),
                                    modifier = Modifier.size(12.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Expenses",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 13.sp,
                                color = if (isDark) Color(0xFFD1D5DB) else Color(0xFF4B5563)
                            )

                            Spacer(modifier = Modifier.height(2.dp))

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
                                        fontSize = 22.sp,
                                        color = if (isDark) Color(0xFFFFFFFF) else Color(0xFF111827)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = if (state.totalExpenses == 0.0) "Tap to add" else "Tap to add more",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 11.sp,
                                color = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
                            )
                        }
                    }
                }
            }
        }

        // --- 4. "Transactions" Section ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 22.dp)
            ) {
                // Header Row: "Transactions" & "See all"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Transactions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = if (isDark) Color(0xFFF3F4F6) else Color(0xFF18181B)
                    )

                    Text(
                        text = "See all",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280),
                        modifier = Modifier.clickable { onNavigateTab(AppTab.HISTORY) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Empty State or List
                if (state.recentTransactions.isEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(22.dp),
                        color = if (isDark) Color(0xFF1A1D21) else Color(0xFFFFFFFF),
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF282B30) else Color(0xFFE8E8E8))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp, horizontal = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isDark) Color(0xFF25282D) else Color(0xFFF3F4F6),
                                modifier = Modifier.size(54.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.ReceiptLong,
                                        contentDescription = null,
                                        tint = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "No Transactions Recorded",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = if (isDark) Color(0xFFF3F4F6) else Color(0xFF18181B)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tap + below to add expenses or income\nand start tracking.",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280),
                                textAlign = TextAlign.Center,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
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

        item {
            Spacer(modifier = Modifier.height(28.dp))
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
    val iconBg = if (isIncome) {
        if (isDark) Color(0xFF172920) else Color(0xFFEAF5EC)
    } else {
        if (isDark) Color(0xFF282B30) else Color(0xFFF3F4F6)
    }
    val iconTint = if (isIncome) {
        if (isDark) Color(0xFF81C784) else Color(0xFF1E7E34)
    } else {
        if (isDark) Color(0xFFD1D5DB) else Color(0xFF374151)
    }
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
        color = if (isDark) Color(0xFF1A1D21) else Color(0xFFFFFFFF),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF282B30) else Color(0xFFE8E8E8))
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

