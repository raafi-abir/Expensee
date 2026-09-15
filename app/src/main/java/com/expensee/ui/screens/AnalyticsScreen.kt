package com.expensee.ui.screens

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expensee.data.model.CategoryEntity
import com.expensee.data.model.TransactionEntity
import com.expensee.domain.FinancialEngine
import com.expensee.domain.FinancialInsight
import com.expensee.domain.FinancialPatternEngine
import com.expensee.domain.LearnedPatternReport
import com.expensee.ui.components.MonthYearPickerSheet
import com.expensee.ui.components.ModernSegmentedControl
import com.expensee.ui.theme.tactilePress
import com.expensee.ui.theme.buttonTactilePress
import com.expensee.ui.theme.cardTactilePress
import com.expensee.ui.theme.AppMotion
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.expensee.ui.theme.AppTheme
import com.expensee.ui.theme.BrandGraphite
import com.expensee.ui.theme.ModernCardBg
import com.expensee.ui.theme.ModernCardBorder
import com.expensee.ui.theme.ModernPageBg
import com.expensee.ui.theme.ModernTextDark
import com.expensee.ui.theme.ModernTextLight
import com.expensee.ui.theme.ModernTextSubtle
import kotlin.math.roundToInt

@Immutable
data class ExpenseCategoryItem(
    val title: String,
    val percentageStr: String,
    val percentageVal: Float,
    val amountStr: String,
    val trendStr: String,
    val color: Color
)

private val ANALYTICS_MONTH_NAMES = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
)

