package com.expensee.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.expensee.data.model.CategoryEntity
import com.expensee.data.model.TransactionEntity
import com.expensee.domain.BudgetCalculation
import com.expensee.domain.BudgetState
import com.expensee.domain.FinancialEngine
import com.expensee.ui.theme.AppDateFormatters
import com.expensee.ui.theme.AppMotion
import com.expensee.ui.theme.AppTheme
import com.expensee.ui.theme.BrandGraphite
import com.expensee.ui.theme.ExpenseRed
import com.expensee.ui.theme.FrostedBorder
import com.expensee.ui.theme.FrostedExpenseAmount
import com.expensee.ui.theme.FrostedExpenseContainer
import com.expensee.ui.theme.FrostedExpenseOnContainer
import com.expensee.ui.theme.FrostedIncomeAmount
import com.expensee.ui.theme.FrostedIncomeContainer
import com.expensee.ui.theme.FrostedIncomeOnContainer
import com.expensee.ui.theme.IncomeGreen
import com.expensee.ui.theme.ModernCardBg
import com.expensee.ui.theme.ModernCardBorder
import com.expensee.ui.theme.ModernExpenseRed
import com.expensee.ui.theme.ModernIncomeBlue
import com.expensee.ui.theme.ModernIncomeBlueBg
import com.expensee.ui.theme.ModernTextDark
import com.expensee.ui.theme.ModernTextSubtle
import com.expensee.ui.theme.WarningAmber
import com.expensee.ui.theme.buttonTactilePress
import com.expensee.ui.theme.cardTactilePress
import com.expensee.ui.theme.tactilePress

