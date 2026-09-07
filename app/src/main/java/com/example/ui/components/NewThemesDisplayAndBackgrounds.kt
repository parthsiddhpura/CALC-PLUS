package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.model.ThemePalette
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// ==============================================================================
// 1. ORTYL MINIMAL MATTE (Matte Charcoal with Dynamic Industrial Calipers & Amber Pulses)
// ==============================================================================

private data class OrtylDustMote(
    val initialX: Float,
    val initialY: Float,
    val speedY: Float,
    val size: Float,
    val alpha: Float
)

private data class OrtylTapShockwave(
    val center: Offset,
    val radius: Float,
    val alpha: Float
)

@Composable
fun OrtylScreenBackground(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ortyl_caliper_anim")

    // Dynamic precision laser caliper scan (sweeping vertically across the matte chassis)
    val caliperProgress by infiniteTransition.animateFloat(
        initialValue = -0.1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ortyl_caliper_sweep"
    )

    // Slow meditative rotation of the architectural drafting compass dial
    val dialRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 48000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ortyl_compass_rotation"
    )

    // High-frequency PCB circuit energy pulse
    val energyPulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ortyl_energy_pulse"
    )

    // Subtle breathing glow of amber telemetry indicators
    val amberGlow by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ortyl_amber_glow"
    )

    // Suspended cleanroom amber micro-motes
    val dustMotes = remember {
        val rand = Random(42)
        List(20) {
            OrtylDustMote(
                initialX = rand.nextFloat(),
                initialY = rand.nextFloat(),
                speedY = 0.03f + rand.nextFloat() * 0.05f,
                size = 1.2f + rand.nextFloat() * 2.2f,
                alpha = 0.25f + rand.nextFloat() * 0.45f
            )
        }
    }

    // Interactive multi-ring caliper shockwaves on user tap
    var tapShockwaves by remember { mutableStateOf<List<OrtylTapShockwave>>(emptyList()) }
    val scope = rememberCoroutineScope()
    val tracePath1 = remember { Path() }
    val tracePath2 = remember { Path() }
    val diamondPath = remember { Path() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    scope.launch {
                        val steps = 30
                        for (step in 1..steps) {
                            val progress = step.toFloat() / steps
                            val currentShockwave = OrtylTapShockwave(
                                center = offset,
                                radius = progress * 460f,
                                alpha = (1f - progress) * 0.55f
                            )
                            tapShockwaves = listOf(currentShockwave)
                            delay(16)
                        }
                        tapShockwaves = emptyList()
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Deep Matte Charcoal Ceramic Surface (Braun / Dieter Rams aesthetic)
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF131315),
                        Color(0xFF1A1A1D),
                        Color(0xFF141416)
                    )
                )
            )

            // 2. Rotating Architectural Compass / Drafting Reticle in Background
            val compassCenter = Offset(width * 0.5f, height * 0.32f)
            val compassRadius = width * 0.38f

            rotate(dialRotation, pivot = compassCenter) {
                // Outer hairline compass ring
                drawCircle(
                    color = Color(0x0CFFFFFF),
                    radius = compassRadius,
                    center = compassCenter,
                    style = Stroke(width = 1f)
                )
                drawCircle(
                    color = Color(0x08FFB703),
                    radius = compassRadius * 0.82f,
                    center = compassCenter,
                    style = Stroke(width = 0.8f)
                )

                // 24 Millimeter Radial Degree Ticks
                for (i in 0 until 24) {
                    val angleDeg = i * 15f
                    val rad = Math.toRadians(angleDeg.toDouble())
                    val isMajor = (i % 6 == 0)
                    val tickLen = if (isMajor) 14.dp.toPx() else 6.dp.toPx()
                    val tickColor = if (isMajor) Color(0x25FFB703) else Color(0x0EFFFFFF)

                    val startX = compassCenter.x + (cos(rad) * (compassRadius - tickLen)).toFloat()
                    val startY = compassCenter.y + (sin(rad) * (compassRadius - tickLen)).toFloat()
                    val endX = compassCenter.x + (cos(rad) * compassRadius).toFloat()
                    val endY = compassCenter.y + (sin(rad) * compassRadius).toFloat()

                    drawLine(
                        color = tickColor,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = if (isMajor) 1.4f else 0.8f
                    )
                }

                // Precision quadrant crosshairs
                drawLine(
                    color = Color(0x0BFFFFFF),
                    start = Offset(compassCenter.x - compassRadius * 0.4f, compassCenter.y),
                    end = Offset(compassCenter.x + compassRadius * 0.4f, compassCenter.y),
                    strokeWidth = 0.8f
                )
                drawLine(
                    color = Color(0x0BFFFFFF),
                    start = Offset(compassCenter.x, compassCenter.y - compassRadius * 0.4f),
                    end = Offset(compassCenter.x, compassCenter.y + compassRadius * 0.4f),
                    strokeWidth = 0.8f
                )
            }

            // 3. Technical Grid Lines with Margin Millimeter Rulers
            val colSpacing = width / 6f
            val rulerY = caliperProgress * height
            for (col in 1..5) {
                val cx = col * colSpacing
                drawLine(
                    color = Color(0x0AFFFFFF),
                    start = Offset(cx, 0f),
                    end = Offset(cx, height),
                    strokeWidth = 0.7f
                )
            }

            // Margin Rulers on Left & Right with Dynamic Laser Illumination
            for (step in 0 until 35) {
                val my = step * (height / 35f)
                val dist = kotlin.math.abs(my - rulerY)
                val proximity = (1f - (dist / 140f)).coerceIn(0f, 1f)
                val tickColor = if (proximity > 0f) {
                    Color(0xFFFFB703).copy(alpha = 0.2f + proximity * 0.7f)
                } else {
                    Color(0x12FFFFFF)
                }
                val tickW = if (step % 5 == 0) 10.dp.toPx() else 5.dp.toPx()

                // Left margin tick
                drawLine(
                    color = tickColor,
                    start = Offset(0f, my),
                    end = Offset(tickW, my),
                    strokeWidth = 1f
                )
                // Right margin tick
                drawLine(
                    color = tickColor,
                    start = Offset(width - tickW, my),
                    end = Offset(width, my),
                    strokeWidth = 1f
                )
            }

            // 4. PCB Circuit Traces with High-Velocity Amber Energy Pulses
            val traceColor = Color(0x18FFB703)
            val tracePulseColor = Color(0xFFFFB703).copy(alpha = 0.85f * amberGlow)

            // Top-left circuit trace corridor
            tracePath1.reset()
            tracePath1.moveTo(24.dp.toPx(), 40.dp.toPx())
            tracePath1.lineTo(width * 0.4f, 40.dp.toPx())
            tracePath1.lineTo(width * 0.4f + 20.dp.toPx(), 60.dp.toPx())
            tracePath1.lineTo(width * 0.85f, 60.dp.toPx())
            drawPath(tracePath1, traceColor, style = Stroke(width = 1f))

            // Travelling pulse on trace 1
            val p1X = 24.dp.toPx() + (width * 0.85f - 24.dp.toPx()) * energyPulse
            drawCircle(
                color = tracePulseColor,
                radius = 2.4.dp.toPx(),
                center = Offset(p1X, if (p1X < width * 0.4f) 40.dp.toPx() else 60.dp.toPx())
            )

            // Bottom circuit trace corridor
            val bY = height - 48.dp.toPx()
            tracePath2.reset()
            tracePath2.moveTo(width * 0.12f, bY)
            tracePath2.lineTo(width * 0.6f, bY)
            tracePath2.lineTo(width * 0.65f, bY - 14.dp.toPx())
            tracePath2.lineTo(width * 0.9f, bY - 14.dp.toPx())
            drawPath(tracePath2, traceColor, style = Stroke(width = 1f))

            // 5. Dynamic Precision Scanning Laser Caliper Line
            if (rulerY in -20f..(height + 20f)) {
                // Amber scanning laser beam
                val laserAlpha = 0.25f * amberGlow
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFFFFB703).copy(alpha = laserAlpha * 0.4f),
                            Color(0xFFFFB703).copy(alpha = laserAlpha),
                            Color(0xFFFFE082).copy(alpha = laserAlpha * 1.2f),
                            Color(0xFFFFB703).copy(alpha = laserAlpha),
                            Color.Transparent
                        )
                    ),
                    start = Offset(0f, rulerY),
                    end = Offset(width, rulerY),
                    strokeWidth = 1.6.dp.toPx()
                )

                // Laser Caliper Target Bracket [ + ]
                val crosshairX = width * 0.72f
                val chSize = 8.dp.toPx()
                drawLine(
                    color = Color(0xFFFFB703).copy(alpha = 0.85f * amberGlow),
                    start = Offset(crosshairX - chSize, rulerY),
                    end = Offset(crosshairX + chSize, rulerY),
                    strokeWidth = 1.2f
                )
                drawLine(
                    color = Color(0xFFFFB703).copy(alpha = 0.85f * amberGlow),
                    start = Offset(crosshairX, rulerY - chSize),
                    end = Offset(crosshairX, rulerY + chSize),
                    strokeWidth = 1.2f
                )
                // Reticle diamond
                diamondPath.reset()
                diamondPath.moveTo(crosshairX, rulerY - chSize * 1.4f)
                diamondPath.lineTo(crosshairX + chSize * 1.4f, rulerY)
                diamondPath.lineTo(crosshairX, rulerY + chSize * 1.4f)
                diamondPath.lineTo(crosshairX - chSize * 1.4f, rulerY)
                diamondPath.close()
                drawPath(diamondPath, Color(0xFFFFB703).copy(alpha = 0.35f * amberGlow), style = Stroke(width = 1f))
            }

            // 6. Suspended Cleanroom Amber Micro-Motes
            dustMotes.forEachIndexed { i, mote ->
                val my = ((mote.initialY - (dialRotation / 360f) * mote.speedY * 3f) % 1.0f + 1.0f) % 1.0f * height
                val mx = (mote.initialX * width + sin(dialRotation * 0.05f + i) * 16f) % width
                drawCircle(
                    color = Color(0xFFFFB703).copy(alpha = mote.alpha * amberGlow),
                    radius = mote.size,
                    center = Offset(mx, my)
                )
            }

            // 7. Interactive Multi-Ring Caliper Shockwaves on User Tap
            tapShockwaves.forEach { sw ->
                if (sw.alpha > 0.01f) {
                    // Outer expanding circular acoustic wave
                    drawCircle(
                        color = Color(0xFFFFB703).copy(alpha = sw.alpha),
                        center = sw.center,
                        radius = sw.radius,
                        style = Stroke(width = 1.8.dp.toPx())
                    )
                    // Inner precision calibration reticle
                    drawCircle(
                        color = Color(0xFFFFE082).copy(alpha = sw.alpha * 0.7f),
                        center = sw.center,
                        radius = sw.radius * 0.65f,
                        style = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round)
                    )
                    // 4 calibration ticks extending from outer ring
                    val tickLen = 12.dp.toPx()
                    val r = sw.radius
                    val tCol = Color(0xFFFFB703).copy(alpha = sw.alpha)
                    drawLine(tCol, Offset(sw.center.x - r - tickLen, sw.center.y), Offset(sw.center.x - r, sw.center.y), strokeWidth = 1.5f)
                    drawLine(tCol, Offset(sw.center.x + r, sw.center.y), Offset(sw.center.x + r + tickLen, sw.center.y), strokeWidth = 1.5f)
                    drawLine(tCol, Offset(sw.center.x, sw.center.y - r - tickLen), Offset(sw.center.x, sw.center.y - r), strokeWidth = 1.5f)
                    drawLine(tCol, Offset(sw.center.x, sw.center.y + r), Offset(sw.center.x, sw.center.y + r + tickLen), strokeWidth = 1.5f)
                }
            }
        }
    }
}

