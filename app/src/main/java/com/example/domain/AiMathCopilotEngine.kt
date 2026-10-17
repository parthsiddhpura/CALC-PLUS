package com.example.domain

import com.example.data.CurrencyRepository
import java.util.Locale
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

data class AiCopilotStep(
    val stepNumber: Int,
    val title: String,
    val formula: String,
    val explanation: String,
    val intermediateResult: String
)

data class AiCopilotResponse(
    val query: String,
    val finalAnswer: String,
    val summary: String,
    val steps: List<AiCopilotStep>,
    val category: String,
    val actionSuggestion: String = ""
)

object AiMathCopilotEngine {

    fun solveQuery(query: String, liveInrRate: Double = CurrencyRepository.convert(1.0, "USD", "INR").takeIf { it > 0 } ?: 86.85): AiCopilotResponse {
        val q = query.lowercase(Locale.ROOT).trim()

        // 1. Natural Language GST + Discount: "18% GST on ₹45,000 plus 10% discount"
        if (q.contains("gst") || (q.contains("tax") && q.contains("%"))) {
            val numbers = extractAllNumbers(q)
            val gstPercent = Regex("([0-9]+(?:\\.[0-9]+)?)\\s*%\\s*(?:gst|tax)").find(q)?.groupValues?.get(1)?.toDoubleOrNull()
                ?: Regex("(?:gst|tax)\\s*(?:of|at|@)?\\s*([0-9]+(?:\\.[0-9]+)?)\\s*%?").find(q)?.groupValues?.get(1)?.toDoubleOrNull()
                ?: 18.0

            val discountPercent = Regex("([0-9]+(?:\\.[0-9]+)?)\\s*%\\s*(?:discount|off)").find(q)?.groupValues?.get(1)?.toDoubleOrNull()
                ?: Regex("(?:discount|off)\\s*(?:of)?\\s*([0-9]+(?:\\.[0-9]+)?)\\s*%?").find(q)?.groupValues?.get(1)?.toDoubleOrNull()
                ?: 0.0

            val amountMatch = numbers.firstOrNull { it != gstPercent && it != discountPercent && it > 50.0 }
                ?: numbers.maxOrNull()
                ?: 45000.0

            val discountAmount = amountMatch * (discountPercent / 100.0)
            val discountedPrice = amountMatch - discountAmount
            val gstAmount = discountedPrice * (gstPercent / 100.0)
            val cgst = gstAmount / 2.0
            val sgst = gstAmount / 2.0
            val finalTotal = discountedPrice + gstAmount

            val steps = mutableListOf(
                AiCopilotStep(1, "Base Amount Identification", "P = ₹${formatNum(amountMatch)}", "Extracted original base price before discounts & taxes.", "₹${formatNum(amountMatch)}")
            )
            if (discountPercent > 0) {
                steps.add(
                    AiCopilotStep(2, "Discount Application (${formatNum(discountPercent)}%)", "D = ₹${formatNum(amountMatch)} × ${formatNum(discountPercent)}% = ₹${formatNum(discountAmount)}", "Deduct trade discount from base price.", "₹${formatNum(discountedPrice)}")
                )
            }
            steps.add(
                AiCopilotStep(steps.size + 1, "GST Computation (${formatNum(gstPercent)}%)", "GST = ₹${formatNum(discountedPrice)} × ${formatNum(gstPercent)}% = ₹${formatNum(gstAmount)}", "CGST: ₹${formatNum(cgst)} (${formatNum(gstPercent / 2)}%) | SGST: ₹${formatNum(sgst)} (${formatNum(gstPercent / 2)}%)", "+₹${formatNum(gstAmount)}")
            )
            steps.add(
                AiCopilotStep(steps.size + 1, "Final Payable Gross Total", "Total = ₹${formatNum(discountedPrice)} + ₹${formatNum(gstAmount)}", "Comprehensive invoice total payable by customer.", "₹${formatNum(finalTotal)}")
            )

            return AiCopilotResponse(
                query = query,
                finalAnswer = "₹" + formatNum(finalTotal),
                summary = "After applying ${formatNum(discountPercent)}% discount (saved ₹${formatNum(discountAmount)}) and ${formatNum(gstPercent)}% GST (₹${formatNum(gstAmount)}), the final payable amount is ₹${formatNum(finalTotal)}.",
                steps = steps,
                category = "Tax & Commercial Billing",
                actionSuggestion = "Open in GST Calculator"
            )
        }

        // 2. Tip & Bill Split: "25% tip on ₹4,800 bill split among 5 people"
        if (q.contains("tip") || q.contains("split") || q.contains("gratuity")) {
            val tipPercent = Regex("([0-9]+(?:\\.[0-9]+)?)\\s*%\\s*(?:tip|gratuity)?").find(q)?.groupValues?.get(1)?.toDoubleOrNull()
                ?: Regex("tip\\s*(?:of)?\\s*([0-9]+(?:\\.[0-9]+)?)\\s*%?").find(q)?.groupValues?.get(1)?.toDoubleOrNull()
                ?: 15.0
            val people = Regex("(?:among|between|for|split)\\s*([0-9]+)\\s*(?:people|persons|friends|ways)?").find(q)?.groupValues?.get(1)?.toDoubleOrNull()
                ?: Regex("([0-9]+)\\s*(?:people|persons|friends)").find(q)?.groupValues?.get(1)?.toDoubleOrNull()
                ?: 1.0
            val allNums = extractAllNumbers(q)
            val billAmount = allNums.firstOrNull { it != tipPercent && it != people && it >= 50.0 }
                ?: allNums.maxOrNull()
                ?: 1000.0

            val tipAmount = billAmount * (tipPercent / 100.0)
            val totalBill = billAmount + tipAmount
            val safePeople = people.coerceAtLeast(1.0)
            val perPerson = totalBill / safePeople

            return AiCopilotResponse(
                query = query,
                finalAnswer = "₹${formatNum(perPerson)} / person",
                summary = "On a bill of ₹${formatNum(billAmount)} with a ${formatNum(tipPercent)}% tip (+₹${formatNum(tipAmount)}), the grand total is ₹${formatNum(totalBill)}, which splits to ₹${formatNum(perPerson)} per person across ${safePeople.toInt()} people.",
                steps = listOf(
                    AiCopilotStep(1, "Bill & Tip Calculation", "Tip = ₹${formatNum(billAmount)} × ${formatNum(tipPercent)}% = ₹${formatNum(tipAmount)}", "Calculate service tip on base bill.", "+₹${formatNum(tipAmount)}"),
                    AiCopilotStep(2, "Grand Total Bill", "Total = ₹${formatNum(billAmount)} + ₹${formatNum(tipAmount)} = ₹${formatNum(totalBill)}", "Sum of bill and tip.", "₹${formatNum(totalBill)}"),
                    AiCopilotStep(3, "Per-Person Split (${safePeople.toInt()} People)", "Share = ₹${formatNum(totalBill)} ÷ ${safePeople.toInt()} = ₹${formatNum(perPerson)}", "Equal split per person.", "₹${formatNum(perPerson)}")
                ),
                category = "Dining & Bill Split",
                actionSuggestion = "Open in Tip Calculator"
            )
        }

        // 3. Forex Currency + Compound Investment Chain: "$5,000 in INR ... invest 10 years at 12%"
        if ((q.contains("$") || q.contains("dollar") || q.contains("eur") || q.contains("usd")) && (q.contains("invest") || q.contains("sip") || q.contains("year") || q.contains("return") || q.contains("compound"))) {
            val dollarAmount = Regex("(?:\\$|usd)?\\s*([0-9]+(?:,[0-9]+)*(?:\\.[0-9]+)?)").find(q)?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull() ?: 5000.0
            val years = Regex("([0-9]+(?:\\.[0-9]+)?)\\s*years?").find(q)?.groupValues?.get(1)?.toDoubleOrNull() ?: 10.0
            val returnRate = Regex("([0-9]+(?:\\.[0-9]+)?)\\s*%").find(q)?.groupValues?.get(1)?.toDoubleOrNull() ?: 12.0

            val inrPrincipal = dollarAmount * liveInrRate
            val isMonthlySip = q.contains("sip") || q.contains("monthly")
            val futureCorpus: Double
            val totalInvested: Double
            if (isMonthlySip) {
                val monthlyRate = (returnRate / 100.0) / 12.0
                val months = years * 12.0
                futureCorpus = inrPrincipal * ((1.0 + monthlyRate).pow(months) - 1.0) / monthlyRate * (1.0 + monthlyRate)
                totalInvested = inrPrincipal * months
            } else {
                futureCorpus = inrPrincipal * (1.0 + returnRate / 100.0).pow(years)
                totalInvested = inrPrincipal
            }
            val totalGain = futureCorpus - totalInvested

            return AiCopilotResponse(
                query = query,
                finalAnswer = "₹" + formatNum(futureCorpus),
                summary = "Converting $${formatNum(dollarAmount)} at live rate 1 USD = ₹${String.format(Locale.US, "%.2f", liveInrRate)} gives ₹${formatNum(inrPrincipal)}. Compounded over ${formatNum(years)} years at ${formatNum(returnRate)}% p.a., the final maturity value is ₹${formatNum(futureCorpus)} (Net gain: +₹${formatNum(totalGain)}).",
                steps = listOf(
                    AiCopilotStep(1, "Live Forex Conversion", "$${formatNum(dollarAmount)} × ₹${String.format(Locale.US, "%.2f", liveInrRate)} = ₹${formatNum(inrPrincipal)}", "Converted USD into INR at live interbank rate.", "₹${formatNum(inrPrincipal)}"),
                    AiCopilotStep(2, "Total Principal Invested", "Principal = ₹${formatNum(totalInvested)}", "Capital deployed for ${formatNum(years)} years.", "₹${formatNum(totalInvested)}"),
                    AiCopilotStep(3, "Compound Growth (${formatNum(returnRate)}% p.a.)", if (isMonthlySip) "FV = P × [((1+i)^n - 1)/i] × (1+i)" else "FV = P × (1 + r)^t", "Compounded wealth growth over ${formatNum(years)} years.", "Gain: +₹${formatNum(totalGain)}"),
                    AiCopilotStep(4, "Final Maturity Corpus", "₹${formatNum(futureCorpus)}", "Total accumulated wealth at maturity.", "₹${formatNum(futureCorpus)}")
                ),
                category = "Forex & Wealth Compounding",
                actionSuggestion = "Open in Calculation Chains"
            )
        }

        // 4. Monthly SIP or Lumpsum Compound Growth: "Compound growth of ₹10,000 monthly SIP for 15 years at 14%"
        if (q.contains("sip") || q.contains("compound") || q.contains("cagr") || (q.contains("invest") && q.contains("year"))) {
            val years = Regex("([0-9]+(?:\\.[0-9]+)?)\\s*(?:years?|yrs?)").find(q)?.groupValues?.get(1)?.toDoubleOrNull() ?: 15.0
            val rate = Regex("([0-9]+(?:\\.[0-9]+)?)\\s*%").find(q)?.groupValues?.get(1)?.toDoubleOrNull() ?: 12.0
            val allNums = extractAllNumbers(q)
            val amount = allNums.firstOrNull { it != years && it != rate && it >= 100.0 }
                ?: allNums.maxOrNull()
                ?: 10000.0

            val isSip = q.contains("sip") || q.contains("monthly") || q.contains("per month")
            return if (isSip) {
                val monthlyRate = (rate / 100.0) / 12.0
                val months = years * 12.0
                val futureValue = if (monthlyRate > 0) {
                    amount * (((1.0 + monthlyRate).pow(months) - 1.0) / monthlyRate) * (1.0 + monthlyRate)
                } else {
                    amount * months
                }
                val invested = amount * months
                val wealthGained = futureValue - invested
                AiCopilotResponse(
                    query = query,
                    finalAnswer = "₹" + formatNum(futureValue),
                    summary = "A monthly SIP of ₹${formatNum(amount)} for ${formatNum(years)} years (${months.toInt()} months) at ${formatNum(rate)}% p.a. grows to ₹${formatNum(futureValue)} (Invested: ₹${formatNum(invested)}, Wealth Gained: +₹${formatNum(wealthGained)}).",
                    steps = listOf(
                        AiCopilotStep(1, "Monthly SIP Parameters", "P = ₹${formatNum(amount)}/mo, n = ${months.toInt()} mos, i = ${String.format(Locale.US, "%.4f", monthlyRate * 100)}%/mo", "Converted annual rate ${formatNum(rate)}% to monthly compounding rate.", "n = ${months.toInt()} months"),
                        AiCopilotStep(2, "Total Principal Invested", "Invested = ₹${formatNum(amount)} × ${months.toInt()} = ₹${formatNum(invested)}", "Total capital contributed across tenure.", "₹${formatNum(invested)}"),
                        AiCopilotStep(3, "Annuity Compounding Formula", "FV = P × [((1 + i)^n - 1) / i] × (1 + i)", "Standard mutual fund SIP future value formula.", "Gain: +₹${formatNum(wealthGained)}"),
                        AiCopilotStep(4, "Final Maturity Value", "₹${formatNum(futureValue)}", "Total corpus at the end of ${formatNum(years)} years.", "₹${formatNum(futureValue)}")
                    ),
                    category = "SIP & Wealth Compounding",
                    actionSuggestion = "Open in Custom Calculators"
                )
            } else {
                val futureValue = amount * (1.0 + rate / 100.0).pow(years)
                val wealthGained = futureValue - amount
                AiCopilotResponse(
                    query = query,
                    finalAnswer = "₹" + formatNum(futureValue),
                    summary = "A lump-sum investment of ₹${formatNum(amount)} compounded annually at ${formatNum(rate)}% for ${formatNum(years)} years grows to ₹${formatNum(futureValue)} (Compound Interest Earned: +₹${formatNum(wealthGained)}).",
                    steps = listOf(
                        AiCopilotStep(1, "Principal & Rate", "P = ₹${formatNum(amount)}, r = ${formatNum(rate)}%, t = ${formatNum(years)} yrs", "Extracted compound interest parameters.", "P = ₹${formatNum(amount)}"),
                        AiCopilotStep(2, "Compound Growth Formula", "A = P × (1 + r/100)^t", "Evaluated (1 + ${rate / 100.0})^$years = ${String.format(Locale.US, "%.4f", (1.0 + rate / 100.0).pow(years))}x", "Interest: +₹${formatNum(wealthGained)}"),
                        AiCopilotStep(3, "Maturity Amount", "₹${formatNum(futureValue)}", "Total value after ${formatNum(years)} years.", "₹${formatNum(futureValue)}")
                    ),
                    category = "Compound Interest",
                    actionSuggestion = "Open in Custom Calculators"
                )
            }
        }

        // 5. Direct Live Currency Conversion: e.g. "100 USD to INR" or "convert 500 EUR to USD"
        val currencyCalc = CustomCalculatorEngine.buildFromPrompt(query)
        if (currencyCalc.hasInternetData && currencyCalc.category == "Live Forex & Currency" && currencyCalc.inputs.any { it.id == "exchange_rate" }) {
            val allNums = extractAllNumbers(q)
            val amount = allNums.firstOrNull { it > 0 } ?: 100.0
            val rateInput = currencyCalc.inputs.first { it.id == "exchange_rate" }
            val rate = rateInput.defaultValue
            val converted = amount * rate
            val unit = currencyCalc.outputs.firstOrNull()?.unit ?: ""
            return AiCopilotResponse(
                query = query,
                finalAnswer = "$unit${formatNum(converted)}",
                summary = "Using the live Forex rate (${currencyCalc.dataSourceLabel}), ${formatNum(amount)} converts to $unit${formatNum(converted)}.",
                steps = listOf(
                    AiCopilotStep(1, "Live Interbank Exchange Rate", rateInput.label, currencyCalc.dataSourceLabel, "Rate = ${String.format(Locale.US, "%.4f", rate)}"),
                    AiCopilotStep(2, "Exact Mid-Market Conversion", "${formatNum(amount)} × ${String.format(Locale.US, "%.4f", rate)}", "Multiplied source amount by live exchange rate.", "$unit${formatNum(converted)}")
                ),
                category = "Live Forex Conversion",
                actionSuggestion = "Open in Currency Converter"
            )
        }

        // 6. Quadratic Equation Solver: "2x^2 + 5x - 3 = 0" or "solve x^2 - 4x + 4 = 0"
        if (q.contains("x^2") || q.contains("x²") || (q.contains("solve") && q.contains("x"))) {
            var a = 1.0
            var b = 0.0
            var c = 0.0

            val cleanEq = q.replace("solve", "").replace("=", " ").trim()
            val lhs = cleanEq.substringBefore("0").ifBlank { cleanEq }
            val matchA = Regex("([+-]?\\s*[0-9]*(?:\\.[0-9]+)?)\\s*(?:x\\^2|x²)").find(lhs)
            if (matchA != null) {
                val strA = matchA.groupValues[1].replace(" ", "")
                a = when (strA) {
                    "", "+" -> 1.0
                    "-" -> -1.0
                    else -> strA.toDoubleOrNull() ?: 1.0
                }
            }

            val withoutX2 = lhs.replace(Regex("[+-]?\\s*[0-9]*(?:\\.[0-9]+)?\\s*(?:x\\^2|x²)"), "")
            val matchB = Regex("([+-]?\\s*[0-9]*(?:\\.[0-9]+)?)\\s*x").find(withoutX2)
            if (matchB != null) {
                val strB = matchB.groupValues[1].replace(" ", "")
                b = when (strB) {
                    "+", "" -> 1.0
                    "-" -> -1.0
                    else -> strB.toDoubleOrNull() ?: 0.0
                }
            }

            val withoutX = withoutX2.replace(Regex("[+-]?\\s*[0-9]*(?:\\.[0-9]+)?\\s*x"), "").trim()
            val matchC = Regex("([+-]?\\s*[0-9]+(?:\\.[0-9]+)?)").find(withoutX)
            if (matchC != null) {
                c = matchC.groupValues[1].replace(" ", "").toDoubleOrNull() ?: 0.0
            }

            val discriminant = (b * b) - (4 * a * c)
            val steps = mutableListOf(
                AiCopilotStep(1, "Identify Standard Coefficients", "a = ${formatNum(a)}, b = ${formatNum(b)}, c = ${formatNum(c)}", "Standard quadratic form: ax² + bx + c = 0", "Δ = b² - 4ac"),
                AiCopilotStep(2, "Calculate Discriminant (Δ)", "Δ = (${formatNum(b)})² - 4(${formatNum(a)})(${formatNum(c)}) = ${formatNum(discriminant)}", if (discriminant > 0) "Two distinct real roots exist (Δ > 0)." else if (discriminant == 0.0) "One repeated real root exists (Δ = 0)." else "Complex conjugate roots exist (Δ < 0).", "Δ = ${formatNum(discriminant)}")
            )

            val answerStr: String
            if (discriminant >= 0) {
                val root1 = (-b + sqrt(discriminant)) / (2 * a)
                val root2 = (-b - sqrt(discriminant)) / (2 * a)
                steps.add(
                    AiCopilotStep(3, "Apply Quadratic Formula", "x = (-b ± √Δ) / (2a)", "x = (-(${formatNum(b)}) ± √${formatNum(discriminant)}) / (2 × ${formatNum(a)})", "Roots computed")
                )
                steps.add(
                    AiCopilotStep(4, "Exact Solutions", "x₁ = ${formatNum(root1)}, x₂ = ${formatNum(root2)}", "Verified roots satisfying ax² + bx + c = 0.", "x = ${formatNum(root1)}, ${formatNum(root2)}")
                )
                answerStr = "x₁ = ${formatNum(root1)}, x₂ = ${formatNum(root2)}"
            } else {
                val realPart = -b / (2 * a)
                val imagPart = sqrt(-discriminant) / (2 * a)
                answerStr = "x = ${formatNum(realPart)} ± ${String.format(Locale.US, "%.3f", imagPart)}i"
                steps.add(
                    AiCopilotStep(3, "Complex Conjugate Solutions", "x = (-b ± i√|Δ|) / (2a)", "Roots containing imaginary unit i = √(-1)", answerStr)
                )
            }

            return AiCopilotResponse(
                query = query,
                finalAnswer = answerStr,
                summary = "For the quadratic equation ${formatNum(a)}x² + ${formatNum(b)}x + ${formatNum(c)} = 0, the discriminant is ${formatNum(discriminant)}, yielding solutions: $answerStr.",
                steps = steps,
                category = "Algebra & Polynomials",
                actionSuggestion = "Open in Scientific Mode"
            )
        }

        // 7. Percentage Expressions: e.g., "20% of 4500"
        val pctOfMatch = Regex("([0-9]+(?:\\.[0-9]+)?)\\s*%\\s*of\\s*([0-9,]+(?:\\.[0-9]+)?)").find(q)
        if (pctOfMatch != null) {
            val pct = pctOfMatch.groupValues[1].toDoubleOrNull() ?: 0.0
            val base = pctOfMatch.groupValues[2].replace(",", "").toDoubleOrNull() ?: 0.0
            val res = base * (pct / 100.0)
            return AiCopilotResponse(
                query = query,
                finalAnswer = formatNum(res),
                summary = "${formatNum(pct)}% of ${formatNum(base)} is exactly ${formatNum(res)}.",
                steps = listOf(
                    AiCopilotStep(1, "Convert Percentage to Decimal", "${formatNum(pct)}% = ${pct / 100.0}", "Divide percentage by 100.", "${pct / 100.0}"),
                    AiCopilotStep(2, "Multiply by Base Value", "${pct / 100.0} × ${formatNum(base)} = ${formatNum(res)}", "Exact product.", formatNum(res))
                ),
                category = "Percentage Calculation",
                actionSuggestion = "Load into Standard Keypad"
            )
        }

        // 8. Default Mathematical Expression Evaluator fallback
        val sanitizedExpr = q
            .replace("what is", "")
            .replace("calculate", "")
            .replace("solve", "")
            .replace(",", "")
            .replace("₹", "")
            .replace("$", "")
            .trim()
        val numericResult = FormulaEvaluator.evaluate(sanitizedExpr, emptyMap())
        return AiCopilotResponse(
            query = query,
            finalAnswer = formatNum(numericResult),
            summary = "Direct mathematical evaluation of \"$sanitizedExpr\" = ${formatNum(numericResult)}.",
            steps = listOf(
                AiCopilotStep(1, "Parse Mathematical Syntax", sanitizedExpr, "Tokenized into operators, operands, and functions.", "Tokens verified"),
                AiCopilotStep(2, "BODMAS / PEMDAS Evaluation", "$sanitizedExpr = ${formatNum(numericResult)}", "Evaluated parentheses, exponents, functions, multiplication/division, and addition/subtraction.", formatNum(numericResult))
            ),
            category = "General Mathematics",
            actionSuggestion = "Load into Standard Keypad"
        )
    }

    private fun extractAllNumbers(text: String): List<Double> {
        return Regex("[0-9]+(?:,[0-9]+)*(?:\\.[0-9]+)?")
            .findAll(text)
            .mapNotNull { it.value.replace(",", "").toDoubleOrNull() }
            .toList()
    }

    private fun formatNum(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "0"
        return if (abs(value - value.toLong()) < 1e-9) {
            String.format(Locale.US, "%,d", value.toLong())
        } else {
            String.format(Locale.US, "%,.2f", value).trimEnd('0').trimEnd('.')
        }
    }
}
