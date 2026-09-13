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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.model.ThemePalette
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Full-screen animated backgrounds and tactile atmospheric overlays
 * crafted specifically for the 4 new premier studio themes:
 * 1. Retro Circuit 90034 (Embossed crimson conduit, vintage PCB traces, flowing electric pulses)
 * 2. Nothing Dossier Mono (Minimalist perforated dot matrix, asymmetric folder tab, sweeping LED wave)
 * 3. Swiss Bauhaus Dossier (Architectural tabbed card layers, warm dot grid, poppy red & golden ochre)
 * 4. Terracotta Studio (Matte clay body, glossy obsidian OLED header, glass specular gleam)
 */
private class StudioPathCache(
    val leftPcb: Path = Path(),
    val rightPcb: Path = Path(),
    val folderTabPath: Path = Path()
)

@Composable
fun StudioScreenBackground(
    theme: ThemePalette,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "studio_bg_anim")
    val flowProgressAnim = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "flow_progress"
    )
    val breathPulseAnim = infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath_pulse"
    )
    val glassSweepAnim = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "glass_sweep"
    )
    val studioCache = remember { StudioPathCache() }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val flow = flowProgressAnim.value
        val pulse = breathPulseAnim.value
        val sweep = glassSweepAnim.value

        when {
            theme.isRetroCircuit -> {
                drawRetroCircuitAtmosphere(w, h, flow, pulse, studioCache)
            }
            theme.isNothingDossier -> {
                drawNothingDossierAtmosphere(w, h, flow, pulse, studioCache)
            }
            theme.isBauhausDossier -> {
                drawBauhausDossierAtmosphere(w, h, flow, pulse)
            }
            theme.isTerracottaStudio -> {
                drawTerracottaStudioAtmosphere(w, h, sweep, pulse)
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------
// 1. RETRO CIRCUIT 90034 (Cyber-Vermilion Chassis, Warm Cream & Illuminated Gold PCB)
// Inspired directly by uploaded image: dad8332b0a3897bdff7cee441e2b326c.jpg
// ------------------------------------------------------------------------------------------------
private fun DrawScope.drawRetroCircuitAtmosphere(
    w: Float,
    h: Float,
    flow: Float,
    pulse: Float,
    cache: StudioPathCache
) {
    // 1A. Full Unified Housing: Rich Imperial Vermilion-Crimson Chassis
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF9E141D), Color(0xFF7E0F16), Color(0xFF56090E)),
            startY = 0f,
            endY = h
        ),
        size = Size(w, h)
    )

    // Subtle center chassis glow for tactile dimensionality
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0x24FFA3A8), Color.Transparent),
            center = Offset(w * 0.5f, h * 0.28f),
            radius = w * 0.75f
        ),
        radius = w * 0.75f,
        center = Offset(w * 0.5f, h * 0.28f)
    )

    // 1B. Upper Header: Embossed Industrial Conduit Track
    val conduitY = 14.dp.toPx()
    val conduitStroke = 3.dp.toPx()
    // Conduit drop shadow
    drawLine(
        color = Color(0x60000000),
        start = Offset(26.dp.toPx(), conduitY + 1.2.dp.toPx()),
        end = Offset(w - 26.dp.toPx(), conduitY + 1.2.dp.toPx()),
        strokeWidth = conduitStroke + 1.dp.toPx(),
        cap = StrokeCap.Round
    )
    // Conduit vibrant vermilion body
    drawLine(
        color = Color(0xFFD32F2F),
        start = Offset(26.dp.toPx(), conduitY),
        end = Offset(w - 26.dp.toPx(), conduitY),
        strokeWidth = conduitStroke,
        cap = StrokeCap.Round
    )
    // Conduit specular sheen highlight
    drawLine(
        color = Color(0x80FFA4A2),
        start = Offset(30.dp.toPx(), conduitY - 0.5.dp.toPx()),
        end = Offset(w - 30.dp.toPx(), conduitY - 0.5.dp.toPx()),
        strokeWidth = 1.dp.toPx(),
        cap = StrokeCap.Round
    )

    // Hex screws / corner rivets in upper chassis
    val screwLeft = Offset(14.dp.toPx(), conduitY)
    val screwRight = Offset(w - 14.dp.toPx(), conduitY)
    for (screw in listOf(screwLeft, screwRight)) {
        drawCircle(color = Color(0xFF380509), radius = 3.8.dp.toPx(), center = screw)
        drawCircle(color = Color(0xFFF59E0B), radius = 2.4.dp.toPx(), center = screw)
        drawLine(
            color = Color(0xFF380509),
            start = Offset(screw.x - 1.6.dp.toPx(), screw.y),
            end = Offset(screw.x + 1.6.dp.toPx(), screw.y),
            strokeWidth = 0.8.dp.toPx()
        )
    }

    // 1C. Silk-Screen Circuit Traces Along Left and Right Margins (Clean margin tracking outside keys)
    val traceColor = Color(0xFFD48B4B).copy(alpha = 0.45f)
    val traceStroke = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

    // Left Margin PCB Bus
    val lx = 8.dp.toPx()
    cache.leftPcb.reset()
    cache.leftPcb.moveTo(lx, 32.dp.toPx())
    cache.leftPcb.lineTo(lx, h * 0.45f)
    cache.leftPcb.lineTo(lx + 5.dp.toPx(), h * 0.49f)
    cache.leftPcb.lineTo(lx + 5.dp.toPx(), h * 0.70f)
    cache.leftPcb.lineTo(lx, h * 0.74f)
    cache.leftPcb.lineTo(lx, h - 22.dp.toPx())
    drawPath(cache.leftPcb, traceColor, style = traceStroke)

    // Left micro-vias / solder pads with gold plating
    val leftPads = listOf(
        Offset(lx, 38.dp.toPx()),
        Offset(lx + 5.dp.toPx(), h * 0.54f),
        Offset(lx + 5.dp.toPx(), h * 0.65f),
        Offset(lx, h - 26.dp.toPx())
    )
    for (p in leftPads) {
        drawCircle(color = Color(0xFFF59E0B), radius = 2.6.dp.toPx(), center = p)
        drawCircle(color = Color(0xFF380509), radius = 1.1.dp.toPx(), center = p)
    }

    // Right Margin PCB Bus
    val rx = w - 8.dp.toPx()
    cache.rightPcb.reset()
    cache.rightPcb.moveTo(rx, 34.dp.toPx())
    cache.rightPcb.lineTo(rx, h * 0.42f)
    cache.rightPcb.lineTo(rx - 5.dp.toPx(), h * 0.46f)
    cache.rightPcb.lineTo(rx - 5.dp.toPx(), h * 0.67f)
    cache.rightPcb.lineTo(rx, h * 0.71f)
    cache.rightPcb.lineTo(rx, h - 22.dp.toPx())
    drawPath(cache.rightPcb, traceColor, style = traceStroke)

    // Right test pads
    val rightPads = listOf(
        Offset(rx, 40.dp.toPx()),
        Offset(rx - 5.dp.toPx(), h * 0.50f),
        Offset(rx - 5.dp.toPx(), h * 0.61f),
        Offset(rx, h - 26.dp.toPx())
    )
    for (p in rightPads) {
        drawCircle(color = Color(0xFFF59E0B), radius = 2.6.dp.toPx(), center = p)
        drawCircle(color = Color(0xFF380509), radius = 1.1.dp.toPx(), center = p)
    }

    // 1D. Bottom Grounding Bar
    val chinY = h - 6.dp.toPx()
    drawLine(
        color = Color(0x50F59E0B),
        start = Offset(24.dp.toPx(), chinY),
        end = Offset(w - 24.dp.toPx(), chinY),
        strokeWidth = 1.2.dp.toPx()
    )

    // 1E. ANIMATED FLOWING ELECTRONS (Amber pulse on left, Cyan pulse on right)
    val leftFlowY = 38.dp.toPx() + (flow % 1f) * (h - 70.dp.toPx())
    val leftFlowX = if (leftFlowY in (h * 0.49f)..(h * 0.70f)) lx + 5.dp.toPx() else lx
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFC233), Color(0x66FFC233), Color.Transparent),
            center = Offset(leftFlowX, leftFlowY),
            radius = 7.dp.toPx()
        ),
        radius = 7.dp.toPx(),
        center = Offset(leftFlowX, leftFlowY)
    )
    drawCircle(color = Color.White, radius = 1.8.dp.toPx(), center = Offset(leftFlowX, leftFlowY))

    // Right cyan packet (offset phase)
    val rightFlowProgress = (flow + 0.5f) % 1f
    val rightFlowY = 40.dp.toPx() + rightFlowProgress * (h - 70.dp.toPx())
    val rightFlowX = if (rightFlowY in (h * 0.46f)..(h * 0.67f)) rx - 5.dp.toPx() else rx
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF00E5FF), Color(0x6600E5FF), Color.Transparent),
            center = Offset(rightFlowX, rightFlowY),
            radius = 6.5.dp.toPx()
        ),
        radius = 6.5.dp.toPx(),
        center = Offset(rightFlowX, rightFlowY)
    )
    drawCircle(color = Color.White, radius = 1.6.dp.toPx(), center = Offset(rightFlowX, rightFlowY))

    // Corner SMD Status LED (Soft pulsing warm amber)
    val ledPos = Offset(w - 16.dp.toPx(), 28.dp.toPx())
    drawCircle(
        color = Color(0xFFFFC233).copy(alpha = 0.25f + pulse * 0.5f),
        radius = 4.dp.toPx() + pulse * 2.dp.toPx(),
        center = ledPos
    )
    drawCircle(color = Color(0xFFFFC233), radius = 2.dp.toPx(), center = ledPos)
}

