package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.IronManSuitType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-fidelity procedural Arc Reactor component for keys, badges, and icons.
 * Features 6 outer mechanical mounting lugs, 10 copper-wound electromagnets with cyan backlights,
 * and a radiant palladium core with continuous rotational magnetic flux and breathing animations.
 */
@Composable
fun ArcReactorIcon(
    modifier: Modifier = Modifier,
    glowColor: Color = Color(0xFF00F0FF),
    isOvercharging: Boolean = false,
    showOuterTabs: Boolean = true
) {
    // Static values for butter-smooth zero-recomposition performance
    val fluxRotVal = 30f
    val corePulseVal = 1.0f

    val overchargeScale = animateFloatAsState(
        targetValue = if (isOvercharging) 1.25f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "arc_overcharge_scale"
    )

    Canvas(modifier = modifier) {
        val diameter = size.minDimension
        val radius = diameter / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        val overchargeVal = overchargeScale.value

        // 1. Outer Dark Metallic Housing Ring
        drawCircle(
            color = Color(0xFF04070D),
            radius = radius * 0.98f,
            center = center
        )

        // 2. 6 Outer Mechanical Mounting Tabs
        if (showOuterTabs) {
            val tabCount = 6
            val tabRadius = radius * 0.94f
            val tabWidth = radius * 0.16f
            val tabLength = radius * 0.12f

            for (i in 0 until tabCount) {
                val angleDeg = i * 60f
                val angleRad = Math.toRadians(angleDeg.toDouble())
                val cosA = cos(angleRad).toFloat()
                val sinA = sin(angleRad).toFloat()

                val tabCenter = Offset(center.x + tabRadius * cosA, center.y + tabRadius * sinA)

                rotate(degrees = angleDeg, pivot = tabCenter) {
                    drawRect(
                        color = Color(0xFF1E2A3A),
                        topLeft = Offset(tabCenter.x - tabWidth / 2f, tabCenter.y - tabLength / 2f),
                        size = Size(tabWidth, tabLength)
                    )
                    drawCircle(
                        color = glowColor.copy(alpha = 0.85f),
                        radius = tabWidth * 0.2f,
                        center = tabCenter
                    )
                }
            }
        }

        // 3. Dark Titanium Outer Rim
        drawCircle(
            color = Color(0xFF0F1824),
            radius = radius * 0.88f,
            center = center,
            style = Stroke(width = radius * 0.08f)
        )

        // 4. Luminous Cyan Transformer Track
        drawCircle(
            color = glowColor.copy(alpha = 0.35f),
            radius = radius * 0.72f,
            center = center,
            style = Stroke(width = radius * 0.22f)
        )

        // 5. 10 Copper-wound Electromagnets with rotating magnetic flux
        val coilsCount = 10
        val coilRadius = radius * 0.72f
        val coilWidth = radius * 0.14f
        val coilLength = radius * 0.20f

        rotate(degrees = fluxRotVal, pivot = center) {
            for (i in 0 until coilsCount) {
                val angleDeg = i * (360f / coilsCount)
                val angleRad = Math.toRadians(angleDeg.toDouble())
                val cosA = cos(angleRad).toFloat()
                val sinA = sin(angleRad).toFloat()

                val coilCenter = Offset(center.x + coilRadius * cosA, center.y + coilRadius * sinA)

                rotate(degrees = angleDeg, pivot = coilCenter) {
                    drawRect(
                        color = Color(0xFFB87333),
                        topLeft = Offset(coilCenter.x - coilWidth / 2f, coilCenter.y - coilLength / 2f),
                        size = Size(coilWidth, coilLength)
                    )
                    drawRect(
                        color = Color(0xFF111923),
                        topLeft = Offset(coilCenter.x - coilWidth * 0.25f, coilCenter.y - coilLength / 2f),
                        size = Size(coilWidth * 0.5f, coilLength)
                    )
                    drawCircle(
                        color = glowColor.copy(alpha = 0.95f),
                        radius = coilWidth * 0.28f,
                        center = coilCenter
                    )
                }
            }
        }

        // 6. Arc Reactor Luminous Core
        val coreRadius = radius * 0.38f * corePulseVal * overchargeVal

        // Ambient radial corona glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = if (isOvercharging) 0.95f else 0.80f),
                    glowColor.copy(alpha = 0.85f),
                    Color(0xFF0077B6).copy(alpha = 0.50f),
                    Color.Transparent
                ),
                center = center,
                radius = coreRadius * 1.50f
            ),
            radius = coreRadius * 1.50f,
            center = center
        )

        // Brilliant pure white central disc
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White,
                    Color(0xFFE0FFFF),
                    glowColor
                ),
                center = center,
                radius = coreRadius * 0.72f
            ),
            radius = coreRadius * 0.72f,
            center = center
        )

        // Inner glowing ring boundary
        drawCircle(
            color = Color.White.copy(alpha = 0.95f),
            radius = coreRadius * 0.74f,
            center = center,
            style = Stroke(width = 1.dp.toPx())
        )
    }
}