@Composable
fun MonthSelectorBar(
    month: Int,
    year: Int,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit,
    onSelectMonth: ((month: Int, year: Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    val monthTitle = "${monthNames.getOrElse(month - 1) { "Month" }} $year"
    var showPicker by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("month_selector_bar"),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, FrostedBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onPrevClick,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("prev_month_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                    contentDescription = "Previous Month",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp)
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(enabled = onSelectMonth != null) { showPicker = true }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = monthTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag("month_title_text")
                    )
                    if (onSelectMonth != null) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Select Month",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            IconButton(
                onClick = onNextClick,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("next_month_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = "Next Month",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }

    if (showPicker && onSelectMonth != null) {
        MonthYearPickerSheet(
            selectedMonth = month,
            selectedYear = year,
            onMonthYearSelected = onSelectMonth,
            onDismiss = { showPicker = false }
        )
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, FrostedBorder)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
    }
}

@Composable
fun BudgetProgressBar(
    status: BudgetCalculation,
    currencySymbol: String = "৳",
    modifier: Modifier = Modifier
) {
    val progress = (status.percentageUsed / 100.0).toFloat().coerceIn(0f, 1f)
    val animatedProgress = animateFloatAsState(
        targetValue = progress,
        animationSpec = AppMotion.ProgressSpring,
        label = "budgetProgress"
    )

    val stateColor = when (status.state) {
        BudgetState.EXCEEDED -> ExpenseRed
        BudgetState.APPROACHING -> WarningAmber
        BudgetState.NORMAL -> IncomeGreen
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${FinancialEngine.formatAmount(status.spent, currencySymbol)} / ${FinancialEngine.formatAmount(status.limit, currencySymbol)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${status.percentageUsed.toInt()}% used",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = stateColor
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { animatedProgress.value },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = stateColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val remainingText = if (status.state == BudgetState.EXCEEDED) {
                "Exceeded by ${FinancialEngine.formatAmount(status.spent - status.limit, currencySymbol)}"
            } else {
                "${FinancialEngine.formatAmount(status.remaining, currencySymbol)} remaining"
            }
            Text(
                text = remainingText,
                style = MaterialTheme.typography.bodySmall,
                color = if (status.state == BudgetState.EXCEEDED) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TransactionRowItem(
    transaction: TransactionEntity,
    category: CategoryEntity?,
    currencySymbol: String = "৳",
    onClick: () -> Unit = {},
    onDeleteClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isIncome = transaction.type == "INCOME"
    val icon = remember(category?.iconName) {
        CategoryIconHelper.getIcon(category?.iconName ?: "more_horiz")
    }
    val badgeBg = if (isIncome) ModernIncomeBlueBg else (if (AppTheme.colors.isDark) Color(0xFF262626) else Color(0xFFEBEBEB))
    val badgeIconTint = if (isIncome) ModernIncomeBlue else (if (AppTheme.colors.isDark) Color(0xFFE5E5E5) else BrandGraphite)
    val formattedAmount = remember(transaction.amount, isIncome, currencySymbol) {
        (if (isIncome) "+" else "-") + FinancialEngine.formatAmount(transaction.amount, currencySymbol)
    }
    val amountColor = if (isIncome) Color(0xFF10B981) else ModernExpenseRed

    val dateText = remember(transaction.date) {
        AppDateFormatters.formatDateTime(transaction.date)
    }

    val rowInteractionSource = remember { MutableInteractionSource() }
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .cardTactilePress(pressedScale = 0.985f, interactionSource = rowInteractionSource)
            .clickable(
                interactionSource = rowInteractionSource,
                indication = null,
                onClick = onClick
            )
            .testTag("transaction_item_${transaction.id}"),
        color = ModernCardBg,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, ModernCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category icon badge
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(badgeBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = category?.name ?: "Category",
                    tint = badgeIconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category?.name ?: "Uncategorized",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (transaction.note.isNotBlank()) {
                    Text(
                        text = transaction.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = dateText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    Text(
                        text = " • ${transaction.paymentMethod}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Amount
            Text(
                text = formattedAmount,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )

            if (onDeleteClick != null) {
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("delete_transaction_${transaction.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = ModernTextSubtle,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ConfirmDeleteDialog(
    title: String = "Delete Transaction",
    message: String = "Are you sure you want to delete this transaction? This action cannot be undone.",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ModernCardBg,
        titleContentColor = ModernTextDark,
        textContentColor = ModernTextSubtle,
        title = {
            Text(text = title, fontWeight = FontWeight.Bold)
        },
        text = {
            Text(text = message)
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                modifier = Modifier.testTag("confirm_delete_button")
            ) {
                Text(text = "Delete", color = ExpenseRed, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_delete_button")
            ) {
                Text(text = "Cancel", color = ModernTextSubtle)
            }
        }
    )
}

/**
 * Segmented control with an animated sliding indicator.
 */
@Composable
fun <T> ModernSegmentedControl(
    items: List<T>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    itemContent: @Composable (item: T, isSelected: Boolean) -> Unit
) {
    Surface(
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(16.dp),
        color = AppTheme.colors.chipBg
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        ) {
            val count = items.size
            if (count > 0) {
                val tabWidth = maxWidth / count
                val animatedIndexState = animateFloatAsState(
                    targetValue = selectedIndex.toFloat(),
                    animationSpec = AppMotion.IndicatorSpring,
                    label = "segmented_indicator"
                )

                Box(
                    modifier = Modifier
                        .offset {
                            val px = (tabWidth.toPx() * animatedIndexState.value).roundToInt()
                            IntOffset(x = px, y = 0)
                        }
                        .width(tabWidth)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ModernCardBg)
                        .border(
                            1.dp,
                            ModernCardBorder.copy(alpha = 0.5f),
                            RoundedCornerShape(12.dp)
                        )
                )

                Row(modifier = Modifier.fillMaxSize()) {
                    items.forEachIndexed { index, item ->
                        val isSelected = selectedIndex == index
                        val interactionSource = remember { MutableInteractionSource() }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(12.dp))
                                .tactilePress(pressedScale = 0.95f, interactionSource = interactionSource)
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null,
                                    onClick = { onItemSelected(index) }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            itemContent(item, isSelected)
                        }
                    }
                }
            }
        }
    }
}
