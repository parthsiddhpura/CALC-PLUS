package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ButtonShapeType
import com.example.model.DisplayFontType
import com.example.model.PressAnimationType
import com.example.model.ThemeId
import com.example.model.ThemePalette
import com.example.ui.theme.DisplayFontHelper

@Composable
fun CalculatorButton(
    text: String,
    onClick: () -> Unit,
    theme: ThemePalette,
    modifier: Modifier = Modifier,
    backgroundColor: Color = theme.numberButtonBg,
    backgroundBrush: Brush? = null,
    textColor: Color = theme.numberButtonText,
    borderColor: Color = theme.numberButtonBorder,
    borderWidth: Dp = theme.borderWidthDp,
    fontSize: TextUnit = 22.sp,
    fontWeight: FontWeight = FontWeight.SemiBold,
    isSpanTwo: Boolean = false,
    contentDescription: String? = null,
    testTag: String = "btn_$text",
    icon: (@Composable () -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Resolve per-key palette overrides (used for Girl Math pastel & colorful layouts)
    val resolvedBgColor = theme.customKeyColors?.get(text) ?: backgroundColor
    val resolvedTextColor = theme.customKeyTextColors?.get(text) ?: textColor

    // Ultra-crisp, zero-latency physical tactile spring
    val scaleState = animateFloatAsState(
        targetValue = when {
            isPressed && theme.pressAnimation == PressAnimationType.JELLY_SQUISH -> 0.86f
            isPressed && theme.pressAnimation == PressAnimationType.BOUNCE -> 0.88f
            isPressed && theme.pressAnimation == PressAnimationType.PIXEL_STEP -> 0.96f
            isPressed -> 0.92f
            else -> 1.0f
        },
        animationSpec = spring(
            dampingRatio = if (theme.pressAnimation == PressAnimationType.JELLY_SQUISH) 0.58f else 0.72f,
            stiffness = if (theme.pressAnimation == PressAnimationType.JELLY_SQUISH) 1200f else 1600f
        ),
        label = "btn_scale"
    )

    // Specialized offsets based on animation modes
    val sinkOffsetY = when {
        isPressed && theme.pressAnimation == PressAnimationType.DEEP_SINK -> 3.5f
        isPressed && (theme.pressAnimation == PressAnimationType.PIXEL_STEP || theme.isPixelArt) -> 2.5f
        else -> 0f
    }
    val pixelOffsetX = if (isPressed && (theme.pressAnimation == PressAnimationType.PIXEL_STEP || theme.isPixelArt)) 2.5f else 0f
    val brutalDiff = if (theme.isBrutalistShadow && isPressed) 3.5f else 0f

    val isNothingTheme = theme.isNothingDossier || theme.id == ThemeId.NOTHING_DOSSIER

    val currentBgColor = if (isPressed) {
        when {
            theme.hasBatSignal -> Color(0xFF283344)
            theme.hasArcReactor -> Color(0xFF1F2D42)
            isNothingTheme -> {
                if (resolvedBgColor == theme.operatorButtonBg) {
                    Color(0xFFD8DEE8)
                } else {
                    Color(0xFF232730)
                }
            }
            theme.pressAnimation == PressAnimationType.NEON_GLOW -> theme.accentColor.copy(alpha = 0.35f)
            theme.pressAnimation == PressAnimationType.JELLY_SQUISH -> resolvedBgColor.copy(alpha = 0.90f)
            else -> resolvedBgColor.copy(alpha = 0.84f)
        }
    } else resolvedBgColor

    val shape: Shape = remember(theme.shapeType, theme.cornerRadiusDp) {
        theme.getShape()
    }

    val fontFamily = remember(theme.displayFont) {
        DisplayFontHelper.getFontFamily(theme.displayFont)
    }

    // Neko Cat-Culator sweet kitten expressions per key
    val nekoFace = remember(text, theme.isNekoMochi) {
        if (!theme.isNekoMochi && theme.shapeType != ButtonShapeType.NEKO_EARS) null else {
            when (text) {
                "7" -> "(•ㅅ•)"
                "8" -> "(^•ω•^)"
                "9" -> "(=^･ω･^=)"
                "4" -> "(^•ﻌ•^)"
                "5" -> "( ˘ ³˘)"
                "6" -> "(• ̀ω•́ )"
                "1" -> "( > ᴗ < )"
                "2" -> "(^._.^)ﾉ"
                "3" -> "(=①ω①=)"
                "0" -> "(ㅇㅅㅇ)"
                "." -> "🐾"
                "=" -> "(=^ ◡ ^=)"
                "+" -> "(•‿•)"
                "−" -> "(｡•ㅅ•｡)"
                "×" -> "(^>ω<^)"
                "÷" -> "(•ω•)"
                "AC" -> "( > < )"
                "⌫" -> "(≗ ≗)"
                "%" -> "(✿•ㅅ•)"
                "±" -> "(^._.^)"
                else -> null
            }
        }
    }

    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .graphicsLayer {
                val currentScale = scaleState.value
                scaleX = currentScale
                scaleY = currentScale
                if (sinkOffsetY != 0f) {
                    translationY = sinkOffsetY.dp.toPx()
                }
                if (pixelOffsetX != 0f) {
                    translationX = pixelOffsetX.dp.toPx()
                }
            }
            .testTag(testTag)
    ) {
        // Neo-brutalist or 8-Bit Pixel hard drop shadow underlay
        if (theme.isBrutalistShadow || (theme.isPixelArt && !isPressed)) {
            val shadowOffset = if (theme.isPixelArt) 2.5.dp else 3.5.dp
            val shadowColor = if (theme.isPixelArt) Color(0xFF1E1024) else theme.brutalistShadowColor
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = shadowOffset, y = shadowOffset)
                    .clip(shape)
                    .background(shadowColor)
            )
        }

        // Main Button Surface
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (theme.isBrutalistShadow && brutalDiff > 0f) {
                        Modifier.graphicsLayer {
                            val px = brutalDiff.dp.toPx()
                            translationX = px
                            translationY = px
                        }
                    } else Modifier
                )
                .then(
                    if (theme.hasShadow && !theme.isBrutalistShadow && theme.shadowElevationDp > 0.dp) {
                        Modifier.shadow(
                            elevation = if (isPressed) theme.shadowElevationDp / 2 else theme.shadowElevationDp,
                            shape = shape
                        )
                    } else Modifier
                )
                .clip(shape)
                .then(
                    if (backgroundBrush != null) {
                        Modifier.background(backgroundBrush)
                    } else {
                        Modifier.background(currentBgColor)
                    }
                )
                .then(
                    run {
                        val isRingActive = isPressed && (
                            theme.pressAnimation == PressAnimationType.NEON_GLOW ||
                            theme.hasBatSignal ||
                            theme.hasArcReactor ||
                            isNothingTheme
                        )
                        if (borderWidth > 0.dp || isRingActive) {
                            val activeBorderColor = if (isRingActive) {
                                when {
                                    isNothingTheme && (text == "=" || backgroundBrush != null) -> Color.White
                                    isNothingTheme -> theme.accentColor
                                    else -> theme.accentColor
                                }
                            } else borderColor
                            val activeWidth = if (isRingActive) {
                                if (isNothingTheme) 2.dp else (borderWidth + 1.2.dp)
                            } else borderWidth
                            Modifier.border(activeWidth, activeBorderColor, shape)
                        } else Modifier
                    }
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    role = Role.Button,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            // Chunky 8-Bit Pixel Bevel
            if (theme.isPixelArt || theme.shapeType == ButtonShapeType.PIXEL_BLOCK) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val w = size.width
                    val h = size.height
                    val b = 2.2.dp.toPx()
                    if (!isPressed) {
                        // Top & Left highlight
                        drawLine(
                            color = Color.White.copy(alpha = 0.45f),
                            start = Offset(0f, b / 2),
                            end = Offset(w, b / 2),
                            strokeWidth = b
                        )
                        drawLine(
                            color = Color.White.copy(alpha = 0.35f),
                            start = Offset(b / 2, 0f),
                            end = Offset(b / 2, h),
                            strokeWidth = b
                        )
                        // Bottom & Right shadow
                        drawLine(
                            color = Color.Black.copy(alpha = 0.45f),
                            start = Offset(0f, h - b / 2),
                            end = Offset(w, h - b / 2),
                            strokeWidth = b
                        )
                        drawLine(
                            color = Color.Black.copy(alpha = 0.35f),
                            start = Offset(w - b / 2, 0f),
                            end = Offset(w - b / 2, h),
                            strokeWidth = b
                        )
                    }
                }
            }

            // Y2K Juicy Jelly Specular Highlight Overlay
            if (theme.isY2kGlossy || theme.shapeType == ButtonShapeType.GLOSSY_JELLY) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val w = size.width
                    val h = size.height
                    // Specular glossy reflection dome on upper half
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = if (isPressed) 0.35f else 0.65f),
                                Color.White.copy(alpha = 0.15f),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = h * 0.48f
                        ),
                        topLeft = Offset(w * 0.08f, 2.5.dp.toPx()),
                        size = Size(w * 0.84f, h * 0.42f),
                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                    )
                }
            }

            // Kawaii Neko Cat Ears on top corners of the mochi button
            if (theme.isNekoMochi || theme.shapeType == ButtonShapeType.NEKO_EARS) {
                val leftEarPath = remember { Path() }
                val leftInnerPath = remember { Path() }
                val rightEarPath = remember { Path() }
                val rightInnerPath = remember { Path() }

                Canvas(modifier = Modifier.matchParentSize()) {
                    val w = size.width
                    val h = size.height
                    val earH = h * 0.22f
                    val strokePx = borderWidth.toPx().coerceAtLeast(1.2f)

                    // Left Ear Outer
                    leftEarPath.reset()
                    leftEarPath.moveTo(w * 0.10f, earH)
                    leftEarPath.lineTo(w * 0.20f, 1.dp.toPx())
                    leftEarPath.lineTo(w * 0.34f, earH * 0.90f)
                    leftEarPath.close()
                    drawPath(leftEarPath, color = currentBgColor)

                    // Left Ear Inner Blush
                    leftInnerPath.reset()
                    leftInnerPath.moveTo(w * 0.15f, earH * 0.82f)
                    leftInnerPath.lineTo(w * 0.20f, 3.dp.toPx())
                    leftInnerPath.lineTo(w * 0.29f, earH * 0.78f)
                    leftInnerPath.close()
                    drawPath(leftInnerPath, color = Color(0xFFFF85A1).copy(alpha = 0.85f))
                    if (borderWidth > 0.dp) {
                        drawPath(leftEarPath, color = borderColor, style = Stroke(width = strokePx))
                    }

                    // Right Ear Outer
                    rightEarPath.reset()
                    rightEarPath.moveTo(w * 0.66f, earH * 0.90f)
                    rightEarPath.lineTo(w * 0.80f, 1.dp.toPx())
                    rightEarPath.lineTo(w * 0.90f, earH)
                    rightEarPath.close()
                    drawPath(rightEarPath, color = currentBgColor)

                    // Right Ear Inner Blush
                    rightInnerPath.reset()
                    rightInnerPath.moveTo(w * 0.71f, earH * 0.78f)
                    rightInnerPath.lineTo(w * 0.80f, 3.dp.toPx())
                    rightInnerPath.lineTo(w * 0.85f, earH * 0.82f)
                    rightInnerPath.close()
                    drawPath(rightInnerPath, color = Color(0xFFFF85A1).copy(alpha = 0.85f))
                    if (borderWidth > 0.dp) {
                        drawPath(rightEarPath, color = borderColor, style = Stroke(width = strokePx))
                    }
                }
            }

            // Button Icon or Text Content
            if (icon != null) {
                icon()
            } else {
                if (nekoFace != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 2.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = text,
                            color = resolvedTextColor,
                            fontSize = (fontSize.value * 0.84f).sp,
                            fontWeight = fontWeight,
                            fontFamily = fontFamily,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = nekoFace,
                            color = resolvedTextColor.copy(alpha = 0.72f),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Text(
                        text = text,
                        color = resolvedTextColor,
                        fontSize = fontSize,
                        fontWeight = fontWeight,
                        fontFamily = fontFamily,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