@Composable
fun AnalyticsScreen(
    transactions: List<TransactionEntity>,
    categories: List<CategoryEntity>,
    insights: List<FinancialInsight>,
    selectedMonth: Int,
    selectedYear: Int,
    currencySymbol: String,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectMonth: (month: Int, year: Int) -> Unit = { _, _ -> },
    patternReport: LearnedPatternReport? = null,
    modifier: Modifier = Modifier
) {
    var selectedType by remember { mutableStateOf("EXPENSE") } // "EXPENSE" or "INCOME"
    var isDonutView by remember { mutableStateOf(true) }
    var showMonthPicker by remember { mutableStateOf(false) }

    val currentMonthName = ANALYTICS_MONTH_NAMES.getOrElse(selectedMonth - 1) { "Month" }

    val filteredTransactions = remember(transactions, selectedType) {
        transactions.filter { it.type == selectedType }
    }
    val calculatedTotal = remember(filteredTransactions) {
        filteredTransactions.sumOf { it.amount }
    }

    // Default to zero formatting, no mock random numbers
    val displayTotal = remember(calculatedTotal, currencySymbol) {
        FinancialEngine.formatAmount(calculatedTotal, currencySymbol)
    }

    // Compute or retrieve pattern report
    val activeReport = remember(patternReport, transactions, categories, selectedMonth, selectedYear, currencySymbol) {
        patternReport ?: FinancialPatternEngine.analyzeAndEstimate(
            transactions = transactions,
            categories = categories,
            selectedMonth = selectedMonth,
            selectedYear = selectedYear,
            budgetLimit = 0.0,
            monthlyIncomeTarget = 0.0,
            currencySymbol = currencySymbol
        )
    }

    // Real dynamic category breakdown from transactions
    val categoryBreakdown: List<ExpenseCategoryItem> = remember(filteredTransactions, categories, calculatedTotal, currencySymbol) {
        if (calculatedTotal <= 0.0) {
            emptyList<ExpenseCategoryItem>()
        } else {
            val catMap = categories.associateBy { it.id }
            val palette = listOf(
                BrandGraphite, Color(0xFF38BDF8), Color(0xFF10B981),
                Color(0xFFF97316), Color(0xFFFBBF24), Color(0xFFEC4899),
                Color(0xFF64748B), Color(0xFF14B8A6)
            )
            filteredTransactions
                .groupBy { it.categoryId }
                .entries
                .toList()
                .mapIndexed { index, entry ->
                    val catId = entry.key
                    val txList = entry.value
                    val cat = catMap[catId]
                    val amount = txList.sumOf { it.amount }
                    val fraction = (amount / calculatedTotal).toFloat()
                    val pct = (fraction * 100).roundToInt()
                    val color = try {
                        if (cat?.colorHex != null) Color(android.graphics.Color.parseColor(cat.colorHex))
                        else palette[index % palette.size]
                    } catch (_: Exception) {
                        palette[index % palette.size]
                    }
                    ExpenseCategoryItem(
                        title = cat?.name ?: "Other",
                        percentageStr = "$pct% of total",
                        percentageVal = fraction.coerceIn(0f, 1f),
                        amountStr = FinancialEngine.formatAmount(amount, currencySymbol),
                        trendStr = "${txList.size} transaction${if (txList.size > 1) "s" else ""}",
                        color = color
                    )
                }
                .sortedByDescending { it.percentageVal }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ModernPageBg)
            .testTag("analytics_screen")
    ) {
        // Header bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = onPrevMonth) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ModernTextDark
                        )
                    }
                    Text(
                        text = "Report",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = ModernTextDark
                    )
                }

                // Month dropdown pill
                Surface(
                    onClick = { showMonthPicker = true },
                    shape = RoundedCornerShape(20.dp),
                    color = ModernCardBg,
                    border = BorderStroke(1.dp, ModernCardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                            tint = ModernTextDark,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Type selector
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                val types = listOf("EXPENSE", "INCOME")
                ModernSegmentedControl(
                    items = types,
                    selectedIndex = if (selectedType == "EXPENSE") 0 else 1,
                    onItemSelected = { selectedType = types[it] },
                    modifier = Modifier.fillMaxWidth()
                ) { typeItem, isSelected ->
                    Text(
                        text = if (typeItem == "EXPENSE") "Expenses" else "Income",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) ModernTextDark else ModernTextSubtle
                    )
                }
            }
        }

        // Report view controls
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedType == "EXPENSE") "Expenses Breakdown" else "Income Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ModernTextDark
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Bar chart button
                    val barInteraction = remember { MutableInteractionSource() }
                    Surface(
                        shape = CircleShape,
                        color = if (!isDonutView) BrandGraphite else Color.Transparent,
                        modifier = Modifier
                            .size(36.dp)
                            .buttonTactilePress(pressedScale = 0.92f, interactionSource = barInteraction)
                            .clickable(
                                interactionSource = barInteraction,
                                indication = null,
                                onClick = { isDonutView = false }
                            )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = "Bar Chart",
                                tint = if (!isDonutView) Color.White else ModernTextSubtle,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Donut chart button
                    val donutInteraction = remember { MutableInteractionSource() }
                    Surface(
                        shape = CircleShape,
                        color = if (isDonutView) BrandGraphite else Color.Transparent,
                        modifier = Modifier
                            .size(36.dp)
                            .buttonTactilePress(pressedScale = 0.92f, interactionSource = donutInteraction)
                            .clickable(
                                interactionSource = donutInteraction,
                                indication = null,
                                onClick = { isDonutView = true }
                            )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PieChart,
                                contentDescription = "Donut Chart",
                                tint = if (isDonutView) Color.White else ModernTextSubtle,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Chart representation
        item {
            val animatedDonutProgress = animateFloatAsState(
                targetValue = if (calculatedTotal > 0.0) 1f else 0f,
                animationSpec = AppMotion.ProgressSpring,
                label = "donut_chart_spring"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isDonutView) {
                    val emptyRingColor = if (AppTheme.colors.isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                    // Donut Ring Canvas
                    Canvas(modifier = Modifier.size(210.dp)) {
                        val strokeWidth = 30.dp.toPx()
                        val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                        val arcOffset = Offset(strokeWidth / 2f, strokeWidth / 2f)
                        val progress = animatedDonutProgress.value

                        if (calculatedTotal <= 0.0) {
                            // Neutral empty ring when zero transactions
                            drawArc(
                                color = emptyRingColor,
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                topLeft = arcOffset,
                                size = arcSize,
                                style = Stroke(width = strokeWidth)
                            )
                        } else {
                            val arcStroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            var currentAngle = -90f
                            categoryBreakdown.forEach { item ->
                                val sweep = (item.percentageVal * 360f * progress).coerceAtLeast(1f)
                                drawArc(
                                    color = item.color,
                                    startAngle = currentAngle,
                                    sweepAngle = (sweep - 2f).coerceAtLeast(1f),
                                    useCenter = false,
                                    topLeft = arcOffset,
                                    size = arcSize,
                                    style = arcStroke
                                )
                                currentAngle += sweep
                            }
                        }
                    }

                    // Center Text: "Total Expenses" + Real displayTotal
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (selectedType == "EXPENSE") "Total Expenses" else "Total Income",
                            style = MaterialTheme.typography.bodySmall,
                            color = ModernTextSubtle
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = displayTotal,
                            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
                            fontWeight = FontWeight.Bold,
                            color = ModernTextDark
                        )
                    }
                } else {
                    // Clean Bar distribution view
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (categoryBreakdown.isEmpty()) {
                            Text(
                                text = "No categories recorded yet.",
                                style = MaterialTheme.typography.bodySmall,
                                color = ModernTextSubtle,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        } else {
                            for (catItem in categoryBreakdown.take(4)) {
                                AnalyticsBarRow(catItem = catItem)
                            }
                        }
                    }
                }
            }
        }

        // Forecast and estimation
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Estimation & Forecast",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ModernTextDark
                )
                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = ModernCardBg,
                    border = BorderStroke(1.dp, ModernCardBorder)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (AppTheme.colors.isDark) Color(0xFF262626) else Color(0xFFEBEBEB),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = "Pace",
                                        tint = if (AppTheme.colors.isDark) Color(0xFFE5E5E5) else BrandGraphite,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Monthly Spending Trajectory",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ModernTextDark
                                )
                                Text(
                                    text = "Computed from ${activeReport.daysElapsed} days elapsed (${activeReport.daysRemaining} days remaining)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ModernTextSubtle
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Grid of 4 calculated estimation metrics
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Daily Burn Pace
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                color = AppTheme.colors.chipBg,
                                border = BorderStroke(1.dp, ModernCardBorder)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Daily Burn Pace",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ModernTextSubtle
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = FinancialEngine.formatAmount(activeReport.averageDailyBurnRate, currencySymbol) + "/d",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ModernTextDark
                                    )
                                }
                            }

                            // Projected Month-End Spend
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                color = AppTheme.colors.chipBg,
                                border = BorderStroke(1.dp, ModernCardBorder)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Projected Spend",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ModernTextSubtle
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = FinancialEngine.formatAmount(activeReport.estimatedMonthEndExpense, currencySymbol),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (activeReport.isProjectedToOverspend) Color(0xFFEF4444) else (if (AppTheme.colors.isDark) Color(0xFFE5E5E5) else BrandGraphite)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Safe Daily Allowance
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                color = AppTheme.colors.chipBg,
                                border = BorderStroke(1.dp, ModernCardBorder)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Safe Daily Limit",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ModernTextSubtle
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (activeReport.dailySafeToSpend > 0) {
                                            FinancialEngine.formatAmount(activeReport.dailySafeToSpend, currencySymbol) + "/d"
                                        } else {
                                            "No budget set"
                                        },
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF10B981)
                                    )
                                }
                            }

                            // Projected Net Savings
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                color = AppTheme.colors.chipBg,
                                border = BorderStroke(1.dp, ModernCardBorder)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Projected Net",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ModernTextSubtle
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val netSavings = activeReport.estimatedMonthEndSavings
                                    Text(
                                        text = (if (netSavings >= 0) "+" else "") + FinancialEngine.formatAmount(netSavings, currencySymbol),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (netSavings >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Spending pattern analysis
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Learned Behavioral Patterns",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ModernTextDark
                )
                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = ModernCardBg,
                    border = BorderStroke(1.dp, ModernCardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Peak Day Pattern
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Peak Spending Day",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ModernTextDark
                                )
                            }
                            Text(
                                text = activeReport.peakSpendingDay?.let {
                                    "${it.dayName} (${FinancialEngine.formatAmount(it.averageAmount, currencySymbol)} avg)"
                                } ?: "No data yet",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0284C7)
                            )
                        }

                        // Weekday vs Weekend Split
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                    contentDescription = null,
                                    tint = Color(0xFFF97316),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Weekday vs Weekend",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ModernTextDark
                                )
                            }
                            Text(
                                text = if (activeReport.hasData) {
                                    "${activeReport.weekdayPercentage}% / ${activeReport.weekendPercentage}%"
                                } else "0% / 0%",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = ModernTextDark
                            )
                        }

                        // Dominant Payment Method
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CreditCard,
                                    contentDescription = null,
                                    tint = if (AppTheme.colors.isDark) Color(0xFFE5E5E5) else BrandGraphite,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Dominant Payment Channel",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ModernTextDark
                                )
                            }
                            Text(
                                text = activeReport.dominantPaymentMethod ?: "Not detected",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (AppTheme.colors.isDark) Color(0xFFE5E5E5) else BrandGraphite
                            )
                        }

                        // Average Transaction Ticket Size
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Avg Transaction Size",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ModernTextDark
                                )
                            }
                            Text(
                                text = FinancialEngine.formatAmount(activeReport.averageTransactionAmount, currencySymbol),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        }
                    }
                }
            }
        }

        // Pattern observations
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Learned Observations",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ModernTextDark
                )
                Spacer(modifier = Modifier.height(10.dp))

                val observations = activeReport.patternInsights
                if (observations.isEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = ModernCardBg,
                        border = BorderStroke(1.dp, ModernCardBorder)
                    ) {
                        Text(
                            text = "No behavioral observations recorded yet. Add transactions to generate habit analysis.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ModernTextSubtle,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        observations.forEach { insight ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = if (insight.isPositive) (if (AppTheme.colors.isDark) Color(0xFF143823) else Color(0xFFF0FDF4)) else (if (AppTheme.colors.isDark) Color(0xFF3D2115) else Color(0xFFFFF7ED)),
                                border = BorderStroke(
                                    1.dp,
                                    if (insight.isPositive) (if (AppTheme.colors.isDark) Color(0xFF166534) else Color(0xFFBBF7D0)) else (if (AppTheme.colors.isDark) Color(0xFF9A3412) else Color(0xFFFFEDD5))
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = if (insight.isPositive) Icons.Default.CheckCircle else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (insight.isPositive) Color(0xFF16A34A) else Color(0xFFEA580C),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = insight.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (insight.isPositive) (if (AppTheme.colors.isDark) Color(0xFF86EFAC) else Color(0xFF14532D)) else (if (AppTheme.colors.isDark) Color(0xFFFDBA74) else Color(0xFF7C2D12))
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = insight.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (insight.isPositive) (if (AppTheme.colors.isDark) Color(0xFF4ADE80) else Color(0xFF166534)) else (if (AppTheme.colors.isDark) Color(0xFFFB923C) else Color(0xFF9A3412))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Category breakdown header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedType == "EXPENSE") "All Expense Categories" else "All Income Categories",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ModernTextDark
                )
                Text(
                    text = "Total $displayTotal",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = ModernTextDark
                )
            }
        }

        // Category breakdown list
        if (categoryBreakdown.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = ModernCardBg,
                    border = BorderStroke(1.dp, ModernCardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No transactions in this category period",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = ModernTextDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Log income or expenses to see real breakdown reports.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ModernTextSubtle
                        )
                    }
                }
            }
        } else {
            items(categoryBreakdown) { item ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = ModernCardBg,
                    border = BorderStroke(1.dp, ModernCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
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
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(item.color)
                                    )
                                Column {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ModernTextDark
                                    )
                                    Text(
                                        text = item.percentageStr,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ModernTextSubtle
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = item.amountStr,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ModernTextDark
                                )
                                Text(
                                    text = item.trendStr,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = item.color
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Progress Bar
                        LinearProgressIndicator(
                            progress = { item.percentageVal },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = item.color,
                            trackColor = AppTheme.colors.chipBg
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showMonthPicker) {
        MonthYearPickerSheet(
            selectedMonth = selectedMonth,
            selectedYear = selectedYear,
            onMonthYearSelected = { month, year ->
                onSelectMonth(month, year)
            },
            onDismiss = { showMonthPicker = false }
        )
    }
}

@Composable
private fun AnalyticsBarRow(catItem: ExpenseCategoryItem) {
    val animatedBarProgress = animateFloatAsState(
        targetValue = catItem.percentageVal,
        animationSpec = AppMotion.ProgressSpring,
        label = "cat_bar_${catItem.title}"
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = catItem.title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = ModernTextDark
        )
        Text(
            text = catItem.amountStr,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = catItem.color
        )
    }
    LinearProgressIndicator(
        progress = { animatedBarProgress.value },
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp)),
        color = catItem.color,
        trackColor = AppTheme.colors.chipBg
    )
}