/**
 * Color scheme configuration for the different Iron Man suits.
 */
data class IronManSuitColors(
    val armorMain: Color,
    val armorDark: Color,
    val faceplate: Color,
    val faceplateDark: Color,
    val arcGlow: Color,
    val eyeGlow: Color,
    val goldAccent: Color
)

fun getSuitColors(suitType: IronManSuitType): IronManSuitColors {
    return when (suitType) {
        IronManSuitType.MARK_85_CLASSIC -> IronManSuitColors(
            armorMain = Color(0xFFC81927),
            armorDark = Color(0xFF780A14),
            faceplate = Color(0xFFF7B900),
            faceplateDark = Color(0xFFBF8800),
            arcGlow = Color(0xFF00F0FF),
            eyeGlow = Color(0xFFFFFFFF),
            goldAccent = Color(0xFFF7B900)
        )
        IronManSuitType.STEALTH_STRIKE -> IronManSuitColors(
            armorMain = Color(0xFF1E293B),
            armorDark = Color(0xFF0F172A),
            faceplate = Color(0xFF475569),
            faceplateDark = Color(0xFF334155),
            arcGlow = Color(0xFF00E5FF),
            eyeGlow = Color(0xFF80D8FF),
            goldAccent = Color(0xFF38BDF8)
        )
        IronManSuitType.SILVER_CENTURION -> IronManSuitColors(
            armorMain = Color(0xFFB71C1C),
            armorDark = Color(0xFF5F0910),
            faceplate = Color(0xFFECEFF1),
            faceplateDark = Color(0xFFB0BEC5),
            arcGlow = Color(0xFF2979FF),
            eyeGlow = Color(0xFFE3F2FD),
            goldAccent = Color(0xFFCFD8DC)
        )
        IronManSuitType.HULKBUSTER -> IronManSuitColors(
            armorMain = Color(0xFF8B0000),
            armorDark = Color(0xFF420000),
            faceplate = Color(0xFFFFA000),
            faceplateDark = Color(0xFFFF6F00),
            arcGlow = Color(0xFFFF6D00),
            eyeGlow = Color(0xFFFFE082),
            goldAccent = Color(0xFFFFB300)
        )
    }
}

/**
 * Ambient background for the entire screen when an Iron Man theme is active:
 * - Pitch Black OLED canvas (#000000).
 * - Subtle ambient energy gradient glow tuned to each suit's reactor color.
 * - Smooth holographic laser scanline traversing down the screen.
 * Clean, minimal, and premium — completely free of character drawings.
 */
@Composable
fun IronManScreenBackground(
    modifier: Modifier = Modifier,
    suitType: IronManSuitType = IronManSuitType.MARK_85_CLASSIC
) {
    val suitColors = remember(suitType) { getSuitColors(suitType) }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Pure Pitch Black OLED Base
            drawRect(color = Color(0xFF000000))

            // 2. Subtle Stark Industries ambient radial energy glow at top center
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        suitColors.arcGlow.copy(alpha = 0.05f),
                        Color.Transparent
                    ),
                    center = Offset(width / 2f, height * 0.20f),
                    radius = width * 0.65f
                ),
                radius = width * 0.65f,
                center = Offset(width / 2f, height * 0.20f)
            )
        }
    }
}

