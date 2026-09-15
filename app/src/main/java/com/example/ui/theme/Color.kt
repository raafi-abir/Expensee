package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

// Theme color definitions

data class AppColors(
    val isDark: Boolean,
    val pageBg: Color,
    val cardBg: Color,
    val cardBorder: Color,
    val textDark: Color,
    val textSubtle: Color,
    val textLight: Color,
    val segmentTrack: Color,
    val surfaceElevated: Color,
    val chipBg: Color,
    val inputBg: Color,
    val incomeBlueBg: Color,
    val expenseRedBg: Color,
    val goalOrangeBg: Color,
    val activeNavTint: Color,
    val inactiveNavTint: Color
)

val LightAppColors = AppColors(
    isDark = false,
    pageBg = Color(0xFFF9FAFB),
    cardBg = Color(0xFFFFFFFF),
    cardBorder = Color(0xFFE5E7EB),
    textDark = Color(0xFF111827),
    textSubtle = Color(0xFF6B7280),
    textLight = Color(0xFF9CA3AF),
    segmentTrack = Color(0xFFE5E7EB),
    surfaceElevated = Color(0xFFFFFFFF),
    chipBg = Color(0xFFF3F4F6),
    inputBg = Color(0xFFF9FAFB),
    incomeBlueBg = Color(0xFFE0F2FE),
    expenseRedBg = Color(0xFFFEE2E2),
    goalOrangeBg = Color(0xFFFFEDD5),
    activeNavTint = Color(0xFF838383),
    inactiveNavTint = Color(0xFF9CA3AF)
)

val DarkAppColors = AppColors(
    isDark = true,
    pageBg = Color(0xFF121212),
    cardBg = Color(0xFF1E1E1E),
    cardBorder = Color(0xFF2E2E2E),
    textDark = Color(0xFFF3F4F6),
    textSubtle = Color(0xFFA3A3A3),
    textLight = Color(0xFF737373),
    segmentTrack = Color(0xFF262626),
    surfaceElevated = Color(0xFF262626),
    chipBg = Color(0xFF262626),
    inputBg = Color(0xFF181818),
    incomeBlueBg = Color(0xFF0C273D),
    expenseRedBg = Color(0xFF3B1219),
    goalOrangeBg = Color(0xFF381D0E),
    activeNavTint = Color(0xFFB5B5B5),
    inactiveNavTint = Color(0xFF737373)
)

val LocalAppColors = compositionLocalOf { LightAppColors }

object AppTheme {
    val colors: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current
}

// Frosted palette

// Canvas and Backgrounds
val FrostedBackground = Color(0xFFF7F2FA)
val FrostedNavBackground = Color(0xFFF3EDF7)
val FrostedSurface = Color(0xFFFFFFFF)
val FrostedSurfaceTranslucent = Color(0xF2FFFFFF)
val FrostedSurfaceVariant = Color(0xFFE8DEF8)

// Typography & Content
val FrostedTextPrimary = Color(0xFF1D1B20)
val FrostedTextSecondary = Color(0xFF49454F)
val FrostedTextMuted = Color(0xFF79747E)

// Borders & Glass Accents
val FrostedBorder = Color(0x4DCAC4D0) // border-[#CAC4D0]/30
val FrostedBorderSolid = Color(0xFFCAC4D0)
val FrostedGlassWhite = Color(0x4DFFFFFF) // bg-white/30 backdrop
val FrostedGlassWhiteBorder = Color(0x66FFFFFF) // border-white/40
val FrostedGlassHighBorder = Color(0x99FFFFFF) // border-white/60

// Brand colors
val BrandPrimary = Color(0xFF838383)
val BrandPrimaryDark = Color(0xFF5E5E5E)
val BrandPrimaryLight = Color(0xFF9E9E9E)
val BrandPrimaryDarkVariant = Color(0xFFB5B5B5) // Tone 80 of #838383 family for dark mode high contrast
val BrandPrimaryContainerLight = Color(0xFFE8E8E8)
val BrandPrimaryContainerDark = Color(0xFF383838)
val BrandPrimaryOnContainerLight = Color(0xFF1E1E1E)
val BrandPrimaryOnContainerDark = Color(0xFFE5E5E5)

// Dashboard Structural Colors (Explicitly preserved as Ash / Charcoal)
val DashboardAshHeader = Color(0xFF414141)
val DashboardDarkAshHeader = Color(0xFF262626)

// Brand alias mapped to the primary brand color (#838383)
val BrandGraphite = BrandPrimary
val BrandGraphiteDark = BrandPrimaryDark
val BrandGraphiteLight = BrandPrimaryLight
val BrandGraphiteContainerLight = BrandPrimaryContainerLight
val BrandGraphiteContainerDark = BrandPrimaryContainerDark

