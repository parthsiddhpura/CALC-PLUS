package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AngleMode
import com.example.model.ThemePalette
import kotlin.random.Random

/**
 * Professional, butter-smooth 10-column landscape keypad combining full
 * scientific functions on the left with high-precision standard numpad on the right.
 * Provides unmatched touch feedback, zero-overhead layout, and 30+ scientific functions.
 */
@Composable
fun LandscapeScientificKeypad(
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
    onMemoryAdd: () -> Unit = {},
    onMemorySubtract: () -> Unit = {},
    onMemoryRecall: () -> Unit = {},
    onMemoryClear: () -> Unit = {},
    onEng: () -> Unit = {},
    onAns: () -> Unit = {},
    onDecimalConverter: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isHyperbolic by remember { mutableStateOf(false) }

    val rowSpacing = 4.dp
    val colSpacing = 4.dp

    // Smooth color animation for toggle keys
    val secondBg by animateColorAsState(
        targetValue = if (isSecondFunction) theme.accentColor else theme.functionButtonBg,
        animationSpec = tween(150),
        label = "2nd_bg"
    )
    val secondText by animateColorAsState(
        targetValue = if (isSecondFunction) theme.backgroundColor else theme.functionButtonText,
        animationSpec = tween(150),
        label = "2nd_text"
    )

    val hypBg by animateColorAsState(
        targetValue = if (isHyperbolic) theme.accentColor.copy(alpha = 0.85f) else theme.functionButtonBg,
        animationSpec = tween(150),
        label = "hyp_bg"
    )
    val hypText by animateColorAsState(
        targetValue = if (isHyperbolic) theme.backgroundColor else theme.functionButtonText,
        animationSpec = tween(150),
        label = "hyp_text"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(rowSpacing)
    ) {
        // ==========================================
        // ROW 1: 2ⁿᵈ, hyp, (, ), Ans, DEG/RAD | AC, ⌫, %, ÷
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(colSpacing)
        ) {
            // Col 1: 2nd
            CalculatorButton(
                text = "2ⁿᵈ",
                onClick = onToggleSecondFunction,
                theme = theme,
                backgroundColor = secondBg,
                textColor = secondText,
                borderColor = theme.functionButtonBorder,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                testTag = "land_btn_2nd"
            )
            // Col 2: hyp
            CalculatorButton(
                text = "hyp",
                onClick = { isHyperbolic = !isHyperbolic },
                theme = theme,
                backgroundColor = hypBg,
                textColor = hypText,
                borderColor = theme.functionButtonBorder,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                testTag = "land_btn_hyp"
            )
            // Col 3: (
            CalculatorButton(
                text = "(",
                onClick = { onInput("(") },
                theme = theme,
                backgroundColor = theme.functionButtonBg,
                textColor = theme.functionButtonText,
                borderColor = theme.functionButtonBorder,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                testTag = "land_btn_open_paren"
            )
            // Col 4: )
            CalculatorButton(
                text = ")",
                onClick = { onInput(")") },
                theme = theme,
                backgroundColor = theme.functionButtonBg,
                textColor = theme.functionButtonText,
                borderColor = theme.functionButtonBorder,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                testTag = "land_btn_close_paren"
            )
            // Col 5: Ans
            CalculatorButton(
                text = "Ans",
                onClick = onAns,
                theme = theme,
                backgroundColor = theme.functionButtonBg,
                textColor = theme.accentColor,
                borderColor = theme.functionButtonBorder,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                testTag = "land_btn_ans"
            )
            // Col 6: DEG / RAD
            CalculatorButton(
                text = angleMode.name,
                onClick = onToggleAngleMode,
                theme = theme,
                backgroundColor = theme.functionButtonBg,
                textColor = theme.accentColor,
                borderColor = theme.functionButtonBorder,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                testTag = "land_btn_angle_mode"
            )
            // Col 7: AC
            CalculatorButton(
                text = "AC",
                onClick = onClear,
                theme = theme,
                backgroundColor = if (theme.isDark) Color(0xFFD9480F).copy(alpha = 0.28f) else Color(0xFFFFECEB),
                textColor = if (theme.isDark) Color(0xFFFF922B) else Color(0xFFC92A2A),
                borderColor = if (theme.isDark) Color(0xFFD9480F).copy(alpha = 0.55f) else Color(0xFFFFC9C9),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                testTag = "land_btn_ac"
            )
            // Col 8: ⌫
            CalculatorButton(
                text = "⌫",
                onClick = onBackspace,
                theme = theme,
                backgroundColor = theme.functionButtonBg,
                textColor = theme.functionButtonText,
                borderColor = theme.functionButtonBorder,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                testTag = "land_btn_backspace"
            )
            // Col 9: %
            CalculatorButton(
                text = "%",
                onClick = { onInput("%") },
                theme = theme,
                backgroundColor = theme.functionButtonBg,
                textColor = theme.functionButtonText,
                borderColor = theme.functionButtonBorder,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f),
                testTag = "land_btn_percent"
            )
            // Col 10: ÷
            CalculatorButton(
                text = "÷",
                onClick = { onInput("÷") },
                theme = theme,
                backgroundColor = theme.operatorButtonBg,
                textColor = theme.operatorButtonText,
                borderColor = theme.operatorButtonBorder,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                testTag = "land_btn_divide"
            )
        }

        // ==========================================
        // ROW 2: sin, cos, tan, xʸ, x², 1/x | 7, 8, 9, ×
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(colSpacing)
        ) {
            val sinText = when {
                isHyperbolic && isSecondFunction -> "sinh⁻¹"
                isHyperbolic -> "sinh"
                isSecondFunction -> "sin⁻¹"
                else -> "sin"
            }
            val sinFunc = when {
                isHyperbolic && isSecondFunction -> "asinh"
                isHyperbolic -> "sinh"
                isSecondFunction -> "asin"
                else -> "sin"
            }
            CalculatorButton(
                text = sinText,
                onClick = { onFunction(sinFunc) },
                theme = theme,
                backgroundColor = theme.functionButtonBg,
                textColor = theme.functionButtonText,
                borderColor = theme.functionButtonBorder,
                fontSize = if (sinText.length > 4) 10.sp else 12.sp,
                modifier = Modifier.weight(1f),
                testTag = "land_btn_sin"
            )

            val cosText = when {
                isHyperbolic && isSecondFunction -> "cosh⁻¹"
                isHyperbolic -> "cosh"
                isSecondFunction -> "cos⁻¹"
                else -> "cos"
            }
            val cosFunc = when {
                isHyperbolic && isSecondFunction -> "acosh"
                isHyperbolic -> "cosh"
                isSecondFunction -> "acos"
                else -> "cos"
            }
            CalculatorButton(
                text = cosText,
                onClick = { onFunction(cosFunc) },
                theme = theme,
                backgroundColor = theme.functionButtonBg,
                textColor = theme.functionButtonText,
                borderColor = theme.functionButtonBorder,
                fontSize = if (cosText.length > 4) 10.sp else 12.sp,
                modifier = Modifier.weight(1f),
                testTag = "land_btn_cos"
            )

            val tanText = when {
                isHyperbolic && isSecondFunction -> "tanh⁻¹"
                isHyperbolic -> "tanh"
                isSecondFunction -> "tan⁻¹"
                else -> "tan"
            }
            val tanFunc = when {
                isHyperbolic && isSecondFunction -> "atanh"
                isHyperbolic -> "tanh"
                isSecondFunction -> "atan"
                else -> "tan"
            }
            CalculatorButton(
                text = tanText,
                onClick = { onFunction(tanFunc) },
                theme = theme,
                backgroundColor = theme.functionButtonBg,
                textColor = theme.functionButtonText,
                borderColor = theme.functionButtonBorder,
                fontSize = if (tanText.length > 4) 10.sp else 12.sp,
                modifier = Modifier.weight(1f),
                testTag = "land_btn_tan"
            )

            // xʸ or ʸ√x
            CalculatorButton(
                text = if (isSecondFunction) "ʸ√x" else "xʸ",
                onClick = {
                    if (isSecondFunction) onInput("^(1/") else onInput("^(")
                },
                theme = theme,
                backgroundColor = theme.functionButtonBg,
                textColor = theme.functionButtonText,
                borderColor = theme.functionButtonBorder,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f),
                testTag = "land_btn_power"
            )

            // x² or √x
            CalculatorButton(
                text = if (isSecondFunction) "√x" else "x²",
                onClick = {
                    if (isSecondFunction) onFunction("sqrt") else onInput("^2")
                },
                theme = theme,
                backgroundColor = theme.functionButtonBg,
                textColor = theme.functionButtonText,
                borderColor = theme.functionButtonBorder,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f),
                testTag = "land_btn_sqr_sqrt"
            )

            // 1/x reciprocal
            CalculatorButton(
                text = "1/x",
                onClick = { onInput("^(-1)") },
                theme = theme,
                backgroundColor = theme.functionButtonBg,
                textColor = theme.functionButtonText,
                borderColor = theme.functionButtonBorder,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f),
                testTag = "land_btn_reciprocal"
            )

            // Numpad 7
            CalculatorButton(
                text = "7",
                onClick = { onInput("7") },
                theme = theme,
                backgroundColor = theme.numberButtonBg,
                textColor = theme.numberButtonText,
                borderColor = theme.numberButtonBorder,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                testTag = "land_btn_7"
            )
            // Numpad 8
            CalculatorButton(
                text = "8",
                onClick = { onInput("8") },
                theme = theme,
                backgroundColor = theme.numberButtonBg,
                textColor = theme.numberButtonText,
                borderColor = theme.numberButtonBorder,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                testTag = "land_btn_8"
            )
            // Numpad 9
            CalculatorButton(
                text = "9",
                onClick = { onInput("9") },
                theme = theme,
                backgroundColor = theme.numberButtonBg,
                textColor = theme.numberButtonText,
                borderColor = theme.numberButtonBorder,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                testTag = "land_btn_9"
            )
            // Operator ×
            CalculatorButton(
                text = "×",
                onClick = { onInput("×") },
                theme = theme,
                backgroundColor = theme.operatorButtonBg,
                textColor = theme.operatorButtonText,
                borderColor = theme.operatorButtonBorder,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                testTag = "land_btn_multiply"
            )
        }

        // ==========================================
        // ROW 3: ln, log, log₂, x³, x!, |x| | 4, 5, 6, −
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(colSpacing)
        ) {
            // ln or eˣ
            CalculatorButton(
                text = if (isSecondFunction) "eˣ" else "ln",
                onClick = {
                    if (isSecondFunction) onFunction("exp") else onFunction("ln")
                },
                theme = theme,
                backgroundColor = theme.functionButtonBg,
                textColor = theme.functionButtonText,
                borderColor = theme.functionButtonBorder,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f),
                testTag = "land_btn_ln"
            )

            // log or 10ˣ
            CalculatorButton(
                text = if (isSecondFunction) "10ˣ" else "log",
                onClick = {
                    if (isSecondFunction) onInput("10^(") else onFunction("log")
                },
                theme = theme,
                backgroundColor = theme.functionButtonBg,
                textColor = theme.functionButtonText,
                borderColor = theme.functionButtonBorder,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f),
                testTag = "land_btn_log"
            )

            // log₂ or 2ˣ
            CalculatorButton(
                text = if (isSecondFunction) "2ˣ" else "log₂",
                onClick = {
                    if (isSecondFunction) onInput("2^(") else onFunction("log2")
                },
                theme = theme,
                backgroundColor = theme.functionButtonBg,
                textColor = theme.functionButtonText,
                borderColor = theme.functionButtonBorder,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f),
                testTag = "land_btn_log2"
            )

            // x³ or ∛x
            CalculatorButton(
                text = if (isSecondFunction) "∛x" else "x³",
                onClick = {
                    if (isSecondFunction) onFunction("cbrt") else onInput("^3")
                },
                theme = theme,
                backgroundColor = theme.functionButtonBg,
                textColor = theme.functionButtonText,
                borderColor = theme.functionButtonBorder,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f),
                testTag = "land_btn_cube"
            )

            // x! Factorial
            CalculatorButton(
                text = "x!",
                onClick = { onInput("!") },
                theme = theme,
                backgroundColor = theme.functionButtonBg,
                textColor = theme.functionButtonText,
                borderColor = theme.functionButtonBorder,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f),
                testTag = "land_btn_factorial"
            )

            // |x| Absolute value
            CalculatorButton(
                text = "|x|",
                onClick = { onFunction("abs") },
                theme = theme,
                backgroundColor = theme.functionButtonBg,
                textColor = theme.functionButtonText,
                borderColor = theme.functionButtonBorder,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f),
                testTag = "land_btn_abs"
            )

            // Numpad 4
            CalculatorButton(
                text = "4",
                onClick = { onInput("4") },
                theme = theme,
                backgroundColor = theme.numberButtonBg,
                textColor = theme.numberButtonText,
                borderColor = theme.numberButtonBorder,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                testTag = "land_btn_4"
            )
            // Numpad 5
            CalculatorButton(
                text = "5",
                onClick = { onInput("5") },
                theme = theme,
                backgroundColor = theme.numberButtonBg,
                textColor = theme.numberButtonText,
                borderColor = theme.numberButtonBorder,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                testTag = "land_btn_5"
            )
            // Numpad 6
            CalculatorButton(
                text = "6",
                onClick = { onInput("6") },
                theme = theme,
                backgroundColor = theme.numberButtonBg,
                textColor = theme.numberButtonText,
                borderColor = theme.numberButtonBorder,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                testTag = "land_btn_6"
            )
            // Operator −
            CalculatorButton(
                text = "−",
                onClick = { onInput("−") },
                theme = theme,
                backgroundColor = theme.operatorButtonBg,
                textColor = theme.operatorButtonText,
                borderColor = theme.operatorButtonBorder,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                testTag = "land_btn_subtract"
            )
        }

        // ==========================================
        // ROW 4: π, e, φ, rand, exp, mod | 1, 2, 3, +
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(colSpacing)
        ) {
            // π
            CalculatorButton(
                text = "π",
                onClick = { onConstant("π") },
                theme = theme,
                backgroundColor = theme.functionButtonBg,
                textColor = theme.functionButtonText,
                borderColor = theme.functionButtonBorder,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f),
                testTag = "land_btn_pi"
            )

            // e
            CalculatorButton(
                text = "e",
                onClick = { onConstant("e") },
                theme = theme,
                backgroundColor = theme.functionButtonBg,
                textColor = theme.functionButtonText,
                borderColor = theme.functionButtonBorder,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f),
                testTag = "land_btn_e"
            )

            // φ Golden Ratio
            CalculatorButton(
                text = "φ",
                onClick = { onConstant("φ") },
                theme = theme,
                backgroundColor = theme.functionButtonBg,
                textColor = theme.functionButtonText,
                borderColor = theme.functionButtonBorder,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f),
                testTag = "land_btn_phi"
            )

            // rand
            CalculatorButton(
                text = "rand",
                onClick = {
                    val rnd = String.format(java.util.Locale.US, "%.4f", Random.nextDouble())
                    onInput(rnd)
                },
                theme = theme,
                backgroundColor = theme.functionButtonBg,
                textColor = theme.functionButtonText,
                borderColor = theme.functionButtonBorder,
                fontSize = 11.sp,
                modifier = Modifier.weight(1f),
                testTag = "land_btn_rand"
            )

            // exp
            CalculatorButton(
                text = "exp",
                onClick = { onFunction("exp") },
                theme = theme,
                backgroundColor = theme.functionButtonBg,
                textColor = theme.functionButtonText,
                borderColor = theme.functionButtonBorder,
                fontSize = 11.sp,
                modifier = Modifier.weight(1f),
                testTag = "land_btn_exp"
            )

            // mod
            CalculatorButton(
                text = "mod",
                onClick = { onInput("%") },
                theme = theme,
                backgroundColor = theme.functionButtonBg,
                textColor = theme.functionButtonText,
                borderColor = theme.functionButtonBorder,
                fontSize = 11.sp,
                modifier = Modifier.weight(1f),
                testTag = "land_btn_mod"
            )

            // Numpad 1
            CalculatorButton(
                text = "1",
                onClick = { onInput("1") },
                theme = theme,
                backgroundColor = theme.numberButtonBg,
                textColor = theme.numberButtonText,
                borderColor = theme.numberButtonBorder,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                testTag = "land_btn_1"
            )
            // Numpad 2
            CalculatorButton(
                text = "2",
                onClick = { onInput("2") },
                theme = theme,
                backgroundColor = theme.numberButtonBg,
                textColor = theme.numberButtonText,
                borderColor = theme.numberButtonBorder,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                testTag = "land_btn_2"
            )
            // Numpad 3
            CalculatorButton(
                text = "3",
                onClick = { onInput("3") },
                theme = theme,
                backgroundColor = theme.numberButtonBg,
                textColor = theme.numberButtonText,
                borderColor = theme.numberButtonBorder,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                testTag = "land_btn_3"
            )
            // Operator +
            CalculatorButton(
                text = "+",
                onClick = { onInput("+") },
                theme = theme,
                backgroundColor = theme.operatorButtonBg,
                textColor = theme.operatorButtonText,
                borderColor = theme.operatorButtonBorder,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                testTag = "land_btn_add"
            )
        }

        // ==========================================
        // ROW 5: EE, round, floor, ceil, Eng, F↔D | ±, 0, ., =
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(colSpacing)
        ) {
            // Col 1: EE (Scientific notation exponent *10^)
            CalculatorButton(
                text = "EE",
                onClick = { onInput("*10^(") },
                theme = theme,
                backgroundColor = theme.functionButtonBg,
                textColor = theme.functionButtonText,
                borderColor = theme.functionButtonBorder,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f),
                testTag = "land_btn_ee"
            )

            // Col 2: round
            CalculatorButton(
                text = "rnd",
                onClick = { onFunction("round") },
                theme = theme,
                backgroundColor = theme.functionButtonBg,
                textColor = theme.functionButtonText,
                borderColor = theme.functionButtonBorder,
                fontSize = 11.sp,
                modifier = Modifier.weight(1f),
                testTag = "land_btn_round"
            )

            // Col 3: floor
            CalculatorButton(
                text = "floor",
                onClick = { onFunction("floor") },
                theme = theme,
                backgroundColor = theme.functionButtonBg,
                textColor = theme.functionButtonText,
                borderColor = theme.functionButtonBorder,
                fontSize = 10.sp,
                modifier = Modifier.weight(1f),
                testTag = "land_btn_floor"
            )

            // Col 4: ceil
            CalculatorButton(
                text = "ceil",
                onClick = { onFunction("ceil") },
                theme = theme,
                backgroundColor = theme.functionButtonBg,
                textColor = theme.functionButtonText,
                borderColor = theme.functionButtonBorder,
                fontSize = 10.sp,
                modifier = Modifier.weight(1f),
                testTag = "land_btn_ceil"
            )

            // Col 5: Eng
            CalculatorButton(
                text = "Eng",
                onClick = onEng,
                theme = theme,
                backgroundColor = theme.functionButtonBg,
                textColor = theme.accentColor,
                borderColor = theme.functionButtonBorder,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                testTag = "land_btn_eng"
            )

            // Col 6: F↔D
            CalculatorButton(
                text = "F↔D",
                onClick = onDecimalConverter,
                theme = theme,
                backgroundColor = theme.functionButtonBg,
                textColor = theme.accentColor,
                borderColor = theme.functionButtonBorder,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                testTag = "land_btn_f_d"
            )

            // Col 7: ± Negate
            CalculatorButton(
                text = "±",
                onClick = onNegate,
                theme = theme,
                backgroundColor = theme.numberButtonBg,
                textColor = theme.numberButtonText,
                borderColor = theme.numberButtonBorder,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                testTag = "land_btn_negate"
            )

            // Col 8: 0
            CalculatorButton(
                text = "0",
                onClick = { onInput("0") },
                theme = theme,
                backgroundColor = theme.numberButtonBg,
                textColor = theme.numberButtonText,
                borderColor = theme.numberButtonBorder,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                testTag = "land_btn_0"
            )

            // Col 9: . Decimal
            CalculatorButton(
                text = ".",
                onClick = { onInput(".") },
                theme = theme,
                backgroundColor = theme.numberButtonBg,
                textColor = theme.numberButtonText,
                borderColor = theme.numberButtonBorder,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                testTag = "land_btn_dot"
            )

            // Col 10: = (Accent color)
            CalculatorButton(
                text = "=",
                onClick = onEquals,
                theme = theme,
                backgroundColor = theme.accentColor,
                textColor = theme.backgroundColor,
                borderColor = theme.accentColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                testTag = "land_btn_equals"
            )
        }
    }
}
