package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Procedural vector path populator for the classic geometric Batman insignia.
 * Normalized and scaled to fit the provided width and height cleanly without heap allocation.
 */
fun populateBatmanLogoPath(path: Path, width: Float, height: Float) {
    path.reset()
    val cx = width / 2f
    val cy = height / 2f
    val halfW = width / 2f
    val halfH = height / 2f

    // Center top notch between ears
    path.moveTo(cx, cy - 0.28f * halfH)

    // Right ear inner slope
    path.lineTo(cx + 0.05f * halfW, cy - 0.30f * halfH)
    // Right ear tip (sharp point upward)
    path.lineTo(cx + 0.085f * halfW, cy - 0.72f * halfH)
    // Right ear outer slope down to wing neck
    path.lineTo(cx + 0.16f * halfW, cy - 0.44f * halfH)

    // Right upper wing arching upward and out to sharp wing tip
    path.cubicTo(
        cx + 0.32f * halfW, cy - 0.68f * halfH,
        cx + 0.68f * halfW, cy - 0.74f * halfH,
        cx + halfW, cy - 0.35f * halfH
    )

    // Outer wing edge curving down to first bottom scallop
    path.cubicTo(
        cx + 0.88f * halfW, cy - 0.05f * halfH,
        cx + 0.78f * halfW, cy + 0.18f * halfH,
        cx + 0.68f * halfW, cy + 0.18f * halfH
    )

    // First scallop curving in to second cusp
    path.cubicTo(
        cx + 0.60f * halfW, cy + 0.40f * halfH,
        cx + 0.46f * halfW, cy + 0.52f * halfH,
        cx + 0.38f * halfW, cy + 0.48f * halfH
    )

    // Second scallop curving in to third cusp
    path.cubicTo(
        cx + 0.32f * halfW, cy + 0.70f * halfH,
        cx + 0.18f * halfW, cy + 0.78f * halfH,
        cx + 0.12f * halfW, cy + 0.72f * halfH
    )

    // Third scallop curving in to central tail tip
    path.cubicTo(
        cx + 0.08f * halfW, cy + 0.88f * halfH,
        cx + 0.03f * halfW, cy + 0.96f * halfH,
        cx, cy + halfH
    )

    // Left side - symmetrical mirror
    path.cubicTo(
        cx - 0.03f * halfW, cy + 0.96f * halfH,
        cx - 0.08f * halfW, cy + 0.88f * halfH,
        cx - 0.12f * halfW, cy + 0.72f * halfH
    )
    path.cubicTo(
        cx - 0.18f * halfW, cy + 0.78f * halfH,
        cx - 0.32f * halfW, cy + 0.70f * halfH,
        cx - 0.38f * halfW, cy + 0.48f * halfH
    )
    path.cubicTo(
        cx - 0.46f * halfW, cy + 0.52f * halfH,
        cx - 0.60f * halfW, cy + 0.40f * halfH,
        cx - 0.68f * halfW, cy + 0.18f * halfH
    )
    path.cubicTo(
        cx - 0.78f * halfW, cy + 0.18f * halfH,
        cx - 0.88f * halfW, cy - 0.05f * halfH,
        cx - halfW, cy - 0.35f * halfH
    )
    // Left upper wing arching back to neck
    path.cubicTo(
        cx - 0.68f * halfW, cy - 0.74f * halfH,
        cx - 0.32f * halfW, cy - 0.68f * halfH,
        cx - 0.16f * halfW, cy - 0.44f * halfH
    )
    // Left ear outer slope
    path.lineTo(cx - 0.085f * halfW, cy - 0.72f * halfH)
    // Left ear inner slope back to center notch
    path.lineTo(cx - 0.05f * halfW, cy - 0.30f * halfH)
    path.close()
}

/**
 * Procedural vector path generator for the classic geometric Batman insignia.
 */
fun createBatmanLogoPath(width: Float, height: Float): Path {
    val path = Path()
    populateBatmanLogoPath(path, width, height)
    return path
}

/**
 * Populates a stylized flying bat silhouette with organic wing flapping articulation.
 * [flapCycle] ranges from -1.0 (wings swept down) to +1.0 (wings arched up).
 */