@Composable
fun OrtylDisplayOverlay(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ortyl_display_telemetry_anim")

    // Dynamic wave phase for live oscilloscope frequency ripple
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ortyl_wave_phase"
    )

    // Dual-state precision amber status LED beacon
    val beaconPulse by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ortyl_beacon_pulse"
    )

    val wavePath = remember { Path() }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Subtle matte inner display border
        drawRoundRect(
            color = Color(0x18FFFFFF),
            topLeft = Offset(1f, 1f),
            size = Size(width - 2f, height - 2f),
            cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx()),
            style = Stroke(width = 1f)
        )

        // Precision 4-corner calibration alignment brackets
        val bracketLen = 14.dp.toPx()
        val bPad = 8.dp.toPx()
        val bracketColor = Color(0x40FFB703)

        // Top-Left
        drawLine(bracketColor, Offset(bPad, bPad), Offset(bPad + bracketLen, bPad), strokeWidth = 1.2f)
        drawLine(bracketColor, Offset(bPad, bPad), Offset(bPad, bPad + bracketLen), strokeWidth = 1.2f)

        // Top-Right
        drawLine(bracketColor, Offset(width - bPad - bracketLen, bPad), Offset(width - bPad, bPad), strokeWidth = 1.2f)
        drawLine(bracketColor, Offset(width - bPad, bPad), Offset(width - bPad, bPad + bracketLen), strokeWidth = 1.2f)

        // Bottom-Left
        drawLine(bracketColor, Offset(bPad, height - bPad), Offset(bPad + bracketLen, height - bPad), strokeWidth = 1.2f)
        drawLine(bracketColor, Offset(bPad, height - bPad), Offset(bPad, height - bPad - bracketLen), strokeWidth = 1.2f)

        // Bottom-Right
        drawLine(bracketColor, Offset(width - bPad - bracketLen, height - bPad), Offset(width - bPad, height - bPad), strokeWidth = 1.2f)
        drawLine(bracketColor, Offset(width - bPad, height - bPad), Offset(width - bPad, height - bPad - bracketLen), strokeWidth = 1.2f)

        // Live Micro Oscilloscope Frequency Waveform along display lower baseline
        val waveBaseY = height - 12.dp.toPx()
        val waveWidth = width - 36.dp.toPx()
        val waveStartX = 18.dp.toPx()
        wavePath.reset()
        wavePath.moveTo(waveStartX, waveBaseY)
        val segments = 24
        for (s in 0..segments) {
            val px = waveStartX + (s.toFloat() / segments) * waveWidth
            val py = waveBaseY + sin(wavePhase + s * 0.45f) * 3.2.dp.toPx()
            wavePath.lineTo(px, py.toFloat())
        }
        drawPath(
            path = wavePath,
            color = Color(0x28FFB703),
            style = Stroke(width = 1f)
        )

        // Status indicator LED dot with dual glowing corona in top-right
        val ledOffset = Offset(width - 20.dp.toPx(), 20.dp.toPx())
        drawCircle(
            color = Color(0xFFFFB703).copy(alpha = 0.20f * beaconPulse),
            radius = 7.dp.toPx(),
            center = ledOffset
        )
        drawCircle(
            color = Color(0xFFFFB703).copy(alpha = beaconPulse),
            radius = 2.8.dp.toPx(),
            center = ledOffset
        )
    }
}

