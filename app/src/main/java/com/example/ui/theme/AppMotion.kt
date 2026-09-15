package com.example.ui.theme

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Shared motion specifications and spring configurations.
 */
object AppMotion {
    // Tactile press & release feedback
    val TouchSpring: AnimationSpec<Float> = spring(
        dampingRatio = 0.74f,
        stiffness = Spring.StiffnessMedium
    )

    // Card tactile press: very subtle, physical (0.985x)
    val CardPressSpring: AnimationSpec<Float> = spring(
        dampingRatio = 0.76f,
        stiffness = Spring.StiffnessMedium
    )

    // Button tactile press: slightly more defined (0.96x) with fast recovery
    val ButtonPressSpring: AnimationSpec<Float> = spring(
        dampingRatio = 0.72f,
        stiffness = Spring.StiffnessMedium
    )

    // Center FAB tactile press (0.92x)
    val FabPressSpring: AnimationSpec<Float> = spring(
        dampingRatio = 0.68f,
        stiffness = Spring.StiffnessMediumLow
    )

    // Tab switching and segmented control indicator glide
    val IndicatorSpring: AnimationSpec<Float> = spring(
        dampingRatio = 0.82f,
        stiffness = Spring.StiffnessMediumLow
    )

    // Smooth value changes (balances, counters)
    val ValueSpring: AnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessLow
    )

    // Smooth progress interpolation (budget bars, goal progress)
    val ProgressSpring: AnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessLow
    )

    // Quick micro-transitions
    val QuickTween = tween<Float>(durationMillis = 180, easing = FastOutSlowInEasing)

    // Spatial horizontal screen slide with natural deceleration
    val ScreenSlideSpring: FiniteAnimationSpec<IntOffset> = spring(
        dampingRatio = 0.86f,
        stiffness = 420f
    )

    // Spatial subtle scale spring for scene-like transitions
    val ScreenScaleSpring: FiniteAnimationSpec<Float> = spring(
        dampingRatio = 0.88f,
        stiffness = 400f
    )

    // Coordinated screen fade
    val ScreenFadeTween = tween<Float>(durationMillis = 220, easing = FastOutSlowInEasing)
}

/**
 * Tactile press scale response for interactive elements.
 */
fun Modifier.tactilePress(
    pressedScale: Float = 0.985f,
    interactionSource: MutableInteractionSource? = null
): Modifier = composed {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val isPressed by source.collectIsPressedAsState()
    val scaleState = animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = AppMotion.TouchSpring,
        label = "tactileScale"
    )

    this.graphicsLayer {
        scaleX = scaleState.value
        scaleY = scaleState.value
    }
}

/**
 * Card-specific tactile press scale response.
 */
fun Modifier.cardTactilePress(
    pressedScale: Float = 0.985f,
    interactionSource: MutableInteractionSource? = null
): Modifier = composed {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val isPressed by source.collectIsPressedAsState()
    val scaleState = animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = AppMotion.CardPressSpring,
        label = "cardTactileScale"
    )

    this.graphicsLayer {
        scaleX = scaleState.value
        scaleY = scaleState.value
    }
}

/**
 * Button-specific tactile press scale response.
 */
fun Modifier.buttonTactilePress(
    pressedScale: Float = 0.96f,
    interactionSource: MutableInteractionSource? = null
): Modifier = composed {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val isPressed by source.collectIsPressedAsState()
    val scaleState = animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = AppMotion.ButtonPressSpring,
        label = "btnTactileScale"
    )

    this.graphicsLayer {
        scaleX = scaleState.value
        scaleY = scaleState.value
    }
}

/**
 * Floating action button tactile press scale response.
 */
fun Modifier.fabTactilePress(
    pressedScale: Float = 0.92f,
    interactionSource: MutableInteractionSource? = null
): Modifier = composed {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val isPressed by source.collectIsPressedAsState()
    val scaleState = animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = AppMotion.FabPressSpring,
        label = "fabTactileScale"
    )

    this.graphicsLayer {
        scaleX = scaleState.value
        scaleY = scaleState.value
    }
}

/**
 * Animated financial amount display using vertical slide and opacity transitions.
 */
@Composable
fun AnimatedFinancialAmount(
    amountText: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.CenterStart
) {
    Box(
        modifier = modifier.clipToBounds(),
        contentAlignment = contentAlignment
    ) {
        AnimatedContent(
            targetState = amountText,
            transitionSpec = {
                val slideEnter = slideInVertically(
                    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
                ) { height -> height / 5 }
                val fadeEnter = fadeIn(
                    animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)
                )
                val slideExit = slideOutVertically(
                    animationSpec = tween(durationMillis = 160, easing = FastOutSlowInEasing)
                ) { height -> -height / 5 }
                val fadeExit = fadeOut(
                    animationSpec = tween(durationMillis = 140, easing = FastOutSlowInEasing)
                )

                (slideEnter + fadeEnter).togetherWith(slideExit + fadeExit)
            },
            label = "financial_amount_motion"
        ) { targetAmount ->
            Text(
                text = targetAmount,
                style = style,
                color = color
            )
        }
    }
}

/**
 * Date and time formatting helpers with internal caching for list presentation.
 */
object AppDateFormatters {
    private val timeFormat = SimpleDateFormat("MMM d, hh:mm a", Locale.getDefault())
    private val dayFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    private val headerFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
    private val fullDateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())

    private var cachedDayOfYear = -1
    private var cachedYear = -1
    private var cachedTodayStr = ""
    private var cachedYesterdayStr = ""
    private val dayGroupCache = LinkedHashMap<Long, String>(64, 0.75f, true)

    @Synchronized
    private fun ensureDayCache() {
        val nowCal = Calendar.getInstance()
        val currentDay = nowCal.get(Calendar.DAY_OF_YEAR)
        val currentYear = nowCal.get(Calendar.YEAR)
        if (currentDay != cachedDayOfYear || currentYear != cachedYear) {
            cachedDayOfYear = currentDay
            cachedYear = currentYear
            cachedTodayStr = headerFormat.format(nowCal.time)
            nowCal.add(Calendar.DAY_OF_YEAR, -1)
            cachedYesterdayStr = headerFormat.format(nowCal.time)
            dayGroupCache.clear()
        }
    }

    @Synchronized
    fun formatDateTime(timestamp: Long): String {
        return timeFormat.format(Date(timestamp))
    }

    @Synchronized
    fun formatDate(timestamp: Long): String {
        return dayFormat.format(Date(timestamp))
    }

    @Synchronized
    fun formatShortDate(timestamp: Long): String {
        return dayFormat.format(Date(timestamp))
    }

    @Synchronized
    fun formatFullDate(timestamp: Long): String {
        return fullDateFormat.format(Date(timestamp))
    }

    @Synchronized
    fun formatHeader(timestamp: Long): String {
        return headerFormat.format(Date(timestamp))
    }

    @Synchronized
    fun formatDayGroup(timestamp: Long): String {
        ensureDayCache()
        val cached = dayGroupCache[timestamp]
        if (cached != null) return cached

        val dateStr = headerFormat.format(Date(timestamp))
        val result = when (dateStr) {
            cachedTodayStr -> "Today, $dateStr"
            cachedYesterdayStr -> "Yesterday, $dateStr"
            else -> dateStr
        }
        if (dayGroupCache.size > 128) {
            val oldest = dayGroupCache.keys.firstOrNull()
            if (oldest != null) dayGroupCache.remove(oldest)
        }
        dayGroupCache[timestamp] = result
        return result
    }
}
