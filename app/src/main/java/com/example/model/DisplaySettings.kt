package com.example.model

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

enum class DisplaySeparatorStyle(val displayName: String, val sample: String) {
    COMMA("Comma (,)", "1,234,567.89"),
    SPACE("Space ( )", "1 234 567.89"),
    INDIAN("Indian (Lakh/Crore)", "12,34,567.89"),
    NONE("None (Raw)", "1234567.89")
}

enum class DisplayPrecisionMode(val displayName: String, val decimalPlaces: Int?) {
    AUTO("Auto", null),
    TWO_DECIMALS("2 Decimals (0.00)", 2),
    FOUR_DECIMALS("4 Decimals (0.0000)", 4),
    SIX_DECIMALS("6 Decimals (0.000000)", 6),
    EXACT("Full / Exact", -1)
}

enum class DisplayScaleSize(val displayName: String, val resultSp: Int, val exprSp: Int) {
    COMPACT("Compact", 58, 26),
    STANDARD("Standard", 78, 34),
    LARGE("Large", 92, 40)
}

enum class DisplayNotation(val displayName: String, val sample: String) {
    STANDARD("Standard", "1,250,000"),
    SCIENTIFIC("Scientific (e)", "1.25e+6"),
    ENGINEERING("Engineering (3x)", "1.25 × 10⁶")
}

@androidx.compose.runtime.Immutable
data class DisplayConfig(
    val separatorStyle: DisplaySeparatorStyle = DisplaySeparatorStyle.INDIAN,
    val precisionMode: DisplayPrecisionMode = DisplayPrecisionMode.AUTO,
    val scaleSize: DisplayScaleSize = DisplayScaleSize.STANDARD,
    val notation: DisplayNotation = DisplayNotation.STANDARD,
    val showLivePreview: Boolean = true,
    val showStatusBadges: Boolean = true,
    val showScanlinesOverride: Boolean? = null,
    val copyOnTap: Boolean = true
)

object DisplayFormatter {

    private val DIGIT_SPACE_REGEX = Regex("(?<=\\d)\\s+(?=\\d)")
    private val OPERATOR_SPLIT_REGEX = Regex("([+\\-×÷*/%^()= ])")
    private val SUPER_MAP = mapOf(
        '0' to '⁰', '1' to '¹', '2' to '²', '3' to '³', '4' to '⁴',
        '5' to '⁵', '6' to '⁶', '7' to '⁷', '8' to '⁸', '9' to '⁹', '-' to '⁻'
    )

    fun formatNumber(
        valueStr: String,
        separatorStyle: DisplaySeparatorStyle = DisplaySeparatorStyle.INDIAN,
        precisionMode: DisplayPrecisionMode = DisplayPrecisionMode.AUTO,
        notation: DisplayNotation = DisplayNotation.STANDARD
    ): String {
        val clean = valueStr.trim().replace(",", "").replace(DIGIT_SPACE_REGEX, "")
        if (clean.isEmpty() || clean == "Error" || clean == "NaN" || clean == "Infinity" || clean == "-Infinity") {
            return clean
        }

        val doubleVal = clean.toDoubleOrNull() ?: return clean

        // Handle Notation
        if (notation == DisplayNotation.SCIENTIFIC && doubleVal != 0.0 && (Math.abs(doubleVal) >= 10000 || Math.abs(doubleVal) < 0.001)) {
            val df = DecimalFormat("0.######E0", DecimalFormatSymbols(Locale.US))
            return df.format(doubleVal).lowercase()
        }

        if (notation == DisplayNotation.ENGINEERING && doubleVal != 0.0 && (Math.abs(doubleVal) >= 1000 || Math.abs(doubleVal) < 0.01)) {
            val exponent = (Math.floor(Math.log10(Math.abs(doubleVal)) / 3.0) * 3).toInt()
            val mantissa = doubleVal / Math.pow(10.0, exponent.toDouble())
            val df = DecimalFormat("#,##0.####", DecimalFormatSymbols(Locale.US))
            val superExp = exponent.toString().map { SUPER_MAP[it] ?: it }.joinToString("")
            return "${df.format(mantissa)} × 10$superExp"
        }

        // Handle Precision Mode
        if (precisionMode.decimalPlaces != null && precisionMode.decimalPlaces >= 0) {
            val places = precisionMode.decimalPlaces
            val pattern = if (places == 0) "#,##0" else "#,##0." + "0".repeat(places)
            val df = DecimalFormat(pattern, DecimalFormatSymbols(Locale.US))
            val formatted = df.format(doubleVal)
            return applySeparator(formatted, separatorStyle)
        }

        // For raw/auto numbers: separate integer part and fractional part
        val isNegative = clean.startsWith("-")
        val unsigned = if (isNegative) clean.substring(1) else clean
        val parts = unsigned.split(".", limit = 2)
        val intPart = parts[0]
        val fracPart = if (parts.size > 1) parts[1] else null
        val hasTrailingDot = clean.endsWith(".") && fracPart == null

        val formattedInt = when (separatorStyle) {
            DisplaySeparatorStyle.NONE -> intPart
            DisplaySeparatorStyle.COMMA -> formatWithStandardGrouping(intPart, ',')
            DisplaySeparatorStyle.SPACE -> formatWithStandardGrouping(intPart, ' ')
            DisplaySeparatorStyle.INDIAN -> formatWithIndianGrouping(intPart)
        }

        val result = StringBuilder(formattedInt.length + (fracPart?.length ?: 0) + 2)
        if (isNegative) result.append("-")
        result.append(formattedInt)
        if (fracPart != null) {
            result.append(".").append(fracPart)
        } else if (hasTrailingDot) {
            result.append(".")
        }

        return result.toString()
    }