// ==============================================================================
// 2. OLED STEALTH VOID (True 0-Lux Pitch Black with Brushed Titanium & Silver Horizon)
// ==============================================================================

private data class CelestialSilverStar(
    val x: Float,
    val y: Float,
    val phaseOffset: Float,
    val size: Float,
    val baseAlpha: Float
)

private data class OledDisplayTapEffect(
    val id: Long,
    val center: Offset,
    val anim: Animatable<Float, androidx.compose.animation.core.AnimationVector1D>
)

private data class OledScreenTapEffect(
    val id: Long,
    val center: Offset,
    val anim: Animatable<Float, androidx.compose.animation.core.AnimationVector1D>
)

@Composable
fun OledStealthVoidScreenBackground(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "oled_titanium_void_anim")

    // Subtle titanium light sweep traversing through the absolute zero-black abyss
    val titaniumSweepProgress by infiniteTransition.animateFloat(
        initialValue = -0.15f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "oled_titanium_sweep"
    )

    // Meditative breathing of the subtle geometric orbital horizon
    val horizonBreath by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "oled_horizon_breath"
    )

    // Slow astronomical rotation of subtle celestial calibration reticle
    val reticleRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 64000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "oled_reticle_rotation"
    )

    // Star scintillation frequency
    val starTwinkle by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "oled_star_twinkle"
    )

    // Celestial silver micro-stars pre-allocated
    val silverStars = remember {
        val rand = Random(4096)
        List(28) {
            CelestialSilverStar(
                x = rand.nextFloat(),
                y = rand.nextFloat(),
                phaseOffset = rand.nextFloat() * (2 * PI).toFloat(),
                size = 1.0f + rand.nextFloat() * 1.6f,
                baseAlpha = 0.18f + rand.nextFloat() * 0.45f
            )
        }
    }

    // Interactive Liquid Silver Tap Ripples with smooth 60fps Animatable
    var screenTaps by remember { mutableStateOf<List<OledScreenTapEffect>>(emptyList()) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val downChange = event.changes.firstOrNull { it.changedToDown() }
                        if (downChange != null) {
                            val tapPos = downChange.position
                            val anim = Animatable(0f)
                            val effect = OledScreenTapEffect(
                                id = System.nanoTime(),
                                center = tapPos,
                                anim = anim
                            )
                            screenTaps = (screenTaps + effect).takeLast(4)
                            scope.launch {
                                anim.animateTo(
                                    targetValue = 1f,
                                    animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
                                )
                                screenTaps = screenTaps.filter { it.id != effect.id }
                            }
                        }
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. PURE TRUE OLED BLACK CANVAS (0-Lux Pixel Diodes Completely Shut Off)
            drawRect(color = Color(0xFF000000))

            // 2. Subtle Architectural Stealth Blueprint Grid (36dp spacing)
            val gridStep = 36.dp.toPx()
            val gridColor = Color(0x0CFFFFFF)
            var gx = 0f
            while (gx <= width) {
                drawLine(
                    color = gridColor,
                    start = Offset(gx, 0f),
                    end = Offset(gx, height),
                    strokeWidth = 0.8f
                )
                gx += gridStep
            }
            var gy = 0f
            while (gy <= height) {
                drawLine(
                    color = gridColor,
                    start = Offset(0f, gy),
                    end = Offset(width, gy),
                    strokeWidth = 0.8f
                )
                gy += gridStep
            }

            // Grid Intersection Micro Crosshairs (+)
            gx = gridStep
            while (gx < width) {
                gy = gridStep
                while (gy < height) {
                    val crossLen = 2.5.dp.toPx()
                    drawLine(
                        color = Color(0x1EFFFFFF),
                        start = Offset(gx - crossLen, gy),
                        end = Offset(gx + crossLen, gy),
                        strokeWidth = 0.8f
                    )
                    drawLine(
                        color = Color(0x1EFFFFFF),
                        start = Offset(gx, gy - crossLen),
                        end = Offset(gx, gy + crossLen),
                        strokeWidth = 0.8f
                    )
                    gy += gridStep * 2
                }
                gx += gridStep * 2
            }

            // 3. Subtle Geometric Titanium Orbital Reticle (Center of Calculator)
            val horizonCenter = Offset(width * 0.5f, height * 0.40f)
            val baseRadius = width * 0.34f * horizonBreath

            rotate(reticleRotation, pivot = horizonCenter) {
                // Outer hairline titanium ring
                drawCircle(
                    color = Color(0x0AFFFFFF),
                    radius = baseRadius,
                    center = horizonCenter,
                    style = Stroke(width = 0.8f)
                )
                // Mid orbital ring with surgical degree notches
                drawCircle(
                    color = Color(0x14FFFFFF),
                    radius = baseRadius * 0.70f,
                    center = horizonCenter,
                    style = Stroke(width = 1f)
                )
                // 12 subtle quadrant markers
                for (i in 0 until 12) {
                    val angleDeg = i * 30f
                    val rad = Math.toRadians(angleDeg.toDouble())
                    val tickLen = if (i % 3 == 0) 8.dp.toPx() else 4.dp.toPx()
                    val r = baseRadius * 0.70f
                    val startX = horizonCenter.x + (cos(rad) * (r - tickLen)).toFloat()
                    val startY = horizonCenter.y + (sin(rad) * (r - tickLen)).toFloat()
                    val endX = horizonCenter.x + (cos(rad) * r).toFloat()
                    val endY = horizonCenter.y + (sin(rad) * r).toFloat()
                    drawLine(
                        color = Color(0x1AFFFFFF),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = 0.8f
                    )
                }
            }

            // 4. Silky Titanium Horizon Sweep (Ultra-subtle, elegant monochromatic light)
            val sweepY = titaniumSweepProgress * height
            if (sweepY in -20f..(height + 20f)) {
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x08FFFFFF),
                            Color(0x28FFFFFF),
                            Color(0x55FFFFFF),
                            Color(0x28FFFFFF),
                            Color(0x08FFFFFF),
                            Color.Transparent
                        )
                    ),
                    start = Offset(0f, sweepY),
                    end = Offset(width, sweepY),
                    strokeWidth = 1.2.dp.toPx()
                )
            }

            // 5. Subtle Celestial Silver Starlight in the Infinite Void with Faint Constellations
            for (i in 0 until silverStars.size - 1 step 2) {
                val s1 = silverStars[i]
                val s2 = silverStars[i + 1]
                val x1 = s1.x * width
                val y1 = s1.y * height
                val x2 = s2.x * width
                val y2 = s2.y * height
                val distSq = (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2)
                if (distSq < (width * 0.35f) * (width * 0.35f)) {
                    drawLine(
                        color = Color(0x0BFFFFFF),
                        start = Offset(x1, y1),
                        end = Offset(x2, y2),
                        strokeWidth = 0.6f
                    )
                }
            }

            silverStars.forEach { star ->
                val px = star.x * width
                val py = star.y * height
                val brightness = (sin(starTwinkle * (2 * PI).toFloat() + star.phaseOffset) * 0.5f + 0.5f)
                val alpha = (star.baseAlpha * brightness).coerceIn(0.08f, 0.70f)
                drawCircle(
                    color = Color(0xFFFFFFFF).copy(alpha = alpha),
                    radius = star.size,
                    center = Offset(px, py)
                )
            }

            // 6. Minimalist Surgical Corner Alignment Brackets (Subtle Titanium)
            val bracketColor = Color(0x25FFFFFF)
            val bLen = 14.dp.toPx()
            // Top corners
            drawLine(bracketColor, Offset(16.dp.toPx(), 32.dp.toPx()), Offset(16.dp.toPx() + bLen, 32.dp.toPx()), strokeWidth = 1f)
            drawLine(bracketColor, Offset(16.dp.toPx(), 32.dp.toPx()), Offset(16.dp.toPx(), 32.dp.toPx() + bLen), strokeWidth = 1f)
            drawLine(bracketColor, Offset(width - 16.dp.toPx() - bLen, 32.dp.toPx()), Offset(width - 16.dp.toPx(), 32.dp.toPx()), strokeWidth = 1f)
            drawLine(bracketColor, Offset(width - 16.dp.toPx(), 32.dp.toPx()), Offset(width - 16.dp.toPx(), 32.dp.toPx() + bLen), strokeWidth = 1f)

            // Bottom corners
            val bPadY = height - 36.dp.toPx()
            drawLine(bracketColor, Offset(16.dp.toPx(), bPadY), Offset(16.dp.toPx() + bLen, bPadY), strokeWidth = 1f)
            drawLine(bracketColor, Offset(16.dp.toPx(), bPadY), Offset(16.dp.toPx(), bPadY - bLen), strokeWidth = 1f)
            drawLine(bracketColor, Offset(width - 16.dp.toPx() - bLen, bPadY), Offset(width - 16.dp.toPx(), bPadY), strokeWidth = 1f)
            drawLine(bracketColor, Offset(width - 16.dp.toPx(), bPadY), Offset(width - 16.dp.toPx(), bPadY - bLen), strokeWidth = 1f)

            // 7. Interactive Liquid Silver Acoustic Shockwaves on Tap (Butter Smooth)
            screenTaps.forEach { effect ->
                val p = effect.anim.value
                val alpha = (1f - p) * 0.65f
                if (alpha > 0.01f) {
                    // Crisp expanding silver ring
                    drawCircle(
                        color = Color(0xFFFFFFFF).copy(alpha = alpha),
                        center = effect.center,
                        radius = p * 380f,
                        style = Stroke(width = 1.4.dp.toPx())
                    )
                    // Inner refined calibration ring
                    drawCircle(
                        color = Color(0xFFCBD5E1).copy(alpha = alpha * 0.55f),
                        center = effect.center,
                        radius = p * 240f,
                        style = Stroke(width = 0.8.dp.toPx())
                    )
                }
            }
        }
    }
}