fun populateAnimatedFlyingBatPath(path: Path, width: Float, height: Float, flapCycle: Float) {
    path.reset()
    val cx = width / 2f
    val cy = height / 2f
    val hw = width / 2f
    val hh = height / 2f

    // Dynamic wing flap vertical offset
    val wingTipYOffset = flapCycle * (hh * 0.45f)
    val midWingYOffset = flapCycle * (hh * 0.22f)

    // Head center with tiny pointed ears
    path.moveTo(cx, cy - 0.35f * hh)
    path.lineTo(cx + 0.06f * hw, cy - 0.40f * hh)
    path.lineTo(cx + 0.09f * hw, cy - 0.78f * hh) // right ear tip
    path.lineTo(cx + 0.16f * hw, cy - 0.44f * hh)

    // Right upper wing leading edge stretching out with dynamic flex
    path.cubicTo(
        cx + 0.42f * hw, cy - (0.75f * hh) + midWingYOffset,
        cx + 0.78f * hw, cy - (0.60f * hh) + wingTipYOffset * 0.7f,
        cx + hw, cy - (0.22f * hh) + wingTipYOffset
    )

    // Right wing trailing edge scallops
    path.cubicTo(
        cx + 0.82f * hw, cy + (0.22f * hh) + wingTipYOffset * 0.5f,
        cx + 0.66f * hw, cy + (0.36f * hh) + midWingYOffset * 0.5f,
        cx + 0.50f * hw, cy + (0.22f * hh)
    )
    path.cubicTo(
        cx + 0.38f * hw, cy + 0.48f * hh,
        cx + 0.25f * hw, cy + 0.58f * hh,
        cx + 0.15f * hw, cy + 0.40f * hh
    )
    // Tail tip
    path.cubicTo(
        cx + 0.08f * hw, cy + 0.80f * hh,
        cx + 0.03f * hw, cy + hh,
        cx, cy + 0.88f * hh
    )

    // Left side mirror with identical flap physics
    path.cubicTo(
        cx - 0.03f * hw, cy + hh,
        cx - 0.08f * hw, cy + 0.80f * hh,
        cx - 0.15f * hw, cy + 0.40f * hh
    )
    path.cubicTo(
        cx - 0.25f * hw, cy + 0.58f * hh,
        cx - 0.38f * hw, cy + 0.48f * hh,
        cx - 0.50f * hw, cy + (0.22f * hh)
    )
    path.cubicTo(
        cx - 0.66f * hw, cy + (0.36f * hh) + midWingYOffset * 0.5f,
        cx - 0.82f * hw, cy + (0.22f * hw) + wingTipYOffset * 0.5f,
        cx - hw, cy - (0.22f * hh) + wingTipYOffset
    )
    path.cubicTo(
        cx - 0.78f * hw, cy - (0.60f * hh) + wingTipYOffset * 0.7f,
        cx - 0.42f * hw, cy - (0.75f * hh) + midWingYOffset,
        cx - 0.16f * hw, cy - 0.44f * hh
    )
    path.lineTo(cx - 0.09f * hw, cy - 0.78f * hh) // left ear tip
    path.lineTo(cx - 0.06f * hw, cy - 0.40f * hh)
    path.close()
}

fun createAnimatedFlyingBatPath(width: Float, height: Float, flapCycle: Float): Path {
    val path = Path()
    populateAnimatedFlyingBatPath(path, width, height, flapCycle)
    return path
}

/**
 * Populates aerodynamic tactical Batarang path for interactive flight strikes.
 */
fun populateBatarangPath(path: Path, width: Float, height: Float) {
    path.reset()
    val cx = width / 2f
    val cy = height / 2f
    val hw = width / 2f
    val hh = height / 2f

    // Center grip crest
    path.moveTo(cx, cy - 0.20f * hh)
    path.lineTo(cx + 0.12f * hw, cy - 0.40f * hh)
    path.cubicTo(
        cx + 0.45f * hw, cy - 0.70f * hh,
        cx + 0.80f * hw, cy - 0.50f * hh,
        cx + hw, cy - 0.10f * hh
    )
    // Right blade cutting edge
    path.cubicTo(
        cx + 0.75f * hw, cy + 0.30f * hh,
        cx + 0.45f * hw, cy + 0.50f * hh,
        cx + 0.20f * hw, cy + 0.30f * hh
    )
    // Center notch
    path.lineTo(cx, cy + 0.65f * hh)
    // Left blade cutting edge
    path.lineTo(cx - 0.20f * hw, cy + 0.30f * hh)
    path.cubicTo(
        cx - 0.45f * hw, cy + 0.50f * hh,
        cx - 0.75f * hw, cy + 0.30f * hh,
        cx - hw, cy - 0.10f * hh
    )
    path.cubicTo(
        cx - 0.80f * hw, cy - 0.50f * hh,
        cx - 0.45f * hw, cy - 0.70f * hh,
        cx - 0.12f * hw, cy - 0.40f * hh
    )
    path.close()
}

fun createBatarangPath(width: Float, height: Float): Path {
    val path = Path()
    populateBatarangPath(path, width, height)
    return path
}

/**
 * Miniature standalone Batman Logo icon for Badges, Buttons, or Headers
 */
@Composable
fun BatmanLogoIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color(0xFFFFE500),
    strokeColor: Color? = null
) {
    val logoPath = remember { Path() }
    Canvas(modifier = modifier) {
        populateBatmanLogoPath(logoPath, size.width, size.height)
        drawPath(path = logoPath, color = tint, style = Fill)
        if (strokeColor != null) {
            drawPath(
                path = logoPath,
                color = strokeColor,
                style = Stroke(width = 1.2f, join = StrokeJoin.Round, cap = StrokeCap.Round)
            )
        }
    }
}

