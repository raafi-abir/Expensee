package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionEntity
import com.example.ui.theme.AppTheme
import com.example.ui.theme.BrandGraphite
import com.example.ui.theme.ModernCardBg
import com.example.ui.theme.ModernCardBorder
import com.example.ui.theme.ModernExpenseRed
import com.example.ui.theme.ModernExpenseRedBg
import com.example.ui.theme.ModernIncomeBlue
import com.example.ui.theme.ModernIncomeBlueBg
import com.example.ui.theme.ModernTextDark
import com.example.ui.theme.ModernTextLight
import com.example.ui.theme.ModernTextSubtle
import com.example.ui.theme.AppDateFormatters
import java.util.Calendar

private val AMOUNT_REGEX = Regex("""^\d*\.?\d{0,2}$""")
private val PAYMENT_METHODS = listOf(
    Pair("Cash", Icons.Default.Payments),
    Pair("Card", Icons.Default.CreditCard),
    Pair("Bank", Icons.Default.AccountBalance),
    Pair("Mobile Banking", Icons.Default.PhoneAndroid),
    Pair("Other", Icons.Default.Category)
)
private val QUICK_ADD_INCOME = listOf(250, 500, 1000, 2500, 5000)
private val QUICK_ADD_EXPENSE = listOf(10, 25, 50, 100, 250)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionSheet(
    categories: List<CategoryEntity>,
    currencySymbol: String = "$",
    initialTransaction: TransactionEntity? = null,
    defaultType: String = "EXPENSE",
    onSave: (amount: Double, type: String, categoryId: Long, date: Long, note: String, paymentMethod: String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var type by remember { mutableStateOf(initialTransaction?.type ?: defaultType) }
    var amountText by remember {
        mutableStateOf(
            initialTransaction?.amount?.let {
                if (it % 1 == 0.0) it.toInt().toString() else it.toString()
            } ?: ""
        )
    }
    var note by remember { mutableStateOf(initialTransaction?.note ?: "") }
    var paymentMethod by remember { mutableStateOf(initialTransaction?.paymentMethod ?: "Cash") }

    val filteredCategories = remember(categories, type) { categories.filter { it.type == type } }
    var selectedCategoryId by remember(type) {
        mutableStateOf(
            initialTransaction?.categoryId ?: filteredCategories.firstOrNull()?.id ?: 1L
        )
    }

    var selectedDateMillis by remember { mutableLongStateOf(initialTransaction?.date ?: System.currentTimeMillis()) }

    val quickAddAmounts = if (type == "INCOME") QUICK_ADD_INCOME else QUICK_ADD_EXPENSE

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ModernCardBg,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(44.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.cardBorder)
            )
        },
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        modifier = Modifier.testTag("add_transaction_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (initialTransaction == null) {
                            if (type == "EXPENSE") "Add Expense" else "Add Income"
                        } else {
                            "Edit Transaction"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = ModernTextDark
                    )
                    Text(
                        text = if (type == "EXPENSE") "Record your daily spending" else "Record an incoming payment",
                        style = MaterialTheme.typography.bodySmall,
                        color = ModernTextSubtle
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AppTheme.colors.chipBg)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = ModernTextDark,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Segmented Pill Toggle: Expense vs Income
            val transactionTypes = listOf("EXPENSE", "INCOME")
            ModernSegmentedControl(
                items = transactionTypes,
                selectedIndex = if (type == "EXPENSE") 0 else 1,
                onItemSelected = { index ->
                    val newType = transactionTypes[index]
                    type = newType
                    selectedCategoryId = categories.firstOrNull { it.type == newType }?.id ?: 1L
                },
                modifier = Modifier.fillMaxWidth()
            ) { itemType, isSelected ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (itemType == "EXPENSE") Icons.AutoMirrored.Filled.TrendingDown else Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = if (itemType == "EXPENSE") "Expense" else "Income",
                        tint = if (isSelected) (if (itemType == "EXPENSE") ModernExpenseRed else ModernIncomeBlue) else ModernTextSubtle,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (itemType == "EXPENSE") "Expense" else "Income",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) (if (itemType == "EXPENSE") ModernExpenseRed else ModernIncomeBlue) else ModernTextSubtle
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Amount Input Hero Card
            val isExpense = type == "EXPENSE"
            Surface(
                color = if (isExpense) (if (AppTheme.colors.isDark) Color(0xFF261318) else Color(0xFFFFF8F8)) else (if (AppTheme.colors.isDark) Color(0xFF0E1E2D) else Color(0xFFF4FAFF)),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.5.dp, if (isExpense) ModernExpenseRedBg else ModernIncomeBlueBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "AMOUNT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = ModernTextSubtle
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = currencySymbol,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isExpense) ModernExpenseRed else ModernIncomeBlue
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { input ->
                                if (input.isEmpty() || input.matches(AMOUNT_REGEX)) {
                                    amountText = input
                                }
                            },
                            placeholder = {
                                Text(
                                    "0.00",
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ModernTextLight
                                )
                            },
                            textStyle = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 36.sp,
                                color = ModernTextDark
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .width(220.dp)
                                .testTag("amount_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Increment Chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        quickAddAmounts.forEach { amount ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AppTheme.colors.chipBg)
                                    .border(1.dp, ModernCardBorder, RoundedCornerShape(10.dp))
                                    .clickable {
                                        val current = amountText.toDoubleOrNull() ?: 0.0
                                        val updated = if (current == 0.0) amount.toDouble() else current + amount
                                        amountText = if (updated % 1.0 == 0.0) updated.toInt().toString() else "%.2f".format(updated)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "+$currencySymbol$amount",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ModernTextDark
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Category Selection Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ModernTextDark
                )
                val selectedCat = categories.find { it.id == selectedCategoryId }
                if (selectedCat != null) {
                    Text(
                        text = selectedCat.name,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isExpense) ModernExpenseRed else ModernIncomeBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Category Carousel
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filteredCategories, key = { it.id }) { cat ->
                    val isSelected = cat.id == selectedCategoryId
                    val catColor = CategoryIconHelper.parseColor(cat.colorHex)
                    val icon = CategoryIconHelper.getIcon(cat.iconName)

                    Surface(
                        color = if (isSelected) catColor.copy(alpha = 0.12f) else AppTheme.colors.chipBg,
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) catColor else ModernCardBorder
                        ),
                        modifier = Modifier
                            .clickable { selectedCategoryId = cat.id }
                            .testTag("category_chip_${cat.name}")
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) catColor else catColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = cat.name,
                                    tint = if (isSelected) Color.White else catColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = cat.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) ModernTextDark else ModernTextSubtle,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Date Shortcuts Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ModernCardBg,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, ModernCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Date",
                                tint = if (AppTheme.colors.isDark) Color(0xFFE5E5E5) else BrandGraphite,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Date",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = ModernTextDark
                            )
                        }

                        Text(
                            text = AppDateFormatters.formatShortDate(selectedDateMillis),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = ModernTextSubtle
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val (todayMillis, yesterdayMillis) = remember {
                            val cal = Calendar.getInstance()
                            val t = cal.timeInMillis
                            cal.add(Calendar.DAY_OF_YEAR, -1)
                            val y = cal.timeInMillis
                            t to y
                        }

                        val isToday = kotlin.math.abs(selectedDateMillis - todayMillis) < 24 * 60 * 60 * 1000

                        // Today Chip
                        val isDark = AppTheme.colors.isDark
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isToday) (if (isDark) Color(0xFFE5E5E5) else BrandGraphite) else AppTheme.colors.chipBg)
                                .border(
                                    1.dp,
                                    if (isToday) (if (isDark) Color(0xFFE5E5E5) else BrandGraphite) else ModernCardBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedDateMillis = System.currentTimeMillis() }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                                .testTag("date_today_chip")
                        ) {
                            Text(
                                text = "Today",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                                color = if (isToday) (if (isDark) Color(0xFF171717) else Color.White) else ModernTextDark
                            )
                        }

                        // Yesterday Chip
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (!isToday) (if (isDark) Color(0xFFE5E5E5) else BrandGraphite) else AppTheme.colors.chipBg)
                                .border(
                                    1.dp,
                                    if (!isToday) (if (isDark) Color(0xFFE5E5E5) else BrandGraphite) else ModernCardBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedDateMillis = yesterdayMillis }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                                .testTag("date_yesterday_chip")
                        ) {
                            Text(
                                text = "Yesterday",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (!isToday) FontWeight.Bold else FontWeight.Medium,
                                color = if (!isToday) (if (isDark) Color(0xFF171717) else Color.White) else ModernTextDark
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Payment Method
            Text(
                text = "Payment Method",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = ModernTextDark
            )
            Spacer(modifier = Modifier.height(10.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(PAYMENT_METHODS, key = { it.first }) { (method, icon) ->
                    val isSelected = method == paymentMethod
                    Surface(
                        color = if (isSelected) (if (AppTheme.colors.isDark) Color(0xFF2E2E2E) else Color(0xFFEBEBEB)) else ModernCardBg,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) (if (AppTheme.colors.isDark) Color(0xFFE5E5E5) else BrandGraphite) else ModernCardBorder
                        ),
                        modifier = Modifier
                            .clickable { paymentMethod = method }
                            .testTag("payment_method_$method")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = method,
                                tint = if (isSelected) (if (AppTheme.colors.isDark) Color(0xFFE5E5E5) else BrandGraphite) else ModernTextSubtle,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = method,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) (if (AppTheme.colors.isDark) Color(0xFFE5E5E5) else BrandGraphite) else ModernTextDark
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Note / Memo Field
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (optional)") },
                placeholder = { Text("e.g. Dinner with team, Groceries") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = "Note",
                        tint = ModernTextSubtle
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("note_input"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandGraphite,
                    unfocusedBorderColor = ModernCardBorder,
                    focusedContainerColor = AppTheme.colors.inputBg,
                    unfocusedContainerColor = AppTheme.colors.inputBg
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Save / Update Action Button
            val isValid = (amountText.toDoubleOrNull() ?: 0.0) > 0.0
            val actionLabel = if (initialTransaction == null) {
                if (isExpense) "Save Expense" else "Save Income"
            } else {
                "Update Transaction"
            }

            val disabledBg = if (AppTheme.colors.isDark) Color(0xFF262626) else Color(0xFFE2E8F0)

            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount > 0.0) {
                        onSave(amount, type, selectedCategoryId, selectedDateMillis, note, paymentMethod)
                        onDismiss()
                    }
                },
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(
                        if (isValid) {
                            Brush.linearGradient(
                                if (isExpense) {
                                    listOf(Color(0xFFEF4444), Color(0xFFDC2626))
                                } else {
                                    listOf(Color(0xFF0284C7), Color(0xFF0369A1))
                                }
                            )
                        } else {
                            Brush.linearGradient(listOf(disabledBg, disabledBg))
                        },
                        shape = RoundedCornerShape(18.dp)
                    )
                    .testTag("save_transaction_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = if (isValid) Color.White else ModernTextLight,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isValid) Color.White else ModernTextLight
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