@Composable
fun OledStealthVoidDisplayOverlay(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "oled_display_telemetry_anim")

    // Serene breathing pulse of the platinum status beacon
    val beaconGlow by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "oled_beacon_glow"
    )

    // Animated Quantum Waveform Spectrum Phase (Silky, mathematical audio/quantum ribbon)
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "oled_wave_phase"
    )

    // Breathing amplitude for the waveform
    val waveAmpFactor by infiniteTransition.animateFloat(
        initialValue = 0.80f,
        targetValue = 1.20f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "oled_wave_amp"
    )

    // Interactive Tap Ripples in the Display - Smooth 60fps Animatable
    var displayTaps by remember { mutableStateOf<List<OledDisplayTapEffect>>(emptyList()) }
    val scope = rememberCoroutineScope()

    val wavePath = remember { Path() }
    val glowPath = remember { Path() }
    val echoPath = remember { Path() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val downChange = event.changes.firstOrNull { it.changedToDown() }
                        if (downChange != null) {
                            val tapPos = downChange.position
                            val anim = Animatable(0f)
                            val effect = OledDisplayTapEffect(
                                id = System.nanoTime(),
                                center = tapPos,
                                anim = anim
                            )
                            displayTaps = (displayTaps + effect).takeLast(4)
                            scope.launch {
                                anim.animateTo(
                                    targetValue = 1f,
                                    animationSpec = tween(durationMillis = 620, easing = FastOutSlowInEasing)
                                )
                                displayTaps = displayTaps.filter { it.id != effect.id }
                            }
                        }
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Subtle Technical Dot-Matrix Display Grid (18dp spacing)
            val dotSpacing = 18.dp.toPx()
            val dotRadius = 0.9.dp.toPx()
            val dotColor = Color(0xFFFFFFFF).copy(alpha = 0.05f * beaconGlow)
            var dx = dotSpacing
            while (dx < width - dotSpacing) {
                var dy = dotSpacing
                while (dy < height - dotSpacing) {
                    drawCircle(
                        color = dotColor,
                        radius = dotRadius,
                        center = Offset(dx, dy)
                    )
                    dy += dotSpacing
                }
                dx += dotSpacing
            }

            // 2. Active Quantum Harmonic Waveform Ribbon (Flowing across bottom of display)
            val baseY = height * 0.78f
            wavePath.reset()
            glowPath.reset()
            echoPath.reset()

            val stepPx = 8f
            val totalPoints = (width / stepPx).toInt() + 1

            glowPath.moveTo(0f, height)

            for (i in 0..totalPoints) {
                val x = i * stepPx
                // Multi-harmonic sine + cosine superposition for ultra-organic fluid motion
                val y1 = sin(x * 0.022f + wavePhase) * 6.dp.toPx() * waveAmpFactor
                val y2 = cos(x * 0.045f - wavePhase * 0.8f) * 2.5.dp.toPx() * waveAmpFactor
                val curY = (baseY + y1 + y2).coerceIn(10f, height - 4f)

                if (i == 0) {
                    wavePath.moveTo(x, curY)
                    glowPath.lineTo(x, curY)
                } else {
                    wavePath.lineTo(x, curY)
                    glowPath.lineTo(x, curY)
                }
            }

            glowPath.lineTo(width, height)
            glowPath.close()

            // Subtle vertical underglow under the waveform
            drawPath(
                path = glowPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0x16FFFFFF), Color.Transparent),
                    startY = baseY - 12.dp.toPx(),
                    endY = height
                )
            )

            // Primary silky titanium waveform stroke
            drawPath(
                path = wavePath,
                color = Color(0x35FFFFFF),
                style = Stroke(width = 1.3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Secondary ghost harmonic echo stroke
            for (i in 0..totalPoints) {
                val x = i * stepPx
                val yEcho = sin(x * 0.016f - wavePhase * 1.2f) * 4.dp.toPx() * waveAmpFactor
                val curY = (baseY + yEcho + 3.dp.toPx()).coerceIn(10f, height - 4f)
                if (i == 0) echoPath.moveTo(x, curY) else echoPath.lineTo(x, curY)
            }
            drawPath(
                path = echoPath,
                color = Color(0x14FFFFFF),
                style = Stroke(width = 0.8.dp.toPx(), cap = StrokeCap.Round)
            )

            // 3. Pure OLED Display Bezel with Ultra-thin Titanium Rim
            drawRoundRect(
                color = Color(0x22FFFFFF),
                topLeft = Offset(1f, 1f),
                size = Size(width - 2f, height - 2f),
                cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx()),
                style = Stroke(width = 1f)
            )

            // Surgical Titanium Corner Registration Brackets
            val bLen = 12.dp.toPx()
            val pad = 8.dp.toPx()
            val cColor = Color(0x38FFFFFF)

            // Top-Left
            drawLine(cColor, Offset(pad, pad), Offset(pad + bLen, pad), strokeWidth = 1f)
            drawLine(cColor, Offset(pad, pad), Offset(pad, pad + bLen), strokeWidth = 1f)

            // Top-Right
            drawLine(cColor, Offset(width - pad - bLen, pad), Offset(width - pad, pad), strokeWidth = 1f)
            drawLine(cColor, Offset(width - pad, pad), Offset(width - pad, pad + bLen), strokeWidth = 1f)

            // Minimalist Platinum 0-LUX Power Efficiency Indicator in Top-Right
            val statusX = width - 38.dp.toPx()
            val statusY = 18.dp.toPx()
            for (b in 0 until 4) {
                val barAlpha = if (b < 3) 0.75f * beaconGlow else 0.25f
                drawRoundRect(
                    color = Color(0xFFFFFFFF).copy(alpha = barAlpha),
                    topLeft = Offset(statusX + b * 4.5.dp.toPx(), statusY - (b + 1) * 1.8.dp.toPx()),
                    size = Size(2.4.dp.toPx(), (b + 1) * 1.8.dp.toPx()),
                    cornerRadius = CornerRadius(0.8.dp.toPx(), 0.8.dp.toPx())
                )
            }

            // Serene Platinum Status Beacon Dot in Top-Left
            val ledOffset = Offset(18.dp.toPx(), 18.dp.toPx())
            drawCircle(
                color = Color(0xFFFFFFFF).copy(alpha = 0.15f * beaconGlow),
                radius = 5.dp.toPx(),
                center = ledOffset
            )
            drawCircle(
                color = Color(0xFFFFFFFF).copy(alpha = 0.85f * beaconGlow),
                radius = 2.0.dp.toPx(),
                center = ledOffset
            )

            // 4. Interactive Display Tap Animation (Butter Smooth Optical Caliper & Ripple)
            displayTaps.forEach { effect ->
                val p = effect.anim.value
                val alpha = (1f - p) * 0.85f
                val center = effect.center

                if (alpha > 0.01f) {
                    // (a) Expanding Optical Caliper Ring in Pure White / Platinum
                    val ringRadius = p * 160.dp.toPx()
                    drawCircle(
                        color = Color(0xFFFFFFFF).copy(alpha = alpha),
                        center = center,
                        radius = ringRadius,
                        style = Stroke(width = 1.4.dp.toPx())
                    )

                    // (b) Secondary Inner Concentric Calibration Ring
                    val innerRadius = p * 95.dp.toPx()
                    drawCircle(
                        color = Color(0xFFCBD5E1).copy(alpha = alpha * 0.55f),
                        center = center,
                        radius = innerRadius,
                        style = Stroke(width = 0.9.dp.toPx())
                    )

                    // (c) Surgical Optical Focus Crosshair Ticks (+)
                    val crossOffset = (6 + p * 20).dp.toPx()
                    val tickLen = 6.dp.toPx()
                    val crossColor = Color(0xFFFFFFFF).copy(alpha = alpha * 0.9f)
                    // Left
                    drawLine(crossColor, Offset(center.x - crossOffset - tickLen, center.y), Offset(center.x - crossOffset, center.y), strokeWidth = 1.2f)
                    // Right
                    drawLine(crossColor, Offset(center.x + crossOffset, center.y), Offset(center.x + crossOffset + tickLen, center.y), strokeWidth = 1.2f)
                    // Top
                    drawLine(crossColor, Offset(center.x, center.y - crossOffset - tickLen), Offset(center.x, center.y - crossOffset), strokeWidth = 1.2f)
                    // Bottom
                    drawLine(crossColor, Offset(center.x, center.y + crossOffset), Offset(center.x, center.y + crossOffset + tickLen), strokeWidth = 1.2f)

                    // (d) Rotating Optical Aperture Brackets (Autofocus Target Reticle)
                    rotate(p * 45f, pivot = center) {
                        val bracketDist = p * 55.dp.toPx()
                        val bSize = 5.dp.toPx()
                        val bColor = Color(0xFFCBD5E1).copy(alpha = alpha * 0.75f)
                        // Top-left
                        drawLine(bColor, Offset(center.x - bracketDist, center.y - bracketDist), Offset(center.x - bracketDist + bSize, center.y - bracketDist), strokeWidth = 1f)
                        drawLine(bColor, Offset(center.x - bracketDist, center.y - bracketDist), Offset(center.x - bracketDist, center.y - bracketDist + bSize), strokeWidth = 1f)
                        // Top-right
                        drawLine(bColor, Offset(center.x + bracketDist - bSize, center.y - bracketDist), Offset(center.x + bracketDist, center.y - bracketDist), strokeWidth = 1f)
                        drawLine(bColor, Offset(center.x + bracketDist, center.y - bracketDist), Offset(center.x + bracketDist, center.y - bracketDist + bSize), strokeWidth = 1f)
                        // Bottom-left
                        drawLine(bColor, Offset(center.x - bracketDist, center.y + bracketDist), Offset(center.x - bracketDist + bSize, center.y + bracketDist), strokeWidth = 1f)
                        drawLine(bColor, Offset(center.x - bracketDist, center.y + bracketDist - bSize), Offset(center.x - bracketDist, center.y + bracketDist), strokeWidth = 1f)
                        // Bottom-right
                        drawLine(bColor, Offset(center.x + bracketDist - bSize, center.y + bracketDist), Offset(center.x + bracketDist, center.y + bracketDist), strokeWidth = 1f)
                        drawLine(bColor, Offset(center.x + bracketDist, center.y + bracketDist - bSize), Offset(center.x + bracketDist, center.y + bracketDist), strokeWidth = 1f)
                    }

                    // (e) 4 Micro-Photons Radiating Outward
                    val sparkDist = p * 85.dp.toPx()
                    for (k in 0 until 4) {
                        val angleRad = (k * 90f + p * 30f) * (PI / 180.0)
                        val sx = center.x + (cos(angleRad) * sparkDist).toFloat()
                        val sy = center.y + (sin(angleRad) * sparkDist).toFloat()
                        drawCircle(
                            color = Color(0xFFFFFFFF).copy(alpha = alpha * 0.8f),
                            radius = 1.6.dp.toPx(),
                            center = Offset(sx, sy)
                        )
                    }
                }
            }
        }
    }
}

