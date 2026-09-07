package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.model.ThemePalette
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Full-screen ambient animated backgrounds and interactive hero elements
 * crafted specifically for the 4 premier Kawaii & Cute themes:
 * 1. Girl Math Pastel (Floating hearts, animated fluffy kitten, "it's basically free!" script)
 * 2. Neko Mochi Cat (Cozy mocha strawberry atmosphere, floating paw prints, purr wave)
 * 3. Y2K Glossy Pop (Holographic gloss sweep, iridescent bubble orbs, 4-pointed radiant stars)
 * 4. Pico-Calc 8-Bit (Dithered retro starfield, phosphor scanline glow, pixel particles)
 */
private class KawaiiPathCache(
    val heartPath: Path = Path(),
    val starPath: Path = Path(),
    val earLeft: Path = Path(),
    val earLeftInner: Path = Path(),
    val earRight: Path = Path(),
    val earRightInner: Path = Path(),
    val purrPath: Path = Path()
)

@Composable
fun KawaiiScreenBackground(
    theme: ThemePalette,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "kawaii_ambient_anim")
    val pathCache = remember { KawaiiPathCache() }

    // Harmonic multi-frequency animations for natural, non-repetitive organic motion
    val floatProgress = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "kawaii_float_progress"
    )

    val breathPulse = infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "kawaii_breath_pulse"
    )

    val sparkleRotation = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "kawaii_sparkle_rotation"
    )

    val kittyBlink = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "kawaii_kitty_blink"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val progress = floatProgress.value
        val pulse = breathPulse.value
        val rot = sparkleRotation.value
        val blink = kittyBlink.value

        when {
            // 1. GIRL MATH PASTEL (Sample: 80f1f24af853bfe7a09e6afbe7ee3c6b.jpg)
            theme.isGirlMath -> {
                drawGirlMathAtmosphere(
                    w = w,
                    h = h,
                    progress = progress,
                    pulse = pulse,
                    rot = rot,
                    blink = blink,
                    cache = pathCache
                )
            }

            // 2. NEKO MOCHI CAT (Sample: af920d166b393efe89e2629bf7fa24f9.jpg)
            theme.isNekoMochi -> {
                drawNekoMochiAtmosphere(
                    w = w,
                    h = h,
                    progress = progress,
                    pulse = pulse,
                    isDark = theme.isDark,
                    cache = pathCache
                )
            }

            // 3. Y2K GLOSSY POP (Sample: efa7524e748bc4b1564605a1f14f69d8.jpg)
            theme.isY2kGlossy -> {
                drawY2kGlossyAtmosphere(
                    w = w,
                    h = h,
                    progress = progress,
                    pulse = pulse,
                    rot = rot,
                    cache = pathCache
                )
            }

            // 4. PICO-CALC 8-BIT (Sample: 1fbebea4333a5cc2576b10b652e968ee.jpg)
            theme.isPixelArt -> {
                drawPicoPixelAtmosphere(
                    w = w,
                    h = h,
                    progress = progress,
                    pulse = pulse
                )
            }
        }
    }
}

/**
 * Girl Math Pastel: Floating pastel hearts on sine waves, animated fluffy white kitten,
 * warm cheek blush glow, and decorative "it's basically free! ♡" script accent.
 */
