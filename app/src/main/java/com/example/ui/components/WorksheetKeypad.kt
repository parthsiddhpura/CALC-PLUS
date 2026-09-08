package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.WorksheetTapeEngine
import com.example.model.ThemePalette
import com.example.model.WorksheetSettings

enum class KeypadInputMode {
    NUMERIC_K1,
    TEXT_ABC
}

/**
 * 4x6 Keypad + ABC Text Mode + Dock Bar matching CalcTape Pro video frames 00:24, 01:16, 02:36
 */
@Composable
fun WorksheetKeypadView(
    theme: ThemePalette,
    settings: WorksheetSettings,
    grandTotal: Double,
    currentNote: String,
    quickVariableValue: Double?,
    activeInputAmount: Double? = null,
    canUndo: Boolean,
    canRedo: Boolean,
    onDigit: (String) -> Unit,
    onOperator: (String) -> Unit,
    onPercentage: () -> Unit,
    onSubtotal: () -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onMemoryPlus: () -> Unit,
    onMemoryMinus: () -> Unit,
    onMemoryRecall: () -> Unit,
    onMemoryClear: () -> Unit,
    onCustomKey: () -> Unit,
    onOpenCustomKeyDialog: () -> Unit,
    onOpenVariables: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onToggleKeyboard: () -> Unit = {},
    onUpdateNote: (String) -> Unit,
    onInsertQuickVariable: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    var inputMode by remember { mutableStateOf(KeypadInputMode.NUMERIC_K1) }
    var noteEditingText by remember(currentNote) { mutableStateOf(currentNote) }

    // Authentic colors from CalcTape Pro screenshot
    val barBg = Color(0xFF242A33)
    val numBtnBg = Color(0xFF32373F) // Dark charcoal for digits
    val numBtnText = Color(0xFFF1F5F9) // Off-white/clean white
    val steelBlueBg = Color(0xFF759AB4) // Steel-blue for operators
    val steelBlueText = Color(0xFF1E2E3C) // Dark navy text on steel-blue
    val memoryBtnBg = Color(0xFF4C5561) // Slate gray for MR, M+, M-
    val memoryBtnText = Color(0xFF8E9BA8) // Slate muted text
    val clearBtnBg = Color(0xFFCD6126) // Burnt orange for AC
    val clearBtnText = Color.White
    val textColor = Color(0xFFF8FAFC)
    val subtextColor = Color(0xFF94A3B8)
    val accentColor = theme.accentColor

    fun playFeedback() {
        if (settings.hapticsEnabled) {
            haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF1F242B))
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        // --- DOCK CONTROL BAR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .background(barBg, RoundedCornerShape(8.dp))
                .padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Mode Switchers (K1, ABC, Keyboard, Undo, Redo)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // K1 Tab Pill
                DockTabPill(
                    label = "K1",
                    isSelected = inputMode == KeypadInputMode.NUMERIC_K1,
                    accentColor = Color(0xFF454F5E),
                    onClick = {
                        playFeedback()
                        inputMode = KeypadInputMode.NUMERIC_K1
                    }
                )

                // ABC Tab Pill
                DockTabPill(
                    label = "ABC",
                    isSelected = inputMode == KeypadInputMode.TEXT_ABC,
                    accentColor = Color(0xFF454F5E),
                    onClick = {
                        playFeedback()
                        inputMode = KeypadInputMode.TEXT_ABC
                    }
                )

                // Keyboard Toggle Icon
                IconButton(
                    onClick = {
                        playFeedback()
                        onToggleKeyboard()
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Keyboard,
                        contentDescription = "Keyboard",
                        tint = subtextColor,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Undo
                IconButton(
                    onClick = {
                        playFeedback()
                        onUndo()
                    },
                    enabled = canUndo,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo",
                        tint = if (canUndo) textColor else textColor.copy(alpha = 0.3f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Redo
                IconButton(
                    onClick = {
                        playFeedback()
                        onRedo()
                    },
                    enabled = canRedo,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Redo,
                        contentDescription = "Redo",
                        tint = if (canRedo) textColor else textColor.copy(alpha = 0.3f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Quick Variable Pill [ ↹ 500.00 ]
                if (quickVariableValue != null) {
                    val pillValStr = WorksheetTapeEngine.formatNumber(quickVariableValue, settings.indianDigitGrouping, settings.decimals)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = accentColor.copy(alpha = 0.2f),
                        modifier = Modifier.clickable {
                            playFeedback()
                            onInsertQuickVariable(quickVariableValue)
                        }
                    ) {
                        Text(
                            text = "↹ $pillValStr",
                            color = accentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Right: Live Grand Total Display with tap-to-copy
            val grandTotalFormatted = WorksheetTapeEngine.formatNumber(grandTotal, settings.indianDigitGrouping, settings.decimals)
            val isTotalNegative = grandTotal < 0
            val totalColor = if (isTotalNegative) Color(0xFFE53935) else Color(0xFFF1F5F9)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable {
                        clipboardManager.setText(AnnotatedString(grandTotalFormatted))
                        playFeedback()
                        Toast.makeText(context, "Copied total: $grandTotalFormatted", Toast.LENGTH_SHORT).show()
                    }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = grandTotalFormatted,
                    color = totalColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = subtextColor,
                    modifier = Modifier.size(13.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // --- KEYPAD BODY ---
        if (inputMode == KeypadInputMode.NUMERIC_K1) {
            // 4x6 Authentic CalcTape Pro Keypad
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Row 1: AC, ⌫, MR, Customise Button
                Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    KeyButton(
                        label = "AC",
                        bg = clearBtnBg,
                        textColor = clearBtnText,
                        fontSize = 19.sp,
                        modifier = Modifier.weight(1f),
                        onClick = { playFeedback(); onClear() }
                    )
                    KeyButton(
                        icon = Icons.Default.Backspace,
                        bg = steelBlueBg,
                        textColor = Color.White,
                        modifier = Modifier.weight(1f),
                        onClick = { playFeedback(); onBackspace() }
                    )
                    KeyButton(
                        label = "MR",
                        bg = memoryBtnBg,
                        textColor = memoryBtnText,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f),
                        onClick = { playFeedback(); onMemoryRecall() }
                    )
                    KeyButton(
                        label = "Customise\nButton",
                        bg = steelBlueBg,
                        textColor = steelBlueText,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f),
                        onClick = { playFeedback(); onCustomKey() },
                        onLongClick = { playFeedback(); onOpenCustomKeyDialog() }
                    )
                }

                // Row 2: M+, M-, MC, ÷
                val dynamicAmount = activeInputAmount ?: (if (grandTotal != 0.0) grandTotal else null)
                val mPlusSubLabel = if (dynamicAmount != null && dynamicAmount != 0.0) {
                    "+${WorksheetTapeEngine.formatNumber(dynamicAmount, settings.indianDigitGrouping, settings.decimals)}"
                } else null
                val mMinusSubLabel = if (dynamicAmount != null && dynamicAmount != 0.0) {
                    "-${WorksheetTapeEngine.formatNumber(dynamicAmount, settings.indianDigitGrouping, settings.decimals)}"
                } else null

                Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    KeyButton(
                        label = "M+",
                        subLabel = mPlusSubLabel,
                        bg = memoryBtnBg,
                        textColor = memoryBtnText,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f),
                        onClick = { playFeedback(); onMemoryPlus() }
                    )
                    KeyButton(
                        label = "M-",
                        subLabel = mMinusSubLabel,
                        bg = memoryBtnBg,
                        textColor = memoryBtnText,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f),
                        onClick = { playFeedback(); onMemoryMinus() }
                    )
                    KeyButton(
                        label = "MC",
                        bg = steelBlueBg,
                        textColor = steelBlueText,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f),
                        onClick = { playFeedback(); onMemoryClear() }
                    )
                    KeyButton(
                        label = "÷",
                        bg = steelBlueBg,
                        textColor = steelBlueText,
                        fontSize = 22.sp,
                        modifier = Modifier.weight(1f),
                        onClick = { playFeedback(); onOperator("/") }
                    )
                }

                // Row 3: 7, 8, 9, x
                Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    KeyButton("7", bg = numBtnBg, textColor = numBtnText, fontSize = 21.sp, modifier = Modifier.weight(1f)) { playFeedback(); onDigit("7") }
                    KeyButton("8", bg = numBtnBg, textColor = numBtnText, fontSize = 21.sp, modifier = Modifier.weight(1f)) { playFeedback(); onDigit("8") }
                    KeyButton("9", bg = numBtnBg, textColor = numBtnText, fontSize = 21.sp, modifier = Modifier.weight(1f)) { playFeedback(); onDigit("9") }
                    KeyButton("x", bg = steelBlueBg, textColor = steelBlueText, fontSize = 20.sp, modifier = Modifier.weight(1f)) { playFeedback(); onOperator("*") }
                }

                // Row 4: 4, 5, 6, −
                Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    KeyButton("4", bg = numBtnBg, textColor = numBtnText, fontSize = 21.sp, modifier = Modifier.weight(1f)) { playFeedback(); onDigit("4") }
                    KeyButton("5", bg = numBtnBg, textColor = numBtnText, fontSize = 21.sp, modifier = Modifier.weight(1f)) { playFeedback(); onDigit("5") }
                    KeyButton("6", bg = numBtnBg, textColor = numBtnText, fontSize = 21.sp, modifier = Modifier.weight(1f)) { playFeedback(); onDigit("6") }
                    KeyButton("−", bg = steelBlueBg, textColor = steelBlueText, fontSize = 22.sp, modifier = Modifier.weight(1f)) { playFeedback(); onOperator("-") }
                }

                // Row 5: 1, 2, 3, +
                Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    KeyButton("1", bg = numBtnBg, textColor = numBtnText, fontSize = 21.sp, modifier = Modifier.weight(1f)) { playFeedback(); onDigit("1") }
                    KeyButton("2", bg = numBtnBg, textColor = numBtnText, fontSize = 21.sp, modifier = Modifier.weight(1f)) { playFeedback(); onDigit("2") }
                    KeyButton("3", bg = numBtnBg, textColor = numBtnText, fontSize = 21.sp, modifier = Modifier.weight(1f)) { playFeedback(); onDigit("3") }
                    KeyButton("+", bg = steelBlueBg, textColor = steelBlueText, fontSize = 22.sp, modifier = Modifier.weight(1f)) { playFeedback(); onOperator("+") }
                }

                // Row 6: 0, ., %, =/↵
                Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    KeyButton("0", bg = numBtnBg, textColor = numBtnText, fontSize = 21.sp, modifier = Modifier.weight(1f)) { playFeedback(); onDigit("0") }
                    KeyButton(".", bg = numBtnBg, textColor = numBtnText, fontSize = 21.sp, modifier = Modifier.weight(1f)) { playFeedback(); onDigit(".") }
                    KeyButton("%", bg = steelBlueBg, textColor = steelBlueText, fontSize = 20.sp, modifier = Modifier.weight(1f)) { playFeedback(); onPercentage() }
                    KeyButton(
                        label = "=/↵",
                        bg = steelBlueBg,
                        textColor = steelBlueText,
                        fontSize = 18.sp,
                        modifier = Modifier.weight(1f),
                        onClick = { playFeedback(); onSubtotal() }
                    )
                }
            }
        } else {
            // ABC Text Comment Mode
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF262C35), RoundedCornerShape(8.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Remarks / Notes for active line:",
                    color = textColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = noteEditingText,
                    onValueChange = {
                        noteEditingText = it
                        onUpdateNote(it)
                    },
                    placeholder = { Text("e.g. chai, wax & wick, discount...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedBorderColor = accentColor
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick preset tags
                Text(text = "Quick tags:", color = subtextColor, fontSize = 11.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("chai", "cash", "discount", "tax", "rent", "total").forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = accentColor.copy(alpha = 0.15f),
                            modifier = Modifier.clickable {
                                noteEditingText = tag
                                onUpdateNote(tag)
                            }
                        ) {
                            Text(
                                text = tag,
                                color = accentColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { inputMode = KeypadInputMode.NUMERIC_K1 },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(40.dp)
                ) {
                    Text(text = "Done / Return to Keypad", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun cardBg(isLightCanvas: Boolean): Color {
    return if (isLightCanvas) Color(0xFFFFFFFF) else Color(0xFF1E2530)
}

@Composable
private fun DockTabPill(
    label: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (isSelected) accentColor else Color.Transparent,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else accentColor,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun KeyButton(
    label: String? = null,
    subLabel: String? = null,
    bg: Color,
    textColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    fontSize: androidx.compose.ui.unit.TextUnit = 18.sp,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bg,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black.copy(alpha = 0.12f)),
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(2.dp)) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, tint = textColor, modifier = Modifier.size(20.dp))
            } else if (subLabel != null && label != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text(
                        text = label,
                        color = textColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subLabel,
                        color = textColor.copy(alpha = 0.85f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1
                    )
                }
            } else if (label != null) {
                Text(
                    text = label,
                    color = textColor,
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = (fontSize.value * 1.1).sp
                )
            }
        }
    }
}