// ==============================================================================
// 3. STARRY NIGHT GOTHAM (From Photo 3: Van Gogh Swirling Sky over Batman)
// ==============================================================================

private data class StarryVortex(val cx: Float, val cy: Float, val radius: Float, val speedFactor: Float)

@Composable
fun StarryGothamScreenBackground(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "starry_gotham_anim")

    // Continuous slow hypnotic rotation of Van Gogh's starry brushstroke vortexes
    val vortexSpin by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 40000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "starry_vortex_spin"
    )

    // Pulsing radiant star glow
    val starPulse by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starry_star_pulse"
    )

    // Bats swooping across the starry sky
    val batFlightProgress by infiniteTransition.animateFloat(
        initialValue = -0.1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "starry_bat_flight"
    )

    // Pre-allocated starry whirlpool centers matching Photo 3's composition
    val vortexes = remember {
        listOf(
            StarryVortex(0.72f, 0.10f, 95f, 1.0f), // Big bright moon whirlpool top-right
            StarryVortex(0.42f, 0.28f, 120f, -0.8f), // Central master spiral
            StarryVortex(0.18f, 0.48f, 80f, 1.1f),  // Lower-left star swirl
            StarryVortex(0.82f, 0.32f, 75f, -1.2f), // Right-side nebula swirl
            StarryVortex(0.12f, 0.16f, 65f, 0.9f)   // Top-left twilight star
        )
    }

    // Tap starry burst
    var tapBurstRadius by remember { mutableFloatStateOf(0f) }
    var tapBurstAlpha by remember { mutableFloatStateOf(0f) }
    var tapBurstCenter by remember { mutableStateOf(Offset.Zero) }
    val scope = rememberCoroutineScope()
    val vgCache = remember { VanGoghPathCache() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    tapBurstCenter = offset
                    scope.launch {
                        tapBurstRadius = 0f
                        tapBurstAlpha = 0.6f
                        val steps = 28
                        for (i in 1..steps) {
                            tapBurstRadius = (i.toFloat() / steps) * 460f
                            tapBurstAlpha = 0.6f * (1f - (i.toFloat() / steps))
                            delay(16)
                        }
                        tapBurstAlpha = 0f
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Deep Midnight Blue Impressionist Canvas Gradient
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F2038),
                        Color(0xFF142C4E),
                        Color(0xFF1B3D66),
                        Color(0xFF0E1A2C)
                    )
                )
            )

            // 2. Van Gogh Swirling Painterly Brushstrokes & Stars
            vortexes.forEach { vortex ->
                val vcx = vortex.cx * width
                val vcy = vortex.cy * height
                val vr = vortex.radius * (width / 400f).coerceIn(0.8f, 1.5f)
                val currentAngle = vortexSpin * vortex.speedFactor

                drawVanGoghStarWhirlpool(
                    center = Offset(vcx, vcy),
                    baseRadius = vr,
                    angle = currentAngle,
                    pulse = starPulse
                )
            }

            // 3. Sweeping Wind Currents in the Sky (painterly dashed arcs)
            vgCache.wavePath.reset()
            vgCache.wavePath.moveTo(0f, height * 0.42f)
            vgCache.wavePath.cubicTo(
                width * 0.35f, height * 0.32f,
                width * 0.65f, height * 0.52f,
                width, height * 0.38f
            )
            drawPath(
                path = vgCache.wavePath,
                color = Color(0x334882B4),
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
            )
            drawPath(
                path = vgCache.wavePath,
                color = Color(0x22FFD166),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // 4. Gotham Gothic Cathedral Spires & Skyline (Middle/Bottom)
            val skylineTop = height * 0.72f
            drawGothicCathedralSkyline(
                topY = skylineTop,
                width = width,
                height = height,
                path = vgCache.skylinePath
            )

            // 5. Batman Overlooking Gotham (Foreground silhouette in bottom-left on roof ledge)
            val batmanCenterX = width * 0.22f
            val batmanBaseY = height * 0.96f
            val batmanScale = (width / 420f).coerceIn(0.85f, 1.35f)
            drawBatmanVanGoghSilhouette(
                centerX = batmanCenterX,
                baseY = batmanBaseY,
                scale = batmanScale,
                cache = vgCache
            )

            // 6. Flying Bats swooping through the starry swirl
            val batX = batFlightProgress * width * 1.2f - width * 0.1f
            val batY = height * 0.45f + sin(batFlightProgress * 4 * PI.toFloat()) * 40f
            drawBatSilhouette(center = Offset(batX, batY), size = 18.dp.toPx(), path = vgCache.batPath)
            drawBatSilhouette(center = Offset(batX - 45f, batY + 25f), size = 12.dp.toPx(), path = vgCache.batPath)

            // 7. Interactive Starry Tap Burst
            if (tapBurstAlpha > 0.01f) {
                drawCircle(
                    color = Color(0xFFFFD166).copy(alpha = tapBurstAlpha),
                    center = tapBurstCenter,
                    radius = tapBurstRadius,
                    style = Stroke(width = 2.5.dp.toPx())
                )
                drawCircle(
                    color = Color(0xFF64B5F6).copy(alpha = tapBurstAlpha * 0.65f),
                    center = tapBurstCenter,
                    radius = tapBurstRadius * 0.7f,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }
        }
    }
}