// ------------------------------------------------------------------------------------------------
// 2. NOTHING DOSSIER MONO (Perforated Dot Matrix & Asymmetric Folder Tab)
// Inspired directly by uploaded image: 6963819ac965069e66eb449120af0579.jpg
// ------------------------------------------------------------------------------------------------
private fun DrawScope.drawNothingDossierAtmosphere(
    w: Float,
    h: Float,
    flow: Float,
    pulse: Float,
    cache: StudioPathCache
) {
    // 2A. Deep Smoked Obsidian Background
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF141619), Color(0xFF0F1012), Color(0xFF0A0B0C))
        ),
        size = Size(w, h)
    )

    // 2B. Subtle, Calm Dot Matrix Across Entire Canvas (Refined, low opacity)
    val dotSpacing = 18.dp.toPx()
    val dotRadius = 1.1.dp.toPx()
    val waveCenterProgress = flow * 1.5f - 0.25f

    var y = 14.dp.toPx()
    while (y < h - 14.dp.toPx()) {
        var x = 14.dp.toPx()
        while (x < w - 14.dp.toPx()) {
            val dist = ((x / w) * 0.5f + (y / h) * 0.5f)
            val waveProximity = (1f - kotlin.math.abs(dist - waveCenterProgress) * 5f).coerceIn(0f, 1f)
            val dotAlpha = 0.12f + waveProximity * 0.22f

            drawCircle(
                color = Color(0xFFD0D4DC).copy(alpha = dotAlpha),
                radius = dotRadius,
                center = Offset(x, y)
            )
            x += dotSpacing
        }
        y += dotSpacing
    }

    // 2C. Upper Header: Asymmetric Folder Tab Contour Framing Header
    val tabH = 34.dp.toPx()
    cache.folderTabPath.reset()
    cache.folderTabPath.moveTo(12.dp.toPx(), 6.dp.toPx())
    cache.folderTabPath.lineTo(w * 0.40f, 6.dp.toPx())
    cache.folderTabPath.cubicTo(w * 0.44f, 6.dp.toPx(), w * 0.48f, tabH, w * 0.54f, tabH)
    cache.folderTabPath.lineTo(w - 12.dp.toPx(), tabH)

    // Tab accent stroke
    drawPath(
        path = cache.folderTabPath,
        color = Color(0xFF262A32),
        style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
    )

    // 2D. Signature Nothing Red LED Dot Indicator at tab shoulder
    val redLed = Offset(w * 0.40f + 8.dp.toPx(), 18.dp.toPx())
    drawCircle(
        color = Color(0xFFEB261E).copy(alpha = 0.25f + pulse * 0.55f),
        radius = 4.dp.toPx() + pulse * 2.dp.toPx(),
        center = redLed
    )
    drawCircle(color = Color(0xFFEB261E), radius = 2.dp.toPx(), center = redLed)

    // Margin tick marks
    val tickLength = 5.dp.toPx()
    val tickYs = listOf(h * 0.35f, h * 0.55f, h * 0.75f)
    for (ty in tickYs) {
        drawLine(
            color = Color(0xFF2A2E37),
            start = Offset(4.dp.toPx(), ty),
            end = Offset(4.dp.toPx() + tickLength, ty),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = Color(0xFF2A2E37),
            start = Offset(w - 4.dp.toPx() - tickLength, ty),
            end = Offset(w - 4.dp.toPx(), ty),
            strokeWidth = 1.dp.toPx()
        )
    }
}

