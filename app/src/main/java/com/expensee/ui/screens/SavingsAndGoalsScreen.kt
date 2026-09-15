package com.expensee.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Delete
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WavingHand
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expensee.ui.util.GoogleAuthHelper
import com.expensee.data.model.CategoryEntity
import com.expensee.data.model.SavingsGoalEntity
import com.expensee.data.model.SubscriptionEntity
import com.expensee.data.model.TransactionEntity
import com.expensee.domain.FinancialEngine
import com.expensee.ui.theme.AccentEmerald
import com.expensee.ui.theme.AppTheme
import com.expensee.ui.theme.BrandGraphite
import com.expensee.ui.theme.ModernCardBg
import com.expensee.ui.theme.ModernCardBorder
import com.expensee.ui.theme.ModernExpenseRed
import com.expensee.ui.theme.ModernExpenseRedBg
import com.expensee.ui.theme.ModernGoalOrange
import com.expensee.ui.theme.ModernGoalOrangeBg
import com.expensee.ui.theme.ModernIncomeBlue
import com.expensee.ui.theme.ModernIncomeBlueBg
import com.expensee.ui.theme.ModernPageBg
import com.expensee.ui.theme.ModernTextDark
import com.expensee.ui.theme.ModernTextLight
import com.expensee.ui.theme.ModernTextSubtle
import com.expensee.ui.components.ModernSegmentedControl
import com.expensee.ui.theme.tactilePress
import com.expensee.ui.theme.cardTactilePress
import com.expensee.ui.theme.buttonTactilePress
import androidx.compose.foundation.interaction.MutableInteractionSource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsAndGoalsScreen(
    goals: List<SavingsGoalEntity>,
    subscriptions: List<SubscriptionEntity>,
    categories: List<CategoryEntity>,
    transactions: List<TransactionEntity>,
    currentCurrencyCode: String,
    currentCurrencySymbol: String,
    userName: String = "User",
    monthlyIncome: Double = 0.0,
    monthlyBudget: Double = 0.0,
    themeMode: String = "SYSTEM",
    googleAccountEmail: String? = null,
    googleAccountName: String? = null,
    isCloudSyncEnabled: Boolean = false,
    lastSyncTimestamp: Long = 0L,
    syncStatus: String = "IDLE",
    onUpdateThemeMode: (String) -> Unit = {},
    onConnectGoogleAccount: (email: String, displayName: String) -> Unit = { _, _ -> },
    onDisconnectGoogleAccount: () -> Unit = {},
    onToggleCloudSync: (Boolean) -> Unit = {},
    onTriggerSyncNow: () -> Unit = {},
    onAddGoal: (name: String, target: Double, initial: Double) -> Unit,
    onAdjustGoalFunds: (goalId: Long, delta: Double) -> Unit,
    onDeleteGoal: (Long) -> Unit,
    onAddSubscription: (name: String, amount: Double, cycle: String) -> Unit,
    onToggleSubscription: (SubscriptionEntity) -> Unit,
    onDeleteSubscription: (Long) -> Unit,
    onUpdateCurrency: (code: String, symbol: String) -> Unit,
    onExportCsv: () -> String,
    onResetData: () -> Unit,
    onOpenCustomizeProfile: () -> Unit = {},
    onReopenOnboarding: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showAddSubDialog by remember { mutableStateOf(false) }
    var adjustingGoal by remember { mutableStateOf<SavingsGoalEntity?>(null) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var exportCsvText by remember { mutableStateOf<String?>(null) }

    val accountPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val email = GoogleAuthHelper.extractAccountEmail(result.data)
        if (!email.isNullOrBlank()) {
            val name = GoogleAuthHelper.formatDisplayNameFromEmail(email)
            onConnectGoogleAccount(email, name)
        }
    }

    // Tab state: 0 = General & Preferences, 1 = Subscriptions, 2 = Savings Goals
    var selectedSection by remember { mutableIntStateOf(0) }

    val initials = remember(userName) {
        val trimmed = userName.trim()
        if (trimmed.isEmpty()) "U"
        else {
            val parts = trimmed.split(" ").filter { it.isNotBlank() }
            if (parts.size >= 2) {
                "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
            } else {
                parts[0].take(2).uppercase()
            }
        }
    }

    val totalSubMonthly = remember(subscriptions) {
        subscriptions.filter { it.active }.sumOf {
            if (it.billingCycle == "ANNUAL") it.amount / 12.0 else it.amount
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ModernPageBg)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .testTag("savings_goals_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Title Header
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = ModernTextDark
                    )
                    Text(
                        text = "Account, preferences & subscriptions",
                        style = MaterialTheme.typography.bodySmall,
                        color = ModernTextSubtle
                    )
                }
            }
        }

        // Profile & Membership Hero Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = ModernCardBg,
                border = BorderStroke(1.dp, ModernCardBorder),
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar with Neutral Graphite
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(BrandGraphite),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = userName.ifBlank { "EXP-MAP User" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ModernTextDark
                            )
                            Text(
                                text = "${FinancialEngine.formatAmount(monthlyIncome, currentCurrencySymbol)}/mo expected · ${FinancialEngine.formatAmount(monthlyBudget, currentCurrencySymbol)}/mo budget",
                                style = MaterialTheme.typography.bodySmall,
                                color = ModernTextSubtle
                            )
                        }

                        // Edit Profile Button
                        Surface(
                            onClick = onOpenCustomizeProfile,
                            shape = RoundedCornerShape(12.dp),
                            color = if (AppTheme.colors.isDark) Color(0xFF262626) else Color(0xFFEBEBEB)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Profile",
                                    tint = if (AppTheme.colors.isDark) Color(0xFFE5E5E5) else BrandGraphite,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Edit",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (AppTheme.colors.isDark) Color(0xFFE5E5E5) else BrandGraphite
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Summary stats
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(AppTheme.colors.chipBg)
                            .padding(horizontal = 18.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TOTAL TRACKED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ModernTextSubtle
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${transactions.size} records",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = ModernTextDark
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "ACTIVE CURRENCY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ModernTextSubtle
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$currentCurrencyCode ($currentCurrencySymbol)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (AppTheme.colors.isDark) Color(0xFFE5E5E5) else BrandGraphite
                            )
                        }
                    }
                }
            }
        }

        // Segmented Section Selector (General, Subscriptions, Goals) with spring indicator
        item {
            val sections = listOf("General", "Subscriptions", "Goals")
            ModernSegmentedControl(
                items = sections,
                selectedIndex = selectedSection,
                onItemSelected = { selectedSection = it },
                modifier = Modifier.fillMaxWidth()
            ) { title, isSelected ->
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) ModernTextDark else ModernTextSubtle
                )
            }
        }

        // Tab: General & Preferences
        if (selectedSection == 0) {
            // Section: Preferences
            item {
                Text(
                    text = "App Preferences",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ModernTextDark
                )
            }

            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = ModernCardBg,
                    border = BorderStroke(1.dp, ModernCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Profile & Monthly Income Item
                        ModernSettingsItem(
                            icon = Icons.Default.Person,
                            iconBg = if (AppTheme.colors.isDark) Color(0xFF262626) else Color(0xFFEBEBEB),
                            iconTint = if (AppTheme.colors.isDark) Color(0xFFE5E5E5) else BrandGraphite,
                            title = "Profile & Targets",
                            subtitle = "${userName.ifBlank { "User" }} · ${FinancialEngine.formatAmount(monthlyIncome, currentCurrencySymbol)}/mo",
                            actionText = "Customize",
                            onClick = onOpenCustomizeProfile
                        )

                        SettingsDivider()

                        // Theme & Appearance Item
                        val themeSubtitle = when (themeMode) {
                            "LIGHT" -> "Light mode"
                            "DARK" -> "Dark mode"
                            else -> "System default"
                        }
                        ModernSettingsItem(
                            icon = Icons.Default.BrightnessAuto,
                            iconBg = if (AppTheme.colors.isDark) Color(0xFF1E293B) else Color(0xFFE0F2FE),
                            iconTint = Color(0xFF0284C7),
                            title = "Theme & Appearance",
                            subtitle = themeSubtitle,
                            actionText = "Change",
                            onClick = onOpenCustomizeProfile
                        )

                        SettingsDivider()

                        // Currency Selector Item
                        ModernSettingsItem(
                            icon = Icons.Default.CurrencyExchange,
                            iconBg = if (AppTheme.colors.isDark) Color(0xFF262626) else Color(0xFFEBEBEB),
                            iconTint = if (AppTheme.colors.isDark) Color(0xFFE5E5E5) else BrandGraphite,
                            title = "Primary Currency",
                            subtitle = "$currentCurrencyCode ($currentCurrencySymbol)",
                            actionText = "Change",
                            onClick = { showCurrencyDialog = true }
                        )
                    }
                }
            }

            // Section: Google Drive Cloud Sync
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cloud Sync & Backup",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ModernTextDark
                    )
                    if (!googleAccountEmail.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isCloudSyncEnabled) AccentEmerald.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = if (isCloudSyncEnabled) "Sync Active" else "Sync Paused",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isCloudSyncEnabled) AccentEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = ModernCardBg,
                    border = BorderStroke(1.dp, ModernCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (googleAccountEmail.isNullOrBlank()) {
                            // Not connected state
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(ModernIncomeBlueBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudSync,
                                        contentDescription = null,
                                        tint = ModernIncomeBlue,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Google Drive Sync",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ModernTextDark
                                    )
                                    Text(
                                        text = "Safely sync budgets & expenses to your private Google Drive AppData folder",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ModernTextSubtle
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    accountPickerLauncher.launch(GoogleAuthHelper.createGoogleAccountPickerIntent())
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandGraphite)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Connect Google Account",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        } else {
                            // Connected state
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(AccentEmerald.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudDone,
                                        contentDescription = null,
                                        tint = AccentEmerald,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = googleAccountName ?: "Google Account",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ModernTextDark
                                    )
                                    Text(
                                        text = googleAccountEmail,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AccentEmerald
                                    )
                                }
                                TextButton(onClick = onDisconnectGoogleAccount) {
                                    Text(
                                        text = "Disconnect",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }

                            SettingsDivider()

                            // Auto sync switch
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Auto-sync Changes",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ModernTextDark
                                    )
                                    Text(
                                        text = "Silently keep data synchronized across your devices",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ModernTextSubtle
                                    )
                                }
                                Switch(
                                    checked = isCloudSyncEnabled,
                                    onCheckedChange = onToggleCloudSync,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = BrandGraphite
                                    )
                                )
                            }

                            SettingsDivider()

                            // Status & Manual Sync Now Button
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    val statusText = when (syncStatus) {
                                        "SYNCING" -> "Syncing in progress..."
                                        "SUCCESS" -> if (lastSyncTimestamp > 0L) {
                                            val diffMin = (System.currentTimeMillis() - lastSyncTimestamp) / (60 * 1000)
                                            if (diffMin < 1) "Synced just now" else "Synced ${diffMin}m ago"
                                        } else "Synced successfully"
                                        "ERROR" -> "Sync failed. Tap to retry."
                                        else -> if (lastSyncTimestamp > 0L) {
                                            val diffMin = (System.currentTimeMillis() - lastSyncTimestamp) / (60 * 1000)
                                            if (diffMin < 1) "Last sync: just now" else "Last sync: ${diffMin}m ago"
                                        } else "Not synced yet"
                                    }
                                    val statusColor = when (syncStatus) {
                                        "SYNCING" -> ModernIncomeBlue
                                        "SUCCESS" -> AccentEmerald
                                        "ERROR" -> ModernExpenseRed
                                        else -> ModernTextSubtle
                                    }
                                    Text(
                                        text = "Sync Status",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = ModernTextDark
                                    )
                                    Text(
                                        text = statusText,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = statusColor
                                    )
                                }

                                OutlinedButton(
                                    onClick = onTriggerSyncNow,
                                    enabled = syncStatus != "SYNCING",
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, BrandGraphite.copy(alpha = 0.5f))
                                ) {
                                    if (syncStatus == "SYNCING") {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = BrandGraphite
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Sync,
                                            contentDescription = null,
                                            tint = BrandGraphite,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Sync Now",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = BrandGraphite
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section: Data & Backup
            item {
                Text(
                    text = "Data Management",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ModernTextDark
                )
            }

            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = ModernCardBg,
                    border = BorderStroke(1.dp, ModernCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Export CSV
                        ModernSettingsItem(
                            icon = Icons.Default.Download,
                            iconBg = ModernIncomeBlueBg,
                            iconTint = ModernIncomeBlue,
                            title = "Export Transactions (CSV)",
                            subtitle = "Download or copy monthly financial records",
                            actionText = "Export",
                            onClick = { exportCsvText = onExportCsv() }
                        )

                        SettingsDivider()

                        // Welcome Setup Wizard Re-run
                        ModernSettingsItem(
                            icon = Icons.Default.WavingHand,
                            iconBg = if (AppTheme.colors.isDark) Color(0xFF3B2D1B) else Color(0xFFFEF3C7),
                            iconTint = Color(0xFFD97706),
                            title = "Re-run Welcome Setup",
                            subtitle = "Launch the first-time setup and income onboarding wizard",
                            actionText = "Launch",
                            onClick = onReopenOnboarding
                        )

                        SettingsDivider()

                        // Reset Data
                        ModernSettingsItem(
                            icon = Icons.Default.RestartAlt,
                            iconBg = ModernExpenseRedBg,
                            iconTint = ModernExpenseRed,
                            title = "Reset & Re-seed Demo Data",
                            subtitle = "Clear custom records and load realistic sample data",
                            actionText = "Reset",
                            actionTextColor = ModernExpenseRed,
                            onClick = { showResetConfirmDialog = true }
                        )
                    }
                }
            }

            // Section: App Info
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = AppTheme.colors.chipBg,
                    border = BorderStroke(1.dp, ModernCardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Expensee",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = ModernTextDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Version 3.0.0 • Private & Drive Sync Enabled",
                            style = MaterialTheme.typography.bodySmall,
                            color = ModernTextSubtle
                        )
                    }
                }
            }
        }

        // Tab: Subscriptions
        if (selectedSection == 1) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Active Subscriptions",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = ModernTextDark
                        )
                        Text(
                            text = "Track recurring Netflix, Spotify, or Gym bills",
                            style = MaterialTheme.typography.bodySmall,
                            color = ModernTextSubtle
                        )
                    }

                    Button(
                        onClick = { showAddSubDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandGraphite
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Add",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Monthly Spend Summary Card
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = ModernCardBg,
                    border = BorderStroke(1.dp, ModernCardBorder),
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "MONTHLY COMMITMENT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ModernTextSubtle,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${FinancialEngine.formatAmount(totalSubMonthly, currentCurrencySymbol)} / month",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = ModernTextDark
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (AppTheme.colors.isDark) Color(0xFF262626) else Color(0xFFEBEBEB)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Subscriptions,
                                contentDescription = "Subscriptions",
                                tint = if (AppTheme.colors.isDark) Color(0xFFE5E5E5) else BrandGraphite,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            if (subscriptions.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = ModernCardBg,
                        border = BorderStroke(1.dp, ModernCardBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(AppTheme.colors.chipBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Subscriptions,
                                    contentDescription = null,
                                    tint = ModernTextSubtle,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No subscriptions tracked",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ModernTextDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Add regular services to manage upcoming billing dates.",
                                style = MaterialTheme.typography.bodySmall,
                                color = ModernTextSubtle
                            )
                        }
                    }
                }
            } else {
                items(subscriptions, key = { it.id }, contentType = { "subscription_item" }) { sub ->
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
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (sub.active) (if (AppTheme.colors.isDark) Color(0xFF262626) else Color(0xFFEBEBEB)) else AppTheme.colors.chipBg
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CreditCard,
                                        contentDescription = "Subscription",
                                        tint = if (sub.active) (if (AppTheme.colors.isDark) Color(0xFFE5E5E5) else BrandGraphite) else ModernTextSubtle,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = sub.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = ModernTextDark
                                    )
                                    Text(
                                        text = "${FinancialEngine.formatAmount(sub.amount, currentCurrencySymbol)} • ${sub.billingCycle.lowercase()}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ModernTextSubtle
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(
                                    checked = sub.active,
                                    onCheckedChange = { onToggleSubscription(sub) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = BrandGraphite,
                                        uncheckedThumbColor = Color.White,
                                        uncheckedTrackColor = Color(0xFFE2E8F0)
                                    )
                                )

                                Spacer(modifier = Modifier.width(4.dp))

                                IconButton(
                                    onClick = { onDeleteSubscription(sub.id) },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = ModernExpenseRed.copy(alpha = 0.7f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Tab: Savings Goals
        if (selectedSection == 2) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Savings Goals",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = ModernTextDark
                        )
                        Text(
                            text = "Track your long-term milestones",
                            style = MaterialTheme.typography.bodySmall,
                            color = ModernTextSubtle
                        )
                    }

                    Button(
                        onClick = { showAddGoalDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandGraphite
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Goal",
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "New Goal",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            if (goals.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = ModernCardBg,
                        border = BorderStroke(1.dp, ModernCardBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(ModernIncomeBlueBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Savings,
                                    contentDescription = null,
                                    tint = ModernIncomeBlue,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No savings goals created",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ModernTextDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Set targets for a new car, wedding, house or emergency fund.",
                                style = MaterialTheme.typography.bodySmall,
                                color = ModernTextSubtle
                            )
                        }
                    }
                }
            } else {
                items(goals, key = { it.id }, contentType = { "goal_item" }) { goal ->
                    val progress = if (goal.targetAmount > 0) {
                        (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f)
                    } else 0f
                    val percentage = (progress * 100).toInt()

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = ModernCardBg,
                        border = BorderStroke(1.dp, ModernCardBorder),
                        shadowElevation = 1.dp
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(if (AppTheme.colors.isDark) Color(0xFF262626) else Color(0xFFEBEBEB)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Savings,
                                            contentDescription = "Goal",
                                            tint = if (AppTheme.colors.isDark) Color(0xFFE5E5E5) else BrandGraphite,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = goal.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = ModernTextDark
                                        )
                                        Text(
                                            text = "Target: ${FinancialEngine.formatAmount(goal.targetAmount, currentCurrencySymbol)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ModernTextSubtle
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { onDeleteGoal(goal.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = ModernTextLight,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Progress Bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Saved: ${FinancialEngine.formatAmount(goal.currentAmount, currentCurrencySymbol)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ModernTextDark
                                )
                                Text(
                                    text = "$percentage%",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (AppTheme.colors.isDark) Color(0xFFE5E5E5) else BrandGraphite
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (AppTheme.colors.isDark) Color(0xFFE5E5E5) else BrandGraphite,
                                trackColor = AppTheme.colors.chipBg
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(
                                    onClick = { adjustingGoal = goal },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, if (AppTheme.colors.isDark) Color(0xFFE5E5E5) else BrandGraphite),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text(
                                        text = "Deposit / Withdraw",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (AppTheme.colors.isDark) Color(0xFFE5E5E5) else BrandGraphite
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }

    // Currency dialog
    if (showCurrencyDialog) {
        val currencyOptions = listOf(
            Triple("USD", "$", "US Dollar"),
            Triple("EUR", "€", "Euro"),
            Triple("GBP", "£", "British Pound"),
            Triple("JPY", "¥", "Japanese Yen"),
            Triple("BDT", "৳", "Bangladeshi Taka"),
            Triple("CAD", "$", "Canadian Dollar"),
            Triple("AUD", "$", "Australian Dollar"),
            Triple("INR", "₹", "Indian Rupee")
        )

        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = {
                Text(
                    text = "Select Primary Currency",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ModernTextDark
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    currencyOptions.forEach { (code, symbol, name) ->
                        val isSelected = code == currentCurrencyCode
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onUpdateCurrency(code, symbol)
                                    showCurrencyDialog = false
                                },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) (if (AppTheme.colors.isDark) Color(0xFF262626) else Color(0xFFEBEBEB)) else AppTheme.colors.chipBg,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) (if (AppTheme.colors.isDark) Color(0xFFE5E5E5) else BrandGraphite) else ModernCardBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) (if (AppTheme.colors.isDark) Color(0xFF4A4A4A) else BrandGraphite) else (if (AppTheme.colors.isDark) Color(0xFF334155) else Color(0xFFE2E8F0))
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = symbol,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = if (isSelected) Color.White else ModernTextDark
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "$code ($symbol)",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = ModernTextDark
                                        )
                                        Text(
                                            text = name,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ModernTextSubtle
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = if (AppTheme.colors.isDark) Color(0xFFE5E5E5) else BrandGraphite,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCurrencyDialog = false }) {
                    Text("Close", color = ModernTextSubtle, fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = ModernCardBg
        )
    }

    // New goal dialog
    if (showAddGoalDialog) {
        var goalName by remember { mutableStateOf("") }
        var targetText by remember { mutableStateOf("") }
        var initialText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddGoalDialog = false },
            title = {
                Text(
                    text = "New Savings Goal",
                    fontWeight = FontWeight.Bold,
                    color = ModernTextDark
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = goalName,
                        onValueChange = { goalName = it },
                        label = { Text("Goal Name") },
                        placeholder = { Text("e.g. New Car, Trip to Bali") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = targetText,
                        onValueChange = { targetText = it },
                        label = { Text("Target Amount ($currentCurrencySymbol)") },
                        placeholder = { Text("10000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = initialText,
                        onValueChange = { initialText = it },
                        label = { Text("Initial Deposit (optional)") },
                        placeholder = { Text("500") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = targetText.toDoubleOrNull() ?: 0.0
                        val initial = initialText.toDoubleOrNull() ?: 0.0
                        if (goalName.isNotBlank() && target > 0) {
                            onAddGoal(goalName, target, initial)
                            showAddGoalDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGraphite),
                    shape = RoundedCornerShape(14.dp),
                    enabled = goalName.isNotBlank() && (targetText.toDoubleOrNull() ?: 0.0) > 0
                ) {
                    Text("Create Goal", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddGoalDialog = false }) {
                    Text("Cancel", color = ModernTextSubtle)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = ModernCardBg
        )
    }

    // Adjust goal funds dialog
    if (adjustingGoal != null) {
        var deltaText by remember { mutableStateOf("") }
        var isDeposit by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { adjustingGoal = null },
            title = {
                Text(
                    text = "${adjustingGoal?.name}: Update Funds",
                    fontWeight = FontWeight.Bold,
                    color = ModernTextDark
                )
            },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { isDeposit = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDeposit) Color(0xFF10B981) else AppTheme.colors.chipBg,
                                contentColor = if (isDeposit) Color.White else ModernTextDark
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Deposit (+)", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { isDeposit = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isDeposit) ModernExpenseRed else AppTheme.colors.chipBg,
                                contentColor = if (!isDeposit) Color.White else ModernTextDark
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Withdraw (-)", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = deltaText,
                        onValueChange = { deltaText = it },
                        label = { Text("Amount ($currentCurrencySymbol)") },
                        placeholder = { Text("100") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = deltaText.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            val delta = if (isDeposit) amount else -amount
                            onAdjustGoalFunds(adjustingGoal!!.id, delta)
                            adjustingGoal = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGraphite),
                    shape = RoundedCornerShape(14.dp),
                    enabled = (deltaText.toDoubleOrNull() ?: 0.0) > 0
                ) {
                    Text("Apply", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { adjustingGoal = null }) {
                    Text("Cancel", color = ModernTextSubtle)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = ModernCardBg
        )
    }

    // Add subscription dialog
    if (showAddSubDialog) {
        var subName by remember { mutableStateOf("") }
        var amountText by remember { mutableStateOf("") }
        var cycle by remember { mutableStateOf("MONTHLY") }

        AlertDialog(
            onDismissRequest = { showAddSubDialog = false },
            title = {
                Text(
                    text = "Add Subscription",
                    fontWeight = FontWeight.Bold,
                    color = ModernTextDark
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = subName,
                        onValueChange = { subName = it },
                        label = { Text("Subscription Name") },
                        placeholder = { Text("e.g. Netflix, Spotify, Gym") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Amount ($currentCurrencySymbol)") },
                        placeholder = { Text("14.99") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { cycle = "MONTHLY" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (cycle == "MONTHLY") BrandGraphite else AppTheme.colors.chipBg,
                                contentColor = if (cycle == "MONTHLY") Color.White else ModernTextDark
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Monthly", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { cycle = "ANNUAL" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (cycle == "ANNUAL") BrandGraphite else AppTheme.colors.chipBg,
                                contentColor = if (cycle == "ANNUAL") Color.White else ModernTextDark
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Annual", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        if (subName.isNotBlank() && amount > 0) {
                            onAddSubscription(subName, amount, cycle)
                            showAddSubDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGraphite),
                    shape = RoundedCornerShape(14.dp),
                    enabled = subName.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0
                ) {
                    Text("Save", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSubDialog = false }) {
                    Text("Cancel", color = ModernTextSubtle)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = ModernCardBg
        )
    }

    // CSV export dialog
    if (exportCsvText != null) {
        AlertDialog(
            onDismissRequest = { exportCsvText = null },
            title = {
                Text(
                    text = "CSV Export Ready",
                    fontWeight = FontWeight.Bold,
                    color = ModernTextDark
                )
            },
            text = {
                Column {
                    Text(
                        text = "Transactions are formatted as CSV for Excel or Google Sheets.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ModernTextSubtle
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        color = AppTheme.colors.chipBg,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ModernCardBorder)
                    ) {
                        Text(
                            text = exportCsvText ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(10.dp),
                            fontFamily = FontFamily.Monospace,
                            color = ModernTextDark
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Transactions CSV", exportCsvText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "CSV copied to clipboard!", Toast.LENGTH_SHORT).show()
                        exportCsvText = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGraphite),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Copy CSV", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { exportCsvText = null }) {
                    Text("Close", color = ModernTextSubtle)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = ModernCardBg
        )
    }

    // Reset confirm dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = ModernExpenseRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Reset Demo Data?",
                        fontWeight = FontWeight.Bold,
                        color = ModernTextDark
                    )
                }
            },
            text = {
                Text(
                    text = "This will restore standard categories, realistic transactions, budgets, and savings goals matching your setup.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ModernTextSubtle
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetData()
                        showResetConfirmDialog = false
                        Toast.makeText(context, "Database reset complete", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ModernExpenseRed),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Reset Data", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel", color = ModernTextSubtle)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = ModernCardBg
        )
    }
}

// Subcomponents for Settings
@Composable
private fun ModernSettingsItem(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    actionText: String? = null,
    actionTextColor: Color = BrandGraphite,
    onClick: () -> Unit
) {
    val rowInteraction = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .buttonTactilePress(pressedScale = 0.98f, interactionSource = rowInteraction)
            .clickable(
                interactionSource = rowInteraction,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ModernTextDark
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = ModernTextSubtle
                )
            }
        }

        if (actionText != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = actionTextColor
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = actionTextColor,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
private fun ModernSettingsToggle(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ModernTextDark
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = ModernTextSubtle
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = BrandGraphite,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFE2E8F0)
            )
        )
    }
}

@Composable
private fun SettingsDivider() {
    androidx.compose.material3.HorizontalDivider(
        modifier = Modifier.padding(vertical = 4.dp),
        color = ModernCardBorder,
        thickness = 1.dp
    )
}