private fun DrawScope.drawVanGoghStarWhirlpool(center: Offset, baseRadius: Float, angle: Float, pulse: Float) {
    // 1. Radiant luminous core (warm golden starlight)
    val coreRadius = baseRadius * 0.25f * pulse
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFF7C2), Color(0xFFFFD166), Color(0x66FFB703), Color.Transparent),
            center = center,
            radius = coreRadius * 2.2f
        ),
        radius = coreRadius * 2.2f,
        center = center
    )
    drawCircle(
        color = Color(0xFFFFFFFF),
        radius = coreRadius * 0.6f,
        center = center
    )

    // 2. Concentric painterly brushstroke arcs rotating smoothly
    rotate(angle, pivot = center) {
        val strokeColor1 = Color(0x88FFD166)
        val strokeColor2 = Color(0x884FC3F7)
        val strokeColor3 = Color(0x771976D2)

        for (ring in 1..4) {
            val r = baseRadius * (0.35f + ring * 0.22f)
            val strokeW = (3f + ring * 0.8f).dp.toPx()
            val color = if (ring % 2 == 0) strokeColor1 else if (ring == 1) strokeColor2 else strokeColor3

            val arcCount = 6
            val sweep = 42f
            for (i in 0 until arcCount) {
                val startA = i * (360f / arcCount)
                drawArc(
                    color = color,
                    startAngle = startA,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(center.x - r, center.y - r),
                    size = Size(r * 2, r * 2),
                    style = Stroke(width = strokeW, cap = StrokeCap.Round)
                )
            }
        }
    }
}

private class VanGoghPathCache(
    val wavePath: Path = Path(),
    val skylinePath: Path = Path(),
    val ledgePath: Path = Path(),
    val capePath: Path = Path(),
    val leftEar: Path = Path(),
    val rightEar: Path = Path(),
    val batPath: Path = Path()
)

private fun DrawScope.drawGothicCathedralSkyline(topY: Float, width: Float, height: Float, path: Path) {
    val skylineColor = Color(0xFF090E17)
    val windowGold = Color(0x66FFD166)

    path.reset()
    path.moveTo(0f, height)
    path.lineTo(0f, topY + 40f)
    path.lineTo(width * 0.15f, topY + 40f)
    path.lineTo(width * 0.20f, topY + 20f)
    path.lineTo(width * 0.35f, topY + 60f)
    // Cathedral Central Main Spire
    path.lineTo(width * 0.70f, topY + 50f)
    path.lineTo(width * 0.74f, topY - 75f) // Spire peak!
    path.lineTo(width * 0.78f, topY + 50f)
    // Secondary spires
    path.lineTo(width * 0.82f, topY - 35f)
    path.lineTo(width * 0.86f, topY + 45f)
    path.lineTo(width * 0.92f, topY - 20f)
    path.lineTo(width, topY + 50f)
    path.lineTo(width, height)
    path.close()
    drawPath(path, skylineColor)

    // Warm glowing Gothic cathedral windows
    val winWidth = 4.dp.toPx()
    val winHeight = 10.dp.toPx()
    val windowPositions = listOf(
        Offset(width * 0.73f, topY + 30f),
        Offset(width * 0.75f, topY + 30f),
        Offset(width * 0.84f, topY + 35f),
        Offset(width * 0.25f, topY + 55f),
        Offset(width * 0.30f, topY + 58f)
    )
    windowPositions.forEach { pos ->
        drawRoundRect(
            color = windowGold,
            topLeft = pos,
            size = Size(winWidth, winHeight),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
        )
    }
}