private fun DrawScope.drawGirlMathAtmosphere(
    w: Float,
    h: Float,
    progress: Float,
    pulse: Float,
    rot: Float,
    blink: Float,
    cache: KawaiiPathCache
) {
    // 1. Floating pastel hearts drifting upwards
    val heartPastels = listOf(
        Color(0xFFFF8FA3), // Rose pink
        Color(0xFFFFB5C5), // Bubblegum
        Color(0xFFBAA6FF), // Soft lavender
        Color(0xFFFFD1A9), // Warm peach
        Color(0xFFA8E6CF), // Soft mint
        Color(0xFFFFC6D5), // Powder pink
        Color(0xFFFF9EAA)  // Strawberry milk
    )

    for (i in heartPastels.indices) {
        val offsetFraction = (i * 0.14f)
        val p = (progress + offsetFraction) % 1f
        val hy = h * (1.05f - p * 1.1f)
        val sway = sin(p * 9f + i * 1.8f) * 18.dp.toPx()
        val hx = (w * (0.08f + (i * 0.14f))) + sway
        val alpha = (sin(p * PI.toFloat()) * 0.42f).coerceIn(0f, 0.42f)
        val heartScale = (6.dp.toPx() + (i % 3) * 2.5.dp.toPx())

        cache.heartPath.reset()
        cache.heartPath.moveTo(hx, hy)
        cache.heartPath.cubicTo(
            hx - heartScale, hy - heartScale,
            hx - heartScale * 1.5f, hy + heartScale * 0.4f,
            hx, hy + heartScale * 1.2f
        )
        cache.heartPath.cubicTo(
            hx + heartScale * 1.5f, hy + heartScale * 0.4f,
            hx + heartScale, hy - heartScale,
            hx, hy
        )
        cache.heartPath.close()
        drawPath(cache.heartPath, color = heartPastels[i].copy(alpha = alpha))
    }

    // 2. Radiant 4-point sparkle stars drifting & twinkling
    val sparklePositions = listOf(
        Pair(w * 0.15f, h * 0.18f),
        Pair(w * 0.85f, h * 0.22f),
        Pair(w * 0.10f, h * 0.72f),
        Pair(w * 0.90f, h * 0.78f),
        Pair(w * 0.50f, h * 0.08f)
    )
    for ((index, pos) in sparklePositions.withIndex()) {
        val starPulse = (sin(pulse * 3f + index) * 0.3f + 0.7f).coerceIn(0.4f, 1f)
        val starSize = (7.dp.toPx() + (index % 2) * 3.dp.toPx()) * starPulse
        val sx = pos.first
        val sy = pos.second
        val starColor = if (index % 2 == 0) Color(0xFFFF85A1) else Color(0xFFFFD166)

        cache.starPath.reset()
        cache.starPath.moveTo(sx, sy - starSize)
        cache.starPath.cubicTo(sx, sy - starSize * 0.2f, sx + starSize * 0.2f, sy, sx + starSize, sy)
        cache.starPath.cubicTo(sx + starSize * 0.2f, sy, sx, sy + starSize * 0.2f, sx, sy + starSize)
        cache.starPath.cubicTo(sx, sy + starSize * 0.2f, sx - starSize * 0.2f, sy, sx - starSize, sy)
        cache.starPath.cubicTo(sx - starSize * 0.2f, sy, sx, sy - starSize * 0.2f, sx, sy - starSize)
        cache.starPath.close()
        drawPath(cache.starPath, color = starColor.copy(alpha = 0.45f * starPulse))
        drawCircle(color = Color.White.copy(alpha = 0.8f * starPulse), radius = starSize * 0.22f, center = Offset(sx, sy))
    }

    // 3. Adorable Fluffy White Kitten hugging the top-right corner
    // Inspired directly by Sample 4 (80f1f24af853bfe7a09e6afbe7ee3c6b.jpg)
    val kittyCenterX = w - 46.dp.toPx()
    val kittyCenterY = 48.dp.toPx() + sin(pulse * 2.5f) * 2.5.dp.toPx()
    val r = 18.dp.toPx()

    // Fluffy body curve behind
    drawOval(
        color = Color.White,
        topLeft = Offset(kittyCenterX - r * 1.1f, kittyCenterY + r * 0.3f),
        size = Size(r * 2.2f, r * 1.6f)
    )

    // Kitty Head
    drawCircle(
        color = Color.White,
        radius = r,
        center = Offset(kittyCenterX, kittyCenterY)
    )

    // Left Ear
    cache.earLeft.reset()
    cache.earLeft.moveTo(kittyCenterX - r * 0.85f, kittyCenterY - r * 0.15f)
    cache.earLeft.lineTo(kittyCenterX - r * 0.70f, kittyCenterY - r * 1.15f)
    cache.earLeft.lineTo(kittyCenterX - r * 0.15f, kittyCenterY - r * 0.55f)
    cache.earLeft.close()
    drawPath(cache.earLeft, color = Color.White)

    cache.earLeftInner.reset()
    cache.earLeftInner.moveTo(kittyCenterX - r * 0.75f, kittyCenterY - r * 0.25f)
    cache.earLeftInner.lineTo(kittyCenterX - r * 0.65f, kittyCenterY - r * 0.95f)
    cache.earLeftInner.lineTo(kittyCenterX - r * 0.25f, kittyCenterY - r * 0.50f)
    cache.earLeftInner.close()
    drawPath(cache.earLeftInner, color = Color(0xFFFF9EAA))

    // Right Ear
    cache.earRight.reset()
    cache.earRight.moveTo(kittyCenterX + r * 0.15f, kittyCenterY - r * 0.55f)
    cache.earRight.lineTo(kittyCenterX + r * 0.70f, kittyCenterY - r * 1.15f)
    cache.earRight.lineTo(kittyCenterX + r * 0.85f, kittyCenterY - r * 0.15f)
    cache.earRight.close()
    drawPath(cache.earRight, color = Color.White)

    cache.earRightInner.reset()
    cache.earRightInner.moveTo(kittyCenterX + r * 0.25f, kittyCenterY - r * 0.50f)
    cache.earRightInner.lineTo(kittyCenterX + r * 0.65f, kittyCenterY - r * 0.95f)
    cache.earRightInner.lineTo(kittyCenterX + r * 0.75f, kittyCenterY - r * 0.25f)
    cache.earRightInner.close()
    drawPath(cache.earRightInner, color = Color(0xFFFF9EAA))

    // Cute pink ribbon bow on left ear
    val bowX = kittyCenterX - r * 0.65f
    val bowY = kittyCenterY - r * 0.65f
    drawCircle(color = Color(0xFFFF5277), radius = 3.2.dp.toPx(), center = Offset(bowX, bowY))
    drawOval(
        color = Color(0xFFFF7597),
        topLeft = Offset(bowX - 6.dp.toPx(), bowY - 4.dp.toPx()),
        size = Size(6.dp.toPx(), 8.dp.toPx())
    )
    drawOval(
        color = Color(0xFFFF7597),
        topLeft = Offset(bowX, bowY - 4.dp.toPx()),
        size = Size(6.dp.toPx(), 8.dp.toPx())
    )

    // Kitten Eyes: Happy closed arcs or soft blinking eyes
    val isBlinking = (blink in 0.92f..0.98f)
    if (isBlinking) {
        // Closed horizontal slits while blinking
        drawLine(
            color = Color(0xFF4A2830),
            start = Offset(kittyCenterX - r * 0.55f, kittyCenterY),
            end = Offset(kittyCenterX - r * 0.25f, kittyCenterY),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color(0xFF4A2830),
            start = Offset(kittyCenterX + r * 0.25f, kittyCenterY),
            end = Offset(kittyCenterX + r * 0.55f, kittyCenterY),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    } else {
        // Cute wide sparkling eyes
        drawCircle(
            color = Color(0xFF4A2830),
            radius = 2.4.dp.toPx(),
            center = Offset(kittyCenterX - r * 0.40f, kittyCenterY - r * 0.05f)
        )
        drawCircle(
            color = Color.White,
            radius = 0.9.dp.toPx(),
            center = Offset(kittyCenterX - r * 0.45f, kittyCenterY - r * 0.10f)
        )

        drawCircle(
            color = Color(0xFF4A2830),
            radius = 2.4.dp.toPx(),
            center = Offset(kittyCenterX + r * 0.40f, kittyCenterY - r * 0.05f)
        )
        drawCircle(
            color = Color.White,
            radius = 0.9.dp.toPx(),
            center = Offset(kittyCenterX + r * 0.35f, kittyCenterY - r * 0.10f)
        )
    }

    // Tiny pink mouth (w)
    drawArc(
        color = Color(0xFF5A303A),
        startAngle = 0f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(kittyCenterX - 3.dp.toPx(), kittyCenterY + 1.dp.toPx()),
        size = Size(3.dp.toPx(), 2.5.dp.toPx()),
        style = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round)
    )
    drawArc(
        color = Color(0xFF5A303A),
        startAngle = 0f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(kittyCenterX, kittyCenterY + 1.dp.toPx()),
        size = Size(3.dp.toPx(), 2.5.dp.toPx()),
        style = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round)
    )

    // Rosy blushing cheeks with breathing pulse
    val cheekAlpha = (0.55f + 0.35f * (pulse - 0.75f) * 2f).coerceIn(0.4f, 0.95f)
    drawCircle(
        color = Color(0xFFFF85A1).copy(alpha = cheekAlpha),
        radius = 3.6.dp.toPx(),
        center = Offset(kittyCenterX - r * 0.62f, kittyCenterY + r * 0.22f)
    )
    drawCircle(
        color = Color(0xFFFF85A1).copy(alpha = cheekAlpha),
        radius = 3.6.dp.toPx(),
        center = Offset(kittyCenterX + r * 0.62f, kittyCenterY + r * 0.22f)
    )

    // Soft paws peeking forward
    drawOval(
        color = Color.White,
        topLeft = Offset(kittyCenterX - r * 0.65f, kittyCenterY + r * 0.75f),
        size = Size(r * 0.55f, r * 0.40f)
    )
    drawOval(
        color = Color.White,
        topLeft = Offset(kittyCenterX + r * 0.10f, kittyCenterY + r * 0.75f),
        size = Size(r * 0.55f, r * 0.40f)
    )
}