/**
 * Highly animated, silky-smooth, and unique Batman display screen atmosphere.
 *
 * Unique features:
 * 1. Sweeping Volumetric Bat-Signal: Searchlight beam roving dynamically across Gotham storm clouds.
 * 2. Animated Bat Swarm: Main glowing white bat with fluid flapping wings + distant silhouette companions.
 * 3. Gotham Rooftop Vigilante:
 *    - Batman breathing idle stance with subtle organic chest expansion.
 *    - Billowing cape with multi-frequency physics (primary billow wave + secondary surface ripples).
 *    - Crisp comic-book white rim lighting tracing cowl, ears, and flowing cape edge.
 *    - Glowing white eye slits with tactical pulse.
 * 4. Atmospheric Gotham Weather: Subtle diagonal rain streaks drifting smoothly down the screen.
 * 5. Periodic Sheet Lightning: Soft cinematic illumination of gothic clouds and skyscraper spires.
 * 6. Interactive Batarang Throw: Tapping unleashes a spinning Batarang across the skyline with sonar ripples.
 * 7. Wayne Tech HUD Telemetry: Rotating radar reticle, tactical frequency sweep, and encrypted alert quotes.
 */
@Composable
fun BatmanDisplayOverlay(
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    var isTapped by remember { mutableStateOf(false) }
    var activeEasterEgg by remember { mutableStateOf(false) }
    var quoteIndex by remember { mutableIntStateOf(0) }
    var batarangActive by remember { mutableStateOf(false) }
    val batarangAnim = remember { Animatable(0f) }
    val lightningAnim = remember { Animatable(0f) }

    // Pre-allocated paths to avoid GC allocations during high frame-rate animations
    val batPath = remember { Path() }
    val distBatPath = remember { Path() }
    val batarangPath = remember { Path() }
    val ledgePath = remember { Path() }
    val capePath = remember { Path() }
    val cowlPath = remember { Path() }
    val rimLightPath = remember { Path() }
    val capeRimLightPath = remember { Path() }
    val leftEyePath = remember { Path() }
    val rightEyePath = remember { Path() }
    val signalConePath = remember { Path() }
    val signalInsigniaPath = remember { Path() }

    val quotes = listOf(
        "I AM VENGEANCE. I AM THE NIGHT.",
        "BAT-SIGNAL: SUMMONED // WAYNE TECH",
        "GOTHAM DEFENSE: 100% SECURE",
        "JUSTICE NEVER SLEEPS.",
        "TACTICAL SONAR: TARGET LOCKED",
        "THE NIGHT BELONGS TO US."
    )

    // Master Infinite Transition for silky-smooth 60/120fps clock synchrony
    val infiniteTransition = rememberInfiniteTransition(label = "batman_master_anim")

    // 1. Sweeping Bat-Signal Searchlight Beam (Harmonic pendulum sweep)
    val searchlightAngle = infiniteTransition.animateFloat(
        initialValue = -16f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "searchlight_sweep_angle"
    )

    val signalPulseAlpha = infiniteTransition.animateFloat(
        initialValue = 0.16f,
        targetValue = 0.36f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "signal_pulse_glow"
    )

    // 2. Animated Flying Bats Flight Traversal & Wing Flap Physics
    val leadBatXProgress = infiniteTransition.animateFloat(
        initialValue = -0.15f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "lead_bat_x"
    )

    val wingFlapCycle = infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 320, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bat_wing_flap"
    )

    // 3. Batman Rooftop Stance & Multi-frequency Cape Dynamics
    val breathingRise = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "batman_breathing"
    )

    val capePrimaryFlutter = infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cape_primary_flutter"
    )

    val capeSecondaryRipple = infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cape_secondary_ripple"
    )

    // 4. Cowl Eye Luminescence & Skyline Beacon
    val eyeGlowPulse = infiniteTransition.animateFloat(
        initialValue = 0.70f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "eye_glow_pulse"
    )

    val beaconBlink = infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beacon_blink"
    )

    // 5. Wayne Tech Tactical Sonar Wave & Radar Reticle Rotation
    val sonarPulseProgress = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sonar_pulse"
    )

    val radarRotationAngle = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_rot"
    )

    // 6. Smooth Rain Fall Cycle (0f to 1f continuous seamless fall)
    val rainTime = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rain_cycle"
    )

    // Interactive Tap spring response
    val tapSpringScale = animateFloatAsState(
        targetValue = if (isTapped) 1.06f else 1.0f,
        animationSpec = spring(dampingRatio = 0.52f, stiffness = 850f),
        label = "tap_spring_scale"
    )

    LaunchedEffect(Unit) {
        while (true) {
            delay(9000)
            // Double-strike cinematic lightning with smooth interpolation
            lightningAnim.animateTo(0.45f, tween(70, easing = LinearEasing))
            lightningAnim.animateTo(0.12f, tween(50, easing = LinearEasing))
            lightningAnim.animateTo(0.70f, tween(60, easing = LinearEasing))
            lightningAnim.animateTo(0f, tween(160, easing = FastOutSlowInEasing))
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                isTapped = true
                activeEasterEgg = true
                quoteIndex = (quoteIndex + 1) % quotes.size

                // Trigger interactive Batarang throw and lightning strike smoothly
                scope.launch {
                    lightningAnim.animateTo(0.65f, tween(60, easing = LinearEasing))
                    lightningAnim.animateTo(0.15f, tween(40, easing = LinearEasing))
                    lightningAnim.animateTo(0.45f, tween(60, easing = LinearEasing))
                    lightningAnim.animateTo(0f, tween(160, easing = FastOutSlowInEasing))
                }

                scope.launch {
                    batarangActive = true
                    batarangAnim.snapTo(0f)
                    batarangAnim.animateTo(1f, tween(durationMillis = 650, easing = FastOutSlowInEasing))
                    batarangActive = false
                }

                scope.launch {
                    delay(160)
                    isTapped = false
                    delay(2600)
                    activeEasterEgg = false
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val s = tapSpringScale.value
                    scaleX = s
                    scaleY = s
                }
        ) {
            val width = size.width
            val height = size.height

            val searchlightAngle = searchlightAngle.value
            val signalPulseAlpha = signalPulseAlpha.value
            val leadBatXProgress = leadBatXProgress.value
            val wingFlapCycle = wingFlapCycle.value
            val breathingRise = breathingRise.value
            val capePrimaryFlutter = capePrimaryFlutter.value
            val capeSecondaryRipple = capeSecondaryRipple.value
            val eyeGlowPulse = eyeGlowPulse.value
            val beaconBlink = beaconBlink.value
            val sonarPulseProgress = sonarPulseProgress.value
            val radarRotationAngle = radarRotationAngle.value
            val rainTime = rainTime.value
            val lightningIntensity = lightningAnim.value
            val batarangProgress = batarangAnim.value

            // 1. GOTHAM MIDNIGHT CANVAS WITH DYNAMIC LIGHTNING ILLUMINATION
            val skyGradient = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF04060A),
                    Color(0xFF080C14).copy(
                        red = 0.03f + lightningIntensity * 0.18f,
                        green = 0.05f + lightningIntensity * 0.22f,
                        blue = 0.08f + lightningIntensity * 0.35f
                    ),
                    Color(0xFF07090F)
                )
            )
            drawRect(brush = skyGradient)

            // 2. SOFT DRIFTING GOTHAM STORM CLOUD LAYERS
            val cloudY = height * 0.28f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF131A27).copy(alpha = 0.45f + lightningIntensity * 0.3f),
                        Color(0xFF0C111C).copy(alpha = 0.25f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.35f, cloudY),
                    radius = width * 0.55f
                ),
                radius = width * 0.55f,
                center = Offset(width * 0.35f, cloudY)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF161E2E).copy(alpha = 0.40f + lightningIntensity * 0.35f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.72f, cloudY + 15.dp.toPx()),
                    radius = width * 0.50f
                ),
                radius = width * 0.50f,
                center = Offset(width * 0.72f, cloudY + 15.dp.toPx())
            )

            // 3. GOTHAM CITY SKYLINE SILHOUETTES WITH WINDOWS & ANTENNAS
            val skylineBaseY = height * 0.94f
            val buildings = listOf(
                Triple(0.01f, 0.10f, 34f),
                Triple(0.12f, 0.11f, 52f), // Wayne Tower with spire
                Triple(0.24f, 0.08f, 28f),
                Triple(0.33f, 0.13f, 42f),
                Triple(0.47f, 0.09f, 25f),
                Triple(0.57f, 0.13f, 38f),
                Triple(0.71f, 0.10f, 30f),
                Triple(0.82f, 0.12f, 48f),
                Triple(0.93f, 0.08f, 26f)
            )

            for ((relX, relW, hDp) in buildings) {
                val bx = width * relX
                val bw = width * relW
                val bh = hDp.dp.toPx()
                val by = skylineBaseY - bh

                // Building body
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF121822), Color(0xFF070A0F)),
                        startY = by,
                        endY = skylineBaseY
                    ),
                    topLeft = Offset(bx, by),
                    size = Size(bw, bh)
                )

                // Backlit building edge highlight during lightning or ambient
                drawLine(
                    color = Color.White.copy(alpha = 0.12f + lightningIntensity * 0.35f),
                    start = Offset(bx, by),
                    end = Offset(bx, skylineBaseY),
                    strokeWidth = 0.9f
                )

                // Glowing amber office windows
                var winY = by + 6.dp.toPx()
                while (winY < skylineBaseY - 4.dp.toPx()) {
                    drawCircle(
                        color = Color(0x35FFE500),
                        radius = 1.0.dp.toPx(),
                        center = Offset(bx + bw * 0.38f, winY)
                    )
                    drawCircle(
                        color = Color(0x22FFE500),
                        radius = 0.9.dp.toPx(),
                        center = Offset(bx + bw * 0.72f, winY)
                    )
                    winY += 8.dp.toPx()
                }
            }

            // Wayne Tower mast & flashing red warning beacon (relX = 0.12)
            val towerX = width * 0.12f + (width * 0.11f) / 2f
            val spireTopY = skylineBaseY - 52.dp.toPx() - 16.dp.toPx()
            drawLine(
                color = Color(0x77FFFFFF),
                start = Offset(towerX, skylineBaseY - 52.dp.toPx()),
                end = Offset(towerX, spireTopY),
                strokeWidth = 1.2f
            )
            drawCircle(
                color = Color(0xFFFF2222).copy(alpha = beaconBlink),
                radius = 2.2.dp.toPx(),
                center = Offset(towerX, spireTopY)
            )

            // 4. VOLUMETRIC SWEEPING BAT-SIGNAL SEARCHLIGHT
            // Spot moves with sinusoidal sweep across Gotham sky
            val projectorBaseX = width * 0.32f
            val projectorBaseY = skylineBaseY - 20.dp.toPx()

            val sweepRad = searchlightAngle * (PI / 180f).toFloat()
            val signalDistance = height * 0.65f
            val signalCenterX = projectorBaseX + sin(sweepRad) * signalDistance * 1.1f
            val signalCenterY = (height * 0.30f) + cos(sweepRad) * 10.dp.toPx()

            val beamWidthAtClouds = 64.dp.toPx()
            val beamLeftTop = Offset(signalCenterX - beamWidthAtClouds / 2f, signalCenterY)
            val beamRightTop = Offset(signalCenterX + beamWidthAtClouds / 2f, signalCenterY)
            val beamBottom = Offset(projectorBaseX, projectorBaseY)

            // Volumetric searchlight cone (zero-allocation)
            signalConePath.reset()
            signalConePath.moveTo(beamBottom.x - 4f, beamBottom.y)
            signalConePath.lineTo(beamLeftTop.x, beamLeftTop.y)
            signalConePath.lineTo(beamRightTop.x, beamRightTop.y)
            signalConePath.lineTo(beamBottom.x + 4f, beamBottom.y)
            signalConePath.close()

            drawPath(
                path = signalConePath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x35FFE500).copy(alpha = signalPulseAlpha * 0.9f),
                        Color(0x12FFE500).copy(alpha = signalPulseAlpha * 0.5f),
                        Color.Transparent
                    ),
                    startY = signalCenterY,
                    endY = projectorBaseY
                ),
                style = Fill
            )

            // Luminous circular searchlight spotlight projected on clouds
            val spotRadius = (width.coerceAtMost(height) * 0.32f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFEA00).copy(alpha = signalPulseAlpha * 1.3f.coerceAtMost(0.48f)),
                        Color(0xFFFFD000).copy(alpha = signalPulseAlpha * 0.6f),
                        Color(0x22FFE500),
                        Color.Transparent
                    ),
                    center = Offset(signalCenterX, signalCenterY),
                    radius = spotRadius
                ),
                radius = spotRadius,
                center = Offset(signalCenterX, signalCenterY)
            )

            // Crisp Batman insignia embedded inside the roving spotlight (zero-allocation)
            rotate(degrees = searchlightAngle * 0.4f, pivot = Offset(signalCenterX, signalCenterY)) {
                val logoW = spotRadius * 1.08f
                val logoH = logoW * 0.52f
                populateBatmanLogoPath(signalInsigniaPath, logoW, logoH)
                translate(signalCenterX - logoW / 2f, signalCenterY - logoH / 2f) {
                    // Dark core silhouette inside the spotlight
                    drawPath(
                        path = signalInsigniaPath,
                        color = Color(0x9907090E),
                        style = Fill
                    )
                    // Glowing golden outline
                    drawPath(
                        path = signalInsigniaPath,
                        color = Color(0xFFFFE500).copy(alpha = (signalPulseAlpha * 1.5f).coerceAtMost(0.65f)),
                        style = Stroke(width = 1.6f, join = StrokeJoin.Round)
                    )
                }
            }

            // 5. DETECTIVE MODE SONAR SCANNER PULSE FROM BATMAN'S POST
            val heroLedgeY = height * 0.98f
            val heroX = width * 0.78f
            val heroH = (height * 0.54f).coerceIn(110.dp.toPx(), 200.dp.toPx())
            val heroHeadY = heroLedgeY - heroH + breathingRise.dp.toPx()
            val sonarOrigin = Offset(heroX, heroHeadY + 12.dp.toPx())

            // Expanding sonar rings
            val maxSonarRadius = width * 0.85f
            val curSonarRadius = sonarPulseProgress * maxSonarRadius
            val sonarAlpha = (1f - sonarPulseProgress).coerceIn(0f, 0.45f)

            drawCircle(
                color = Color(0xFFFFE500).copy(alpha = sonarAlpha * 0.5f),
                radius = curSonarRadius,
                center = sonarOrigin,
                style = Stroke(
                    width = 1.2f,
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                        floatArrayOf(12f, 8f),
                        0f
                    )
                )
            )
            // Faint secondary sonar ring
            val secondarySonarRadius = ((sonarPulseProgress + 0.5f) % 1f) * maxSonarRadius
            val secSonarAlpha = (1f - ((sonarPulseProgress + 0.5f) % 1f)).coerceIn(0f, 0.35f)
            drawCircle(
                color = Color(0x66B0E0E6).copy(alpha = secSonarAlpha * 0.3f),
                radius = secondarySonarRadius,
                center = sonarOrigin,
                style = Stroke(width = 0.8f)
            )

            // 6. ANIMATED FLYING WHITE BATS IN GOTHAM SKY WITH WING-BEAT FLAPPING
            // Main lead white bat
            val batX = width * leadBatXProgress
            val batY = height * 0.22f + (sin(leadBatXProgress * 6.28) * 14.dp.toPx()).toFloat()
            val batW = 40.dp.toPx()
            val batH = 20.dp.toPx()

            // Soft luminous motion aura around the white bat
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.40f),
                        Color(0x55B0E0E6),
                        Color.Transparent
                    ),
                    center = Offset(batX, batY),
                    radius = 28.dp.toPx()
                ),
                radius = 28.dp.toPx(),
                center = Offset(batX, batY)
            )

            populateAnimatedFlyingBatPath(batPath, batW, batH, wingFlapCycle)
            translate(batX - batW / 2f, batY - batH / 2f) {
                drawPath(path = batPath, color = Color.White, style = Fill)
                drawPath(
                    path = batPath,
                    color = Color(0xEEFFFFFF),
                    style = Stroke(width = 1.3f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }

            // Distant trailing companion shadow bat
            val distBatX = batX - 55.dp.toPx()
            val distBatY = batY + 16.dp.toPx()
            if (distBatX > -20f && distBatX < width + 20f) {
                val distBatW = 22.dp.toPx()
                val distBatH = 11.dp.toPx()
                populateAnimatedFlyingBatPath(distBatPath, distBatW, distBatH, -wingFlapCycle)
                translate(distBatX - distBatW / 2f, distBatY - distBatH / 2f) {
                    drawPath(path = distBatPath, color = Color(0x99A0B8D0), style = Fill)
                }
            }

            // 7. SLEEK GOTHAM RAIN STREAKS
            val rainAngleOffset = 8.dp.toPx()
            val rainCount = 28
            for (i in 0 until rainCount) {
                val seed = (i * 137.5f) % 1.0f
                val rx = (width * seed + (rainTime * 300f * (0.8f + seed * 0.4f))) % (width + 40.dp.toPx()) - 20.dp.toPx()
                val ry = ((rainTime + seed) % 1.0f) * height
                val rainLen = 14.dp.toPx() + seed * 12.dp.toPx()
                val rainAlpha = 0.14f + (seed * 0.18f)

                drawLine(
                    color = Color.White.copy(alpha = rainAlpha),
                    start = Offset(rx, ry),
                    end = Offset(rx - rainAngleOffset * 0.4f, ry + rainLen),
                    strokeWidth = 0.8f
                )
            }

            // 8. FOREGROUND ROOFTOP PARAPET & GARGOLYE LEDGE
            ledgePath.reset()
            ledgePath.moveTo(heroX - heroH * 0.50f, heroLedgeY)
            ledgePath.lineTo(width + 10f, heroLedgeY - 20.dp.toPx())
            ledgePath.lineTo(width + 10f, height + 10f)
            ledgePath.lineTo(heroX - heroH * 0.60f, height + 10f)
            ledgePath.close()

            drawPath(
                path = ledgePath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF141922), Color(0xFF07090C)),
                    startY = heroLedgeY - 22.dp.toPx(),
                    endY = height
                ),
                style = Fill
            )
            // Ledge razor-sharp rim light
            drawLine(
                color = Color(0x55FFFFFF),
                start = Offset(heroX - heroH * 0.50f, heroLedgeY),
                end = Offset(width, heroLedgeY - 20.dp.toPx()),
                strokeWidth = 1.6f
            )

            // 9. THE DARK KNIGHT FOREGROUND HERO SILHOUETTE
            val headW = heroH * 0.14f
            val headH = heroH * 0.18f

            // Multi-frequency billowing cape waves
            val capeWave1 = capePrimaryFlutter * 5.dp.toPx()
            val capeWave2 = capeSecondaryRipple * 2.5.dp.toPx()
            val totalCapeOffset = capeWave1 + capeWave2

            capePath.reset()
            // Left shoulder anchor
            capePath.moveTo(heroX - headW * 1.15f, heroHeadY + headH * 0.95f)
            // Downward billow arc with dual sinusoidal wind propagation
            capePath.cubicTo(
                heroX - headW * 2.3f + totalCapeOffset, heroHeadY + heroH * 0.42f,
                heroX - headW * 2.8f - totalCapeOffset, heroHeadY + heroH * 0.72f,
                heroX - headW * 2.3f + totalCapeOffset * 1.4f, heroLedgeY + 6.dp.toPx()
            )
            // Scalloped cape hem scallops
            capePath.cubicTo(
                heroX - headW * 1.7f, heroLedgeY + 1.dp.toPx() + totalCapeOffset * 0.3f,
                heroX - headW * 1.3f, heroLedgeY + 4.dp.toPx(),
                heroX - headW * 0.85f, heroLedgeY
            )
            capePath.cubicTo(
                heroX - headW * 0.45f, heroLedgeY + 3.dp.toPx(),
                heroX, heroLedgeY + 1.dp.toPx(),
                heroX + headW * 0.65f, heroLedgeY
            )
            // Up right torso
            capePath.lineTo(heroX + headW * 0.82f, heroHeadY + headH * 1.25f)
            // Neck/shoulder
            capePath.lineTo(heroX + headW * 0.52f, heroHeadY + headH * 0.72f)
            capePath.close()

            // Fill stealth cape
            drawPath(
                path = capePath,
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF06070B), Color(0xFF0E121A), Color(0xFF080B0F)),
                    startX = heroX - headW * 2.8f,
                    endX = heroX + headW * 0.82f
                ),
                style = Fill
            )

            // Batman's Cowl & Stance with sharp pointed ears
            cowlPath.reset()
            cowlPath.moveTo(heroX - headW * 0.68f, heroHeadY + headH)
            cowlPath.lineTo(heroX - headW * 0.56f, heroHeadY + headH * 0.35f)
            // Left ear tip
            cowlPath.lineTo(heroX - headW * 0.48f, heroHeadY - headH * 0.28f)
            // Left ear inner slope
            cowlPath.lineTo(heroX - headW * 0.20f, heroHeadY + headH * 0.14f)
            // Crown notch
            cowlPath.lineTo(heroX + headW * 0.10f, heroHeadY + headH * 0.14f)
            // Right ear tip
            cowlPath.lineTo(heroX + headW * 0.36f, heroHeadY - headH * 0.22f)
            // Right ear outer slope
            cowlPath.lineTo(heroX + headW * 0.44f, heroHeadY + headH * 0.35f)
            // Jawline turned toward Gotham
            cowlPath.lineTo(heroX + headW * 0.52f, heroHeadY + headH * 0.70f)
            // Chin
            cowlPath.lineTo(heroX + headW * 0.22f, heroHeadY + headH * 0.95f)
            // Chest
            cowlPath.lineTo(heroX - headW * 0.68f, heroHeadY + headH)
            cowlPath.close()

            // Fill Cowl
            drawPath(
                path = cowlPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF131822), Color(0xFF06080B)),
                    startY = heroHeadY - headH * 0.28f,
                    endY = heroHeadY + headH
                ),
                style = Fill
            )

            // COMIC-BOOK CRISP WHITE RIM LIGHT ACCENT
            rimLightPath.reset()
            rimLightPath.moveTo(heroX - headW * 0.56f, heroHeadY + headH * 0.62f)
            rimLightPath.lineTo(heroX - headW * 0.56f, heroHeadY + headH * 0.35f)
            rimLightPath.lineTo(heroX - headW * 0.48f, heroHeadY - headH * 0.28f)
            rimLightPath.lineTo(heroX - headW * 0.20f, heroHeadY + headH * 0.14f)
            rimLightPath.lineTo(heroX + headW * 0.10f, heroHeadY + headH * 0.14f)
            rimLightPath.lineTo(heroX + headW * 0.36f, heroHeadY - headH * 0.22f)
            rimLightPath.lineTo(heroX + headW * 0.44f, heroHeadY + headH * 0.35f)

            drawPath(
                path = rimLightPath,
                color = Color.White.copy(alpha = 0.90f),
                style = Stroke(width = 2.0f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // White rim light along the flowing outer cape billow
            capeRimLightPath.reset()
            capeRimLightPath.moveTo(heroX - headW * 1.15f, heroHeadY + headH * 0.95f)
            capeRimLightPath.cubicTo(
                heroX - headW * 2.3f + totalCapeOffset, heroHeadY + heroH * 0.42f,
                heroX - headW * 2.8f - totalCapeOffset, heroHeadY + heroH * 0.72f,
                heroX - headW * 2.3f + totalCapeOffset * 1.4f, heroLedgeY + 6.dp.toPx()
            )

            drawPath(
                path = capeRimLightPath,
                color = Color.White.copy(alpha = 0.45f),
                style = Stroke(width = 1.4f, cap = StrokeCap.Round)
            )

            // Glowing White Cowl Eye Slits!
            val eyeCenterY = heroHeadY + headH * 0.48f
            val eyeLeftX = heroX - headW * 0.14f
            val eyeRightX = heroX + headW * 0.18f

            leftEyePath.reset()
            leftEyePath.moveTo(eyeLeftX - 3.5.dp.toPx(), eyeCenterY + 0.8.dp.toPx())
            leftEyePath.lineTo(eyeLeftX, eyeCenterY - 1.2.dp.toPx())
            leftEyePath.lineTo(eyeLeftX + 3.5.dp.toPx(), eyeCenterY + 0.8.dp.toPx())
            leftEyePath.close()

            rightEyePath.reset()
            rightEyePath.moveTo(eyeRightX - 3.5.dp.toPx(), eyeCenterY + 0.8.dp.toPx())
            rightEyePath.lineTo(eyeRightX, eyeCenterY - 1.2.dp.toPx())
            rightEyePath.lineTo(eyeRightX + 3.5.dp.toPx(), eyeCenterY + 0.8.dp.toPx())
            rightEyePath.close()

            val activeEyeAlpha = eyeGlowPulse.coerceIn(0.6f, 1.0f)
            drawPath(path = leftEyePath, color = Color.White.copy(alpha = activeEyeAlpha), style = Fill)
            drawPath(path = rightEyePath, color = Color.White.copy(alpha = activeEyeAlpha), style = Fill)

            // Gold tactical utility belt highlight
            drawRoundRect(
                color = Color(0x99FFE500),
                topLeft = Offset(heroX - headW * 0.48f, heroHeadY + heroH * 0.60f),
                size = Size(headW * 1.05f, 3.2.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
            )

            // 10. INTERACTIVE BATARANG FLIGHT PATH (When user taps!)
            if (batarangActive) {
                val startBX = heroX - headW * 0.8f
                val startBY = heroHeadY + headH * 0.8f
                val targetBX = width * 0.12f
                val targetBY = height * 0.20f

                // Curved parabolic arc trajectory
                val curBX = startBX + (targetBX - startBX) * batarangProgress
                val arcLift = sin(batarangProgress * PI.toFloat()) * (height * 0.35f)
                val curBY = startBY + (targetBY - startBY) * batarangProgress - arcLift

                val batarangSpinDeg = batarangProgress * 1080f
                val batarangSizeW = 28.dp.toPx()
                val batarangSizeH = 14.dp.toPx()

                // Motion trail glow
                drawCircle(
                    color = Color(0x66FFE500),
                    radius = 16.dp.toPx(),
                    center = Offset(curBX, curBY)
                )

                rotate(degrees = batarangSpinDeg, pivot = Offset(curBX, curBY)) {
                    populateBatarangPath(batarangPath, batarangSizeW, batarangSizeH)
                    translate(curBX - batarangSizeW / 2f, curBY - batarangSizeH / 2f) {
                        drawPath(path = batarangPath, color = Color(0xFFFFE500), style = Fill)
                        drawPath(
                            path = batarangPath,
                            color = Color.White,
                            style = Stroke(width = 1.2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }
                }
            }

            // 11. WAYNE TECH TACTICAL HUD RETICLE (Top Left Corner)
            val hudOrigin = Offset(26.dp.toPx(), 26.dp.toPx())
            val hudRadius = 14.dp.toPx()

            rotate(degrees = radarRotationAngle, pivot = hudOrigin) {
                drawCircle(
                    color = Color(0x44FFE500),
                    radius = hudRadius,
                    center = hudOrigin,
                    style = Stroke(
                        width = 1f,
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                            floatArrayOf(6f, 6f),
                            0f
                        )
                    )
                )
                // Crosshairs
                drawLine(
                    color = Color(0x66FFE500),
                    start = Offset(hudOrigin.x - hudRadius * 1.3f, hudOrigin.y),
                    end = Offset(hudOrigin.x + hudRadius * 1.3f, hudOrigin.y),
                    strokeWidth = 0.8f
                )
                drawLine(
                    color = Color(0x66FFE500),
                    start = Offset(hudOrigin.x, hudOrigin.y - hudRadius * 1.3f),
                    end = Offset(hudOrigin.x, hudOrigin.y + hudRadius * 1.3f),
                    strokeWidth = 0.8f
                )
            }
        }

        // 12. WAYNE TECH HUD TELEMETRY ALERT (Reveals on tap / Easter egg at bottom of display)
        AnimatedVisibility(
            visible = activeEasterEgg,
            enter = fadeIn(tween(180)) + slideInVertically(tween(250)) { -it / 2 },
            exit = fadeOut(tween(250)) + slideOutVertically(tween(250)) { -it / 2 },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp)
        ) {
            Surface(
                color = Color(0xF00A0D14),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFE500)),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    BatmanLogoIcon(
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFFFFE500)
                    )
                    Text(
                        text = quotes[quoteIndex],
                        color = Color(0xFFFFE500),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.8.sp
                    )
                }
            }
        }
    }
}