    private fun applySeparator(formattedNumber: String, separatorStyle: DisplaySeparatorStyle): String {
        return when (separatorStyle) {
            DisplaySeparatorStyle.COMMA -> formattedNumber
            DisplaySeparatorStyle.SPACE -> formattedNumber.replace(",", " ")
            DisplaySeparatorStyle.NONE -> formattedNumber.replace(",", "")
            DisplaySeparatorStyle.INDIAN -> {
                val clean = formattedNumber.replace(",", "")
                val parts = clean.split(".", limit = 2)
                val intPart = parts[0]
                val fracPart = if (parts.size > 1) "." + parts[1] else ""
                formatWithIndianGrouping(intPart) + fracPart
            }
        }
    }

    private fun formatWithStandardGrouping(digits: String, separator: Char): String {
        if (digits.length <= 3) return digits
        val sb = StringBuilder(digits.length + digits.length / 3)
        val len = digits.length
        for (i in digits.indices) {
            sb.append(digits[i])
            val remaining = len - 1 - i
            if (remaining > 0 && remaining % 3 == 0) {
                sb.append(separator)
            }
        }
        return sb.toString()
    }

    private fun formatWithIndianGrouping(digits: String): String {
        if (digits.length <= 3) return digits
        val isNeg = digits.startsWith("-")
        val num = if (isNeg) digits.substring(1) else digits
        if (num.length <= 3) return digits

        val lastThree = num.substring(num.length - 3)
        val rest = num.substring(0, num.length - 3)

        val sb = StringBuilder(digits.length + digits.length / 2)
        if (isNeg) sb.append("-")

        val restLen = rest.length
        for (i in rest.indices) {
            sb.append(rest[i])
            val remaining = restLen - 1 - i
            if (remaining > 0 && remaining % 2 == 0) {
                sb.append(",")
            }
        }
        sb.append(",").append(lastThree)
        return sb.toString()
    }

    fun formatExpression(
        expression: String,
        separatorStyle: DisplaySeparatorStyle = DisplaySeparatorStyle.INDIAN
    ): String {
        if (expression.isEmpty() || separatorStyle == DisplaySeparatorStyle.NONE) return expression

        // Split by standard operators keeping delimiters
        val tokens = expression.split(OPERATOR_SPLIT_REGEX)
        val matches = OPERATOR_SPLIT_REGEX.findAll(expression).map { it.value }.toList()

        val sb = StringBuilder(expression.length + 8)
        var matchIdx = 0

        for (token in tokens) {
            val cleanToken = token.replace(",", "").replace(DIGIT_SPACE_REGEX, "")
            if (cleanToken.isNotBlank() && cleanToken.all { it.isDigit() || it == '.' }) {
                sb.append(formatNumber(cleanToken, separatorStyle, DisplayPrecisionMode.AUTO, DisplayNotation.STANDARD))
            } else {
                sb.append(token)
            }
            if (matchIdx < matches.size) {
                sb.append(matches[matchIdx])
                matchIdx++
            }
        }
        while (matchIdx < matches.size) {
            sb.append(matches[matchIdx])
            matchIdx++
        }

        return sb.toString()
    }
}