private fun DrawScope.drawBatmanVanGoghSilhouette(centerX: Float, baseY: Float, scale: Float, cache: VanGoghPathCache) {
    val s = scale * 1.15f
    val batmanColor = Color(0xFF070B12)

    // 1. Ledge platform
    cache.ledgePath.reset()
    cache.ledgePath.moveTo(0f, baseY)
    cache.ledgePath.lineTo(centerX + 60f * s, baseY)
    cache.ledgePath.lineTo(centerX + 40f * s, baseY + 60f * s)
    cache.ledgePath.lineTo(0f, baseY + 60f * s)
    cache.ledgePath.close()
    drawPath(cache.ledgePath, Color(0xFF06090F))

    // 2. Flowing Cape
    cache.capePath.reset()
    cache.capePath.moveTo(centerX - 16f * s, baseY - 95f * s)
    cache.capePath.cubicTo(
        centerX - 48f * s, baseY - 60f * s,
        centerX - 52f * s, baseY - 20f * s,
        centerX - 38f * s, baseY
    )
    cache.capePath.lineTo(centerX + 32f * s, baseY)
    cache.capePath.cubicTo(
        centerX + 38f * s, baseY - 30f * s,
        centerX + 32f * s, baseY - 70f * s,
        centerX + 16f * s, baseY - 95f * s
    )
    cache.capePath.close()
    drawPath(cache.capePath, batmanColor)

    // 3. Body & Shoulders
    drawRoundRect(
        color = batmanColor,
        topLeft = Offset(centerX - 18f * s, baseY - 105f * s),
        size = Size(36f * s, 60f * s),
        cornerRadius = CornerRadius(8f * s, 8f * s)
    )

    // 4. Cowl with pointed bat ears (viewed from back/three-quarter)
    val headCenterY = baseY - 116f * s
    drawOval(
        color = batmanColor,
        topLeft = Offset(centerX - 12f * s, headCenterY - 12f * s),
        size = Size(24f * s, 26f * s)
    )
    // Left ear
    cache.leftEar.reset()
    cache.leftEar.moveTo(centerX - 11f * s, headCenterY - 8f * s)
    cache.leftEar.lineTo(centerX - 9f * s, headCenterY - 24f * s)
    cache.leftEar.lineTo(centerX - 4f * s, headCenterY - 8f * s)
    cache.leftEar.close()
    drawPath(cache.leftEar, batmanColor)
    // Right ear
    cache.rightEar.reset()
    cache.rightEar.moveTo(centerX + 4f * s, headCenterY - 8f * s)
    cache.rightEar.lineTo(centerX + 9f * s, headCenterY - 24f * s)
    cache.rightEar.lineTo(centerX + 11f * s, headCenterY - 8f * s)
    cache.rightEar.close()
    drawPath(cache.rightEar, batmanColor)

    // Subtle blue rim highlight along cape edges reflecting starry vortex
    val rimLightColor = Color(0x5564B5F6)
    drawLine(
        color = rimLightColor,
        start = Offset(centerX - 16f * s, baseY - 95f * s),
        end = Offset(centerX - 42f * s, baseY - 20f * s),
        strokeWidth = 2.2f * s
    )
}

private fun DrawScope.drawBatSilhouette(center: Offset, size: Float, path: Path) {
    val batColor = Color(0xDD070B12)
    val w = size
    val h = size * 0.45f
    path.reset()
    path.moveTo(center.x, center.y - h * 0.4f)
    path.cubicTo(center.x + w * 0.35f, center.y - h * 1.2f, center.x + w * 0.8f, center.y - h * 0.2f, center.x + w, center.y - h * 0.5f)
    path.cubicTo(center.x + w * 0.7f, center.y + h * 0.6f, center.x + w * 0.35f, center.y + h * 0.2f, center.x, center.y + h)
    path.cubicTo(center.x - w * 0.35f, center.y + h * 0.2f, center.x - w * 0.7f, center.y + h * 0.6f, center.x - w, center.y - h * 0.5f)
    path.cubicTo(center.x - w * 0.8f, center.y - h * 0.2f, center.x - w * 0.35f, center.y - h * 1.2f, center.x, center.y - h * 0.4f)
    path.close()
    drawPath(path, batColor)
}

@Composable
fun StarryGothamDisplayOverlay(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "starry_disp_anim")
    val starSpin by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 28000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "starry_disp_spin"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Radiant Van Gogh starlight whirlpool in top-left corner of display
        val starCenter = Offset(36.dp.toPx(), 28.dp.toPx())
        drawVanGoghStarWhirlpool(
            center = starCenter,
            baseRadius = 36.dp.toPx(),
            angle = starSpin,
            pulse = 1.0f
        )
    }
}

// ==============================================================================
// 4. COSMIC SINGULARITY (From Photo 4: Accretion Disk & Crimson Ringed Planet)
// ==============================================================================

private data class CosmicStar(val x: Float, val y: Float, val radius: Float, val baseAlpha: Float)

@Composable
fun CosmicSingularityScreenBackground(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cosmic_anim")

    // Slow relativistic rotation of the gigantic fiery crimson accretion disk
    val diskRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 36000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cosmic_disk_rotation"
    )

    // Cosmic gravitational breathing / lensing pulse
    val lensingPulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cosmic_lensing_pulse"
    )

    // Pre-allocated distant cosmic stars
    val stars = remember {
        val rand = Random(1337)
        List(40) {
            CosmicStar(
                x = rand.nextFloat(),
                y = rand.nextFloat(),
                radius = 1f + rand.nextFloat() * 1.8f,
                baseAlpha = 0.3f + rand.nextFloat() * 0.7f
            )
        }
    }

    // Interactive Gravitational Tap Lensing wave
    var tapLensRadius by remember { mutableFloatStateOf(0f) }
    var tapLensAlpha by remember { mutableFloatStateOf(0f) }
    var tapLensCenter by remember { mutableStateOf(Offset.Zero) }
    val scope = rememberCoroutineScope()
    val ridgePath = remember { Path() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    tapLensCenter = offset
                    scope.launch {
                        tapLensRadius = 0f
                        tapLensAlpha = 0.55f
                        val steps = 28
                        for (i in 1..steps) {
                            tapLensRadius = (i.toFloat() / steps) * 480f
                            tapLensAlpha = 0.55f * (1f - (i.toFloat() / steps))
                            delay(16)
                        }
                        tapLensAlpha = 0f
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Deep Space Void Background (from Photo 4)
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF080408),
                        Color(0xFF12060E),
                        Color(0xFF160912),
                        Color(0xFF08040A)
                    )
                )
            )

            // 2. Distant Twinkling Stars
            stars.forEach { star ->
                val alpha = (star.baseAlpha * lensingPulse).coerceIn(0.1f, 1f)
                drawCircle(
                    color = Color(0xFFFFF0F5).copy(alpha = alpha),
                    radius = star.radius,
                    center = Offset(star.x * width, star.y * height)
                )
            }

            // 3. Billowing Crimson Cosmic Nebula Dust Clouds in upper space
            drawCrimsonNebulaClouds(width = width, height = height)

            // 4. Central Celestial Singularity / Ringed Planet & Accretion Disk
            val planetCenter = Offset(width * 0.52f, height * 0.46f)
            val planetRadius = width * 0.28f

            drawAccretionDiskAndPlanet(
                center = planetCenter,
                radius = planetRadius,
                rotationDeg = diskRotation,
                lensingPulse = lensingPulse
            )

            // 5. Luminescent Indigo Mountain Ridges & Valley City Lights Below
            drawIndigoPlanetRidge(width = width, height = height, path = ridgePath)

            // 6. Interactive Gravitational Lensing Ripple on tap
            if (tapLensAlpha > 0.01f) {
                drawCircle(
                    color = Color(0xFFFF2A4D).copy(alpha = tapLensAlpha),
                    center = tapLensCenter,
                    radius = tapLensRadius,
                    style = Stroke(width = 2.5.dp.toPx())
                )
                drawCircle(
                    color = Color(0xFFFFAA00).copy(alpha = tapLensAlpha * 0.65f),
                    center = tapLensCenter,
                    radius = tapLensRadius * 0.75f,
                    style = Stroke(width = 1.2.dp.toPx())
                )
            }
        }
    }
}

