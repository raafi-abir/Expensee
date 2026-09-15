package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionEntity
import com.example.domain.FinancialEngine
import com.example.ui.components.MonthSelectorBar
import com.example.ui.components.TransactionRowItem
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AppTheme
import com.example.ui.theme.BrandGraphite
import com.example.ui.theme.ModernCardBg
import com.example.ui.theme.ModernCardBorder
import com.example.ui.theme.ModernExpenseRed
import com.example.ui.theme.ModernTextDark
import com.example.ui.theme.ModernTextSubtle
import com.example.ui.theme.AppDateFormatters

private val FILTER_TYPES = listOf("ALL", "EXPENSE", "INCOME")
private val PAYMENT_METHODS = listOf("Cash", "Bank", "Card", "Mobile Banking")

@Composable
fun TransactionsHistoryScreen(
    transactions: List<TransactionEntity>,
    categories: List<CategoryEntity>,
    selectedMonth: Int,
    selectedYear: Int,
    currencySymbol: String,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectMonth: (month: Int, year: Int) -> Unit = { _, _ -> },
    onTransactionClick: (TransactionEntity) -> Unit,
    onDeleteTransactionClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("ALL") } // ALL, EXPENSE, INCOME
    var selectedPaymentMethod by remember { mutableStateOf<String?>(null) }

    val categoryEntityMap = remember(categories) { categories.associateBy { it.id } }

    val filteredTransactions = remember(transactions, searchQuery, selectedType, selectedPaymentMethod, categoryEntityMap) {
        val hasSearch = searchQuery.isNotBlank()
        transactions.filter { tx ->
            val matchesType = when (selectedType) {
                "EXPENSE" -> tx.type == "EXPENSE"
                "INCOME" -> tx.type == "INCOME"
                else -> true
            }
            if (!matchesType) return@filter false

            val matchesPayment = selectedPaymentMethod == null || tx.paymentMethod.equals(selectedPaymentMethod, ignoreCase = true)
            if (!matchesPayment) return@filter false

            if (!hasSearch) return@filter true

            val catName = categoryEntityMap[tx.categoryId]?.name ?: ""
            tx.note.contains(searchQuery, ignoreCase = true) ||
                    catName.contains(searchQuery, ignoreCase = true) ||
                    tx.paymentMethod.contains(searchQuery, ignoreCase = true)
        }
    }

    // Group filtered transactions by day and compute totals in a single pass
    val (groupedTransactions, totals) = remember(filteredTransactions) {
        val map = LinkedHashMap<String, MutableList<TransactionEntity>>()
        var exp = 0.0
        var inc = 0.0
        for (tx in filteredTransactions) {
            val group = AppDateFormatters.formatDayGroup(tx.date)
            map.getOrPut(group) { ArrayList() }.add(tx)
            if (tx.type == "EXPENSE") exp += tx.amount
            else if (tx.type == "INCOME") inc += tx.amount
        }
        map to (exp to inc)
    }
    val (totalExpenses, totalIncome) = totals

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("transactions_history_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item(key = "month_selector") {
            Spacer(modifier = Modifier.height(4.dp))
            MonthSelectorBar(
                month = selectedMonth,
                year = selectedYear,
                onPrevClick = onPrevMonth,
                onNextClick = onNextMonth,
                onSelectMonth = onSelectMonth
            )
        }

        // Search bar
        item(key = "search_bar") {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search transactions, notes, categories...", color = ModernTextSubtle) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = ModernTextSubtle
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = ModernTextSubtle
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandGraphite,
                    unfocusedBorderColor = ModernCardBorder,
                    focusedContainerColor = AppTheme.colors.inputBg,
                    unfocusedContainerColor = AppTheme.colors.inputBg,
                    focusedTextColor = ModernTextDark,
                    unfocusedTextColor = ModernTextDark
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("history_search_input")
            )
        }

        // Filter chips: Type and Payment Methods
        item(key = "filter_chips") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FILTER_TYPES.forEach { type ->
                    val isSelected = selectedType == type
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedType = type },
                        label = {
                            Text(
                                text = when (type) {
                                    "ALL" -> "All"
                                    "EXPENSE" -> "Expenses"
                                    "INCOME" -> "Income"
                                    else -> type
                                }
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = AppTheme.colors.chipBg,
                            labelColor = ModernTextDark,
                            selectedContainerColor = if (AppTheme.colors.isDark) Color(0xFF2E2E2E) else Color(0xFFEBEBEB),
                            selectedLabelColor = if (AppTheme.colors.isDark) Color(0xFFE5E5E5) else BrandGraphite
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = ModernCardBorder,
                            selectedBorderColor = if (AppTheme.colors.isDark) Color(0xFFE5E5E5) else BrandGraphite
                        ),
                        modifier = Modifier.testTag("filter_type_$type")
                    )
                }

                PAYMENT_METHODS.forEach { method ->
                    val isSelected = selectedPaymentMethod == method
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedPaymentMethod = if (isSelected) null else method
                        },
                        label = { Text(method) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = AppTheme.colors.chipBg,
                            labelColor = ModernTextDark,
                            selectedContainerColor = if (AppTheme.colors.isDark) Color(0xFF2E2E2E) else Color(0xFFEBEBEB),
                            selectedLabelColor = if (AppTheme.colors.isDark) Color(0xFFE5E5E5) else BrandGraphite
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = ModernCardBorder,
                            selectedBorderColor = if (AppTheme.colors.isDark) Color(0xFFE5E5E5) else BrandGraphite
                        ),
                        modifier = Modifier.testTag("filter_payment_$method")
                    )
                }
            }
        }

        // Summary bar
        item(key = "summary_bar") {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ModernCardBg,
                border = BorderStroke(1.dp, ModernCardBorder),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${filteredTransactions.size} transactions",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = ModernTextSubtle
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (totalIncome > 0) {
                            Text(
                                text = "+${FinancialEngine.formatAmount(totalIncome, currencySymbol)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = AccentEmerald
                            )
                        }
                        if (totalExpenses > 0) {
                            Text(
                                text = "-${FinancialEngine.formatAmount(totalExpenses, currencySymbol)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = ModernExpenseRed
                            )
                        }
                    }
                }
            }
        }

        if (filteredTransactions.isEmpty()) {
            item(key = "empty_transactions") {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = ModernCardBg,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, ModernCardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No matching transactions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = ModernTextDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try adjusting your search query or filters.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ModernTextSubtle
                        )
                    }
                }
            }
        } else {
            groupedTransactions.forEach { (dateHeader, txsInGroup) ->
                item(key = "header_$dateHeader", contentType = "date_header") {
                    Text(
                        text = dateHeader,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = ModernTextSubtle,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                items(txsInGroup, key = { it.id }, contentType = { "transaction_item" }) { tx ->
                    val category = categoryEntityMap[tx.categoryId]
                    TransactionRowItem(
                        transaction = tx,
                        category = category,
                        currencySymbol = currencySymbol,
                        onClick = { onTransactionClick(tx) },
                        onDeleteClick = { onDeleteTransactionClick(tx.id) },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }

        item(key = "bottom_spacer") {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
