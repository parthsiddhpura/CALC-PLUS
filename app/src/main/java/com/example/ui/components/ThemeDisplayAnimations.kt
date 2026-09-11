package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.model.ThemeId
import com.example.model.ThemePalette
import kotlin.math.sin

/**
 * Renders a unique, tasteful, professional display animation tailored to every theme's
 * aesthetic category and color palette. Completely free of cheap graphics; uses pure
 * mathematical waves, precision geometric verniers, specular refractions, and glowing light motes.
 */
@Composable
fun ThemeAmbientDisplayAnimation(
    modifier: Modifier = Modifier,
    theme: ThemePalette
) {
    // Static values for butter-smooth zero-recomposition performance
    val wavePhaseVal = 1.0f
    val breathPulseVal = 0.85f
    val sweepProgressVal = 0.5f

    // Pre-allocated paths to ensure 0 GC heap allocations during display drawing
    val reusableWavePath = remember { Path() }
    val reusableOscPath = remember { Path() }
    val reusableHeartPath = remember { Path() }
    val reusableStarPath = remember { Path() }
    val reusableEarLeft = remember { Path() }
    val reusableEarLeftInner = remember { Path() }
    val reusableEarRight = remember { Path() }
    val reusableEarRightInner = remember { Path() }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val accent = theme.accentColor
        val secondary = theme.secondaryAccent

        when {
            // 0A. KAWAII GIRL MATH: Sweet Kitten Peek, Floating Pastel Hearts & Twinkling Sparkles
            theme.isGirlMath || theme.id == ThemeId.GIRL_MATH_PASTEL -> {
                // 5 elegant floating pastel hearts drifting upwards on organic sine-wave paths
                val heartColors = listOf(
                    Color(0xFFFF8FA3),
                    Color(0xFFFFB5C5),
                    Color(0xFFBAA6FF),
                    Color(0xFFFFC6D5),
                    Color(0xFFA8E6CF)
                )
                for (i in 0 until 5) {
                    val offsetFraction = (i * 0.22f)
                    val progress = (sweepProgressVal + offsetFraction) % 1f
                    val heartY = h * (1f - progress)
                    val heartX = (w * (0.12f + i * 0.19f)) + sin(progress * 10f + i) * 14.dp.toPx()
                    val heartAlpha = (sin(progress * Math.PI.toFloat()) * 0.45f).coerceIn(0f, 0.45f)
                    val heartScale = (5.5.dp.toPx() + (i % 3) * 2.dp.toPx())

                    reusableHeartPath.reset()
                    reusableHeartPath.moveTo(heartX, heartY)
                    reusableHeartPath.cubicTo(
                        heartX - heartScale, heartY - heartScale,
                        heartX - heartScale * 1.5f, heartY + heartScale * 0.4f,
                        heartX, heartY + heartScale * 1.2f
                    )
                    reusableHeartPath.cubicTo(
                        heartX + heartScale * 1.5f, heartY + heartScale * 0.4f,
                        heartX + heartScale, heartY - heartScale,
                        heartX, heartY
                    )
                    reusableHeartPath.close()
                    drawPath(reusableHeartPath, color = heartColors[i].copy(alpha = heartAlpha))
                }

                // Adorable fluffy white kitten peeking over the top display frame
                val kittenX = 26.dp.toPx()
                val kittenY = 3.dp.toPx() + sin(breathPulseVal * 3f) * 1.2.dp.toPx()
                val headR = 10.dp.toPx()

                // Head
                drawCircle(
                    color = Color.White,
                    radius = headR,
                    center = Offset(kittenX, kittenY + headR * 0.4f)
                )
                // Left Ear
                reusableEarLeft.reset()
                reusableEarLeft.moveTo(kittenX - headR * 0.8f, kittenY + headR * 0.2f)
                reusableEarLeft.lineTo(kittenX - headR * 0.65f, kittenY - headR * 0.65f)
                reusableEarLeft.lineTo(kittenX - headR * 0.15f, kittenY)
                reusableEarLeft.close()
                drawPath(reusableEarLeft, color = Color.White)

                reusableEarLeftInner.reset()
                reusableEarLeftInner.moveTo(kittenX - headR * 0.70f, kittenY + headR * 0.15f)
                reusableEarLeftInner.lineTo(kittenX - headR * 0.60f, kittenY - headR * 0.45f)
                reusableEarLeftInner.lineTo(kittenX - headR * 0.25f, kittenY)
                reusableEarLeftInner.close()
                drawPath(reusableEarLeftInner, color = Color(0xFFFF9EAA))

                // Right Ear
                reusableEarRight.reset()
                reusableEarRight.moveTo(kittenX + headR * 0.15f, kittenY)
                reusableEarRight.lineTo(kittenX + headR * 0.65f, kittenY - headR * 0.65f)
                reusableEarRight.lineTo(kittenX + headR * 0.8f, kittenY + headR * 0.2f)
                reusableEarRight.close()
                drawPath(reusableEarRight, color = Color.White)

                reusableEarRightInner.reset()
                reusableEarRightInner.moveTo(kittenX + headR * 0.25f, kittenY)
                reusableEarRightInner.lineTo(kittenX + headR * 0.60f, kittenY - headR * 0.45f)
                reusableEarRightInner.lineTo(kittenX + headR * 0.70f, kittenY + headR * 0.15f)
                reusableEarRightInner.close()
                drawPath(reusableEarRightInner, color = Color(0xFFFF9EAA))

                // Cute pink bow by left ear
                drawCircle(color = Color(0xFFFF5277), radius = 2.2.dp.toPx(), center = Offset(kittenX - headR * 0.45f, kittenY - headR * 0.2f))
                drawCircle(color = Color(0xFFFF7597), radius = 1.8.dp.toPx(), center = Offset(kittenX - headR * 0.65f, kittenY - headR * 0.25f))
                drawCircle(color = Color(0xFFFF7597), radius = 1.8.dp.toPx(), center = Offset(kittenX - headR * 0.25f, kittenY - headR * 0.15f))

                // Happy closed eyes (^ . ^)
                drawArc(
                    color = Color(0xFF5A3840),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(kittenX - headR * 0.55f, kittenY + headR * 0.15f),
                    size = Size(3.dp.toPx(), 2.2.dp.toPx()),
                    style = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = Color(0xFF5A3840),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(kittenX + headR * 0.15f, kittenY + headR * 0.15f),
                    size = Size(3.dp.toPx(), 2.2.dp.toPx()),
                    style = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round)
                )

                // Blushing cheeks with gentle warm pulse
                val cheekAlpha = (0.50f + 0.35f * breathPulseVal).coerceIn(0f, 0.95f)
                drawCircle(
                    color = Color(0xFFFF85A1).copy(alpha = cheekAlpha),
                    radius = 2.2.dp.toPx(),
                    center = Offset(kittenX - headR * 0.60f, kittenY + headR * 0.45f)
                )
                drawCircle(
                    color = Color(0xFFFF85A1).copy(alpha = cheekAlpha),
                    radius = 2.2.dp.toPx(),
                    center = Offset(kittenX + headR * 0.60f, kittenY + headR * 0.45f)
                )

                // Ambient bottom pastel glow
                val pinkGlowBrush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFFFF9EAA).copy(alpha = 0.20f * breathPulseVal),
                        Color(0xFFFFD1DC).copy(alpha = 0.45f * breathPulseVal),
                        Color(0xFFFF9EAA).copy(alpha = 0.20f * breathPulseVal),
                        Color.Transparent
                    )
                )
                drawLine(
                    brush = pinkGlowBrush,
                    start = Offset(w * 0.12f, h - 2.dp.toPx()),
                    end = Offset(w * 0.88f, h - 2.dp.toPx()),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // 0B. NEKO MOCHI CAT-CULATOR: Rhythmic Purring Wave & Floating Translucent Pawprints
            theme.isNekoMochi || theme.id == ThemeId.NEKO_MOCHI_CAT -> {
                // Floating cute translucent pawprints rising smoothly
                for (i in 0 until 4) {
                    val offsetFraction = (i * 0.28f)
                    val progress = (sweepProgressVal + offsetFraction) % 1f
                    val pawY = h * (1f - progress)
                    val pawX = (w * (0.15f + i * 0.24f)) + sin(progress * 8f + i) * 10.dp.toPx()
                    val pawAlpha = (sin(progress * Math.PI.toFloat()) * 0.35f).coerceIn(0f, 0.35f)
                    val mainPadR = 3.5.dp.toPx()
                    val toeR = 1.6.dp.toPx()
                    val pawColor = Color(0xFFFFB5C5).copy(alpha = pawAlpha)

                    // Main palm pad
                    drawOval(
                        color = pawColor,
                        topLeft = Offset(pawX - mainPadR, pawY - mainPadR * 0.8f),
                        size = Size(mainPadR * 2f, mainPadR * 1.6f)
                    )
                    // 4 Cute toes
                    drawCircle(color = pawColor, radius = toeR, center = Offset(pawX - mainPadR * 0.75f, pawY - mainPadR * 1.25f))
                    drawCircle(color = pawColor, radius = toeR, center = Offset(pawX - mainPadR * 0.25f, pawY - mainPadR * 1.55f))
                    drawCircle(color = pawColor, radius = toeR, center = Offset(pawX + mainPadR * 0.25f, pawY - mainPadR * 1.55f))
                    drawCircle(color = pawColor, radius = toeR, center = Offset(pawX + mainPadR * 0.75f, pawY - mainPadR * 1.25f))
                }

                // Cozy soothing purr breathing wave along bottom
                val purrBrush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFFFF85A1).copy(alpha = 0.22f * breathPulseVal),
                        Color(0xFFFFB5C5).copy(alpha = 0.48f * breathPulseVal),
                        Color(0xFFFF85A1).copy(alpha = 0.22f * breathPulseVal),
                        Color.Transparent
                    )
                )
                drawLine(
                    brush = purrBrush,
                    start = Offset(w * 0.10f, h - 2.dp.toPx()),
                    end = Offset(w * 0.90f, h - 2.dp.toPx()),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // 0C. Y2K GLOSSY POP: Specular Sheen Sweep & Radiant 4-Point Stars
            theme.isY2kGlossy || theme.id == ThemeId.Y2K_GLOSSY_POP -> {
                // Diagonal specular sheen sweep that smoothly glides across the display
                val sheenX = (sweepProgressVal * (w + 160.dp.toPx())) - 80.dp.toPx()
                val sheenBrush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.04f),
                        Color.White.copy(alpha = 0.22f),
                        Color.White.copy(alpha = 0.04f),
                        Color.Transparent
                    ),
                    start = Offset(sheenX - 35.dp.toPx(), 0f),
                    end = Offset(sheenX + 35.dp.toPx(), h)
                )
                drawRect(brush = sheenBrush)

                // 4-pointed radiant Y2K chrome stars
                fun drawY2kStar(cx: Float, cy: Float, sizePx: Float, alpha: Float) {
                    reusableStarPath.reset()
                    reusableStarPath.moveTo(cx, cy - sizePx)
                    reusableStarPath.cubicTo(cx, cy - sizePx * 0.2f, cx + sizePx * 0.2f, cy, cx + sizePx, cy)
                    reusableStarPath.cubicTo(cx + sizePx * 0.2f, cy, cx, cy + sizePx * 0.2f, cx, cy + sizePx)
                    reusableStarPath.cubicTo(cx, cy + sizePx * 0.2f, cx - sizePx * 0.2f, cy, cx - sizePx, cy)
                    reusableStarPath.cubicTo(cx - sizePx * 0.2f, cy, cx, cy - sizePx * 0.2f, cx, cy - sizePx)
                    reusableStarPath.close()
                    drawPath(reusableStarPath, color = Color(0xFFFF69A6).copy(alpha = alpha * 0.75f))
                    drawCircle(color = Color.White.copy(alpha = alpha), radius = sizePx * 0.25f, center = Offset(cx, cy))
                }
                drawY2kStar(22.dp.toPx(), 16.dp.toPx(), 7.dp.toPx(), breathPulseVal)
                drawY2kStar(w - 22.dp.toPx(), h - 16.dp.toPx(), 6.dp.toPx(), 1.35f - breathPulseVal)
            }

            // 0D. PICO KAWAII PIXEL: 8-Bit Pixel Hearts, Phosphor Scanline & Pixel Sparkles
            theme.isPixelArt || theme.id == ThemeId.PICO_KAWAII_PIXEL -> {
                // Authentic 8-bit phosphor scanlines
                val scanY = sweepProgressVal * h
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFF00FF66).copy(alpha = 0.32f),
                            Color.Transparent
                        )
                    ),
                    start = Offset(0f, scanY),
                    end = Offset(w, scanY),
                    strokeWidth = 1.8.dp.toPx()
                )

                // Drifting 8-bit pixel hearts
                for (i in 0 until 4) {
                    val progress = (sweepProgressVal + i * 0.25f) % 1f
                    val py = h * (1f - progress)
                    val px = (w * (0.16f + i * 0.23f))
                    val pAlpha = (sin(progress * Math.PI.toFloat()) * 0.50f).coerceIn(0f, 0.50f)
                    val s = 2.2.dp.toPx()
                    val pColor = Color(0xFFFF528E).copy(alpha = pAlpha)

                    // Stepped pixel heart grid
                    drawRect(pColor, Offset(px - 2 * s, py - 2 * s), Size(s, s))
                    drawRect(pColor, Offset(px - s, py - 2 * s), Size(s, s))
                    drawRect(pColor, Offset(px + s, py - 2 * s), Size(s, s))
                    drawRect(pColor, Offset(px + 2 * s, py - 2 * s), Size(s, s))
                    drawRect(pColor, Offset(px - 3 * s, py - s), Size(7 * s, s))
                    drawRect(pColor, Offset(px - 3 * s, py), Size(7 * s, s))
                    drawRect(pColor, Offset(px - 2 * s, py + s), Size(5 * s, s))
                    drawRect(pColor, Offset(px - s, py + 2 * s), Size(3 * s, s))
                    drawRect(pColor, Offset(px, py + 3 * s), Size(s, s))
                }
            }

            // 0E. RETRO CIRCUIT 90034: Flowing Logic Pulses & Oscilloscope Trace
            theme.isRetroCircuit || theme.id == ThemeId.RETRO_CIRCUIT_RED -> {
                reusableOscPath.reset()
                val startX = w * 0.08f
                val endX = w * 0.92f
                val waveY = h - 6.dp.toPx()
                val pulseH = 4.dp.toPx()

                var px = startX
                reusableOscPath.moveTo(px, waveY)
                while (px < endX) {
                    val phase = ((px / w) * 8f - sweepProgressVal * 4f)
                    val isHigh = (sin(phase) > 0.3f)
                    val py = if (isHigh) waveY - pulseH else waveY
                    reusableOscPath.lineTo(px, py)
                    px += 6.dp.toPx()
                }
                drawPath(
                    path = reusableOscPath,
                    color = theme.accentColor.copy(alpha = 0.35f + breathPulseVal * 0.35f),
                    style = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Square)
                )

                val blipX = startX + (endX - startX) * sweepProgressVal
                drawCircle(
                    color = theme.secondaryAccent,
                    radius = 2.dp.toPx(),
                    center = Offset(blipX, waveY - pulseH * 0.5f)
                )
            }

            // 0F. NOTHING DOSSIER MONO: Linear Dot Array & Sweeping LED Scanning Wave
            theme.isNothingDossier || theme.id == ThemeId.NOTHING_DOSSIER -> {
                val dotY = h - 5.dp.toPx()
                val dotCount = 18
                val startX = w * 0.15f
                val stepX = (w * 0.70f) / (dotCount - 1)
                for (i in 0 until dotCount) {
                    val dotX = startX + i * stepX
                    val normalizedPos = i.toFloat() / dotCount
                    val proximity = (1f - kotlin.math.abs(normalizedPos - sweepProgressVal) * 5f).coerceIn(0f, 1f)
                    val alpha = 0.15f + proximity * 0.75f
                    val radius = 1.2.dp.toPx() + proximity * 0.8.dp.toPx()
                    drawCircle(
                        color = Color.White.copy(alpha = alpha),
                        radius = radius,
                        center = Offset(dotX, dotY)
                    )
                }
                drawCircle(
                    color = Color(0xFFE52E25).copy(alpha = 0.5f + breathPulseVal * 0.5f),
                    radius = 2.dp.toPx(),
                    center = Offset(w - 14.dp.toPx(), 10.dp.toPx())
                )
            }

            // 0G. SWISS BAUHAUS DOSSIER: Geometric Vernier Scale & Precision Ticks
            theme.isBauhausDossier || theme.id == ThemeId.BAUHAUS_DOSSIER -> {
                val tickY = h - 3.dp.toPx()
                val tickCount = 24
                val startX = w * 0.08f
                val stepX = (w * 0.84f) / (tickCount - 1)
                for (i in 0 until tickCount) {
                    val tx = startX + i * stepX
                    val isMajor = (i % 4 == 0)
                    val tickH = if (isMajor) 5.dp.toPx() else 2.5.dp.toPx()
                    val tickColor = if (isMajor) theme.accentColor else theme.secondaryAccent
                    drawLine(
                        color = tickColor.copy(alpha = 0.35f + breathPulseVal * 0.25f),
                        start = Offset(tx, tickY),
                        end = Offset(tx, tickY - tickH),
                        strokeWidth = 1.2.dp.toPx()
                    )
                }
            }

            // 0H. TERRACOTTA STUDIO: Specular Glass Reflection Gleam & OLED Micro-LED Matrix
            theme.isTerracottaStudio || theme.id == ThemeId.TERRACOTTA_STUDIO -> {
                val gleamX = sweepProgressVal * (w * 1.5f) - (w * 0.25f)
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, Color(0x22FFFFFF), Color.Transparent),
                        startX = gleamX - 30.dp.toPx(),
                        endX = gleamX + 30.dp.toPx()
                    ),
                    start = Offset(gleamX - 20.dp.toPx(), 0f),
                    end = Offset(gleamX + 20.dp.toPx(), h),
                    strokeWidth = 24.dp.toPx()
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.3f + breathPulseVal * 0.4f),
                    radius = 1.5.dp.toPx(),
                    center = Offset(w * 0.5f, h - 4.dp.toPx())
                )
            }

            // 1. NEUMORPHIC CATEGORY: Soft Specular Breathing Refraction & Liquid Light Edge
            theme.category.contains("Neumorphic", ignoreCase = true) -> {
                // Soft glowing liquid light rim along the bottom display edge
                val glowBrush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        accent.copy(alpha = 0.15f * breathPulseVal),
                        accent.copy(alpha = 0.45f * breathPulseVal),
                        accent.copy(alpha = 0.15f * breathPulseVal),
                        Color.Transparent
                    )
                )
                drawLine(
                    brush = glowBrush,
                    start = Offset(w * 0.15f, h - 2f),
                    end = Offset(w * 0.85f, h - 2f),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Delicate specular breathing halo in top-left
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accent.copy(alpha = 0.10f * breathPulseVal),
                            Color.Transparent
                        ),
                        center = Offset(24.dp.toPx(), 20.dp.toPx()),
                        radius = 45.dp.toPx()
                    ),
                    center = Offset(24.dp.toPx(), 20.dp.toPx()),
                    radius = 45.dp.toPx()
                )
            }

            // 2. RETRO & VINTAGE CATEGORY: CRT Phosphor Flutter & Blinking Terminal Cursor
            theme.category.contains("Retro", ignoreCase = true) || theme.category.contains("Vintage", ignoreCase = true) -> {
                // CRT subtle horizontal phosphor scan line
                val scanY = sweepProgressVal * h
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            accent.copy(alpha = 0.22f),
                            Color.Transparent
                        )
                    ),
                    start = Offset(0f, scanY),
                    end = Offset(w, scanY),
                    strokeWidth = 1.dp.toPx()
                )

                // Blinking terminal cursor block in top-right [ █ ]
                if (breathPulseVal > 0.65f) {
                    drawRect(
                        color = accent.copy(alpha = 0.70f),
                        topLeft = Offset(w - 24.dp.toPx(), 14.dp.toPx()),
                        size = Size(6.dp.toPx(), 10.dp.toPx())
                    )
                }
            }

            // 3. KAWAII & PLAYFUL CATEGORY: Floating Pastel Sparkle Motes
            theme.category.contains("Kawaii", ignoreCase = true) || theme.category.contains("Playful", ignoreCase = true) -> {
                val sparkleCount = 4
                for (i in 0 until sparkleCount) {
                    val phaseOffset = i * 1.57f
                    val floatY = h - (12.dp.toPx() + (sin(wavePhaseVal + phaseOffset) * 6.dp.toPx()))
                    val floatX = (w * 0.2f) + i * (w * 0.2f)
                    val alpha = (0.35f + 0.45f * sin(wavePhaseVal * 1.5f + phaseOffset)).coerceIn(0.1f, 0.85f)
                    val starR = (3f + 1.5f * sin(wavePhaseVal + phaseOffset)).dp.toPx()

                    // Draw 4-point subtle star sparkle
                    drawLine(
                        color = accent.copy(alpha = alpha),
                        start = Offset(floatX - starR, floatY),
                        end = Offset(floatX + starR, floatY),
                        strokeWidth = 1.2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = accent.copy(alpha = alpha),
                        start = Offset(floatX, floatY - starR),
                        end = Offset(floatX, floatY + starR),
                        strokeWidth = 1.2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = alpha * 0.9f),
                        radius = starR * 0.35f,
                        center = Offset(floatX, floatY)
                    )
                }
            }

            // 4. INDUSTRIAL & SKEUOMORPHIC CATEGORY: Precision Calibrated Vernier Scale
            theme.category.contains("Industrial", ignoreCase = true) || theme.category.contains("Skeuomorphic", ignoreCase = true) -> {
                val rulerY = h - 6.dp.toPx()
                val totalTicks = 24
                val spacing = w / (totalTicks + 1)

                for (i in 1..totalTicks) {
                    val x = i * spacing
                    val isMajor = (i % 4 == 0)
                    val tickHeight = if (isMajor) 6.dp.toPx() else 3.dp.toPx()
                    val tickAlpha = if (isMajor) 0.50f else 0.25f

                    drawLine(
                        color = accent.copy(alpha = tickAlpha),
                        start = Offset(x, rulerY),
                        end = Offset(x, rulerY - tickHeight),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Precision indicator cursor gliding back and forth
                val cursorX = (w * 0.1f) + ((sin(wavePhaseVal * 0.8f) + 1f) / 2f) * (w * 0.8f)
                drawLine(
                    color = secondary.copy(alpha = 0.85f),
                    start = Offset(cursorX, rulerY),
                    end = Offset(cursorX, rulerY - 9.dp.toPx()),
                    strokeWidth = 1.5.dp.toPx()
                )
                drawCircle(
                    color = secondary,
                    radius = 2.dp.toPx(),
                    center = Offset(cursorX, rulerY - 10.dp.toPx())
                )
            }

            // 5. INTERACTIVE DARK / OBSIDIAN / CYBERPUNK / DEFAULT: Dynamic Multi-Harmonic Calculation Wave
            else -> {
                // Harmonic mathematical wave across baseline (zero allocation)
                reusableWavePath.reset()
                val startX = 0f
                val baseY = h - 6.dp.toPx()
                val waveHeight = 4.dp.toPx()

                var isFirst = true
                var x = startX
                val step = 4.dp.toPx()

                while (x <= w) {
                    val normX = x / w
                    // Dual harmonic calculation wave: fundamental + octave
                    val y = baseY +
                            (sin(normX * 12f + wavePhaseVal) * waveHeight * 0.65f) +
                            (sin(normX * 24f - wavePhaseVal * 1.4f) * waveHeight * 0.35f)

                    if (isFirst) {
                        reusableWavePath.moveTo(x, y)
                        isFirst = false
                    } else {
                        reusableWavePath.lineTo(x, y)
                    }
                    x += step
                }

                // Glowing gradient wave stroke
                val waveBrush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        accent.copy(alpha = 0.35f * breathPulseVal),
                        accent.copy(alpha = 0.80f * breathPulseVal),
                        secondary.copy(alpha = 0.75f * breathPulseVal),
                        accent.copy(alpha = 0.35f * breathPulseVal),
                        Color.Transparent
                    )
                )
                drawPath(
                    path = reusableWavePath,
                    brush = waveBrush,
                    style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
                )

                // Ambient corner bracket accents
                val bracketLen = 10.dp.toPx()
                val bracketColor = accent.copy(alpha = 0.25f * breathPulseVal)
                val strokeW = 1.dp.toPx()

                // Top-left bracket
                drawLine(bracketColor, Offset(4f, 4f), Offset(4f + bracketLen, 4f), strokeW)
                drawLine(bracketColor, Offset(4f, 4f), Offset(4f, 4f + bracketLen), strokeW)

                // Top-right bracket
                drawLine(bracketColor, Offset(w - 4f, 4f), Offset(w - 4f - bracketLen, 4f), strokeW)
                drawLine(bracketColor, Offset(w - 4f, 4f), Offset(w - 4f, 4f + bracketLen), strokeW)
            }
        }
    }
}