// ------------------------------------------------------------------------------------------------
// 3. SWISS BAUHAUS DOSSIER (Archival Linen, Cadmium Vermilion & Architectural Grid)
// Inspired directly by uploaded image: 4fb1d4d7f039bafbc4d7aab11f49ae7f.jpg
// ------------------------------------------------------------------------------------------------
private fun DrawScope.drawBauhausDossierAtmosphere(
    w: Float,
    h: Float,
    flow: Float,
    pulse: Float
) {
    // 3A. Archival Museum Linen Paper
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFF6F2EA), Color(0xFFEFE8DD), Color(0xFFE5DDD0))
        ),
        size = Size(w, h)
    )

    // 3B. Architectural Precision Dot Grid
    val dotSpacing = 18.dp.toPx()
    var dy = 16.dp.toPx()
    while (dy < h - 16.dp.toPx()) {
        var dx = 16.dp.toPx()
        while (dx < w - 16.dp.toPx()) {
            drawCircle(
                color = Color(0xFF2E2B27).copy(alpha = 0.16f),
                radius = 1.dp.toPx(),
                center = Offset(dx, dy)
            )
            dx += dotSpacing
        }
        dy += dotSpacing
    }

    // 3C. Precision Swiss Alignment Crosshairs in Top Right
    val crossCenter = Offset(w - 22.dp.toPx(), 22.dp.toPx())
    val crossLen = 6.dp.toPx()
    drawLine(
        color = Color(0xFF1E1C1A).copy(alpha = 0.35f + pulse * 0.35f),
        start = Offset(crossCenter.x - crossLen, crossCenter.y),
        end = Offset(crossCenter.x + crossLen, crossCenter.y),
        strokeWidth = 1.2.dp.toPx()
    )
    drawLine(
        color = Color(0xFF1E1C1A).copy(alpha = 0.35f + pulse * 0.35f),
        start = Offset(crossCenter.x, crossCenter.y - crossLen),
        end = Offset(crossCenter.x, crossCenter.y + crossLen),
        strokeWidth = 1.2.dp.toPx()
    )

    // 3D. Minimalist Swiss Bauhaus Edge Accents (Cadmium Vermilion & Cobalt)
    drawRect(
        color = Color(0xFFE03A2B),
        topLeft = Offset(0f, h * 0.28f),
        size = Size(3.dp.toPx(), 42.dp.toPx())
    )
    drawRect(
        color = Color(0xFF2563EB),
        topLeft = Offset(w - 3.dp.toPx(), h * 0.45f),
        size = Size(3.dp.toPx(), 42.dp.toPx())
    )

    // 3E. Subtle Scanning Light Horizon Bar
    val scanY = flow * h
    drawLine(
        brush = Brush.horizontalGradient(
            colors = listOf(Color.Transparent, Color(0x35FFFFFF), Color.Transparent)
        ),
        start = Offset(0f, scanY),
        end = Offset(w, scanY),
        strokeWidth = 1.5.dp.toPx()
    )
}

