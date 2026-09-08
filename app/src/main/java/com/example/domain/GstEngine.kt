package com.example.domain

import com.example.model.AngleMode
import com.example.model.GstCalculationType
import com.example.model.GstResult
import com.example.model.GstSlab
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

data class TaxPreset(
    val countryName: String,
    val flagEmoji: String,
    val taxName: String,
    val rates: List<Double>
)

object GstEngine {

    val DEFAULT_SLABS = listOf(
        GstSlab(id = 0, name = "GST+0", ratePercent = 0.0, label = "0%"),
        GstSlab(id = 1, name = "GST+1", ratePercent = 5.0, label = "5%"),
        GstSlab(id = 2, name = "GST+2", ratePercent = 12.0, label = "12%"),
        GstSlab(id = 3, name = "GST+3", ratePercent = 18.0, label = "18%"),
        GstSlab(id = 4, name = "GST+4", ratePercent = 28.0, label = "28%")
    )

    val COUNTRY_PRESETS = listOf(
        TaxPreset("India (GST)", "🇮🇳", "GST", listOf(0.0, 5.0, 12.0, 18.0, 28.0)),
        TaxPreset("United States (Sales Tax)", "🇺🇸", "Tax", listOf(0.0, 4.0, 6.0, 8.25, 10.0)),
        TaxPreset("United Kingdom (VAT)", "🇬🇧", "VAT", listOf(0.0, 5.0, 12.5, 20.0, 20.0)),
        TaxPreset("European Union (VAT)", "🇪🇺", "VAT", listOf(0.0, 5.5, 10.0, 20.0, 23.0)),
        TaxPreset("Canada (GST/HST)", "🇨🇦", "GST", listOf(0.0, 5.0, 12.0, 13.0, 15.0)),
        TaxPreset("Australia & NZ (GST)", "🇦🇺", "GST", listOf(0.0, 5.0, 10.0, 12.5, 15.0)),
        TaxPreset("Japan (消費税)", "🇯🇵", "Tax", listOf(0.0, 8.0, 10.0, 10.0, 10.0)),
        TaxPreset("Singapore (GST)", "🇸🇬", "GST", listOf(0.0, 7.0, 8.0, 9.0, 9.0)),
        TaxPreset("Saudi Arabia & UAE (VAT)", "🇸🇦", "VAT", listOf(0.0, 5.0, 10.0, 15.0, 15.0))
    )

    private val US_SYMBOLS = DecimalFormatSymbols(Locale.US)
    private val currencyDf = DecimalFormat("#,##0.00", US_SYMBOLS)

    fun formatRateLabel(rate: Double): String {
        return if (rate == rate.toLong().toDouble()) {
            "${rate.toLong()}%"
        } else {
            "$rate%"
        }
    }

    fun evaluateAmountOrExpression(input: String): Double {
        if (input.isBlank()) return 0.0
        val trimmed = input.trim().replace(",", "").replace(Regex("(?<=\\d)\\s+(?=\\d)"), "")
        val direct = trimmed.toDoubleOrNull()
        if (direct != null) return direct

        // Gracefully handle in-progress typing with a trailing operator (e.g., "10000+")
        val safeExpr = trimmed.trimEnd('+', '−', '-', '×', '*', '÷', '/', '%')
        val safeDirect = safeExpr.toDoubleOrNull()
        if (safeDirect != null) return safeDirect

        return try {
            val toEval = if (safeExpr.isNotBlank()) safeExpr else trimmed
            val resultStr = CalculatorEngine.evaluate(toEval, AngleMode.DEG)
            if (resultStr == "Error") 0.0 else resultStr.replace(",", "").toDoubleOrNull() ?: 0.0
        } catch (e: Exception) {
            0.0
        }
    }

    fun calculate(
        amount: Double,
        ratePercent: Double,
        type: GstCalculationType
    ): GstResult {
        val bdAmount = BigDecimal.valueOf(amount)
        val bdRate = BigDecimal.valueOf(ratePercent)
        val hundred = BigDecimal.valueOf(100.0)

        if (type == GstCalculationType.EXCLUSIVE) {
            // GST Added: Amount is Net (Excluding GST)
            // GST Amount = Amount * (Rate / 100)
            // Gross Amount = Amount + GST Amount
            val gstAmount = bdAmount.multiply(bdRate).divide(hundred, 4, RoundingMode.HALF_UP)
            val grossAmount = bdAmount.add(gstAmount)
            val halfGst = gstAmount.divide(BigDecimal.valueOf(2.0), 4, RoundingMode.HALF_UP)

            return GstResult(
                netAmount = bdAmount.toDouble(),
                gstRate = ratePercent,
                gstAmount = gstAmount.setScale(2, RoundingMode.HALF_UP).toDouble(),
                cgstAmount = halfGst.setScale(2, RoundingMode.HALF_UP).toDouble(),
                sgstAmount = halfGst.setScale(2, RoundingMode.HALF_UP).toDouble(),
                grossAmount = grossAmount.setScale(2, RoundingMode.HALF_UP).toDouble(),
                type = type
            )
        } else {
            // GST Removed: Amount is Gross (Including GST)
            // Net Amount = Amount / (1 + Rate / 100) = Amount * 100 / (100 + Rate)
            // GST Amount = Amount - Net Amount
            val divisor = hundred.add(bdRate)
            val netAmount = if (divisor.toDouble() > 0.0) {
                bdAmount.multiply(hundred).divide(divisor, 4, RoundingMode.HALF_UP)
            } else {
                bdAmount
            }
            val gstAmount = bdAmount.subtract(netAmount)
            val halfGst = gstAmount.divide(BigDecimal.valueOf(2.0), 4, RoundingMode.HALF_UP)

            return GstResult(
                netAmount = netAmount.setScale(2, RoundingMode.HALF_UP).toDouble(),
                gstRate = ratePercent,
                gstAmount = gstAmount.setScale(2, RoundingMode.HALF_UP).toDouble(),
                cgstAmount = halfGst.setScale(2, RoundingMode.HALF_UP).toDouble(),
                sgstAmount = halfGst.setScale(2, RoundingMode.HALF_UP).toDouble(),
                grossAmount = bdAmount.setScale(2, RoundingMode.HALF_UP).toDouble(),
                type = type
            )
        }
    }

    fun formatCurrency(value: Double): String {
        return currencyDf.format(value)
    }
}