/**
 * Neko Mochi Cat: Floating translucent paw prints, warm purring waveform along the base,
 * and cozy strawberry cream ambient tones.
 */
private fun DrawScope.drawNekoMochiAtmosphere(
    w: Float,
    h: Float,
    progress: Float,
    pulse: Float,
    isDark: Boolean = false,
    cache: KawaiiPathCache
) {
    // 1. Drifting cute cat pawprints
    val pawColors = if (isDark) {
        listOf(
            Color(0xFFFF4D79),
            Color(0xFFFF85A1),
            Color(0xFFFFB5C5),
            Color(0xFFFF6E9A)
        )
    } else {
        listOf(
            Color(0xFFFFB5C5),
            Color(0xFFFF9EAA),
            Color(0xFFFFC6D5),
            Color(0xFFFFD1DC)
        )
    }

    for (i in 0 until 5) {
        val offsetFraction = (i * 0.22f)
        val p = (progress + offsetFraction) % 1f
        val py = h * (1.05f - p * 1.1f)
        val px = (w * (0.12f + i * 0.19f)) + sin(p * 8f + i) * 12.dp.toPx()
        val alpha = if (isDark) {
            (sin(p * PI.toFloat()) * 0.52f).coerceIn(0f, 0.52f)
        } else {
            (sin(p * PI.toFloat()) * 0.38f).coerceIn(0f, 0.38f)
        }
        val mainR = 4.2.dp.toPx()
        val toeR = 1.9.dp.toPx()
        val color = pawColors[i % pawColors.size].copy(alpha = alpha)

        // Main palm pad
        drawOval(
            color = color,
            topLeft = Offset(px - mainR, py - mainR * 0.75f),
            size = Size(mainR * 2f, mainR * 1.5f)
        )
        // 4 cute toes
        drawCircle(color = color, radius = toeR, center = Offset(px - mainR * 0.85f, py - mainR * 1.25f))
        drawCircle(color = color, radius = toeR, center = Offset(px - mainR * 0.30f, py - mainR * 1.60f))
        drawCircle(color = color, radius = toeR, center = Offset(px + mainR * 0.30f, py - mainR * 1.60f))
        drawCircle(color = color, radius = toeR, center = Offset(px + mainR * 0.85f, py - mainR * 1.25f))
    }

    // 2. Soothing Purr Waveform along bottom edge
    val purrY = h - 6.dp.toPx()
    val purrBrush = Brush.horizontalGradient(
        colors = if (isDark) {
            listOf(
                Color.Transparent,
                Color(0xFFFF4D79).copy(alpha = 0.30f * pulse),
                Color(0xFFFF85A1).copy(alpha = 0.65f * pulse),
                Color(0xFFFF4D79).copy(alpha = 0.30f * pulse),
                Color.Transparent
            )
        } else {
            listOf(
                Color.Transparent,
                Color(0xFFFF85A1).copy(alpha = 0.20f * pulse),
                Color(0xFFFFB5C5).copy(alpha = 0.50f * pulse),
                Color(0xFFFF85A1).copy(alpha = 0.20f * pulse),
                Color.Transparent
            )
        }
    )
    cache.purrPath.reset()
    cache.purrPath.moveTo(0f, purrY)
    for (x in 0..w.toInt() step 8) {
        val sineY = purrY + sin((x * 0.04f) + (progress * 12f)) * (2.5.dp.toPx() * pulse)
        cache.purrPath.lineTo(x.toFloat(), sineY)
    }
    drawPath(cache.purrPath, brush = purrBrush, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
}

/**
 * Y2K Glossy Pop: Specular diagonal sheen glints, floating iridescent bubble orbs,
 * and radiant 4-pointed chrome stars.
 */
private fun DrawScope.drawY2kGlossyAtmosphere(
    w: Float,
    h: Float,
    progress: Float,
    pulse: Float,
    rot: Float,
    cache: KawaiiPathCache
) {
    // 1. Diagonal Specular Sheen Sweep that glides smoothly across the background
    val sheenX = (progress * (w + 240.dp.toPx())) - 120.dp.toPx()
    val sheenBrush = Brush.linearGradient(
        colors = listOf(
            Color.Transparent,
            Color.White.copy(alpha = 0.05f),
            Color.White.copy(alpha = 0.24f),
            Color.White.copy(alpha = 0.05f),
            Color.Transparent
        ),
        start = Offset(sheenX - 45.dp.toPx(), 0f),
        end = Offset(sheenX + 45.dp.toPx(), h)
    )
    drawRect(brush = sheenBrush)

    // 2. Floating Iridescent Bubble Orbs
    val bubbleCoords = listOf(
        Pair(w * 0.15f, h * 0.15f),
        Pair(w * 0.85f, h * 0.35f),
        Pair(w * 0.20f, h * 0.80f),
        Pair(w * 0.80f, h * 0.85f)
    )
    for ((idx, coord) in bubbleCoords.withIndex()) {
        val bubbleR = (14.dp.toPx() + (idx % 2) * 6.dp.toPx()) * (0.9f + 0.1f * pulse)
        val bx = coord.first + sin(progress * 6f + idx) * 8.dp.toPx()
        val by = coord.second + cos(progress * 6f + idx) * 8.dp.toPx()

        // Translucent bubble body
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.10f),
                    Color(0xFFA2E8DD).copy(alpha = 0.15f),
                    Color(0xFFFF7597).copy(alpha = 0.25f)
                ),
                center = Offset(bx - bubbleR * 0.3f, by - bubbleR * 0.3f),
                radius = bubbleR
            ),
            radius = bubbleR,
            center = Offset(bx, by)
        )
        // Specular highlight crescent
        drawCircle(
            color = Color.White.copy(alpha = 0.65f),
            radius = bubbleR * 0.25f,
            center = Offset(bx - bubbleR * 0.40f, by - bubbleR * 0.40f)
        )
    }

    // 3. Radiant 4-pointed Y2K Chrome Stars
    fun drawY2kStar(cx: Float, cy: Float, sizePx: Float, alpha: Float) {
        cache.starPath.reset()
        cache.starPath.moveTo(cx, cy - sizePx)
        cache.starPath.cubicTo(cx, cy - sizePx * 0.18f, cx + sizePx * 0.18f, cy, cx + sizePx, cy)
        cache.starPath.cubicTo(cx + sizePx * 0.18f, cy, cx, cy + sizePx * 0.18f, cx, cy + sizePx)
        cache.starPath.cubicTo(cx, cy + sizePx * 0.18f, cx - sizePx * 0.18f, cy, cx - sizePx, cy)
        cache.starPath.cubicTo(cx - sizePx * 0.18f, cy, cx, cy - sizePx * 0.18f, cx, cy - sizePx)
        cache.starPath.close()
        drawPath(cache.starPath, color = Color(0xFFA2E8DD).copy(alpha = alpha * 0.65f))
        drawCircle(color = Color.White.copy(alpha = alpha), radius = sizePx * 0.28f, center = Offset(cx, cy))
    }

    drawY2kStar(w * 0.28f, h * 0.07f, 9.dp.toPx(), (0.6f + 0.4f * pulse).coerceIn(0f, 1f))
    drawY2kStar(w * 0.76f, h * 0.65f, 11.dp.toPx(), (1.3f - 0.4f * pulse).coerceIn(0f, 1f))
    drawY2kStar(w * 0.12f, h * 0.45f, 7.dp.toPx(), (0.7f + 0.3f * pulse).coerceIn(0f, 1f))
}