private fun DrawScope.drawCrimsonNebulaClouds(width: Float, height: Float) {
    val cloudColor1 = Color(0x35E61A35)
    val cloudColor2 = Color(0x20FF4D6D)

    // Top-left billowing cloud
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(cloudColor1, cloudColor2, Color.Transparent),
            center = Offset(width * 0.2f, height * 0.15f),
            radius = width * 0.45f
        ),
        radius = width * 0.45f,
        center = Offset(width * 0.2f, height * 0.15f)
    )

    // Top-right radiant stellar cloud
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0x30E61A35), Color(0x18FF8C00), Color.Transparent),
            center = Offset(width * 0.85f, height * 0.22f),
            radius = width * 0.5f
        ),
        radius = width * 0.5f,
        center = Offset(width * 0.85f, height * 0.22f)
    )
}

private fun DrawScope.drawAccretionDiskAndPlanet(
    center: Offset,
    radius: Float,
    rotationDeg: Float,
    lensingPulse: Float
) {
    // 1. Glowing outer accretion halo
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0x33FF334B), Color(0x15FFAA00), Color.Transparent),
            center = center,
            radius = radius * 2.4f * lensingPulse
        ),
        radius = radius * 2.4f * lensingPulse,
        center = center
    )

    // 2. Back section of the tilted accretion rings (drawn behind the planet)
    rotate(rotationDeg * 0.15f - 24f, pivot = center) {
        val ringCount = 14
        for (i in 0 until ringCount) {
            val ringRadiusX = radius * (1.25f + i * 0.11f) * lensingPulse
            val ringRadiusY = ringRadiusX * 0.38f // Elliptical tilt!
            val ringColor = if (i % 3 == 0) Color(0xCCFF2A4D)
            else if (i % 2 == 0) Color(0xBBFFAA00)
            else Color(0x99E61A35)

            // Draw back half arc
            drawArc(
                color = ringColor,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(center.x - ringRadiusX, center.y - ringRadiusY),
                size = Size(ringRadiusX * 2, ringRadiusY * 2),
                style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }

    // 3. Central Celestial Singularity Planet Sphere (Obsidian dark core)
    drawCircle(
        color = Color(0xFF090408),
        radius = radius,
        center = center
    )

    // Brilliant golden-amber back-lit rim crescent (Photo 4 signature)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFF0D0), Color(0xFFFFAA00), Color(0x66FF334B), Color.Transparent),
            center = Offset(center.x + radius * 0.35f, center.y - radius * 0.35f),
            radius = radius * 0.95f
        ),
        radius = radius,
        center = center
    )
    // Dark core mask to leave only the thin crescent
    drawCircle(
        color = Color(0xFF090408),
        radius = radius * 0.94f,
        center = Offset(center.x - radius * 0.08f, center.y + radius * 0.08f)
    )

    // 4. Front section of the tilted accretion rings (drawn in front of the planet)
    rotate(rotationDeg * 0.15f - 24f, pivot = center) {
        val ringCount = 14
        for (i in 0 until ringCount) {
            val ringRadiusX = radius * (1.25f + i * 0.11f) * lensingPulse
            val ringRadiusY = ringRadiusX * 0.38f
            val ringColor = if (i % 3 == 0) Color(0xEEFF2A4D)
            else if (i % 2 == 0) Color(0xDDFFAA00)
            else Color(0xAAE61A35)

            // Draw front half arc
            drawArc(
                color = ringColor,
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(center.x - ringRadiusX, center.y - ringRadiusY),
                size = Size(ringRadiusX * 2, ringRadiusY * 2),
                style = Stroke(width = 2.0.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

private fun DrawScope.drawIndigoPlanetRidge(width: Float, height: Float, path: Path) {
    val ridgeColor = Color(0xFF0A101D)
    val cityGold = Color(0x77FFAA00)

    path.reset()
    path.moveTo(0f, height)
    path.lineTo(0f, height * 0.85f)
    path.lineTo(width * 0.18f, height * 0.81f)
    path.lineTo(width * 0.38f, height * 0.88f)
    path.lineTo(width * 0.62f, height * 0.83f)
    path.lineTo(width * 0.85f, height * 0.89f)
    path.lineTo(width, height * 0.82f)
    path.lineTo(width, height)
    path.close()
    drawPath(path, ridgeColor)

    // Blue rim light on the planetary peaks
    drawLine(
        color = Color(0x551E88E5),
        start = Offset(0f, height * 0.85f),
        end = Offset(width * 0.18f, height * 0.81f),
        strokeWidth = 2.5f
    )
    drawLine(
        color = Color(0x551E88E5),
        start = Offset(width * 0.38f, height * 0.88f),
        end = Offset(width * 0.62f, height * 0.83f),
        strokeWidth = 2.5f
    )

    // Glowing city light clusters in the valleys below
    val lights = listOf(
        Offset(width * 0.42f, height * 0.91f),
        Offset(width * 0.45f, height * 0.92f),
        Offset(width * 0.48f, height * 0.90f),
        Offset(width * 0.52f, height * 0.91f),
        Offset(width * 0.55f, height * 0.92f)
    )
    lights.forEach { pos ->
        drawCircle(color = cityGold, radius = 2.dp.toPx(), center = pos)
    }
}

@Composable
fun CosmicSingularityDisplayOverlay(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cosmic_disp_anim")
    val rot by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cosmic_disp_rot"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Mini accretion vortex watermark in top-right of LCD
        val vortexCenter = Offset(width - 32.dp.toPx(), 28.dp.toPx())
        val vr = 22.dp.toPx()

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x33FF2A4D), Color(0x12FFAA00), Color.Transparent),
                center = vortexCenter,
                radius = vr * 1.8f
            ),
            radius = vr * 1.8f,
            center = vortexCenter
        )

        rotate(rot, pivot = vortexCenter) {
            drawArc(
                color = Color(0x88FF2A4D),
                startAngle = 30f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(vortexCenter.x - vr, vortexCenter.y - vr * 0.45f),
                size = Size(vr * 2, vr * 0.9f),
                style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = Color(0x88FFAA00),
                startAngle = 210f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(vortexCenter.x - vr * 0.75f, vortexCenter.y - vr * 0.35f),
                size = Size(vr * 1.5f, vr * 0.7f),
                style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}
