package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Animated Apple iOS Liquid Glass Backdrop:
 * - Fluid floating ambient orbs in vibrant iOS system tints.
 * - Dynamic specular chromatic sheen sweeping across glass surfaces.
 */
@Composable
fun LiquidGlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "LiquidGlassSheen")

    // Orb 1 floating motion
    val orb1Progress = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Orb1Motion"
    )

    // Orb 2 floating motion
    val orb2Progress = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(11000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Orb2Motion"
    )

    // Specular light sweep offset
    val sheenOffset = infiniteTransition.animateFloat(
        initialValue = -0.3f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SheenSweep"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B1120))
    ) {
        // Floating Ambient Light Orbs Behind Glass (100% Draw-Phase GPU evaluation)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val p1 = orb1Progress.value
            val p2 = orb2Progress.value
            val sheen = sheenOffset.value

            // Cyan/Blue iOS Orb
            val o1x = w * 0.35f + (cos(p1) * (w * 0.22f))
            val o1y = h * 0.32f + (sin(p1) * (h * 0.16f))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x70007AFF), Color(0x30007AFF), Color.Transparent),
                    center = Offset(o1x, o1y),
                    radius = w * 0.55f
                ),
                radius = w * 0.55f,
                center = Offset(o1x, o1y)
            )

            // Coral/Violet iOS Orb
            val o2x = w * 0.68f + (cos(p2 + 2f) * (w * 0.20f))
            val o2y = h * 0.65f + (sin(p2 + 2f) * (h * 0.18f))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x65FF2D55), Color(0x25AF52DE), Color.Transparent),
                    center = Offset(o2x, o2y),
                    radius = w * 0.60f
                ),
                radius = w * 0.60f,
                center = Offset(o2x, o2y)
            )

            // Emerald Accent Orb
            val o3x = w * 0.25f + (sin(p1) * (w * 0.15f))
            val o3y = h * 0.82f + (cos(p1) * (h * 0.12f))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x4534C759), Color.Transparent),
                    center = Offset(o3x, o3y),
                    radius = w * 0.40f
                ),
                radius = w * 0.40f,
                center = Offset(o3x, o3y)
            )

            // Dynamic Chromatic Sheen Sweep
            val sheenX = w * sheen
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0x15FFFFFF),
                        Color(0x30FFFFFF),
                        Color(0x15FFFFFF),
                        Color.Transparent
                    ),
                    start = Offset(sheenX - 180f, 0f),
                    end = Offset(sheenX + 180f, h)
                )
            )
        }

        content()
    }
}

/**
 * Animated Nothing OS Backdrop:
 * - Iconic Nothing Glyph LED strips that breathe and pulse.
 * - Iconic signature red recording dot with an analog breathing rhythm.
 * - Dot-matrix background alignment.
 */
@Composable
fun NothingGlyphBackground(
    isDark: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "NothingGlyphEffects")

    // Breathing pulse for the signature red dot
    val redDotAlpha = infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "RedDotBreathing"
    )

    // Subtle breathing pulse for Glyph LED lines
    val glyphGlowAlpha = infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlyphGlowPulse"
    )

    val bgColor = if (isDark) Color(0xFF000000) else Color(0xFFF3F3F3)
    val gridDotColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.04f)
    val topGlyphPath = androidx.compose.runtime.remember { Path() }
    val bottomGlyphPath = androidx.compose.runtime.remember { Path() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // Minimalist Background Dot Matrix Grid & Glyph Strips (100% Draw-Phase GPU evaluation)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 44f
            var x = 22f
            while (x < size.width) {
                var y = 22f
                while (y < size.height) {
                    drawCircle(
                        color = gridDotColor,
                        radius = 1.5f,
                        center = Offset(x, y)
                    )
                    y += step
                }
                x += step
            }

            val w = size.width
            val h = size.height
            val glyphAlpha = glyphGlowAlpha.value
            val glyphColor = if (isDark) Color.White.copy(alpha = glyphAlpha) else Color(0xFF1E1E1E).copy(alpha = glyphAlpha)

            // Top-Right Curved Glyph Strip
            topGlyphPath.reset()
            topGlyphPath.moveTo(w * 0.72f, h * 0.06f)
            topGlyphPath.lineTo(w * 0.90f, h * 0.06f)
            topGlyphPath.arcTo(
                rect = androidx.compose.ui.geometry.Rect(w * 0.82f, h * 0.06f, w * 0.94f, h * 0.16f),
                startAngleDegrees = -90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            topGlyphPath.lineTo(w * 0.94f, h * 0.22f)
            drawPath(
                path = topGlyphPath,
                color = glyphColor,
                style = Stroke(width = 3.5f, cap = StrokeCap.Round)
            )

            // Diagonal Center Slash Glyph
            drawLine(
                color = glyphColor,
                start = Offset(w * 0.88f, h * 0.26f),
                end = Offset(w * 0.94f, h * 0.32f),
                strokeWidth = 3.5f,
                cap = StrokeCap.Round
            )

            // Bottom Loop Glyph
            bottomGlyphPath.reset()
            bottomGlyphPath.moveTo(w * 0.06f, h * 0.78f)
            bottomGlyphPath.lineTo(w * 0.06f, h * 0.92f)
            bottomGlyphPath.arcTo(
                rect = androidx.compose.ui.geometry.Rect(w * 0.06f, h * 0.84f, w * 0.18f, h * 0.94f),
                startAngleDegrees = 180f,
                sweepAngleDegrees = -90f,
                forceMoveTo = false
            )
            bottomGlyphPath.lineTo(w * 0.28f, h * 0.94f)
            drawPath(
                path = bottomGlyphPath,
                color = glyphColor,
                style = Stroke(width = 3.5f, cap = StrokeCap.Round)
            )

            // Signature Breathing Red Recording Dot (Nothing Hardware Detail)
            drawCircle(
                color = Color(0xFFD71920).copy(alpha = redDotAlpha.value),
                radius = 4.5.dp.toPx(),
                center = Offset(w - 24.dp.toPx(), 24.dp.toPx())
            )
        }

        content()
    }
}
