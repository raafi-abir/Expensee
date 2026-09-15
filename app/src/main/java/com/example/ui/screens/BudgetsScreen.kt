package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.SavingsGoalEntity
import com.example.data.model.TransactionEntity
import com.example.domain.FinancialEngine
import com.example.ui.components.CategoryIconHelper
import com.example.ui.components.MonthYearPickerSheet
import com.example.ui.theme.AppTheme
import com.example.ui.theme.BrandGraphite
import com.example.ui.theme.ModernCardBg
import com.example.ui.theme.ModernCardBorder
import com.example.ui.theme.ModernPageBg
import com.example.ui.theme.ModernTextDark
import com.example.ui.theme.ModernTextSubtle
import com.example.ui.theme.AppMotion
import com.example.ui.theme.cardTactilePress
import com.example.ui.theme.buttonTactilePress
import androidx.compose.foundation.interaction.MutableInteractionSource

@Immutable
data class CategoryBudgetItemState(
    val budgetId: Long,
    val categoryId: Long,
    val title: String,
    val spent: Double,
    val limit: Double,
    val remaining: Double,
    val percentage: Int,
    val isOverBudget: Boolean,
    val icon: ImageVector,
    val tintColor: Color,
    val iconBgColor: Color
)

private val MONTH_NAMES = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
)