/**
 * Iron Man Display Overlay for the Calculator Display.
 * Every Iron Man suit has a UNIQUE, high-tech, professional animated HUD overlay:
 *
 * 1. MARK 85 (Stark Tech / JARVIS):
 *    - Concentric rotating orbital radar reticle with degree ticks in top-right.
 *    - Continuous sinusoidal quantum calculation energy waveform across the baseline.
 *    - Holographic brackets & live JARVIS quantum telemetry ticker.
 *
 * 2. STEALTH STRIKE (Model 7 / EDITH):
 *    - Sweeping 360° tactical sonar radar cone with range rings & target blips in top-left.
 *    - Dynamic 16-bar frequency spectrum equalizer undulating along the baseline.
 *    - Crosshair tactical brackets & live EDITH stealth telemetry ticker.
 *
 * 3. SILVER CENTURION (Mark 33 / Neo-Armor):
 *    - Dual counter-rotating magnetic flux rings in cobalt blue & chrome silver in top-right.
 *    - Linear magnetic particle accelerator rail with traveling photon energy pulses along the baseline.
 *    - Caliper precision brackets & live CENTURION mag-accel telemetry ticker.
 *
 * 4. HULKBUSTER (Mark 44 / Veronica):
 *    - Heavy 8-segment rotating industrial hydraulic power gauge with thermal warning core.
 *    - Heavy seismic shockwave baseline with repeating chevron teeth.
 *    - Industrial chevron brackets >> and << & live VERONICA hydraulic telemetry ticker.
 */
