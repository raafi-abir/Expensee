package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
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
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AppTheme
import com.example.ui.theme.BrandGraphite
import com.example.ui.theme.ModernCardBg
import com.example.ui.theme.ModernCardBorder
import androidx.compose.runtime.Immutable
import com.example.ui.theme.ModernTextDark
import com.example.ui.theme.ModernTextSubtle

@Immutable
data class CurrencyItem(val code: String, val symbol: String, val name: String)

private val SUPPORTED_CURRENCIES = listOf(
    CurrencyItem("USD", "$", "US Dollar"),
    CurrencyItem("EUR", "€", "Euro"),
    CurrencyItem("GBP", "£", "British Pound"),
    CurrencyItem("BDT", "৳", "Bangladeshi Taka"),
    CurrencyItem("INR", "₹", "Indian Rupee"),
    CurrencyItem("CAD", "$", "Canadian Dollar"),
    CurrencyItem("AUD", "$", "Australian Dollar"),
    CurrencyItem("JPY", "¥", "Japanese Yen")
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CustomizeProfileSheet(
    initialName: String,
    initialMonthlyIncome: Double,
    initialMonthlyBudget: Double,
    initialCurrencyCode: String,
    initialCurrencySymbol: String,
    initialThemeMode: String,
    onThemeSelected: (String) -> Unit = {},
    onSave: (name: String, monthlyIncome: Double, monthlyBudget: Double, currencyCode: String, currencySymbol: String, themeMode: String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember { mutableStateOf(initialName) }
    var incomeText by remember {
        mutableStateOf(if (initialMonthlyIncome % 1 == 0.0) initialMonthlyIncome.toInt().toString() else initialMonthlyIncome.toString())
    }
    var budgetText by remember {
        mutableStateOf(if (initialMonthlyBudget % 1 == 0.0) initialMonthlyBudget.toInt().toString() else initialMonthlyBudget.toString())
    }
    var selectedCurrencyCode by remember { mutableStateOf(initialCurrencyCode) }
    var selectedCurrencySymbol by remember { mutableStateOf(initialCurrencySymbol) }
    var selectedThemeMode by remember { mutableStateOf(initialThemeMode) }

    val currencies = SUPPORTED_CURRENCIES

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
        modifier = Modifier.testTag("customize_profile_sheet")
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
                            .background(if (AppTheme.colors.isDark) Color(0xFF262626) else Color(0xFFEBEBEB)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = if (AppTheme.colors.isDark) Color(0xFFE5E5E5) else BrandGraphite,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Customize Profile",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = ModernTextDark
                        )
                        Text(
                            text = "Personalize your greeting, income & appearance",
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

            Spacer(modifier = Modifier.height(20.dp))

            // 1. Name Input
            Text(
                text = "Your Name (for Greeting)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = ModernTextDark
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("Alex") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandGraphite,
                    unfocusedBorderColor = ModernCardBorder,
                    focusedContainerColor = AppTheme.colors.inputBg,
                    unfocusedContainerColor = AppTheme.colors.inputBg
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_name_input")
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Theme Mode Selection (System, Light, Dark)
            Text(
                text = "Appearance & Theme",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = ModernTextDark
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(
                    Triple("SYSTEM", "System", Icons.Default.BrightnessAuto),
                    Triple("LIGHT", "Light", Icons.Default.LightMode),
                    Triple("DARK", "Dark", Icons.Default.DarkMode)
                ).forEach { (mode, label, icon) ->
                    val isSelected = selectedThemeMode == mode
                    Surface(
                        onClick = {
                            selectedThemeMode = mode
                            onThemeSelected(mode)
                        },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) (if (AppTheme.colors.isDark) Color(0xFF2E2E2E) else Color(0xFFEBEBEB)) else AppTheme.colors.chipBg,
                        border = BorderStroke(
                            1.5.dp,
                            if (isSelected) (if (AppTheme.colors.isDark) Color(0xFFE5E5E5) else BrandGraphite) else ModernCardBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("theme_option_$mode")
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (isSelected) (if (AppTheme.colors.isDark) Color(0xFFE5E5E5) else BrandGraphite) else ModernTextSubtle,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) (if (AppTheme.colors.isDark) Color(0xFFE5E5E5) else BrandGraphite) else ModernTextDark
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. Monthly Income Input
            Text(
                text = "Monthly Income Target",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = ModernTextDark
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = incomeText,
                onValueChange = { input ->
                    if (input.isEmpty() || input.all { it.isDigit() || it == '.' }) {
                        incomeText = input
                    }
                },
                leadingIcon = {
                    Text(
                        text = selectedCurrencySymbol,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (AppTheme.colors.isDark) Color(0xFFE5E5E5) else BrandGraphite,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                },
                placeholder = { Text("5000") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandGraphite,
                    unfocusedBorderColor = ModernCardBorder,
                    focusedContainerColor = AppTheme.colors.inputBg,
                    unfocusedContainerColor = AppTheme.colors.inputBg
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_income_input")
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 4. Monthly Budget Limit
            Text(
                text = "Monthly Spending Budget",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = ModernTextDark
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = budgetText,
                onValueChange = { input ->
                    if (input.isEmpty() || input.all { it.isDigit() || it == '.' }) {
                        budgetText = input
                    }
                },
                leadingIcon = {
                    Text(
                        text = selectedCurrencySymbol,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (AppTheme.colors.isDark) Color(0xFFE5E5E5) else BrandGraphite,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                },
                placeholder = { Text("2500") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandGraphite,
                    unfocusedBorderColor = ModernCardBorder,
                    focusedContainerColor = AppTheme.colors.inputBg,
                    unfocusedContainerColor = AppTheme.colors.inputBg
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_budget_input")
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 5. Currency Selection
            Text(
                text = "Primary Currency",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = ModernTextDark
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                currencies.forEach { item ->
                    val isSelected = item.code == selectedCurrencyCode
                    Surface(
                        onClick = {
                            selectedCurrencyCode = item.code
                            selectedCurrencySymbol = item.symbol
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) (if (AppTheme.colors.isDark) Color(0xFF2E2E2E) else Color(0xFFEBEBEB)) else AppTheme.colors.chipBg,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) (if (AppTheme.colors.isDark) Color(0xFFE5E5E5) else BrandGraphite) else ModernCardBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = item.symbol,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) (if (AppTheme.colors.isDark) Color(0xFFE5E5E5) else BrandGraphite) else ModernTextDark
                            )
                            Text(
                                text = item.code,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) (if (AppTheme.colors.isDark) Color(0xFFE5E5E5) else BrandGraphite) else ModernTextDark
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Save Button
            Button(
                onClick = {
                    val income = incomeText.toDoubleOrNull() ?: initialMonthlyIncome
                    val budget = budgetText.toDoubleOrNull() ?: initialMonthlyBudget
                    onSave(
                        name.ifBlank { "Alex" },
                        income,
                        budget,
                        selectedCurrencyCode,
                        selectedCurrencySymbol,
                        selectedThemeMode
                    )
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_profile_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandGraphite,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save Changes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