/**
 * Subtle Wayne Tech / Batcomputer ambient background effect for the entire calculator screen.
 * Displays fine tactical grid coordinates, faint ambient golden searchlight reflections, and stealth scanlines.
 */
@Composable
fun BatmanScreenBackground(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "batman_screen_bg_anim")
    val ambientPulse = infiniteTransition.animateFloat(
        initialValue = 0.03f,
        targetValue = 0.09f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambient_bg_pulse"
    )
    val gridSweep = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "grid_sweep"
    )

    val scanLineColors = remember {
        listOf(
            Color.Transparent,
            Color(0x25FFE500),
            Color(0x40FFE500),
            Color(0x25FFE500),
            Color.Transparent
        )
    }
    val scanBrush = remember { Brush.horizontalGradient(scanLineColors) }
    val searchlightBaseColor = remember { Color(0xFFFFE500) }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val pulse = ambientPulse.value
        val sweep = gridSweep.value

        // Subtle Wayne Tech tactical grid lines (ultra faint stealth aesthetic)
        val gridStep = 48.dp.toPx()
        var gx = 0f
        while (gx < width) {
            drawLine(
                color = Color(0x0AFFE500),
                start = Offset(gx, 0f),
                end = Offset(gx, height),
                strokeWidth = 0.5f
            )
            gx += gridStep
        }
        var gy = 0f
        while (gy < height) {
            drawLine(
                color = Color(0x0AFFE500),
                start = Offset(0f, gy),
                end = Offset(width, gy),
                strokeWidth = 0.5f
            )
            gy += gridStep
        }

        // Horizontal tactical scan line moving smoothly down the screen
        val scanY = sweep * height
        drawLine(
            brush = scanBrush,
            start = Offset(0f, scanY),
            end = Offset(width, scanY),
            strokeWidth = 1.0f
        )

        // Ambient golden searchlight reflection at bottom-left
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    searchlightBaseColor.copy(alpha = pulse),
                    Color.Transparent
                ),
                center = Offset(width * 0.15f, height * 0.88f),
                radius = width * 0.55f
            ),
            radius = width * 0.55f,
            center = Offset(width * 0.15f, height * 0.88f)
        )
    }
}