// Legacy aliases mapped to clean primary brand
val FrostedBrandViolet = BrandGraphiteDark
val FrostedPurplePrimary = BrandPrimary
val FrostedPurpleContainer = BrandPrimaryContainerLight
val FrostedPurplePill = Color(0xFFE5E7EB)
val FrostedPurpleActive = BrandPrimaryLight

// Hero & Surface Accents
val FrostedGradientStart = BrandPrimary
val FrostedGradientMid = Color(0xFF6E6E6E)
val FrostedGradientEnd = Color(0xFF9E9E9E)

// Financial Semantic Badges & Containers
val FrostedExpenseContainer = Color(0xFFFFDAD6)
val FrostedExpenseOnContainer = Color(0xFF410002)
val FrostedExpenseAmount = Color(0xFF1D1B20)
val ExpenseRed = Color(0xFFBA1A1A)
val ExpenseRedLight = Color(0xFFFFDAD6)
val ExpenseRedContainer = Color(0xFFFFDAD6)

val FrostedIncomeContainer = Color(0xFFD3E3FD)
val FrostedIncomeOnContainer = Color(0xFF041E49)
val FrostedIncomeAmount = Color(0xFF146C2E)
val IncomeGreen = Color(0xFF146C2E)
val IncomeGreenLight = Color(0xFFD3E3FD)
val IncomeGreenContainer = Color(0xFFD1FAE5)

val WarningAmber = Color(0xFFB54708)
val WarningAmberContainer = Color(0xFFFEF0C7)

// Dark Mode Neutral Glass / Surface Variant
val FrostedDarkBackground = Color(0xFF121212)
val FrostedDarkSurface = Color(0xFF1E1E1E)
val FrostedDarkSurfaceElevated = Color(0xFF262626)
val FrostedDarkSurfaceHigher = Color(0xFF2E2E2E)
val FrostedDarkBorder = Color(0xFF2E2E2E)
val FrostedDarkTextPrimary = Color(0xFFF3F4F6)
val FrostedDarkTextSecondary = Color(0xFFA3A3A3)
val FrostedDarkTextMuted = Color(0xFF737373)

// Component and dynamic palette getters
val ModernPurpleGradientStart = BrandGraphite
val ModernPurpleGradientMid = Color(0xFF525252)
val ModernPurpleGradientEnd = Color(0xFF666666)

val ModernPageBg: Color
    @Composable
    @ReadOnlyComposable
    get() = AppTheme.colors.pageBg

val ModernCardBg: Color
    @Composable
    @ReadOnlyComposable
    get() = AppTheme.colors.cardBg

val ModernCardBorder: Color
    @Composable
    @ReadOnlyComposable
    get() = AppTheme.colors.cardBorder

val ModernTextDark: Color
    @Composable
    @ReadOnlyComposable
    get() = AppTheme.colors.textDark

val ModernTextSubtle: Color
    @Composable
    @ReadOnlyComposable
    get() = AppTheme.colors.textSubtle

val ModernTextLight: Color
    @Composable
    @ReadOnlyComposable
    get() = AppTheme.colors.textLight

val ModernIncomeBlue = Color(0xFF0284C7)

val ModernIncomeBlueBg: Color
    @Composable
    @ReadOnlyComposable
    get() = AppTheme.colors.incomeBlueBg

val ModernExpenseRed = Color(0xFFEF4444)

val ModernExpenseRedBg: Color
    @Composable
    @ReadOnlyComposable
    get() = AppTheme.colors.expenseRedBg

val ModernGoalOrange = Color(0xFFF97316)

val ModernGoalOrangeBg: Color
    @Composable
    @ReadOnlyComposable
    get() = AppTheme.colors.goalOrangeBg

val ModernAlertOrange = Color(0xFFFF5722)
val ModernDarkInsightBg = Color(0xFF222222)

val ModernSegmentTrack: Color
    @Composable
    @ReadOnlyComposable
    get() = AppTheme.colors.segmentTrack

// Backward compatibility aliases
val AccentEmerald = FrostedPurplePrimary
val AccentDarkEmerald = FrostedBrandViolet
val AccentTeal = FrostedGradientEnd
val AccentBlue = FrostedPurplePrimary
val AccentPurple = FrostedPurplePrimary

val DarkBackground = FrostedDarkBackground
val DarkSurface = FrostedDarkSurface
val DarkSurfaceElevated = FrostedDarkSurfaceElevated
val DarkSurfaceHigher = FrostedDarkSurfaceHigher
val DarkBorder = FrostedDarkBorder
val DarkTextPrimary = FrostedDarkTextPrimary
val DarkTextSecondary = FrostedDarkTextSecondary
val DarkTextMuted = FrostedDarkTextMuted

val LightBackground = FrostedBackground
val LightSurface = FrostedSurface
val LightSurfaceElevated = FrostedSurfaceVariant
val LightSurfaceHigher = FrostedPurplePill
val LightBorder = FrostedBorderSolid
val LightTextPrimary = FrostedTextPrimary
val LightTextSecondary = FrostedTextSecondary
val LightTextMuted = FrostedTextMuted


