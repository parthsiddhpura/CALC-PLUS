package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import com.example.ui.theme.DisplayFontHelper
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AngleMode
import com.example.model.CalculatorMode
import com.example.model.DisplayConfig
import com.example.model.DisplayFontType
import com.example.model.DisplayFormatter
import com.example.model.DisplayNotation
import com.example.model.DisplayPrecisionMode
import com.example.model.DisplayScaleSize
import com.example.model.DisplaySeparatorStyle
import com.example.model.ThemePalette
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalculatorDisplay(
    expression: String,
    result: String,
    previewResult: String?,
    theme: ThemePalette,
    angleMode: AngleMode,
    hasMemory: Boolean,
    mode: CalculatorMode,
    displayConfig: DisplayConfig = DisplayConfig(),
    historyCount: Int = 0,
    cursorPosition: Int = -1,
    onCursorChange: ((Int) -> Unit)? = null,
    isEvaluated: Boolean = false,
    onToggleAngleMode: (() -> Unit)? = null,
    onOpenHistory: (() -> Unit)? = null,
    onOpenDecimalConverter: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val exprScrollState = rememberScrollState()
    var justCopied by remember { mutableStateOf(false) }

    // Draw-phase cursor blinking animation (zero recomposition on idle blinking)
    val cursorTransition = rememberInfiniteTransition(label = "cursor_blink")
    val cursorBlinkAlpha = cursorTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1060
                1f at 0
                1f at 530
                0f at 531
                0f at 1060
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "cursor_alpha"
    )

    LaunchedEffect(expression) {
        if (exprScrollState.maxValue > 0) {
            exprScrollState.scrollTo(exprScrollState.maxValue)
        }
    }

    val fontFamily = DisplayFontHelper.getFontFamily(theme.displayFont)
    val fontLetterSpacing = DisplayFontHelper.getLetterSpacing(theme.displayFont)
    val fontResultWeight = DisplayFontHelper.getFontWeight(theme.displayFont, isResult = true)

    val screenShape = RoundedCornerShape(theme.cornerRadiusDp.coerceAtLeast(14.dp))

    // Format expression and results according to Display Preferences
    val formattedExpr = remember(expression, displayConfig.separatorStyle) {
        DisplayFormatter.formatExpression(expression, displayConfig.separatorStyle)
    }

    val formattedResult = remember(result, displayConfig.separatorStyle, displayConfig.precisionMode, displayConfig.notation) {
        DisplayFormatter.formatNumber(
            valueStr = result,
            separatorStyle = displayConfig.separatorStyle,
            precisionMode = displayConfig.precisionMode,
            notation = displayConfig.notation
        )
    }

    val formattedPreview = remember(previewResult, displayConfig.separatorStyle, displayConfig.precisionMode, displayConfig.notation) {
        previewResult?.let {
            DisplayFormatter.formatNumber(
                valueStr = it,
                separatorStyle = displayConfig.separatorStyle,
                precisionMode = displayConfig.precisionMode,
                notation = displayConfig.notation
            )
        }
    }

    fun copyToClipboard(content: String, label: String = "Calculation") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, content)
        clipboard.setPrimaryClip(clip)
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        Toast.makeText(context, "Copied: $content", Toast.LENGTH_SHORT).show()
        justCopied = true
        coroutineScope.launch {
            delay(1500)
            justCopied = false
        }
    }

    // Only allow copying after '=' button has calculated an amount; before that nothing will copy
    val canCopy = isEvaluated && formattedResult != "Error" && formattedResult.isNotBlank()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clip(screenShape)
            .background(theme.screenBackground)
            .then(
                if (theme.borderWidthDp > 0.dp) {
                    Modifier.border(theme.borderWidthDp, theme.screenBorderColor, screenShape)
                } else Modifier
            )
            .then(
                if (canCopy) {
                    Modifier.combinedClickable(
                        onClick = {
                            if (displayConfig.copyOnTap) {
                                copyToClipboard(formattedResult, "Result")
                            }
                        },
                        onLongClick = {
                            copyToClipboard(formattedResult, "Result")
                        }
                    )
                } else {
                    Modifier
                }
            )
            .testTag("calculator_display")
    ) {
        val isVeryCompact = maxHeight < 120.dp
        val isCompact = maxHeight < 170.dp
        val innerPadding = if (isVeryCompact) 6.dp else if (isCompact) 8.dp else 12.dp

        // Responsive font scaling factors based on actual available display height
        val heightFactor = (maxHeight.value / 200f).coerceIn(0.60f, 1.15f)
        val configResultSp = displayConfig.scaleSize.resultSp.toFloat()
        val configExprSp = displayConfig.scaleSize.exprSp.toFloat()
        val baseSp = configResultSp * heightFactor
        val exprBaseSize = (configExprSp * heightFactor).toInt().coerceAtLeast(16)

        // CRT Scanline Overlay if enabled
        if (theme.hasScanlines) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val step = 4.dp.toPx()
                var y = 0f
                val scanlineColor = theme.screenTextColor.copy(alpha = 0.10f)
                while (y < size.height) {
                    drawLine(
                        color = scanlineColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f
                    )
                    y += step
                }
            }
        }

        // Batman Display Overlay (Bat-Signal searchlight, insignia, and tactile responses)
        if (theme.hasBatSignal) {
            BatmanDisplayOverlay(modifier = Modifier.matchParentSize())
        } else if (theme.hasArcReactor) {
            IronManDisplayOverlay(
                modifier = Modifier.matchParentSize(),
                suitType = theme.ironManSuit ?: com.example.model.IronManSuitType.MARK_85_CLASSIC,
                accentColor = theme.accentColor
            )
        } else if (theme.isOrtylMinimal) {
            OrtylDisplayOverlay(modifier = Modifier.matchParentSize())
        } else if (theme.isOledStealthVoid) {
            OledStealthVoidDisplayOverlay(modifier = Modifier.matchParentSize())
        } else if (theme.isStarryGotham) {
            StarryGothamDisplayOverlay(modifier = Modifier.matchParentSize())
        } else if (theme.isCosmicSingularity) {
            CosmicSingularityDisplayOverlay(modifier = Modifier.matchParentSize())
        } else {
            // Unique professional ambient display animation for all other theme categories
            ThemeAmbientDisplayAnimation(
                modifier = Modifier.matchParentSize(),
                theme = theme
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Status & Actions Row: Mode, Angle, Badges, Format indicators, Decimal Conv, Quick Copy, History
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (displayConfig.showStatusBadges) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(if (isCompact) 3.dp else 5.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        // Mode Badge
                        Surface(
                            color = theme.accentColor.copy(alpha = 0.22f),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(0.8.dp, theme.accentColor.copy(alpha = 0.7f))
                        ) {
                            Text(
                                text = mode.shortName.uppercase(),
                                color = theme.accentColor,
                                fontSize = if (isCompact) 9.sp else 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = fontFamily,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = if (isCompact) 4.dp else 6.dp, vertical = 2.dp)
                            )
                        }

                        // Wayne Tech HUD Badge for Batman Theme
                        if (theme.hasBatSignal) {
                            Surface(
                                color = Color(0x33FFE500),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(0.8.dp, Color(0x66FFE500))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                                    modifier = Modifier.padding(horizontal = if (isCompact) 4.dp else 6.dp, vertical = 2.dp)
                                ) {
                                    BatmanLogoIcon(
                                        modifier = Modifier.size(if (isCompact) 11.dp else 13.dp),
                                        tint = Color(0xFFFFE500)
                                    )
                                    Text(
                                        text = "WAYNE TECH",
                                        color = Color(0xFFFFE500),
                                        fontSize = if (isCompact) 8.sp else 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = fontFamily,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        // Stark Tech Arc Reactor HUD Badge for Iron Man Theme
                        if (theme.hasArcReactor) {
                            Surface(
                                color = Color(0x3300F0FF),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(0.8.dp, Color(0x6600F0FF))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                                    modifier = Modifier.padding(horizontal = if (isCompact) 4.dp else 6.dp, vertical = 2.dp)
                                ) {
                                    ArcReactorIcon(
                                        modifier = Modifier.size(if (isCompact) 11.dp else 13.dp),
                                        glowColor = Color(0xFF00F0FF),
                                        showOuterTabs = false
                                    )
                                    Text(
                                        text = "STARK TECH",
                                        color = Color(0xFF00F0FF),
                                        fontSize = if (isCompact) 8.sp else 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = fontFamily,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        // Kawaii Themes Status Badges
                        if (theme.isGirlMath) {
                            Surface(
                                color = Color(0xFFFF5277).copy(alpha = 0.18f),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(0.8.dp, Color(0xFFFF85A1).copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = "GIRL MATH ♡",
                                    color = Color(0xFFFF5277),
                                    fontSize = if (isCompact) 8.sp else 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = if (isCompact) 5.dp else 7.dp, vertical = 2.dp)
                                )
                            }
                        } else if (theme.isNekoMochi) {
                            Surface(
                                color = Color(0xFFFF85A1).copy(alpha = 0.28f),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF85A1).copy(alpha = 0.8f))
                            ) {
                                Text(
                                    text = if (theme.isDark) "🐾 NEKO MIDNIGHT" else "🐾 NEKO MOCHI",
                                    color = Color(0xFFFFEBF0),
                                    fontSize = if (isCompact) 8.5.sp else 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = if (isCompact) 5.dp else 7.dp, vertical = 2.dp)
                                )
                            }
                        } else if (theme.isY2kGlossy) {
                            Surface(
                                color = Color(0xFFA2E8DD).copy(alpha = 0.25f),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(0.8.dp, Color(0xFFA2E8DD))
                            ) {
                                Text(
                                    text = "Y2K POP ✨",
                                    color = Color(0xFF144740),
                                    fontSize = if (isCompact) 8.sp else 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = if (isCompact) 5.dp else 7.dp, vertical = 2.dp)
                                )
                            }
                        } else if (theme.isPixelArt) {
                            Surface(
                                color = Color(0xFF00FF66).copy(alpha = 0.16f),
                                shape = RoundedCornerShape(4.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00FF66).copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = "👾 PICO-CALC",
                                    color = Color(0xFF00FF66),
                                    fontSize = if (isCompact) 8.sp else 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = if (isCompact) 5.dp else 7.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Angle Mode Badge (DEG / RAD) - Tap to toggle!
                        if (mode == CalculatorMode.STANDARD || mode == CalculatorMode.SCIENTIFIC) {
                            Surface(
                                color = theme.secondaryAccent.copy(alpha = 0.22f),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(0.8.dp, theme.secondaryAccent.copy(alpha = 0.7f)),
                                modifier = if (onToggleAngleMode != null) {
                                    Modifier.clickable { onToggleAngleMode() }
                                } else Modifier
                            ) {
                                Text(
                                    text = angleMode.name,
                                    color = theme.secondaryAccent,
                                    fontSize = if (isCompact) 9.sp else 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = fontFamily,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = if (isCompact) 4.dp else 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Memory Badge
                        if (hasMemory) {
                            Surface(
                                color = Color(0xFFFFB703).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "M",
                                    color = Color(0xFFFFB703),
                                    fontSize = if (isCompact) 9.sp else 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = fontFamily,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = if (isCompact) 4.dp else 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Display Notation Badge if non-standard
                        if (displayConfig.notation != DisplayNotation.STANDARD) {
                            Surface(
                                color = theme.accentColor.copy(alpha = 0.22f),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(0.8.dp, theme.accentColor.copy(alpha = 0.7f))
                            ) {
                                Text(
                                    text = if (displayConfig.notation == DisplayNotation.SCIENTIFIC) "SCI" else "ENG",
                                    color = theme.accentColor,
                                    fontSize = if (isCompact) 8.sp else 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = fontFamily,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                // Screen pill styling matching screen luminance
                val isScreenDark = theme.screenBackground.luminance() < 0.45f
                val screenPillBg = if (isScreenDark) {
                    Color.White.copy(alpha = 0.12f)
                } else {
                    Color.Black.copy(alpha = 0.08f)
                }
                val screenPillBorder = if (isScreenDark) {
                    androidx.compose.foundation.BorderStroke(0.8.dp, Color.White.copy(alpha = 0.22f))
                } else {
                    androidx.compose.foundation.BorderStroke(0.8.dp, Color.Black.copy(alpha = 0.12f))
                }
                val screenActionTint = theme.screenTextColor

                // Right Actions: Quick Copy Icon, Decimal Converter (F↔D / DEC) and History Icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(if (isCompact) 3.dp else 6.dp)
                ) {
                    // Quick Copy Action Button (only active after calculation equals is pressed)
                    Surface(
                        shape = CircleShape,
                        color = if (justCopied) theme.accentColor.copy(alpha = 0.35f) else screenPillBg,
                        border = if (justCopied) androidx.compose.foundation.BorderStroke(1.dp, theme.accentColor) else screenPillBorder,
                        modifier = Modifier
                            .clip(CircleShape)
                            .then(
                                if (canCopy) {
                                    Modifier.clickable {
                                        copyToClipboard(formattedResult, "Result")
                                    }
                                } else {
                                    Modifier
                                }
                            )
                            .testTag("btn_display_copy")
                    ) {
                        Box(
                            modifier = Modifier.padding(if (isCompact) 4.dp else 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (justCopied) Icons.Default.Done else Icons.Default.ContentCopy,
                                contentDescription = if (canCopy) "Copy Result" else "Calculate first to copy",
                                tint = if (justCopied) {
                                    theme.accentColor
                                } else if (canCopy) {
                                    screenActionTint
                                } else {
                                    screenActionTint.copy(alpha = 0.35f)
                                },
                                modifier = Modifier.size(if (isCompact) 12.dp else 14.dp)
                            )
                        }
                    }

                    // Decimal Conversion Quick Pill Button
                    if (onOpenDecimalConverter != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = theme.accentColor.copy(alpha = 0.16f),
                            border = androidx.compose.foundation.BorderStroke(0.8.dp, theme.accentColor.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .clickable { onOpenDecimalConverter() }
                                .testTag("btn_display_decimal_conv")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = if (isCompact) 5.dp else 7.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Transform,
                                    contentDescription = "Decimal Conversion",
                                    tint = theme.accentColor,
                                    modifier = Modifier.size(if (isCompact) 11.dp else 13.dp)
                                )
                                Text(
                                    text = "F↔D",
                                    color = theme.accentColor,
                                    fontSize = if (isCompact) 9.sp else 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }

                    // History Tape Button
                    if (onOpenHistory != null) {
                        Surface(
                            shape = CircleShape,
                            color = screenPillBg,
                            border = screenPillBorder,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { onOpenHistory() }
                                .testTag("btn_display_history")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (historyCount > 0) {
                                        Badge(
                                            containerColor = theme.secondaryAccent,
                                            contentColor = theme.backgroundColor
                                        ) {
                                            Text(
                                                text = "${historyCount.coerceAtMost(99)}",
                                                fontSize = 8.sp
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier.padding(if (isCompact) 3.dp else 5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = "History",
                                    tint = screenActionTint,
                                    modifier = Modifier.size(if (isCompact) 13.dp else 16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(if (isCompact) 2.dp else 4.dp))

            // Expression Row (with horizontal scroll and interactive touch & drag cursor)
            if (formattedExpr.isNotEmpty() || !isVeryCompact) {
                var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
                var textCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
                var rowCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

                val effectiveCursorPos = if (cursorPosition >= 0) {
                    cursorPosition.coerceIn(0, expression.length)
                } else {
                    expression.length
                }
                val formattedCursorPos = remember(expression, effectiveCursorPos, formattedExpr) {
                    mapRawOffsetToFormatted(expression, effectiveCursorPos, formattedExpr)
                }

                // Scroll expression horizontally to keep cursor visible
                LaunchedEffect(expression, effectiveCursorPos, formattedCursorPos) {
                    val layout = textLayoutResult
                    if (layout != null && formattedExpr.isNotEmpty()) {
                        val safeOffset = formattedCursorPos.coerceIn(0, formattedExpr.length)
                        val cursorRect = layout.getCursorRect(safeOffset)
                        val cursorX = cursorRect.left.toInt()
                        val viewportWidth = exprScrollState.viewportSize
                        if (viewportWidth > 0) {
                            if (cursorX < exprScrollState.value) {
                                exprScrollState.animateScrollTo((cursorX - 40).coerceAtLeast(0))
                            } else if (cursorX > exprScrollState.value + viewportWidth) {
                                exprScrollState.animateScrollTo(cursorX - viewportWidth + 40)
                            }
                        }
                    } else if (exprScrollState.maxValue > 0) {
                        exprScrollState.scrollTo(exprScrollState.maxValue)
                    }
                }

                // Precision Touch & Drag cursor positioning function
                fun updateCursorFromTouchOffset(touchOffsetInRow: Offset) {
                    val layout = textLayoutResult ?: return
                    val textCoords = textCoordinates ?: return
                    val rowCoords = rowCoordinates ?: return

                    if (formattedExpr.isEmpty()) {
                        onCursorChange?.invoke(0)
                        return
                    }

                    // Convert touch position from the Row coordinate space to Text local coordinate space
                    val localInText = textCoords.localPositionOf(rowCoords, touchOffsetInRow)
                    val touchX = localInText.x

                    val lineLeft = layout.getLineLeft(0)
                    val lineRight = layout.getLineRight(0)

                    val targetFormattedOffset = when {
                        touchX <= lineLeft -> 0
                        touchX >= lineRight -> formattedExpr.length
                        else -> {
                            var foundOffset: Int? = null
                            for (i in 0 until formattedExpr.length) {
                                val box = layout.getBoundingBox(i)
                                if (touchX >= box.left && touchX <= box.right) {
                                    val mid = (box.left + box.right) / 2f
                                    foundOffset = if (touchX < mid) i else i + 1
                                    break
                                }
                            }
                            foundOffset ?: layout.getOffsetForPosition(Offset(touchX, layout.size.height / 2f))
                                .coerceIn(0, formattedExpr.length)
                        }
                    }

                    val targetRawOffset = mapFormattedOffsetToRaw(
                        formattedExpr = formattedExpr,
                        formattedOffset = targetFormattedOffset,
                        rawExpr = expression
                    )

                    if (targetRawOffset != effectiveCursorPos) {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                    onCursorChange?.invoke(targetRawOffset)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 38.dp)
                        .onGloballyPositioned { rowCoordinates = it }
                        .pointerInput(formattedExpr, expression) {
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                updateCursorFromTouchOffset(down.position)

                                val pointerId = down.id
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val drag = event.changes.firstOrNull { it.id == pointerId } ?: break
                                    if (!drag.pressed) break

                                    if (drag.positionChange().getDistance() > 1f) {
                                        drag.consume()
                                        updateCursorFromTouchOffset(drag.position)
                                    }
                                }
                            }
                        }
                        .horizontalScroll(exprScrollState),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    val displayText = if (formattedExpr.isEmpty()) "0" else formattedExpr
                    val isPlaceholder = formattedExpr.isEmpty()

                    Text(
                        text = displayText,
                        color = if (isPlaceholder) theme.screenExpressionColor.copy(alpha = 0.4f) else theme.screenExpressionColor,
                        fontSize = exprBaseSize.sp,
                        lineHeight = exprBaseSize.sp,
                        fontFamily = fontFamily,
                        letterSpacing = fontLetterSpacing,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        softWrap = false,
                        onTextLayout = { layoutResult ->
                            textLayoutResult = layoutResult
                        },
                        modifier = Modifier
                            .onGloballyPositioned { textCoordinates = it }
                            .testTag("expression_text")
                            .drawWithContent {
                                drawContent()
                                val isCursorVisible = cursorBlinkAlpha.value > 0.5f
                                // Interactive cursor line
                                if (onCursorChange != null && isCursorVisible && !isPlaceholder) {
                                    val layout = textLayoutResult
                                    if (layout != null && formattedExpr.isNotEmpty()) {
                                        val safeOffset = formattedCursorPos.coerceIn(0, formattedExpr.length)
                                        val cursorRect = layout.getCursorRect(safeOffset)
                                        val cursorWidth = 2.5.dp.toPx()
                                        val cursorTop = cursorRect.top + 1.dp.toPx()
                                        val cursorHeight = (cursorRect.bottom - cursorRect.top - 2.dp.toPx()).coerceAtLeast(18.dp.toPx())

                                        // Subtle glowing halo
                                        drawRoundRect(
                                            color = theme.accentColor.copy(alpha = 0.35f),
                                            topLeft = Offset(cursorRect.left - (cursorWidth / 2) - 2.dp.toPx(), cursorTop - 1.dp.toPx()),
                                            size = Size(cursorWidth + 4.dp.toPx(), cursorHeight + 2.dp.toPx()),
                                            cornerRadius = CornerRadius(2.dp.toPx())
                                        )
                                        // Solid crisp cursor line
                                        drawRoundRect(
                                            color = theme.accentColor,
                                            topLeft = Offset(cursorRect.left - (cursorWidth / 2), cursorTop),
                                            size = Size(cursorWidth, cursorHeight),
                                            cornerRadius = CornerRadius(1.2.dp.toPx())
                                        )
                                        // Bottom teardrop / handle accent
                                        drawCircle(
                                            color = theme.accentColor,
                                            radius = 3.dp.toPx(),
                                            center = Offset(cursorRect.left, cursorTop + cursorHeight + 2.dp.toPx())
                                        )
                                    }
                                } else if (onCursorChange != null && isCursorVisible && isPlaceholder) {
                                    val cursorWidth = 2.5.dp.toPx()
                                    val cursorHeight = (exprBaseSize * 0.9f).dp.toPx()
                                    drawRoundRect(
                                        color = theme.accentColor,
                                        topLeft = Offset(size.width - 2.dp.toPx(), (size.height - cursorHeight) / 2f),
                                        size = Size(cursorWidth, cursorHeight),
                                        cornerRadius = CornerRadius(1.2.dp.toPx())
                                    )
                                }
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.height(if (isCompact) 1.dp else 2.dp))

            // Main Result Row + Live Preview Result
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Live preview if enabled and available
                if (displayConfig.showLivePreview) {
                    AnimatedVisibility(
                        visible = formattedPreview != null && formattedPreview != formattedResult,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        if (formattedPreview != null) {
                            val previewCharCount = formattedPreview.length + 2
                            val previewScale = when {
                                previewCharCount <= 10 -> 1.0f
                                previewCharCount <= 14 -> 0.82f
                                previewCharCount <= 18 -> 0.68f
                                previewCharCount <= 22 -> 0.54f
                                previewCharCount <= 28 -> 0.44f
                                else -> 0.35f
                            }
                            val previewSp = ((exprBaseSize - 2) * previewScale).coerceIn(12f, 24f)
                            Text(
                                text = "= $formattedPreview",
                                color = theme.screenPreviewColor,
                                fontSize = previewSp.sp,
                                lineHeight = previewSp.sp,
                                fontFamily = fontFamily,
                                letterSpacing = fontLetterSpacing,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.End,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Clip, // Never truncate preview with "..."
                                modifier = Modifier
                                    .padding(bottom = 1.dp)
                                    .testTag("preview_result_text")
                            )
                        }
                    }
                }

                // Main Result dynamic auto-scaling: dynamically reduces font size so big amounts fit without truncation
                val charCount = formattedResult.length
                val dynamicScale = when {
                    charCount <= 7 -> 1.0f
                    charCount <= 9 -> 0.82f
                    charCount <= 11 -> 0.68f
                    charCount <= 13 -> 0.54f // For 4,770,644,268 (13 chars) - fits comfortably on screen!
                    charCount <= 15 -> 0.44f
                    charCount <= 18 -> 0.36f
                    charCount <= 22 -> 0.30f
                    charCount <= 26 -> 0.25f
                    charCount <= 32 -> 0.21f
                    else -> 0.18f
                }
                val targetSp = (baseSp * dynamicScale).coerceIn(12f, baseSp)
                val animatedSp by animateFloatAsState(
                    targetValue = targetSp,
                    animationSpec = tween(durationMillis = 80, easing = FastOutSlowInEasing),
                    label = "result_font_scale"
                )

                val resultScrollState = rememberScrollState()
                LaunchedEffect(formattedResult) {
                    if (resultScrollState.maxValue > 0) {
                        resultScrollState.scrollTo(resultScrollState.maxValue)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(resultScrollState),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = formattedResult,
                        color = theme.screenTextColor,
                        fontSize = animatedSp.sp,
                        lineHeight = animatedSp.sp,
                        fontFamily = fontFamily,
                        letterSpacing = fontLetterSpacing,
                        fontWeight = fontResultWeight,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Clip, // Never truncate with "..."!
                        modifier = Modifier.testTag("main_result_text")
                    )
                }
            }
        }
    }
}

/**
 * Maps a character index in formatted expression (which may contain grouping commas/spaces)
 * back to the underlying raw mathematical expression index.
 */
fun mapFormattedOffsetToRaw(
    formattedExpr: String,
    formattedOffset: Int,
    rawExpr: String
): Int {
    if (rawExpr.isEmpty() || formattedOffset <= 0) return 0
    if (formattedOffset >= formattedExpr.length) return rawExpr.length

    var rawIdx = 0
    var fmtIdx = 0
    while (fmtIdx < formattedOffset && rawIdx < rawExpr.length && fmtIdx < formattedExpr.length) {
        val fmtChar = formattedExpr[fmtIdx]
        val rawChar = rawExpr[rawIdx]
        if (fmtChar == rawChar) {
            rawIdx++
            fmtIdx++
        } else {
            // Skips inserted formatting characters like ',' or ' '
            fmtIdx++
        }
    }
    return rawIdx.coerceIn(0, rawExpr.length)
}

/**
 * Maps a raw mathematical expression cursor offset to the visual position in formattedExpr.
 */
fun mapRawOffsetToFormatted(
    rawExpr: String,
    rawOffset: Int,
    formattedExpr: String
): Int {
    if (rawExpr.isEmpty() || rawOffset <= 0) return 0
    if (rawOffset >= rawExpr.length) return formattedExpr.length

    var rawIdx = 0
    var fmtIdx = 0
    while (rawIdx < rawOffset && rawIdx < rawExpr.length && fmtIdx < formattedExpr.length) {
        val fmtChar = formattedExpr[fmtIdx]
        val rawChar = rawExpr[rawIdx]
        if (fmtChar == rawChar) {
            rawIdx++
            fmtIdx++
        } else {
            fmtIdx++
        }
    }
    return fmtIdx.coerceIn(0, formattedExpr.length)
}
