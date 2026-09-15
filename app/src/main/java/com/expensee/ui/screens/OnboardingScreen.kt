package com.expensee.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.WavingHand
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expensee.R
import com.expensee.ui.theme.AccentEmerald
import com.expensee.ui.theme.BrandGraphite
import com.expensee.ui.theme.ModernIncomeBlue
import com.expensee.ui.theme.buttonTactilePress
import com.expensee.ui.util.CurrencyItem
import com.expensee.ui.util.CurrencyUtils
import com.expensee.ui.util.GoogleAuthHelper
import com.expensee.util.TimeGreetingHelper

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
        seedInitialIncomeTx: Boolean,
        googleAccountEmail: String?,
        googleAccountName: String?,
        isCloudSyncEnabled: Boolean
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableIntStateOf(0) }

    var userName by remember { mutableStateOf("") }
    var incomeInput by remember { mutableStateOf("0") }
    var budgetInput by remember { mutableStateOf("0") }
    var seedIncomeTx by remember { mutableStateOf(false) }
    var selectedThemeMode by remember { mutableStateOf("SYSTEM") }

    var googleAccountEmail by remember { mutableStateOf<String?>(null) }
    var googleAccountName by remember { mutableStateOf<String?>(null) }
    var isCloudSyncEnabled by remember { mutableStateOf(false) }

    val accountPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val email = GoogleAuthHelper.extractAccountEmail(result.data)
        if (!email.isNullOrBlank()) {
            googleAccountEmail = email
            val detectedName = GoogleAuthHelper.formatDisplayNameFromEmail(email)
            googleAccountName = detectedName
            isCloudSyncEnabled = true
            if (userName.isBlank()) {
                userName = detectedName
            }
            step = 1
        }
    }

    val currencyOptions = remember { CurrencyUtils.supportedCurrencies }
    var selectedCurrency by remember { mutableStateOf(CurrencyUtils.detectSuggestedCurrency()) }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("onboarding_screen"),
        color = if (step == 0) Color(0xFFEDEAE4) else MaterialTheme.colorScheme.background
    ) {
        AnimatedContent(
            targetState = step,
            transitionSpec = {
                if (targetState > initialState) {
                    (slideInHorizontally { width -> width } + fadeIn(tween(350))) togetherWith
                            (slideOutHorizontally { width -> -width } + fadeOut(tween(300)))
                } else {
                    (slideInHorizontally { width -> -width } + fadeIn(tween(350))) togetherWith
                            (slideOutHorizontally { width -> width } + fadeOut(tween(300)))
                }
            },
            label = "onboarding_step_transitions"
        ) { currentStep ->
            when (currentStep) {
                0 -> {
                    // STEP 0: EXACT REFERENCE WELCOME SCREEN
                    WelcomeStep(
                        onSignInWithGoogle = {
                            accountPickerLauncher.launch(GoogleAuthHelper.createGoogleAccountPickerIntent())
                        },
                        onContinueWithoutAccount = {
                            googleAccountEmail = null
                            googleAccountName = null
                            isCloudSyncEnabled = false
                            step = 1
                        }
                    )
                }

                else -> {
                    // STEPS 1 & 2: PROFILE SETUP & THEME SELECTION
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .navigationBarsPadding()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top Bar: Back button, Step Dots, Skip button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { step-- },
                                modifier = Modifier
                                    .size(38.dp)
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

                            // Step Dots
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(2) { index ->
                                    val isActive = (currentStep - 1) == index
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

                            if (currentStep == 1) {
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
                                                userName.ifBlank { googleAccountName ?: "User" },
                                                selectedCurrency.code,
                                                selectedCurrency.symbol,
                                                inc,
                                                bud,
                                                selectedThemeMode,
                                                seedIncomeTx,
                                                googleAccountEmail,
                                                googleAccountName,
                                                isCloudSyncEnabled
                                            )
                                        }
                                        .padding(8.dp)
                                )
                            } else {
                                Spacer(modifier = Modifier.size(38.dp))
                            }
                        }

                        // Content Body for Step 1 or 2
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            if (currentStep == 1) {
                                IncomeAndCurrencyStep(
                                    name = userName,
                                    onNameChange = { userName = it },
                                    googleEmail = googleAccountEmail,
                                    googleName = googleAccountName,
                                    onDisconnectGoogle = {
                                        googleAccountEmail = null
                                        googleAccountName = null
                                        isCloudSyncEnabled = false
                                    },
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
                            } else {
                                PreferencesAndThemeStep(
                                    userName = userName.ifBlank { googleAccountName ?: "User" },
                                    selectedThemeMode = selectedThemeMode,
                                    onSelectTheme = { selectedThemeMode = it }
                                )
                            }
                        }

                        // Bottom Action Button
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = {
                                    if (currentStep == 1) {
                                        step = 2
                                    } else {
                                        val inc = incomeInput.toDoubleOrNull() ?: 0.0
                                        val bud = budgetInput.toDoubleOrNull() ?: 0.0
                                        onComplete(
                                            userName.ifBlank { googleAccountName ?: "User" },
                                            selectedCurrency.code,
                                            selectedCurrency.symbol,
                                            inc,
                                            bud,
                                            selectedThemeMode,
                                            seedIncomeTx,
                                            googleAccountEmail,
                                            googleAccountName,
                                            isCloudSyncEnabled
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .buttonTactilePress()
                                    .testTag(if (currentStep == 2) "get_started_button" else "next_step_button"),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BrandGraphite
                                )
                            ) {
                                Text(
                                    text = if (currentStep == 2) "Start Managing Money" else "Continue",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = if (currentStep == 2) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Exact replica of the Expensee Welcome Screen design.
 * Features:
 * - Soft neutral organic background with understated curves
 * - Top-right delicate calligraphy motto: Small Steps Big Freedom
 * - Layered graphite hero panel with leaf logo, title, and motto
 * - Overlapping white content card with Google sign-in and Continue without account actions
 * - Understated bottom footer tagline
 */
@Composable
private fun WelcomeStep(
    onSignInWithGoogle: () -> Unit,
    onContinueWithoutAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    val entranceAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        entranceAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFEDEAE4))
    ) {
        // 1. Soft neutral background with subtle organic curved contours
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Large organic curve top-right
            drawCircle(
                color = Color(0xFFE2DFD8).copy(alpha = 0.5f),
                radius = w * 0.95f,
                center = Offset(w * 0.92f, h * 0.12f)
            )

            // Large soft curve mid-left
            drawCircle(
                color = Color(0xFFF7F5F0).copy(alpha = 0.65f),
                radius = w * 0.85f,
                center = Offset(w * 0.05f, h * 0.48f)
            )

            // Bottom-left subtle contour
            drawCircle(
                color = Color(0xFFDFDCD5).copy(alpha = 0.45f),
                radius = w * 0.8f,
                center = Offset(w * 0.2f, h * 0.92f)
            )
        }

        // 2. Top-right decorative calligraphy / motto text
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 18.dp, end = 26.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "Small",
                fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic,
                fontSize = 13.sp,
                color = Color(0xFFA6A49F)
            )
            Text(
                text = "Steps",
                fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic,
                fontSize = 13.sp,
                color = Color(0xFFA6A49F)
            )
            Text(
                text = "Big",
                fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic,
                fontSize = 13.sp,
                color = Color(0xFFA6A49F)
            )
            Text(
                text = "Freedom",
                fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic,
                fontSize = 13.sp,
                color = Color(0xFFA6A49F)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .width(26.dp)
                    .height(1.dp)
                    .background(Color(0xFFC0BEB8))
            )
        }

        // 3. Central layered composition inside scrollable container
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 410.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                // HERO PANEL (Back layer)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                        .graphicsLayer {
                            alpha = entranceAnim.value
                            translationY = -20f * (1f - entranceAnim.value)
                        },
                    shape = RoundedCornerShape(36.dp),
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF656360),
                                        Color(0xFF504E4B)
                                    )
                                )
                            )
                    ) {
                        // Subtle darker organic arcs near corners of hero panel
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                color = Color(0xFF33312E).copy(alpha = 0.45f),
                                radius = size.width * 0.5f,
                                center = Offset(size.width * 0.05f, size.height * 0.1f)
                            )
                            drawCircle(
                                color = Color(0xFF33312E).copy(alpha = 0.35f),
                                radius = size.width * 0.45f,
                                center = Offset(size.width * 0.95f, size.height * 0.55f)
                            )
                        }

                        // Hero header content
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 38.dp, start = 24.dp, end = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_expensee_leaf),
                                contentDescription = "Expensee Logo",
                                tint = Color(0xFFF3F2EE),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Expensee",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                letterSpacing = 0.5.sp,
                                fontSize = 31.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Track today.\nA better tomorrow.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.82f),
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp,
                                fontSize = 14.5.sp
                            )
                        }
                    }
                }

                // WHITE CONTENT CARD (Front layer, overlapping hero panel)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 190.dp)
                        .shadow(
                            elevation = 16.dp,
                            shape = RoundedCornerShape(36.dp),
                            spotColor = Color(0x35000000),
                            ambientColor = Color(0x18000000)
                        )
                        .graphicsLayer {
                            alpha = entranceAnim.value
                            translationY = 30f * (1f - entranceAnim.value)
                        },
                    shape = RoundedCornerShape(36.dp),
                    color = Color.White
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 26.dp, vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Welcome to Expensee",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E1E1E),
                            fontSize = 21.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Take control of your money",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF787672),
                            fontSize = 14.5.sp
                        )
                        Spacer(modifier = Modifier.height(28.dp))

                        // Google Sign-In Button
                        Surface(
                            onClick = onSignInWithGoogle,
                            shape = RoundedCornerShape(27.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFD6D4D0)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .buttonTactilePress()
                                .testTag("google_sign_in_button")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_google_g),
                                    contentDescription = "Google Logo",
                                    modifier = Modifier.size(22.dp),
                                    tint = Color.Unspecified
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Sign in with Google",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF232220),
                                    fontSize = 15.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // "or" Separator
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                thickness = 1.dp,
                                color = Color(0xFFE2E0DC)
                            )
                            Text(
                                text = "or",
                                modifier = Modifier.padding(horizontal = 14.dp),
                                color = Color(0xFF8C8A86),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal
                            )
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                thickness = 1.dp,
                                color = Color(0xFFE2E0DC)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Continue without an account Button
                        Surface(
                            onClick = onContinueWithoutAccount,
                            shape = RoundedCornerShape(27.dp),
                            color = Color(0xFF6B6965),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .buttonTactilePress()
                                .testTag("continue_without_account_button")
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Continue without an account",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(34.dp))

            // Footer Tagline & Underline Accent
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { alpha = entranceAnim.value }
            ) {
                Text(
                    text = "Better spending. A brighter you.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF7E7C77),
                    fontSize = 13.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .width(42.dp)
                        .height(1.5.dp)
                        .background(Color(0xFFB5B3AF), shape = RoundedCornerShape(1.dp))
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

/**
 * Step 1: Profile & Currency Setup
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IncomeAndCurrencyStep(
    name: String,
    onNameChange: (String) -> Unit,
    googleEmail: String?,
    googleName: String?,
    onDisconnectGoogle: () -> Unit,
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
            text = "Set Up Your Profile",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Personalize your display name, currency, and regular monthly figures.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
        )

        // If Google account is connected, show account banner
        if (!googleEmail.isNullOrBlank()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp),
                shape = RoundedCornerShape(16.dp),
                color = AccentEmerald.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, AccentEmerald.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AccentEmerald.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = AccentEmerald,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = googleName ?: "Google Account Connected",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$googleEmail • Cloud sync active",
                                style = MaterialTheme.typography.bodySmall,
                                color = AccentEmerald
                            )
                        }
                    }

                    TextButton(onClick = onDisconnectGoogle) {
                        Text(
                            text = "Unlink",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        // Name input
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "What should we call you? (Optional)",
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
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) BrandGraphite else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = opt.code,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) BrandGraphite else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Monthly Income Input
        Text(
            text = "Monthly Regular Income",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(6.dp))
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
                    color = BrandGraphite,
                    modifier = Modifier.padding(start = 12.dp)
                )
            },
            placeholder = { Text("5000") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandGraphite,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("onboarding_income_input")
        )

        // Seed initial income transaction checkbox
        if ((income.toDoubleOrNull() ?: 0.0) > 0) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSeedIncomeChange(!seedIncome) }
                    .padding(vertical = 4.dp),
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

/**
 * Step 2: Preferences & Theme Step
 */
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
                    Icon(
                        imageVector = Icons.Default.WavingHand,
                        contentDescription = null,
                        tint = BrandGraphite,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Live Dashboard Greeting Preview",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = BrandGraphite
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = greetingText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitleText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Theme Selection Options
        Text(
            text = "Color Theme & Appearance",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))

        ThemeOptionTile(
            title = "System Default",
            subtitle = "Follows your device dark mode and dynamic system theme",
            icon = Icons.Default.BrightnessAuto,
            isSelected = selectedThemeMode == "SYSTEM",
            onClick = { onSelectTheme("SYSTEM") },
            testTag = "theme_option_system"
        )

        Spacer(modifier = Modifier.height(10.dp))

        ThemeOptionTile(
            title = "Light Mode",
            subtitle = "Clean high-contrast daytime interface with crisp graphite accents",
            icon = Icons.Default.LightMode,
            isSelected = selectedThemeMode == "LIGHT",
            onClick = { onSelectTheme("LIGHT") },
            testTag = "theme_option_light"
        )

        Spacer(modifier = Modifier.height(10.dp))

        ThemeOptionTile(
            title = "Dark Mode",
            subtitle = "Eye-friendly charcoal palette optimized for low light environments",
            icon = Icons.Default.DarkMode,
            isSelected = selectedThemeMode == "DARK",
            onClick = { onSelectTheme("DARK") },
            testTag = "theme_option_dark"
        )
    }
}

@Composable
private fun ThemeOptionTile(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) BrandGraphite.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) BrandGraphite else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .buttonTactilePress()
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) BrandGraphite else MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
