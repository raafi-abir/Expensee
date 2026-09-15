package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CurrencyItem
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.BrandGraphite
import com.example.ui.theme.ModernIncomeBlue
import com.example.ui.theme.ModernIncomeBlueBg
import com.example.util.TimeGreetingHelper

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    onComplete: (
        name: String,
        currencyCode: String,
        currencySymbol: String,
        monthlyIncome: Double,
        monthlyBudget: Double,
        themeMode: String,
        seedInitialIncomeTx: Boolean
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableIntStateOf(0) }

    var userName by remember { mutableStateOf("") }
    var incomeInput by remember { mutableStateOf("0") }
    var budgetInput by remember { mutableStateOf("0") }
    var seedIncomeTx by remember { mutableStateOf(false) }
    var selectedThemeMode by remember { mutableStateOf("SYSTEM") }

    val currencyOptions = listOf(
        CurrencyItem("USD", "$", "US Dollar"),
        CurrencyItem("EUR", "€", "Euro"),
        CurrencyItem("GBP", "£", "British Pound"),
        CurrencyItem("BDT", "৳", "Bangladeshi Taka"),
        CurrencyItem("INR", "₹", "Indian Rupee"),
        CurrencyItem("CAD", "$", "Canadian Dollar"),
        CurrencyItem("AUD", "$", "Australian Dollar"),
        CurrencyItem("JPY", "¥", "Japanese Yen")
    )
    var selectedCurrency by remember { mutableStateOf(currencyOptions.first()) }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("onboarding_screen"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: Step Indicator & Back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (step > 0) {
                    IconButton(
                        onClick = { step-- },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(36.dp))
                }

                // Step Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { index ->
                        val isActive = step == index
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(if (isActive) 24.dp else 6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if (isActive) BrandGraphite
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                        )
                    }
                }

                if (step < 2) {
                    Text(
                        text = "Skip",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clickable {
                                val inc = incomeInput.toDoubleOrNull() ?: 5000.0
                                val bud = budgetInput.toDoubleOrNull() ?: 2500.0
                                onComplete(
                                    userName.ifBlank { "Alex" },
                                    selectedCurrency.code,
                                    selectedCurrency.symbol,
                                    inc,
                                    bud,
                                    selectedThemeMode,
                                    seedIncomeTx
                                )
                            }
                            .padding(8.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.size(36.dp))
                }
            }

            // Animated Step Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = step,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "onboarding_steps"
                ) { targetStep ->
                    when (targetStep) {
                        0 -> WelcomeStep(
                            name = userName,
                            onNameChange = { userName = it }
                        )

                        1 -> IncomeAndCurrencyStep(
                            selectedCurrency = selectedCurrency,
                            onSelectCurrency = { selectedCurrency = it },
                            currencies = currencyOptions,
                            income = incomeInput,
                            onIncomeChange = { incomeInput = it },
                            budget = budgetInput,
                            onBudgetChange = { budgetInput = it },
                            seedIncome = seedIncomeTx,
                            onSeedIncomeChange = { seedIncomeTx = it }
                        )

                        else -> PreferencesAndThemeStep(
                            userName = userName,
                            selectedThemeMode = selectedThemeMode,
                            onSelectTheme = { selectedThemeMode = it }
                        )
                    }
                }
            }

            // Bottom Navigation Action Button
            Column(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        if (step < 2) {
                            step++
                        } else {
                            val inc = incomeInput.toDoubleOrNull() ?: 0.0
                            val bud = budgetInput.toDoubleOrNull() ?: 0.0
                            onComplete(
                                userName.ifBlank { "User" },
                                selectedCurrency.code,
                                selectedCurrency.symbol,
                                inc,
                                bud,
                                selectedThemeMode,
                                seedIncomeTx
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag(if (step == 2) "get_started_button" else "next_step_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandGraphite
                    )
                ) {
                    Text(
                        text = if (step == 2) "Start Managing Money" else "Continue",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (step == 2) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomeStep(
    name: String,
    onNameChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Hero Gradient Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(28.dp),
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BrandGraphite)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "Welcome",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "FlowFinance",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Title & Description
        Text(
            text = "Welcome to Your Financial Companion",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Take full control of your income, daily expenses, savings goals, and budgets in one private, offline-first app.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Name input
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "What should we call you?",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                placeholder = { Text("e.g. Alex") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandGraphite,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_name_input")
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Feature Highlights
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FeaturePill(icon = Icons.Default.Savings, text = "Income & Budgets", modifier = Modifier.weight(1f))
            FeaturePill(icon = Icons.Default.PieChart, text = "Clear Visuals", modifier = Modifier.weight(1f))
            FeaturePill(icon = Icons.Default.Security, text = "100% Private", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun FeaturePill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BrandGraphite,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IncomeAndCurrencyStep(
    selectedCurrency: CurrencyItem,
    onSelectCurrency: (CurrencyItem) -> Unit,
    currencies: List<CurrencyItem>,
    income: String,
    onIncomeChange: (String) -> Unit,
    budget: String,
    onBudgetChange: (String) -> Unit,
    seedIncome: Boolean,
    onSeedIncomeChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = "Set Your Income & Currency",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Set your regular monthly earnings and spending targets.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        // Currency Selector
        Text(
            text = "Primary Currency",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            currencies.forEach { opt ->
                val isSelected = opt.code == selectedCurrency.code
                Surface(
                    onClick = { onSelectCurrency(opt) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) BrandGraphite.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) BrandGraphite else Color.Transparent
                    ),
                    modifier = Modifier.testTag("currency_option_${opt.code}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = opt.symbol,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) BrandGraphite else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = opt.code,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) BrandGraphite else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Monthly Income Input
        Text(
            text = "Monthly Income",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Your base monthly earnings or salary.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp, bottom = 6.dp)
        )

        OutlinedTextField(
            value = income,
            onValueChange = { input ->
                if (input.isEmpty() || input.all { it.isDigit() || it == '.' }) {
                    onIncomeChange(input)
                }
            },
            leadingIcon = {
                Text(
                    text = selectedCurrency.symbol,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ModernIncomeBlue,
                    modifier = Modifier.padding(start = 12.dp)
                )
            },
            placeholder = { Text("5000") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ModernIncomeBlue,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("onboarding_income_input")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Preset chips
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(2500, 5000, 7500, 10000).forEach { preset ->
                Surface(
                    onClick = { onIncomeChange(preset.toString()) },
                    shape = RoundedCornerShape(10.dp),
                    color = ModernIncomeBlueBg,
                    border = BorderStroke(1.dp, ModernIncomeBlue.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "${selectedCurrency.symbol}$preset",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = ModernIncomeBlue,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Checkbox: Seed initial income transaction
        Surface(
            onClick = { onSeedIncomeChange(!seedIncome) },
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = seedIncome,
                    onCheckedChange = onSeedIncomeChange,
                    colors = CheckboxDefaults.colors(checkedColor = ModernIncomeBlue)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Record as initial income transaction",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Immediately loads this into your current month's balance",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Monthly Budget Input
        Text(
            text = "Monthly Spending Budget Target",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = budget,
            onValueChange = { input ->
                if (input.isEmpty() || input.all { it.isDigit() || it == '.' }) {
                    onBudgetChange(input)
                }
            },
            leadingIcon = {
                Text(
                    text = selectedCurrency.symbol,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrandGraphite,
                    modifier = Modifier.padding(start = 12.dp)
                )
            },
            placeholder = { Text("2500") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandGraphite,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("onboarding_budget_input")
        )
    }
}

@Composable
private fun PreferencesAndThemeStep(
    userName: String,
    selectedThemeMode: String,
    onSelectTheme: (String) -> Unit
) {
    val greetingText = TimeGreetingHelper.getFullGreeting(userName)
    val subtitleText = TimeGreetingHelper.getTimeSensitiveSubtitle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = "Personalize Experience",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Choose your app appearance and review your dynamic dashboard greeting.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        // Live Greeting Preview Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = BrandGraphite.copy(alpha = 0.12f),
            border = BorderStroke(1.dp, BrandGraphite.copy(alpha = 0.25f))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "✨", fontSize = 18.sp)
                    Text(
                        text = "Dashboard Greeting Preview",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = BrandGraphite
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = greetingText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitleText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Appearance Theme Options
        Text(
            text = "Theme Preference",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "System theme dynamically switches with Android night mode.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(
                Triple("SYSTEM", "System Default", Icons.Default.BrightnessAuto to "Follows device day/night setting"),
                Triple("LIGHT", "Light Mode", Icons.Default.LightMode to "Clean, bright, high-contrast look"),
                Triple("DARK", "Dark Mode", Icons.Default.DarkMode to "Sleek, eye-friendly twilight theme")
            ).forEach { (mode, label, details) ->
                val isSelected = selectedThemeMode == mode
                Surface(
                    onClick = { onSelectTheme(mode) },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) BrandGraphite.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(
                        1.5.dp,
                        if (isSelected) BrandGraphite else Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_theme_$mode")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) BrandGraphite else MaterialTheme.colorScheme.surface
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = details.first,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = details.second,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = BrandGraphite,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
