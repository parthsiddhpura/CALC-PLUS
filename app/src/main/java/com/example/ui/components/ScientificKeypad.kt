package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AngleMode
import com.example.model.ThemePalette

@Composable
fun ScientificKeypad(
    theme: ThemePalette,
    angleMode: AngleMode,
    isSecondFunction: Boolean,
    onToggleAngleMode: () -> Unit,
    onToggleSecondFunction: () -> Unit,
    onInput: (String) -> Unit,
    onFunction: (String) -> Unit,
    onConstant: (String) -> Unit,
    onClear: () -> Unit,
    onBackspace: () -> Unit,
    onNegate: () -> Unit,
    onEquals: () -> Unit,
    onMemoryAdd: () -> Unit,
    onMemorySubtract: () -> Unit,
    onMemoryRecall: () -> Unit,
    onMemoryClear: () -> Unit,
    onEng: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val rowSpacing = 3.dp
    val colSpacing = 3.dp
    val sciKeyHeight = 32.dp
    val stdKeyHeight = 40.dp

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(rowSpacing)
    ) {
        // Sci Row 1: 2nd, DEG/RAD, sin/asin, cos/acos, tan/atan
        Row(
            modifier = Modifier.fillMaxWidth().height(sciKeyHeight),
            horizontalArrangement = Arrangement.spacedBy(colSpacing)
        ) {
            CalculatorButton(
                text = "2ⁿᵈ",
                onClick = onToggleSecondFunction,
                theme = theme,
                backgroundColor = if (isSecondFunction) theme.accentColor else theme.functionButtonBg,
                textColor = if (isSecondFunction) theme.backgroundColor else theme.functionButtonText,
                borderColor = theme.functionButtonBorder,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                testTag = "btn_2nd"
            )
            CalculatorButton(
                text = angleMode.name,
                onClick = onToggleAngleMode,
                theme = theme,
                backgroundColor = theme.functionButtonBg,
                textColor = theme.accentColor,
                borderColor = theme.functionButtonBorder,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                testTag = "btn_deg_rad"
            )
            CalculatorButton(
                text = if (isSecondFunction) "sin⁻¹" else "sin",
                onClick = { onFunction(if (isSecondFunction) "asin" else "sin") },
                theme = theme,
                backgroundColor = theme.functionButtonBg,
                textColor = theme.functionButtonText,
                borderColor = theme.functionButtonBorder,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f),
                testTag = "btn_sin"
            )
            CalculatorButton(
                text = if (isSecondFunction) "cos⁻¹" else "cos",
                onClick = { onFunction(if (isSecondFunction) "acos" else "cos") },
                theme = theme,
                backgroundColor = theme.functionButtonBg,
                textColor = theme.functionButtonText,
                borderColor = theme.functionButtonBorder,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f),
                testTag = "btn_cos"
            )
            CalculatorButton(
                text = if (isSecondFunction) "tan⁻¹" else "tan",
                onClick = { onFunction(if (isSecondFunction) "atan" else "tan") },
                theme = theme,
                backgroundColor = theme.functionButtonBg,
                textColor = theme.functionButtonText,
                borderColor = theme.functionButtonBorder,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f),
                testTag = "btn_tan"
            )
        }

        // Sci Row 2: ln / eˣ, log / 10ˣ, xʸ, √ / ∛, x!
        Row(
            modifier = Modifier.fillMaxWidth().height(sciKeyHeight),
            horizontalArrangement = Arrangement.spacedBy(colSpacing)
        ) {
            CalculatorButton(
                text = if (isSecondFunction) "eˣ" else "ln",
                onClick = {
                    if (isSecondFunction) {
                        onInput("e^(")
                    } else {
                        onFunction("ln")
                    }
                },
                theme = theme,
                backgroundColor = theme.functionButtonBg,
                textColor = theme.functionButtonText,
                borderColor = theme.functionButtonBorder,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f),
                testTag = "btn_ln"
            )
            CalculatorButton(
                text = if (isSecondFunction) "10ˣ" else "log",
                onClick = {
                    if (isSecondFunction) {
                        onInput("10^(")
                    } else {
                        onFunction("log10")
                    }
                },
                theme = theme,
                backgroundColor = theme.functionButtonBg,
                textColor = theme.functionButtonText,
                borderColor = theme.functionButtonBorder,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f),
                testTag = "btn_log"
            )
            CalculatorButton(
                text = "xʸ",
                onClick = { onInput("^") },
                theme = theme,
                backgroundColor = theme.functionButtonBg,
                textColor = theme.functionButtonText,
                borderColor = theme.functionButtonBorder,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f),
                testTag = "btn_power"
            )
            CalculatorButton(
                text = if (isSecondFunction) "∛" else "√",
                onClick = { onFunction(if (isSecondFunction) "cbrt" else "sqrt") },
                theme = theme,
                backgroundColor = theme.functionButtonBg,
                textColor = theme.functionButtonText,
                borderColor = theme.functionButtonBorder,
                fontSize = 15.sp,
                modifier = Modifier.weight(1f),
                testTag = "btn_sqrt"
            )
            CalculatorButton(
                text = "x!",
                onClick = { onInput("!") },
                theme = theme,
                backgroundColor = theme.functionButtonBg,
                textColor = theme.functionButtonText,
                borderColor = theme.functionButtonBorder,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f),
                testTag = "btn_factorial"
            )
        }

        // Sci Row 3: π, e, φ, 1/x, |x|
        Row(
            modifier = Modifier.fillMaxWidth().height(sciKeyHeight),
            horizontalArrangement = Arrangement.spacedBy(colSpacing)
        ) {
            CalculatorButton(
                text = "π",
                onClick = { onConstant("π") },
                theme = theme,
                backgroundColor = theme.functionButtonBg,
                textColor = theme.functionButtonText,
                borderColor = theme.functionButtonBorder,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f),
                testTag = "btn_pi"
            )
            CalculatorButton(
                text = "e",
                onClick = { onConstant("e") },
                theme = theme,
                backgroundColor = theme.functionButtonBg,
                textColor = theme.functionButtonText,
                borderColor = theme.functionButtonBorder,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f),
                testTag = "btn_e"
            )
            CalculatorButton(
                text = if (isSecondFunction) "ENG" else "φ",
                onClick = { if (isSecondFunction) onEng() else onConstant("φ") },
                theme = theme,
                backgroundColor = if (isSecondFunction) theme.accentColor.copy(alpha = 0.22f) else theme.functionButtonBg,
                textColor = if (isSecondFunction) theme.accentColor else theme.functionButtonText,
                borderColor = if (isSecondFunction) theme.accentColor.copy(alpha = 0.6f) else theme.functionButtonBorder,
                fontSize = if (isSecondFunction) 12.sp else 16.sp,
                fontWeight = if (isSecondFunction) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.weight(1f),
                testTag = if (isSecondFunction) "btn_eng" else "btn_phi"
            )
            CalculatorButton(
                text = "1/x",
                onClick = { onInput("1/(") },
                theme = theme,
                backgroundColor = theme.functionButtonBg,
                textColor = theme.functionButtonText,
                borderColor = theme.functionButtonBorder,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f),
                testTag = "btn_reciprocal"
            )
            CalculatorButton(
                text = "|x|",
                onClick = { onFunction("abs") },
                theme = theme,
                backgroundColor = theme.functionButtonBg,
                textColor = theme.functionButtonText,
                borderColor = theme.functionButtonBorder,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f),
                testTag = "btn_abs"
            )
        }

        // Memory & Bracket Row: MC, MR, M+, (, )
        Row(
            modifier = Modifier.fillMaxWidth().height(sciKeyHeight),
            horizontalArrangement = Arrangement.spacedBy(colSpacing)
        ) {
            CalculatorButton(
                text = "MC",
                onClick = onMemoryClear,
                theme = theme,
                backgroundColor = theme.functionButtonBg,
                textColor = theme.functionButtonText.copy(alpha = 0.85f),
                borderColor = theme.functionButtonBorder,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f),
                testTag = "btn_mc"
            )
            CalculatorButton(
                text = "MR",
                onClick = onMemoryRecall,
                theme = theme,
                backgroundColor = theme.functionButtonBg,
                textColor = theme.functionButtonText.copy(alpha = 0.85f),
                borderColor = theme.functionButtonBorder,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f),
                testTag = "btn_mr"
            )
            CalculatorButton(
                text = "M+",
                onClick = onMemoryAdd,
                theme = theme,
                backgroundColor = theme.functionButtonBg,
                textColor = theme.functionButtonText.copy(alpha = 0.85f),
                borderColor = theme.functionButtonBorder,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f),
                testTag = "btn_mplus"
            )
            CalculatorButton(
                text = "(",
                onClick = { onInput("(") },
                theme = theme,
                backgroundColor = theme.functionButtonBg,
                textColor = theme.functionButtonText,
                borderColor = theme.functionButtonBorder,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f),
                testTag = "btn_open_paren"
            )
            CalculatorButton(
                text = ")",
                onClick = { onInput(")") },
                theme = theme,
                backgroundColor = theme.functionButtonBg,
                textColor = theme.functionButtonText,
                borderColor = theme.functionButtonBorder,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f),
                testTag = "btn_close_paren"
            )
        }

        // Main Digits & Arithmetic Grid
        // Row 1: AC, ⌫, %, ÷
        Row(
            modifier = Modifier.fillMaxWidth().height(stdKeyHeight),
            horizontalArrangement = Arrangement.spacedBy(colSpacing)
        ) {
            CalculatorButton(
                text = "AC",
                onClick = onClear,
                theme = theme,
                backgroundColor = theme.functionButtonBg,
                textColor = theme.functionButtonText,
                borderColor = theme.functionButtonBorder,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                testTag = "btn_ac_sci"
            )
            CalculatorButton(
                text = "⌫",
                onClick = onBackspace,
                theme = theme,
                backgroundColor = theme.functionButtonBg,
                textColor = theme.functionButtonText,
                borderColor = theme.functionButtonBorder,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                testTag = "btn_backspace_sci"
            )
            CalculatorButton(
                text = "%",
                onClick = { onInput("%") },
                theme = theme,
                backgroundColor = theme.functionButtonBg,
                textColor = theme.functionButtonText,
                borderColor = theme.functionButtonBorder,
                modifier = Modifier.weight(1f),
                testTag = "btn_percent_sci"
            )
            CalculatorButton(
                text = "÷",
                onClick = { onInput("÷") },
                theme = theme,
                backgroundColor = theme.operatorButtonBg,
                textColor = theme.operatorButtonText,
                borderColor = theme.operatorButtonBorder,
                fontSize = 24.sp,
                modifier = Modifier.weight(1f),
                testTag = "btn_divide_sci"
            )
        }

        // Row 2: 7, 8, 9, ×
        Row(
            modifier = Modifier.fillMaxWidth().height(stdKeyHeight),
            horizontalArrangement = Arrangement.spacedBy(colSpacing)
        ) {
            CalculatorButton(
                text = "7",
                onClick = { onInput("7") },
                theme = theme,
                modifier = Modifier.weight(1f),
                testTag = "btn_7_sci"
            )
            CalculatorButton(
                text = "8",
                onClick = { onInput("8") },
                theme = theme,
                modifier = Modifier.weight(1f),
                testTag = "btn_8_sci"
            )
            CalculatorButton(
                text = "9",
                onClick = { onInput("9") },
                theme = theme,
                modifier = Modifier.weight(1f),
                testTag = "btn_9_sci"
            )
            CalculatorButton(
                text = "×",
                onClick = { onInput("×") },
                theme = theme,
                backgroundColor = theme.operatorButtonBg,
                textColor = theme.operatorButtonText,
                borderColor = theme.operatorButtonBorder,
                fontSize = 24.sp,
                modifier = Modifier.weight(1f),
                testTag = "btn_multiply_sci"
            )
        }

        // Row 3: 4, 5, 6, −
        Row(
            modifier = Modifier.fillMaxWidth().height(stdKeyHeight),
            horizontalArrangement = Arrangement.spacedBy(colSpacing)
        ) {
            CalculatorButton(
                text = "4",
                onClick = { onInput("4") },
                theme = theme,
                modifier = Modifier.weight(1f),
                testTag = "btn_4_sci"
            )
            CalculatorButton(
                text = "5",
                onClick = { onInput("5") },
                theme = theme,
                modifier = Modifier.weight(1f),
                testTag = "btn_5_sci"
            )
            CalculatorButton(
                text = "6",
                onClick = { onInput("6") },
                theme = theme,
                modifier = Modifier.weight(1f),
                testTag = "btn_6_sci"
            )
            CalculatorButton(
                text = "−",
                onClick = { onInput("−") },
                theme = theme,
                backgroundColor = theme.operatorButtonBg,
                textColor = theme.operatorButtonText,
                borderColor = theme.operatorButtonBorder,
                fontSize = 24.sp,
                modifier = Modifier.weight(1f),
                testTag = "btn_subtract_sci"
            )
        }

        // Row 4: 1, 2, 3, +
        Row(
            modifier = Modifier.fillMaxWidth().height(stdKeyHeight),
            horizontalArrangement = Arrangement.spacedBy(colSpacing)
        ) {
            CalculatorButton(
                text = "1",
                onClick = { onInput("1") },
                theme = theme,
                modifier = Modifier.weight(1f),
                testTag = "btn_1_sci"
            )
            CalculatorButton(
                text = "2",
                onClick = { onInput("2") },
                theme = theme,
                modifier = Modifier.weight(1f),
                testTag = "btn_2_sci"
            )
            CalculatorButton(
                text = "3",
                onClick = { onInput("3") },
                theme = theme,
                modifier = Modifier.weight(1f),
                testTag = "btn_3_sci"
            )
            CalculatorButton(
                text = "+",
                onClick = { onInput("+") },
                theme = theme,
                backgroundColor = theme.operatorButtonBg,
                textColor = theme.operatorButtonText,
                borderColor = theme.operatorButtonBorder,
                fontSize = 24.sp,
                modifier = Modifier.weight(1f),
                testTag = "btn_add_sci"
            )
        }

        // Row 5: ±, 0, ., =
        Row(
            modifier = Modifier.fillMaxWidth().height(stdKeyHeight),
            horizontalArrangement = Arrangement.spacedBy(colSpacing)
        ) {
            CalculatorButton(
                text = "±",
                onClick = onNegate,
                theme = theme,
                modifier = Modifier.weight(1f),
                testTag = "btn_negate_sci"
            )
            CalculatorButton(
                text = "0",
                onClick = { onInput("0") },
                theme = theme,
                modifier = Modifier.weight(1f),
                testTag = "btn_0_sci"
            )
            CalculatorButton(
                text = ".",
                onClick = { onInput(".") },
                theme = theme,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                testTag = "btn_dot_sci"
            )
            CalculatorButton(
                text = "=",
                onClick = onEquals,
                theme = theme,
                backgroundBrush = theme.equalsButtonBrush,
                textColor = theme.equalsButtonText,
                borderColor = theme.equalsButtonBorder,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.weight(1f),
                testTag = "btn_equals_sci",
                icon = when {
                    theme.hasBatSignal -> {
                        {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                BatmanLogoIcon(
                                    modifier = Modifier.size(16.dp),
                                    tint = theme.equalsButtonText
                                )
                                Text(
                                    text = "=",
                                    color = theme.equalsButtonText,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                    theme.hasArcReactor -> {
                        {
                            ArcReactorIcon(
                                modifier = Modifier.size(32.dp),
                                glowColor = Color(0xFF00F0FF),
                                showOuterTabs = true
                            )
                        }
                    }
                    else -> null
                }
            )
        }
    }
}