@Composable
fun BudgetsScreen(
    overallBudget: BudgetEntity?,
    categoryBudgets: List<BudgetEntity>,
    transactions: List<TransactionEntity>,
    categories: List<CategoryEntity>,
    goals: List<SavingsGoalEntity> = emptyList(),
    selectedMonth: Int,
    selectedYear: Int,
    currencySymbol: String,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectMonth: (month: Int, year: Int) -> Unit = { _, _ -> },
    onSaveOverallBudget: (Double) -> Unit,
    onDeleteOverallBudget: () -> Unit = {},
    onSaveCategoryBudget: (categoryId: Long, amount: Double) -> Unit,
    onDeleteBudget: (Long) -> Unit,
    onViewAllGoals: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isDark = AppTheme.colors.isDark
    val chipBackground = AppTheme.colors.chipBg
    val inputBackground = AppTheme.colors.inputBg

    var showOverallBudgetDialog by remember { mutableStateOf(false) }
    var showCategoryBudgetDialog by remember { mutableStateOf(false) }
    var showDeleteOverallConfirm by remember { mutableStateOf(false) }
    var budgetToDelete by remember { mutableStateOf<CategoryBudgetItemState?>(null) }
    var editingCategoryBudget by remember { mutableStateOf<CategoryBudgetItemState?>(null) }
    var showMonthPickerSheet by remember { mutableStateOf(false) }

    var overallInput by remember(overallBudget) {
        mutableStateOf(if ((overallBudget?.amount ?: 0.0) > 0) overallBudget!!.amount.toString() else "")
    }
    var selectedCategoryId by remember(categories) {
        mutableStateOf(categories.firstOrNull()?.id ?: 0L)
    }
    var categoryAmountInput by remember { mutableStateOf("") }

    val currentMonthName = MONTH_NAMES.getOrElse(selectedMonth - 1) { "Month $selectedMonth" }

    val monthlyExpenses = remember(transactions) {
        transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    }
    val overallLimit = overallBudget?.amount ?: 0.0
    val budgetProgress = if (overallLimit > 0) {
        (monthlyExpenses / overallLimit).toFloat().coerceIn(0f, 1f)
    } else 0f
    val remainingBudget = (overallLimit - monthlyExpenses).coerceAtLeast(0.0)
    val isOverOverallBudget = monthlyExpenses > overallLimit && overallLimit > 0

    val catMap = remember(categories) { categories.associateBy { it.id } }
    val categoryExpenses = remember(transactions) {
        transactions.filter { it.type == "EXPENSE" }
            .groupBy { it.categoryId }
            .mapValues { (_, txs) -> txs.sumOf { it.amount } }
    }

    val activeCategoryBudgetItems = remember(categoryBudgets, categoryExpenses, catMap, currencySymbol, isDark) {
        categoryBudgets.mapNotNull { b ->
            val cat = catMap[b.categoryId]
            if (cat == null) null
            else {
                val spent = categoryExpenses[b.categoryId] ?: 0.0
                val pct = if (b.amount > 0) ((spent / b.amount) * 100).toInt() else 0
                val isOver = spent > b.amount
                val tint = if (isOver) Color(0xFFEF4444) else (if (isDark) Color(0xFFE5E5E5) else BrandGraphite)
                val iconBg = if (isOver) {
                    if (isDark) Color(0xFF3B1C1C) else Color(0xFFFEE2E2)
                } else {
                    if (isDark) Color(0xFF262626) else Color(0xFFEBEBEB)
                }
                val icon = CategoryIconHelper.getIcon(cat.iconName)
                CategoryBudgetItemState(
                    budgetId = b.id,
                    categoryId = cat.id,
                    title = cat.name,
                    spent = spent,
                    limit = b.amount,
                    remaining = (b.amount - spent).coerceAtLeast(0.0),
                    percentage = pct.coerceIn(0, 100),
                    isOverBudget = isOver,
                    icon = icon,
                    tintColor = tint,
                    iconBgColor = iconBg
                )
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ModernPageBg)
            .testTag("budgets_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item(key = "plan_top_header") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "My Plan",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = ModernTextDark
                    )
                    Text(
                        text = "Goals, limits & allocations",
                        style = MaterialTheme.typography.bodySmall,
                        color = ModernTextSubtle
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Month selector pill
                    Surface(
                        onClick = { showMonthPickerSheet = true },
                        shape = RoundedCornerShape(16.dp),
                        color = chipBackground,
                        border = BorderStroke(1.dp, ModernCardBorder),
                        modifier = Modifier.testTag("plan_month_pill")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "$currentMonthName $selectedYear",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = ModernTextDark
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Select Month",
                                tint = ModernTextSubtle,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Add Category Budget action button
                    Surface(
                        onClick = {
                            editingCategoryBudget = null
                            selectedCategoryId = categories.firstOrNull()?.id ?: 0L
                            categoryAmountInput = ""
                            showCategoryBudgetDialog = true
                        },
                        shape = CircleShape,
                        color = BrandGraphite,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("plan_add_category_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Budget",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Goals section
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Goals",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ModernTextDark
                    )
                    Text(
                        text = "View All",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFFE5E5E5) else BrandGraphite,
                        modifier = Modifier
                            .clickable(onClick = onViewAllGoals)
                            .padding(vertical = 4.dp, horizontal = 6.dp)
                            .testTag("plan_view_all_goals")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (goals.isEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = ModernCardBg,
                        border = BorderStroke(1.dp, ModernCardBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) Color(0xFF262626) else Color(0xFFEBEBEB)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Savings,
                                    contentDescription = "Goals",
                                    tint = if (isDark) Color(0xFFE5E5E5) else BrandGraphite,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Savings Goals",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ModernTextDark
                                )
                                Text(
                                    text = "Plan ahead and track milestones in Settings.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ModernTextSubtle
                                )
                            }
                        }
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(goals) { goal ->
                            val progress = if (goal.targetAmount > 0) {
                                (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f)
                            } else 0f

                            val goalInteraction = remember { MutableInteractionSource() }
                            Surface(
                                modifier = Modifier
                                    .width(220.dp)
                                    .cardTactilePress(pressedScale = 0.98f, interactionSource = goalInteraction)
                                    .clickable(
                                        interactionSource = goalInteraction,
                                        indication = null,
                                        onClick = onViewAllGoals
                                    ),
                                shape = RoundedCornerShape(18.dp),
                                color = ModernCardBg,
                                border = BorderStroke(1.dp, ModernCardBorder)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(if (isDark) Color(0xFF262626) else Color(0xFFEBEBEB)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Flag,
                                                contentDescription = goal.name,
                                                tint = if (isDark) Color(0xFFE5E5E5) else BrandGraphite,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Text(
                                            text = goal.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = ModernTextDark,
                                            maxLines = 1
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = FinancialEngine.formatAmount(goal.currentAmount, currencySymbol),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = ModernTextDark
                                        )
                                        Text(
                                            text = FinancialEngine.formatAmount(goal.targetAmount, currencySymbol),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ModernTextSubtle
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    val animatedGoalProg = animateFloatAsState(
                                        targetValue = progress,
                                        animationSpec = tween(500, easing = FastOutSlowInEasing),
                                        label = "goal_anim_prog"
                                    )
                                    LinearProgressIndicator(
                                        progress = { animatedGoalProg.value },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = if (isDark) Color(0xFFE5E5E5) else BrandGraphite,
                                        trackColor = chipBackground
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Monthly budget section
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "Monthly Budget Goal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ModernTextDark
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (overallLimit <= 0.0) {
                    // Empty state for Monthly Budget
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("plan_empty_overall_budget_card"),
                        shape = RoundedCornerShape(20.dp),
                        color = ModernCardBg,
                        border = BorderStroke(1.dp, ModernCardBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) Color(0xFF262626) else Color(0xFFEBEBEB)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrackChanges,
                                    contentDescription = "Target",
                                    tint = if (isDark) Color(0xFFE5E5E5) else BrandGraphite,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No monthly limit",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ModernTextDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Set a monthly limit to start tracking your overall spending.",
                                style = MaterialTheme.typography.bodySmall,
                                color = ModernTextSubtle,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    overallInput = ""
                                    showOverallBudgetDialog = true
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandGraphite),
                                modifier = Modifier.testTag("plan_set_monthly_budget_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Set Budget", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    // Populated Monthly Budget Card
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("plan_overall_budget_card"),
                        shape = RoundedCornerShape(20.dp),
                        color = ModernCardBg,
                        border = BorderStroke(1.dp, ModernCardBorder),
                        shadowElevation = 1.dp
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            // Top Row: Title + Action buttons (Edit, Delete)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(if (isDark) Color(0xFF262626) else Color(0xFFEBEBEB)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.TrackChanges,
                                            contentDescription = "Target",
                                            tint = if (isDark) Color(0xFFE5E5E5) else BrandGraphite,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "Monthly Spending Target",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = ModernTextDark
                                        )
                                        Text(
                                            text = "$currentMonthName $selectedYear",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ModernTextSubtle
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = {
                                            overallInput = if (overallLimit % 1 == 0.0) overallLimit.toInt().toString() else overallLimit.toString()
                                            showOverallBudgetDialog = true
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Monthly Budget",
                                            tint = ModernTextSubtle,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { showDeleteOverallConfirm = true },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Monthly Budget",
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Amount Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text(
                                        text = "SPENT",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ModernTextSubtle
                                    )
                                    Text(
                                        text = FinancialEngine.formatAmount(monthlyExpenses, currencySymbol),
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isOverOverallBudget) Color(0xFFEF4444) else ModernTextDark
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "TOTAL LIMIT",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ModernTextSubtle
                                    )
                                    Text(
                                        text = FinancialEngine.formatAmount(overallLimit, currencySymbol),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = ModernTextDark
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Progress Bar
                            val animatedBudgetProg = animateFloatAsState(
                                targetValue = budgetProgress,
                                animationSpec = AppMotion.ProgressSpring,
                                label = "budget_anim_prog"
                            )
                            LinearProgressIndicator(
                                progress = { animatedBudgetProg.value },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (isOverOverallBudget) Color(0xFFEF4444) else (if (isDark) Color(0xFFE5E5E5) else BrandGraphite),
                                trackColor = chipBackground
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Details Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Used ${(budgetProgress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = ModernTextSubtle
                                )
                                Text(
                                    text = if (isOverOverallBudget) {
                                        "Over by ${FinancialEngine.formatAmount(monthlyExpenses - overallLimit, currencySymbol)}"
                                    } else {
                                        "${FinancialEngine.formatAmount(remainingBudget, currencySymbol)} Left"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOverOverallBudget) Color(0xFFEF4444) else Color(0xFF10B981)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Status Banner
                            val statusBannerBg = if (isOverOverallBudget) {
                                if (isDark) Color(0xFF3B1C1C) else Color(0xFFFEE2E2)
                            } else {
                                if (isDark) Color(0xFF132E22) else Color(0xFFDCFCE7)
                            }
                            val statusBannerTextColor = if (isOverOverallBudget) Color(0xFFEF4444) else Color(0xFF10B981)

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = statusBannerBg
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isOverOverallBudget) Icons.Default.Warning else Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = statusBannerTextColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (isOverOverallBudget) {
                                            "Over budget by ${FinancialEngine.formatAmount(monthlyExpenses - overallLimit, currencySymbol)}."
                                        } else {
                                            "On track · ${FinancialEngine.formatAmount(remainingBudget, currencySymbol)} remaining for this month."
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = statusBannerTextColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Category budgets section
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Category Budgets",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ModernTextDark
                    )
                    Text(
                        text = "${activeCategoryBudgetItems.size} configured",
                        style = MaterialTheme.typography.labelMedium,
                        color = ModernTextSubtle
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (activeCategoryBudgetItems.isEmpty()) {
                    // Empty state for Category Budgets
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("plan_empty_category_budget_card"),
                        shape = RoundedCornerShape(20.dp),
                        color = ModernCardBg,
                        border = BorderStroke(1.dp, ModernCardBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No category budgets",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = ModernTextDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Set limits for individual spending categories.",
                                style = MaterialTheme.typography.bodySmall,
                                color = ModernTextSubtle,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    editingCategoryBudget = null
                                    selectedCategoryId = categories.firstOrNull()?.id ?: 0L
                                    categoryAmountInput = ""
                                    showCategoryBudgetDialog = true
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandGraphite),
                                modifier = Modifier.testTag("plan_add_first_category_budget_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Category Budget", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        activeCategoryBudgetItems.forEach { item ->
                            val itemInteraction = remember { MutableInteractionSource() }
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .cardTactilePress(pressedScale = 0.985f, interactionSource = itemInteraction)
                                    .testTag("category_budget_item_${item.categoryId}"),
                                shape = RoundedCornerShape(18.dp),
                                color = ModernCardBg,
                                border = BorderStroke(1.dp, ModernCardBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(item.iconBgColor),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = item.icon,
                                                contentDescription = item.title,
                                                tint = item.tintColor,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }

                                        Column {
                                            Text(
                                                text = item.title,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = ModernTextDark
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "${FinancialEngine.formatAmount(item.spent, currencySymbol)} of ${FinancialEngine.formatAmount(item.limit, currencySymbol)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = ModernTextSubtle
                                            )
                                            Text(
                                                text = if (item.isOverBudget) {
                                                    "Over by ${FinancialEngine.formatAmount(item.spent - item.limit, currencySymbol)}"
                                                } else {
                                                    "${FinancialEngine.formatAmount(item.remaining, currencySymbol)} remaining"
                                                },
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (item.isOverBudget) Color(0xFFEF4444) else Color(0xFF10B981)
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        // Circular Progress Ring
                                        CircularProgressRing(
                                            percentage = item.percentage,
                                            color = item.tintColor,
                                            isDark = isDark,
                                            modifier = Modifier.size(44.dp)
                                        )

                                        // Edit Button
                                        IconButton(
                                            onClick = {
                                                editingCategoryBudget = item
                                                selectedCategoryId = item.categoryId
                                                categoryAmountInput = if (item.limit % 1 == 0.0) item.limit.toInt().toString() else item.limit.toString()
                                                showCategoryBudgetDialog = true
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit",
                                                tint = ModernTextSubtle,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        // Delete Button
                                        IconButton(
                                            onClick = { budgetToDelete = item },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = Color(0xFFEF4444),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(90.dp))
        }
    }

    // Monthly budget dialog
    if (showOverallBudgetDialog) {
        AlertDialog(
            onDismissRequest = { showOverallBudgetDialog = false },
            title = {
                Text(
                    text = "Monthly Budget Goal",
                    fontWeight = FontWeight.Bold,
                    color = ModernTextDark
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter overall spending limit for $currentMonthName ($currencySymbol):",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ModernTextDark
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = overallInput,
                        onValueChange = { input ->
                            if (input.isEmpty() || input.all { it.isDigit() || it == '.' }) {
                                overallInput = input
                            }
                        },
                        label = { Text("Budget Limit ($currencySymbol)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandGraphite,
                            unfocusedBorderColor = ModernCardBorder,
                            focusedTextColor = ModernTextDark,
                            unfocusedTextColor = ModernTextDark,
                            focusedContainerColor = inputBackground,
                            unfocusedContainerColor = inputBackground
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = overallInput.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            onSaveOverallBudget(amt)
                        }
                        showOverallBudgetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGraphite)
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showOverallBudgetDialog = false }) {
                    Text("Cancel", color = ModernTextSubtle)
                }
            },
            containerColor = ModernCardBg,
            titleContentColor = ModernTextDark,
            textContentColor = ModernTextDark
        )
    }

    // Category budget dialog
    if (showCategoryBudgetDialog) {
        AlertDialog(
            onDismissRequest = { showCategoryBudgetDialog = false },
            title = {
                Text(
                    text = if (editingCategoryBudget != null) "Edit Category Budget" else "New Category Budget",
                    fontWeight = FontWeight.Bold,
                    color = ModernTextDark
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Select Category:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = ModernTextDark
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(chipBackground)
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSelected = cat.id == selectedCategoryId
                            Surface(
                                onClick = { selectedCategoryId = cat.id },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) (if (isDark) Color(0xFF2E2E2E) else Color(0xFFEBEBEB)) else Color.Transparent,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = CategoryIconHelper.getIcon(cat.iconName),
                                        contentDescription = cat.name,
                                        tint = if (isSelected) (if (isDark) Color(0xFFE5E5E5) else BrandGraphite) else ModernTextSubtle,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = cat.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) (if (isDark) Color(0xFFE5E5E5) else BrandGraphite) else ModernTextDark
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = categoryAmountInput,
                        onValueChange = { input ->
                            if (input.isEmpty() || input.all { it.isDigit() || it == '.' }) {
                                categoryAmountInput = input
                            }
                        },
                        label = { Text("Monthly Limit ($currencySymbol)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandGraphite,
                            unfocusedBorderColor = ModernCardBorder,
                            focusedTextColor = ModernTextDark,
                            unfocusedTextColor = ModernTextDark,
                            focusedContainerColor = inputBackground,
                            unfocusedContainerColor = inputBackground
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = categoryAmountInput.toDoubleOrNull() ?: 0.0
                        if (selectedCategoryId != 0L && amt > 0) {
                            onSaveCategoryBudget(selectedCategoryId, amt)
                        }
                        showCategoryBudgetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGraphite)
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCategoryBudgetDialog = false }) {
                    Text("Cancel", color = ModernTextSubtle)
                }
            },
            containerColor = ModernCardBg,
            titleContentColor = ModernTextDark,
            textContentColor = ModernTextDark
        )
    }

    // Confirm delete category budget dialog
    if (budgetToDelete != null) {
        val item = budgetToDelete!!
        AlertDialog(
            onDismissRequest = { budgetToDelete = null },
            title = {
                Text(
                    text = "Delete Category Budget?",
                    fontWeight = FontWeight.Bold,
                    color = ModernTextDark
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to remove the budget for ${item.title}? Your transactions and category will not be deleted.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ModernTextDark
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteBudget(item.budgetId)
                        budgetToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { budgetToDelete = null }) {
                    Text("Cancel", color = ModernTextSubtle)
                }
            },
            containerColor = ModernCardBg,
            titleContentColor = ModernTextDark,
            textContentColor = ModernTextDark
        )
    }

    // Confirm delete monthly budget dialog
    if (showDeleteOverallConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteOverallConfirm = false },
            title = {
                Text(
                    text = "Delete Monthly Budget?",
                    fontWeight = FontWeight.Bold,
                    color = ModernTextDark
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to remove the monthly budget goal of ${FinancialEngine.formatAmount(overallLimit, currencySymbol)}? Your transactions and categories will remain untouched.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ModernTextDark
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteOverallBudget()
                        showDeleteOverallConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteOverallConfirm = false }) {
                    Text("Cancel", color = ModernTextSubtle)
                }
            },
            containerColor = ModernCardBg,
            titleContentColor = ModernTextDark,
            textContentColor = ModernTextDark
        )
    }

    // Month and year picker sheet
    if (showMonthPickerSheet) {
        MonthYearPickerSheet(
            selectedMonth = selectedMonth,
            selectedYear = selectedYear,
            onMonthYearSelected = { m, y ->
                onSelectMonth(m, y)
                showMonthPickerSheet = false
            },
            onDismiss = { showMonthPickerSheet = false }
        )
    }
}

@Composable
fun CircularProgressRing(
    percentage: Int,
    color: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val trackColor = if (isDark) Color(0xFF2E2E2E) else Color(0xFFE2E8F0)
    val sweep = (percentage / 100f).coerceIn(0f, 1f) * 360f
    val animatedSweep = animateFloatAsState(
        targetValue = sweep,
        animationSpec = AppMotion.ProgressSpring,
        label = "ring_sweep"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 3.5.dp.toPx()
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val arcOffset = Offset(strokeWidth / 2f, strokeWidth / 2f)

            // Background ring track
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = arcOffset,
                size = arcSize,
                style = Stroke(width = strokeWidth)
            )

            // Progress arc (purely reads animatedSweep.value during draw phase)
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = animatedSweep.value,
                useCenter = false,
                topLeft = arcOffset,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Text(
            text = "$percentage%",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = ModernTextDark,
            fontSize = 11.sp
        )
    }
}