@Composable
fun IronManDisplayOverlay(
    modifier: Modifier = Modifier,
    suitType: IronManSuitType = IronManSuitType.MARK_85_CLASSIC,
    accentColor: Color = Color(0xFF00F0FF)
) {
    val haptics = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    var isOvercharging by remember { mutableStateOf(false) }
    var currentQuoteIndex by remember { mutableIntStateOf(0) }
    var showHudBanner by remember { mutableStateOf(false) }

    val suitColors = remember(suitType) { getSuitColors(suitType) }

    // Static aesthetic values for butter-smooth zero-recomposition performance
    val rotationFastVal = 45f
    val rotationCounterVal = 315f
    val radarSweepAngleVal = 120f
    val wavePhaseVal = 1.0f
    val corePulseVal = 0.85f
    val particleProgressVal = 0.5f

    // Suit-specific quotes
    val suitQuotes = remember(suitType) {
        when (suitType) {
            IronManSuitType.MARK_85_CLASSIC -> listOf(
                "JARVIS: Power at 400%, Sir.",
                "STARK TECH: Quantum calculations verified.",
                "I AM IRON MAN.",
                "MARK 85: Nanotech stabilization active.",
                "JARVIS: Arc Reactor output 3.2 GJ/s."
            )
            IronManSuitType.STEALTH_STRIKE -> listOf(
                "EDITH: Tactical sonar scan complete.",
                "STEALTH STRIKE: Radar evasion 99.8%.",
                "EDITH: Encrypted channel locked.",
                "FREQUENCY: Signal intercepted and cleared.",
                "STEALTH: Visual & thermal signature zero."
            )
            IronManSuitType.SILVER_CENTURION -> listOf(
                "FRIDAY: Magnetic accelerator at max capacity.",
                "MARK 33: Pulse cannons online.",
                "NEO-ARMOR: Cobalt flux capacitors nominal.",
                "FRIDAY: Particle stream synchronized.",
                "CENTURION: Vector trajectory aligned."
            )
            IronManSuitType.HULKBUSTER -> listOf(
                "VERONICA: Auxiliary armor lock engaged.",
                "HULKBUSTER: Hydraulic pressure at 1200 PSI.",
                "OVERDRIVE: Repulsor dampeners primed.",
                "VERONICA: Orbital service satellite locked.",
                "MAX OUTPUT: Seismic shock absorption 100%."
            )
        }
    }

    val quantumWavePath = remember { Path() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                isOvercharging = true
                currentQuoteIndex = (currentQuoteIndex + 1) % suitQuotes.size
                showHudBanner = true

                coroutineScope.launch {
                    delay(850)
                    isOvercharging = false
                    delay(2600)
                    showHudBanner = false
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val primaryGlow = suitColors.arcGlow
            val secondary = suitColors.goldAccent
            val rotationFast = rotationFastVal
            val rotationCounter = rotationCounterVal
            val radarSweepAngle = radarSweepAngleVal
            val wavePhase = wavePhaseVal
            val corePulse = corePulseVal
            val particleProgress = particleProgressVal

            when (suitType) {
                // ==========================================================
                // 1. MARK 85 CLASSIC: Quantum Orbit & Sinusoidal Waveform
                // ==========================================================
                IronManSuitType.MARK_85_CLASSIC -> {
                    val reticleCenter = Offset(w - 38.dp.toPx(), 36.dp.toPx())
                    val outerR = 20.dp.toPx()
                    val innerR = 14.dp.toPx()

                    // Outer dashed technical ring (rotating clockwise)
                    rotate(degrees = rotationFast, pivot = reticleCenter) {
                        drawCircle(
                            color = primaryGlow.copy(alpha = 0.35f * corePulse),
                            radius = outerR,
                            center = reticleCenter,
                            style = Stroke(
                                width = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx()), 0f)
                            )
                        )
                    }

                    // Inner quadrant ring (counter rotating)
                    rotate(degrees = rotationCounter, pivot = reticleCenter) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.40f * corePulse),
                            radius = innerR,
                            center = reticleCenter,
                            style = Stroke(
                                width = 1.2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 8.dp.toPx()), 0f)
                            )
                        )
                    }

                    // Center quantum core dot
                    drawCircle(
                        color = primaryGlow,
                        radius = 2.5.dp.toPx(),
                        center = reticleCenter
                    )

                    // Continuous sinusoidal quantum waveform across baseline (zero allocation)
                    quantumWavePath.reset()
                    val baseY = h - 6.dp.toPx()
                    val step = 4.dp.toPx()
                    var x = 0f
                    var isFirst = true

                    while (x <= w) {
                        val normX = x / w
                        val y = baseY + (sin(normX * 10f + wavePhase) * 3.5.dp.toPx() * corePulse) +
                                (sin(normX * 20f - wavePhase * 1.5f) * 1.5.dp.toPx())

                        if (isFirst) {
                            quantumWavePath.moveTo(x, y)
                            isFirst = false
                        } else {
                            quantumWavePath.lineTo(x, y)
                        }
                        x += step
                    }

                    val waveBrush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            primaryGlow.copy(alpha = 0.25f),
                            primaryGlow.copy(alpha = 0.85f * corePulse),
                            Color.White.copy(alpha = 0.90f * corePulse),
                            primaryGlow.copy(alpha = 0.85f * corePulse),
                            primaryGlow.copy(alpha = 0.25f),
                            Color.Transparent
                        )
                    )
                    drawPath(
                        path = quantumWavePath,
                        brush = waveBrush,
                        style = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Sleek holographic corner brackets [ ]
                    val bracketLen = 12.dp.toPx()
                    val bColor = primaryGlow.copy(alpha = 0.45f * corePulse)
                    val bStroke = 1.2.dp.toPx()
                    drawLine(bColor, Offset(4f, 4f), Offset(4f + bracketLen, 4f), bStroke)
                    drawLine(bColor, Offset(4f, 4f), Offset(4f, 4f + bracketLen), bStroke)
                    drawLine(bColor, Offset(w - 4f, 4f), Offset(w - 4f - bracketLen, 4f), bStroke)
                    drawLine(bColor, Offset(w - 4f, 4f), Offset(w - 4f, 4f + bracketLen), bStroke)
                    drawLine(bColor, Offset(4f, h - 4f), Offset(4f + bracketLen, h - 4f), bStroke)
                    drawLine(bColor, Offset(4f, h - 4f), Offset(4f, h - 4f - bracketLen), bStroke)
                    drawLine(bColor, Offset(w - 4f, h - 4f), Offset(w - 4f - bracketLen, h - 4f), bStroke)
                    drawLine(bColor, Offset(w - 4f, h - 4f), Offset(w - 4f, h - 4f - bracketLen), bStroke)
                }

                // ==========================================================
                // 2. STEALTH STRIKE: Sonar Radar Sweep & Digital Spectrum
                // ==========================================================
                IronManSuitType.STEALTH_STRIKE -> {
                    val sonarCenter = Offset(36.dp.toPx(), 34.dp.toPx())
                    val sonarRadius = 22.dp.toPx()

                    // Range distance rings
                    drawCircle(
                        color = primaryGlow.copy(alpha = 0.20f),
                        radius = sonarRadius * 0.5f,
                        center = sonarCenter,
                        style = Stroke(width = 0.8.dp.toPx())
                    )
                    drawCircle(
                        color = primaryGlow.copy(alpha = 0.30f),
                        radius = sonarRadius,
                        center = sonarCenter,
                        style = Stroke(width = 1.dp.toPx())
                    )

                    // Radar sweep line & faint cone
                    val sweepRad = Math.toRadians(radarSweepAngle.toDouble())
                    val sweepEnd = Offset(
                        sonarCenter.x + sonarRadius * cos(sweepRad).toFloat(),
                        sonarCenter.y + sonarRadius * sin(sweepRad).toFloat()
                    )
                    drawLine(
                        color = primaryGlow.copy(alpha = 0.85f),
                        start = sonarCenter,
                        end = sweepEnd,
                        strokeWidth = 1.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    // Target blip that flashes when sweep passes
                    val blipAngle = 135f
                    val angleDiff = abs(radarSweepAngle - blipAngle)
                    val blipAlpha = if (angleDiff < 30f) (1f - (angleDiff / 30f)) else 0.15f
                    val blipPos = Offset(
                        sonarCenter.x + (sonarRadius * 0.7f) * cos(Math.toRadians(blipAngle.toDouble())).toFloat(),
                        sonarCenter.y + (sonarRadius * 0.7f) * sin(Math.toRadians(blipAngle.toDouble())).toFloat()
                    )
                    drawCircle(
                        color = primaryGlow.copy(alpha = blipAlpha),
                        radius = 2.dp.toPx(),
                        center = blipPos
                    )

                    // 16-Bar Digital Frequency Equalizer across bottom edge
                    val barCount = 16
                    val barWidth = 4.dp.toPx()
                    val totalBarsWidth = barCount * barWidth * 2f
                    val startX = (w - totalBarsWidth) / 2f
                    val baseY = h - 4.dp.toPx()

                    for (i in 0 until barCount) {
                        val barPhase = wavePhase * 1.4f + i * 0.45f
                        val barH = (3.dp.toPx() + (sin(barPhase) * 0.5f + 0.5f) * 7.dp.toPx())
                        val bx = startX + i * (barWidth * 2f)

                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(primaryGlow, primaryGlow.copy(alpha = 0.35f)),
                                startY = baseY - barH,
                                endY = baseY
                            ),
                            topLeft = Offset(bx, baseY - barH),
                            size = Size(barWidth, barH)
                        )
                    }

                    // Tactical crosshair corner brackets [+]
                    val chLen = 8.dp.toPx()
                    val chColor = primaryGlow.copy(alpha = 0.50f)
                    val chStroke = 1.dp.toPx()
                    // Top-left +
                    drawLine(chColor, Offset(4f, 8f), Offset(4f + chLen, 8f), chStroke)
                    drawLine(chColor, Offset(8f, 4f), Offset(8f, 4f + chLen), chStroke)
                    // Top-right +
                    drawLine(chColor, Offset(w - 4f - chLen, 8f), Offset(w - 4f, 8f), chStroke)
                    drawLine(chColor, Offset(w - 8f, 4f), Offset(w - 8f, 4f + chLen), chStroke)
                    // Bottom-left +
                    drawLine(chColor, Offset(4f, h - 8f), Offset(4f + chLen, h - 8f), chStroke)
                    drawLine(chColor, Offset(8f, h - 4f - chLen), Offset(8f, h - 4f), chStroke)
                    // Bottom-right +
                    drawLine(chColor, Offset(w - 4f - chLen, h - 8f), Offset(w - 4f, h - 8f), chStroke)
                    drawLine(chColor, Offset(w - 8f, h - 4f - chLen), Offset(w - 8f, h - 4f), chStroke)
                }

                // ==========================================================
                // 3. SILVER CENTURION: Magnetic Accelerator & Dual Flux Rings
                // ==========================================================
                IronManSuitType.SILVER_CENTURION -> {
                    val fluxCenter = Offset(w - 38.dp.toPx(), 36.dp.toPx())
                    val ringRadius = 18.dp.toPx()

                    // Cobalt Blue Outer Flux Ring (tilted clockwise)
                    rotate(degrees = rotationFast, pivot = fluxCenter) {
                        drawOval(
                            color = primaryGlow.copy(alpha = 0.50f * corePulse),
                            topLeft = Offset(fluxCenter.x - ringRadius, fluxCenter.y - ringRadius * 0.55f),
                            size = Size(ringRadius * 2f, ringRadius * 1.1f),
                            style = Stroke(width = 1.2.dp.toPx())
                        )
                        // Orbiting electron packet
                        drawCircle(
                            color = Color.White,
                            radius = 2.dp.toPx(),
                            center = Offset(fluxCenter.x + ringRadius, fluxCenter.y)
                        )
                    }

                    // Chrome Silver Counter Ring (tilted counter-clockwise)
                    rotate(degrees = rotationCounter, pivot = fluxCenter) {
                        drawOval(
                            color = secondary.copy(alpha = 0.45f * corePulse),
                            topLeft = Offset(fluxCenter.x - ringRadius * 0.8f, fluxCenter.y - ringRadius * 0.45f),
                            size = Size(ringRadius * 1.6f, ringRadius * 0.9f),
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }

                    // Magnetic particle accelerator rail along bottom baseline
                    val railY = h - 6.dp.toPx()
                    drawLine(
                        color = secondary.copy(alpha = 0.35f),
                        start = Offset(w * 0.08f, railY),
                        end = Offset(w * 0.92f, railY),
                        strokeWidth = 1.dp.toPx()
                    )

                    // 3 Traveling photon energy packets
                    for (p in 0..2) {
                        val pNorm = (particleProgress + (p * 0.33f)) % 1f
                        val px = (w * 0.08f) + pNorm * (w * 0.84f)
                        val trailLen = 22.dp.toPx()

                        drawLine(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color.Transparent, primaryGlow.copy(alpha = 0.85f)),
                                startX = px - trailLen,
                                endX = px
                            ),
                            start = Offset(px - trailLen, railY),
                            end = Offset(px, railY),
                            strokeWidth = 1.8.dp.toPx()
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 1.8.dp.toPx(),
                            center = Offset(px, railY)
                        )
                    }

                    // Precision caliper brackets with 45° beveled corners
                    val bColor = primaryGlow.copy(alpha = 0.40f)
                    val sW = 1.dp.toPx()
                    val cLen = 14.dp.toPx()
                    // Top-Left caliper
                    drawLine(bColor, Offset(4f, 4f + cLen), Offset(4f, 4f + 4.dp.toPx()), sW)
                    drawLine(bColor, Offset(4f, 4f + 4.dp.toPx()), Offset(4f + 4.dp.toPx(), 4f), sW)
                    drawLine(bColor, Offset(4f + 4.dp.toPx(), 4f), Offset(4f + cLen, 4f), sW)
                    // Top-Right caliper
                    drawLine(bColor, Offset(w - 4f - cLen, 4f), Offset(w - 4f - 4.dp.toPx(), 4f), sW)
                    drawLine(bColor, Offset(w - 4f - 4.dp.toPx(), 4f), Offset(w - 4f, 4f + 4.dp.toPx()), sW)
                    drawLine(bColor, Offset(w - 4f, 4f + 4.dp.toPx()), Offset(w - 4f, 4f + cLen), sW)
                }

                // ==========================================================
                // 4. HULKBUSTER: Veronica Hydraulic Gauge & Seismic Chevron Surge
                // ==========================================================
                IronManSuitType.HULKBUSTER -> {
                    val gaugeCenter = Offset(w - 38.dp.toPx(), 36.dp.toPx())
                    val gaugeRadius = 18.dp.toPx()

                    // Segmented industrial hydraulic arc (8 heavy curved notches)
                    val totalSegments = 8
                    rotate(degrees = rotationFast * 0.4f, pivot = gaugeCenter) {
                        for (i in 0 until totalSegments) {
                            val startAngle = i * 45f + 6f
                            val sweepA = 33f
                            val isHighlighted = (i % 2 == 0)
                            val arcColor = if (isHighlighted) primaryGlow else secondary

                            drawArc(
                                color = arcColor.copy(alpha = (0.35f + 0.50f * corePulse)),
                                startAngle = startAngle,
                                sweepAngle = sweepA,
                                useCenter = false,
                                topLeft = Offset(gaugeCenter.x - gaugeRadius, gaugeCenter.y - gaugeRadius),
                                size = Size(gaugeRadius * 2f, gaugeRadius * 2f),
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Butt)
                            )
                        }
                    }

                    // Center thermal warning core
                    drawCircle(
                        color = primaryGlow.copy(alpha = 0.90f * corePulse),
                        radius = 3.dp.toPx(),
                        center = gaugeCenter
                    )

                    // Heavy seismic shockwave with repeating chevron teeth / / / / /
                    val chevronY = h - 6.dp.toPx()
                    val chevronCount = 14
                    val chevronSpacing = w / (chevronCount + 1)
                    val toothW = 6.dp.toPx()
                    val toothH = 5.dp.toPx()

                    for (i in 1..chevronCount) {
                        val cx = i * chevronSpacing
                        val pulseOffset = sin(wavePhase * 1.8f + i * 0.35f) * 0.5f + 0.5f
                        val toothAlpha = (0.25f + 0.65f * pulseOffset).coerceIn(0.15f, 0.90f)

                        drawLine(
                            color = primaryGlow.copy(alpha = toothAlpha),
                            start = Offset(cx, chevronY),
                            end = Offset(cx + toothW, chevronY - toothH),
                            strokeWidth = 1.8.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }

                    // Heavy hazard corner brackets >> and <<
                    val hColor = primaryGlow.copy(alpha = 0.55f * corePulse)
                    val hStroke = 1.8.dp.toPx()
                    // Top-Left >>
                    drawLine(hColor, Offset(6f, 6f), Offset(12f, 10f), hStroke)
                    drawLine(hColor, Offset(12f, 10f), Offset(6f, 14f), hStroke)
                    drawLine(hColor, Offset(12f, 6f), Offset(18f, 10f), hStroke)
                    drawLine(hColor, Offset(18f, 10f), Offset(12f, 14f), hStroke)

                    // Top-Right <<
                    drawLine(hColor, Offset(w - 6f, 6f), Offset(w - 12f, 10f), hStroke)
                    drawLine(hColor, Offset(w - 12f, 10f), Offset(w - 6f, 14f), hStroke)
                    drawLine(hColor, Offset(w - 12f, 6f), Offset(w - 18f, 10f), hStroke)
                    drawLine(hColor, Offset(w - 18f, 10f), Offset(w - 12f, 14f), hStroke)
                }
            }
        }

        // Tap HUD Banner Popup
        AnimatedVisibility(
            visible = showHudBanner,
            enter = fadeIn(tween(180)) + slideInVertically(tween(250)) { -it / 2 },
            exit = fadeOut(tween(250)) + slideOutVertically(tween(250)) { -it / 2 },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp)
        ) {
            Surface(
                color = Color(0xEE050A14),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, suitColors.arcGlow.copy(alpha = 0.75f)),
                shadowElevation = 8.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    ArcReactorIcon(
                        modifier = Modifier.size(16.dp),
                        glowColor = suitColors.arcGlow,
                        isOvercharging = isOvercharging
                    )
                    Text(
                        text = suitQuotes[currentQuoteIndex],
                        color = suitColors.arcGlow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