// ------------------------------------------------------------------------------------------------
// 4. TERRACOTTA STUDIO (Matte Tuscan Clay Body & Ceramic Specular Sheen)
// Inspired directly by uploaded image: 81338928da9cca53b984614cacd15868.jpg
// ------------------------------------------------------------------------------------------------
private fun DrawScope.drawTerracottaStudioAtmosphere(
    w: Float,
    h: Float,
    sweep: Float,
    pulse: Float
) {
    // 4A. Warm Tuscan Terracotta Clay Body with Studio Radial Lighting
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFC76953), Color(0xFFB55843), Color(0xFF984331)),
            center = Offset(w * 0.5f, h * 0.28f),
            radius = w * 1.05f
        ),
        size = Size(w, h)
    )

    // Subtle stoneware ceramic grain dots along margins
    val grainSpacing = 24.dp.toPx()
    var gy = 20.dp.toPx()
    while (gy < h - 20.dp.toPx()) {
        // Left margin grain
        drawCircle(
            color = Color(0xFF5A1E14).copy(alpha = 0.12f),
            radius = 1.dp.toPx(),
            center = Offset(10.dp.toPx(), gy)
        )
        // Right margin grain
        drawCircle(
            color = Color(0xFF5A1E14).copy(alpha = 0.12f),
            radius = 1.dp.toPx(),
            center = Offset(w - 10.dp.toPx(), gy)
        )
        gy += grainSpacing
    }

    // 4B. Soft Ambient Specular Light Horizon Sweep across top studio zone
    val sweepX = sweep * w
    drawLine(
        brush = Brush.horizontalGradient(
            colors = listOf(Color.Transparent, Color(0x22FFFFFF), Color.Transparent),
            startX = sweepX - 50.dp.toPx(),
            endX = sweepX + 50.dp.toPx()
        ),
        start = Offset(sweepX - 25.dp.toPx(), 0f),
        end = Offset(sweepX + 25.dp.toPx(), h * 0.40f),
        strokeWidth = 35.dp.toPx()
    )

    // 4C. Subtle bottom ceramic footing shadow
    drawLine(
        color = Color(0x35000000),
        start = Offset(20.dp.toPx(), h - 6.dp.toPx()),
        end = Offset(w - 20.dp.toPx(), h - 6.dp.toPx()),
        strokeWidth = 1.5.dp.toPx()
    )
}
