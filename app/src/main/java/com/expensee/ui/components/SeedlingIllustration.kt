package com.expensee.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun SeedlingIllustration(
    modifier: Modifier = Modifier,
    isDark: Boolean = false
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 1. Soft organic ambient curved wash in the background corner
        val washPath = Path().apply {
            moveTo(w * 0.15f, h)
            cubicTo(
                w * 0.30f, h * 0.65f,
                w * 0.45f, h * 0.20f,
                w, h * 0.05f
            )
            lineTo(w, h)
            close()
        }

        val washBrush = if (isDark) {
            Brush.radialGradient(
                colors = listOf(
                    Color(0xFF1B3828).copy(alpha = 0.75f),
                    Color(0xFF162E21).copy(alpha = 0.35f),
                    Color.Transparent
                ),
                center = Offset(w * 0.8f, h * 0.55f),
                radius = w * 0.75f
            )
        } else {
            Brush.radialGradient(
                colors = listOf(
                    Color(0xFFE2EFE5).copy(alpha = 0.90f),
                    Color(0xFFEBF5EE).copy(alpha = 0.45f),
                    Color.Transparent
                ),
                center = Offset(w * 0.8f, h * 0.55f),
                radius = w * 0.75f
            )
        }

        drawPath(path = washPath, brush = washBrush)

        // 2. Seedling Stem
        val stemBaseX = w * 0.68f
        val stemBaseY = h * 0.88f
        val stemMidX = w * 0.67f
        val stemMidY = h * 0.52f
        val stemTopX = w * 0.65f
        val stemTopY = h * 0.40f

        val stemPath = Path().apply {
            moveTo(stemBaseX, stemBaseY)
            quadraticTo(stemMidX, stemMidY, stemTopX, stemTopY)
        }

        val stemBrush = Brush.verticalGradient(
            colors = if (isDark) listOf(Color(0xFF81C784), Color(0xFF2E7D32))
                     else listOf(Color(0xFF4CAF50), Color(0xFF1B5E20)),
            startY = stemTopY,
            endY = stemBaseY
        )

        drawPath(
            path = stemPath,
            brush = stemBrush,
            style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
        )

        // 3. Left Leaf (curves leftwards and upwards from stem tip)
        val leftLeafPath = Path().apply {
            moveTo(stemTopX, stemTopY + 2.dp.toPx())
            cubicTo(
                stemTopX - w * 0.20f, stemTopY + h * 0.04f,
                stemTopX - w * 0.34f, stemTopY - h * 0.12f,
                stemTopX - w * 0.26f, stemTopY - h * 0.18f
            )
            cubicTo(
                stemTopX - w * 0.14f, stemTopY - h * 0.20f,
                stemTopX - w * 0.04f, stemTopY - h * 0.07f,
                stemTopX, stemTopY + 2.dp.toPx()
            )
            close()
        }

        val leftLeafBrush = Brush.linearGradient(
            colors = if (isDark) listOf(Color(0xFF81C784), Color(0xFF2E7D32))
                     else listOf(Color(0xFF66BB6A), Color(0xFF2E7D32)),
            start = Offset(stemTopX - w * 0.28f, stemTopY - h * 0.20f),
            end = Offset(stemTopX, stemTopY)
        )
        drawPath(path = leftLeafPath, brush = leftLeafBrush)

        // 4. Right Leaf (slightly larger, curves rightwards and upwards)
        val rightLeafPath = Path().apply {
            moveTo(stemTopX, stemTopY)
            cubicTo(
                stemTopX + w * 0.06f, stemTopY - h * 0.04f,
                stemTopX + w * 0.30f, stemTopY - h * 0.18f,
                stemTopX + w * 0.26f, stemTopY - h * 0.32f
            )
            cubicTo(
                stemTopX + w * 0.16f, stemTopY - h * 0.30f,
                stemTopX + w * 0.02f, stemTopY - h * 0.16f,
                stemTopX, stemTopY
            )
            close()
        }

        val rightLeafBrush = Brush.linearGradient(
            colors = if (isDark) listOf(Color(0xFFA5D6A7), Color(0xFF388E3C))
                     else listOf(Color(0xFF81C784), Color(0xFF2E7D32)),
            start = Offset(stemTopX + w * 0.28f, stemTopY - h * 0.32f),
            end = Offset(stemTopX, stemTopY)
        )
        drawPath(path = rightLeafPath, brush = rightLeafBrush)
    }
}
