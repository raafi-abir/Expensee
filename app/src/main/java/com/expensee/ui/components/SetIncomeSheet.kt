package com.expensee.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expensee.data.model.CategoryEntity
import com.expensee.data.model.TransactionEntity
import com.expensee.domain.FinancialEngine
import com.expensee.ui.theme.AppTheme
import com.expensee.ui.theme.ModernCardBg
import com.expensee.ui.theme.ModernCardBorder
import com.expensee.ui.theme.ModernIncomeBlue
import com.expensee.ui.theme.ModernIncomeBlueBg
import com.expensee.ui.theme.ModernTextDark
import com.expensee.ui.theme.ModernTextLight
import com.expensee.ui.theme.ModernTextSubtle
import com.expensee.ui.theme.AppDateFormatters

private val PRESET_AMOUNTS = listOf(1000, 2500, 5000, 7500, 10000, 25000)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SetIncomeSheet(
    currentMonthlyIncome: Double,
    currentMonthTotalIncome: Double,
    incomeTransactions: List<TransactionEntity> = emptyList(),
    categories: List<CategoryEntity> = emptyList(),
    currencySymbol: String = "$",
    onSaveMonthlyIncome: (income: Double, recordAsTransaction: Boolean) -> Unit,
    onOpenAddIncomeTransaction: () -> Unit,
    onEditIncomeTransaction: (TransactionEntity) -> Unit = {},
    onDeleteIncomeTransaction: (Long) -> Unit = {},
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var incomeText by remember {
        val initial = if (currentMonthlyIncome > 0) currentMonthlyIncome else (incomeTransactions.firstOrNull()?.amount ?: 0.0)
        mutableStateOf(if (initial > 0) (if (initial % 1 == 0.0) initial.toInt().toString() else initial.toString()) else "")
    }
    var recordAsTransaction by remember { mutableStateOf(true) }

    val presetAmounts = PRESET_AMOUNTS

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
        modifier = Modifier.testTag("set_income_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (AppTheme.colors.isDark) Color(0xFF0E1E2D) else ModernIncomeBlueBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = "Income",
                            tint = ModernIncomeBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Manage Income",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = ModernTextDark
                        )
                        Text(
                            text = "Set baseline earnings or record incoming cash",
                            style = MaterialTheme.typography.bodySmall,
                            color = ModernTextSubtle
                        )
                    }
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

            Spacer(modifier = Modifier.height(16.dp))

            // Current Month Status Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = if (AppTheme.colors.isDark) Color(0xFF0E1E2D) else ModernIncomeBlueBg.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, ModernIncomeBlue.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Logged this month",
                            style = MaterialTheme.typography.labelMedium,
                            color = ModernTextSubtle
                        )
                        Text(
                            text = "+${FinancialEngine.formatAmount(currentMonthTotalIncome, currencySymbol)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ModernIncomeBlue
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onOpenAddIncomeTransaction()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ModernIncomeBlue
                        ),
                        border = BorderStroke(1.dp, ModernIncomeBlue)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Log One-off", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section: Recorded Income Entries
            if (incomeTransactions.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recorded Incomes (${incomeTransactions.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ModernTextDark
                    )
                    Text(
                        text = "Tap pencil to edit",
                        style = MaterialTheme.typography.bodySmall,
                        color = ModernTextSubtle
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                val catMap = remember(categories) { categories.associate { it.id to it.name } }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(AppTheme.colors.chipBg)
                        .border(1.dp, ModernCardBorder, RoundedCornerShape(16.dp))
                ) {
                    incomeTransactions.forEachIndexed { index, tx ->
                        if (index > 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(ModernCardBorder)
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tx.note.ifBlank { catMap[tx.categoryId] ?: "Income" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ModernTextDark
                                )
                                Text(
                                    text = "${catMap[tx.categoryId] ?: "Income"} · ${AppDateFormatters.formatShortDate(tx.date)} · ${tx.paymentMethod}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ModernTextSubtle
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "+${FinancialEngine.formatAmount(tx.amount, currencySymbol)}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ModernIncomeBlue
                                )
                                IconButton(
                                    onClick = {
                                        onDismiss()
                                        onEditIncomeTransaction(tx)
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Income",
                                        tint = ModernIncomeBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        onDeleteIncomeTransaction(tx.id)
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Income",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Section: Monthly Income Target
            Text(
                text = "Monthly Expected Income",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ModernTextDark
            )
            Text(
                text = "Used to evaluate your monthly cash flow, savings rate, and budgets.",
                style = MaterialTheme.typography.bodySmall,
                color = ModernTextSubtle,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
            )

            OutlinedTextField(
                value = incomeText,
                onValueChange = { input ->
                    if (input.isEmpty() || input.all { it.isDigit() || it == '.' }) {
                        incomeText = input
                    }
                },
                leadingIcon = {
                    Text(
                        text = currencySymbol,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = ModernIncomeBlue,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                },
                placeholder = { Text("5000") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ModernIncomeBlue,
                    unfocusedBorderColor = ModernCardBorder,
                    focusedContainerColor = AppTheme.colors.inputBg,
                    unfocusedContainerColor = AppTheme.colors.inputBg
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("monthly_income_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Preset chips
            Text(
                text = "Quick Presets",
                style = MaterialTheme.typography.labelSmall,
                color = ModernTextSubtle
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                presetAmounts.forEach { preset ->
                    val isCurrent = incomeText == preset.toString()
                    Surface(
                        onClick = { incomeText = preset.toString() },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isCurrent) (if (AppTheme.colors.isDark) Color(0xFF0E1E2D) else ModernIncomeBlueBg) else AppTheme.colors.chipBg,
                        border = BorderStroke(
                            1.dp,
                            if (isCurrent) ModernIncomeBlue else ModernCardBorder
                        )
                    ) {
                        Text(
                            text = "$currencySymbol$preset",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                            color = if (isCurrent) ModernIncomeBlue else ModernTextDark,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Checkbox: Record this as an income transaction for this month
            Surface(
                onClick = { recordAsTransaction = !recordAsTransaction },
                shape = RoundedCornerShape(12.dp),
                color = AppTheme.colors.chipBg,
                border = BorderStroke(1.dp, ModernCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = recordAsTransaction,
                        onCheckedChange = { recordAsTransaction = it },
                        colors = CheckboxDefaults.colors(checkedColor = ModernIncomeBlue)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Record as transaction for this month",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = ModernTextDark
                        )
                        Text(
                            text = "Adds an income entry so your current month balance reflects this immediately",
                            style = MaterialTheme.typography.bodySmall,
                            color = ModernTextSubtle
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save Button
            Button(
                onClick = {
                    val amount = incomeText.toDoubleOrNull() ?: 0.0
                    onSaveMonthlyIncome(amount, recordAsTransaction)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_monthly_income_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ModernIncomeBlue)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save Monthly Income",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