/**
 * Pico-Calc 8-Bit: Authentic retro fantasy console canvas with dithered pixel starfield,
 * twinkling 8-bit stepped stars, and phosphor scanlines.
 */
private fun DrawScope.drawPicoPixelAtmosphere(
    w: Float,
    h: Float,
    progress: Float,
    pulse: Float
) {
    // 1. Phosphor Scanline sweep
    val scanY = progress * h
    drawLine(
        brush = Brush.horizontalGradient(
            colors = listOf(
                Color.Transparent,
                Color(0xFF00FF66).copy(alpha = 0.35f),
                Color.Transparent
            )
        ),
        start = Offset(0f, scanY),
        end = Offset(w, scanY),
        strokeWidth = 2.2.dp.toPx()
    )

    // 2. Stepped 8-bit pixel stars scattered across background
    val starCoords = listOf(
        Pair(w * 0.08f, h * 0.12f),
        Pair(w * 0.88f, h * 0.15f),
        Pair(w * 0.92f, h * 0.55f),
        Pair(w * 0.06f, h * 0.62f),
        Pair(w * 0.48f, h * 0.05f),
        Pair(w * 0.82f, h * 0.88f),
        Pair(w * 0.14f, h * 0.92f)
    )

    for ((idx, pos) in starCoords.withIndex()) {
        val starAlpha = (sin(pulse * 3f + idx * 1.5f) * 0.35f + 0.65f).coerceIn(0.2f, 1f)
        val s = 2.4.dp.toPx()
        val px = pos.first
        val py = pos.second
        val starColor = if (idx % 2 == 0) Color(0xFF00FF66) else Color(0xFFFF528E)

        // 8-bit cross-shaped pixel star
        drawRect(color = starColor.copy(alpha = starAlpha), topLeft = Offset(px, py - s), size = Size(s, s))
        drawRect(color = starColor.copy(alpha = starAlpha), topLeft = Offset(px - s, py), size = Size(s * 3f, s))
        drawRect(color = starColor.copy(alpha = starAlpha), topLeft = Offset(px, py + s), size = Size(s, s))
        drawRect(color = Color.White.copy(alpha = starAlpha), topLeft = Offset(px, py), size = Size(s, s))
    }
}
