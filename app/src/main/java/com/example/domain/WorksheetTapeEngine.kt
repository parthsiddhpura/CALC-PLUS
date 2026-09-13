package com.example.domain

import com.example.model.WorksheetDocument
import com.example.model.WorksheetLine
import com.example.model.WorksheetLineType
import com.example.model.WorksheetTemplate
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import java.util.UUID

object WorksheetTapeEngine {

    private val standardFormatter = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.US))

    fun formatNumber(value: Double, indianGrouping: Boolean = true, decimals: Int = 2): String {
        if (value.isNaN() || value.isInfinite()) return "0.00"
        return if (indianGrouping) {
            formatIndianNumber(value, decimals)
        } else {
            val pattern = if (decimals > 0) "#,##0." + "0".repeat(decimals) else "#,##0"
            val df = DecimalFormat(pattern, DecimalFormatSymbols(Locale.US))
            df.format(value)
        }
    }

    fun formatIndianNumber(value: Double, decimals: Int = 2): String {
        val isNegative = value < 0
        val absVal = kotlin.math.abs(value)
        val longPart = absVal.toLong()
        val fracPart = absVal - longPart
        val strLong = longPart.toString()

        val formattedInt = if (strLong.length <= 3) {
            strLong
        } else {
            val last3 = strLong.takeLast(3)
            val rest = strLong.dropLast(3)
            val chunks = mutableListOf<String>()
            var idx = rest.length
            while (idx > 0) {
                val start = kotlin.math.max(0, idx - 2)
                chunks.add(0, rest.substring(start, idx))
                idx -= 2
            }
            chunks.joinToString(",") + "," + last3
        }

        if (decimals <= 0) {
            return (if (isNegative) "-" else "") + formattedInt
        }
        val fracFormatted = String.format(Locale.US, "%.${decimals}f", fracPart)
        val fracStr = if (fracFormatted.contains(".")) fracFormatted.substringAfter(".") else "0".repeat(decimals)
        return (if (isNegative) "-" else "") + "$formattedInt.$fracStr"
    }

    /**
     * Reactively recalculates all lines in the worksheet tape from top to bottom.
     * Maintains a variable map, running accumulator, and percentage delta computations.
     */
    fun recalculate(lines: List<WorksheetLine>): List<WorksheetLine> {
        val variables = mutableMapOf<String, Double>()
        var accumulator = 0.0
        var isFirstInBlock = true

        return lines.mapIndexed { index, line ->
            when (line.lineType) {
                WorksheetLineType.COMMENT_HEADER -> {
                    // Headers don't affect accumulator
                    line.copy(runningTotal = accumulator)
                }

                WorksheetLineType.VARIABLE_SET -> {
                    val numVal = parseNumberOrVariable(line.rawValue, variables)
                    line.variableName?.let { varName ->
                        if (varName.isNotBlank()) {
                            variables[varName.trim().lowercase()] = numVal
                        }
                    }
                    line.copy(
                        evaluatedNumber = numVal,
                        runningTotal = accumulator
                    )
                }

                WorksheetLineType.SUB_TOTAL -> {
                    // Subtotal captures the accumulator and optionally assigns it to a variable
                    val subtotal = accumulator
                    line.variableName?.let { varName ->
                        if (varName.isNotBlank()) {
                            variables[varName.trim().lowercase()] = subtotal
                        }
                    }
                    accumulator = 0.0
                    isFirstInBlock = true
                    line.copy(
                        evaluatedNumber = subtotal,
                        runningTotal = subtotal
                    )
                }

                WorksheetLineType.GRAND_TOTAL -> {
                    line.copy(
                        evaluatedNumber = accumulator,
                        runningTotal = accumulator
                    )
                }

                WorksheetLineType.PERCENTAGE -> {
                    val percentRate = parseNumberOrVariable(line.rawValue, variables)
                    val delta = accumulator * (percentRate / 100.0)
                    accumulator = when (line.operator) {
                        "-" -> accumulator - delta
                        "*" -> accumulator * (percentRate / 100.0)
                        else -> accumulator + delta
                    }
                    val finalDelta = if (line.operator == "-") -delta else delta
                    line.copy(
                        evaluatedNumber = percentRate,
                        percentageDelta = finalDelta,
                        runningTotal = accumulator
                    )
                }

                WorksheetLineType.CALCULATION -> {
                    val numVal = parseNumberOrVariable(line.rawValue, variables)
                    if (isFirstInBlock && (line.operator.isBlank() || line.operator == "+")) {
                        accumulator = numVal
                        isFirstInBlock = false
                    } else {
                        accumulator = when (line.operator) {
                            "-" -> accumulator - numVal
                            "*" -> accumulator * numVal
                            "/" -> if (numVal != 0.0) accumulator / numVal else accumulator
                            else -> accumulator + numVal
                        }
                    }

                    // If line defines an output variable (= Price)
                    line.variableName?.let { varName ->
                        if (varName.isNotBlank()) {
                            variables[varName.trim().lowercase()] = accumulator
                        }
                    }

                    line.copy(
                        evaluatedNumber = numVal,
                        runningTotal = accumulator
                    )
                }
            }
        }
    }

    private fun parseNumberOrVariable(input: String, variables: Map<String, Double>): Double {
        val trimmed = input.trim()
        val varMatch = variables[trimmed.lowercase()]
        if (varMatch != null) return varMatch

        val cleaned = trimmed.replace(",", "").replace("%", "")
        return cleaned.toDoubleOrNull() ?: 0.0
    }

    /**
     * Generate exportable plain text formatted receipt tape.
     */
    fun exportToPlainText(doc: WorksheetDocument): String {
        val sb = StringBuilder()
        sb.appendLine("==========================================")
        sb.appendLine("          ${doc.title.uppercase()}        ")
        sb.appendLine("==========================================")

        doc.lines.forEach { line ->
            when (line.lineType) {
                WorksheetLineType.COMMENT_HEADER -> {
                    sb.appendLine()
                    sb.appendLine("# ${line.note.ifBlank { line.rawValue }}")
                }
                WorksheetLineType.VARIABLE_SET -> {
                    sb.appendLine("${line.variableName ?: "Var"} = ${formatNumber(line.evaluatedNumber)}")
                }
                WorksheetLineType.SUB_TOTAL -> {
                    sb.appendLine("------------------------------------------")
                    val varNote = if (!line.variableName.isNullOrBlank()) " = ${line.variableName}" else ""
                    val comment = if (line.note.isNotBlank()) " ${line.note}" else ""
                    sb.appendLine("+  ${formatNumber(line.evaluatedNumber)}$varNote$comment")
                }
                WorksheetLineType.GRAND_TOTAL -> {
                    sb.appendLine("==========================================")
                    sb.appendLine("TOTAL: ${formatNumber(line.evaluatedNumber)}")
                }
                WorksheetLineType.PERCENTAGE -> {
                    val deltaStr = line.percentageDelta?.let { " | ${if (it >= 0) "+" else ""}${formatNumber(it)}" } ?: ""
                    val comment = if (line.note.isNotBlank()) " ${line.note}" else ""
                    sb.appendLine("${line.operator}  ${formatNumber(line.evaluatedNumber)}%$deltaStr$comment")
                }
                WorksheetLineType.CALCULATION -> {
                    val comment = if (line.note.isNotBlank()) " ${line.note}" else ""
                    val varRef = if (line.rawValue.matches(Regex("[a-zA-Z_]+"))) " ${line.rawValue}" else ""
                    val targetVar = if (!line.variableName.isNullOrBlank()) " = ${line.variableName}" else ""
                    val numDisplay = if (varRef.isNotBlank()) varRef else formatNumber(line.evaluatedNumber)
                    sb.appendLine("${line.operator}  $numDisplay$targetVar$comment")
                }
            }
        }
        sb.appendLine("==========================================")
        sb.appendLine("Grand Total: ${formatNumber(doc.grandTotal)}")
        sb.appendLine("Generated with ChromaTape Worksheet Calculator")
        return sb.toString()
    }

    /**
     * Generate exportable Markdown receipt format.
     */
    fun exportToMarkdown(doc: WorksheetDocument): String {
        val sb = StringBuilder()
        sb.appendLine("## 📋 ${doc.title}")
        sb.appendLine()
        sb.appendLine("| Op | Amount | Notes / Variables | Subtotal |")
        sb.appendLine("|:---:|:---:|:---|:---:|")

        doc.lines.forEach { line ->
            when (line.lineType) {
                WorksheetLineType.COMMENT_HEADER -> {
                    sb.appendLine("| 📝 | **Section** | **${line.note.ifBlank { line.rawValue }}** | - |")
                }
                WorksheetLineType.VARIABLE_SET -> {
                    sb.appendLine("| 🏷️ | `${line.variableName} = ${formatNumber(line.evaluatedNumber)}` | Variable Definition | - |")
                }
                WorksheetLineType.SUB_TOTAL -> {
                    val label = if (!line.variableName.isNullOrBlank()) "Subtotal = ${line.variableName}" else "Subtotal"
                    sb.appendLine("| ➖ | **${formatNumber(line.evaluatedNumber)}** | **$label ${line.note}** | **${formatNumber(line.runningTotal)}** |")
                }
                WorksheetLineType.GRAND_TOTAL -> {
                    sb.appendLine("| 🏁 | **${formatNumber(line.evaluatedNumber)}** | **Grand Total** | **${formatNumber(line.runningTotal)}** |")
                }
                WorksheetLineType.PERCENTAGE -> {
                    val deltaStr = line.percentageDelta?.let { " (${if (it >= 0) "+" else ""}${formatNumber(it)})" } ?: ""
                    sb.appendLine("| `${line.operator}` | `${formatNumber(line.evaluatedNumber)}%` | ${line.note}$deltaStr | `${formatNumber(line.runningTotal)}` |")
                }
                WorksheetLineType.CALCULATION -> {
                    val varRef = if (line.rawValue.matches(Regex("[a-zA-Z_]+"))) line.rawValue else formatNumber(line.evaluatedNumber)
                    val targetVar = if (!line.variableName.isNullOrBlank()) " = ${line.variableName}" else ""
                    sb.appendLine("| `${line.operator}` | `$varRef` | ${line.note}$targetVar | `${formatNumber(line.runningTotal)}` |")
                }
            }
        }
        sb.appendLine()
        sb.appendLine("> **Grand Total**: `${formatNumber(doc.grandTotal)}`")
        return sb.toString()
    }

    /**
     * Generate HTML table format for rich text / email sharing.
     */
    fun exportToHtml(doc: WorksheetDocument, indianGrouping: Boolean = true): String {
        val sb = StringBuilder()
        sb.append("<!DOCTYPE html><html><head><meta charset='utf-8'><title>${doc.title}</title>")
        sb.append("<style>")
        sb.append("body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, monospace; background: #fff; color: #1e293b; padding: 20px; }")
        sb.append(".receipt { max-width: 440px; margin: 0 auto; border: 1px solid #cbd5e1; border-radius: 8px; padding: 18px; box-shadow: 0 4px 6px rgba(0,0,0,0.05); }")
        sb.append("h2 { margin-top: 0; font-size: 18px; border-bottom: 2px solid #0284c7; padding-bottom: 8px; }")
        sb.append("table { width: 100%; border-collapse: collapse; font-family: monospace; font-size: 14px; }")
        sb.append("td { padding: 4px 6px; }")
        sb.append(".op { width: 24px; font-weight: bold; color: #64748b; }")
        sb.append(".num { text-align: right; font-weight: 600; }")
        sb.append(".neg { color: #dc2626; }")
        sb.append(".note { padding-left: 12px; color: #64748b; font-style: italic; }")
        sb.append(".subtotal { border-top: 1px solid #94a3b8; font-weight: bold; color: #0284c7; }")
        sb.append(".total { border-top: 2px solid #0f172a; border-bottom: 2px solid #0f172a; font-size: 16px; font-weight: bold; }")
        sb.append("</style></head><body><div class='receipt'>")
        sb.append("<h2>${doc.title}</h2>")
        if (doc.headerNote.isNotBlank()) {
            sb.append("<p style='color: #64748b; margin-top: -4px;'>${doc.headerNote}</p>")
        }
        sb.append("<table>")
        doc.lines.forEach { line ->
            val numFormatted = formatNumber(line.evaluatedNumber, indianGrouping)
            val isNeg = line.operator == "-" || line.evaluatedNumber < 0
            val numClass = if (isNeg) "num neg" else "num"
            val rowClass = if (line.hasDividerBefore) "subtotal" else ""
            sb.append("<tr class='$rowClass'>")
            sb.append("<td class='op'>${line.operator}</td>")
            sb.append("<td class='$numClass'>$numFormatted</td>")
            sb.append("<td class='note'>${line.note}</td>")
            sb.append("</tr>")
        }
        sb.append("<tr class='total'><td class='op'>=</td><td class='num'>${formatNumber(doc.grandTotal, indianGrouping)}</td><td class='note'>GRAND TOTAL</td></tr>")
        sb.append("</table></div></body></html>")
        return sb.toString()
    }

    /**
     * Triggers the Android Native Print Service (or Save to PDF)
     */
    fun printDocument(context: android.content.Context, doc: WorksheetDocument, indianGrouping: Boolean = true) {
        val printManager = context.getSystemService(android.content.Context.PRINT_SERVICE) as? android.print.PrintManager ?: return
        val jobName = "${doc.title}_Print"
        printManager.print(jobName, TapePrintDocumentAdapter(context, doc, indianGrouping), null)
    }

    /**
     * Backup documents and configuration to JSON string
     */
    fun createBackupJson(
        documents: List<WorksheetDocument>,
        includeWorksheet: Boolean = true,
        includeAllCalculations: Boolean = true,
        includeKeyboards: Boolean = true,
        includeSettings: Boolean = true
    ): String {
        val root = org.json.JSONObject()
        root.put("version", 1)
        root.put("timestamp", System.currentTimeMillis())
        root.put("includeWorksheet", includeWorksheet)
        root.put("includeAllCalculations", includeAllCalculations)
        root.put("includeKeyboards", includeKeyboards)
        root.put("includeSettings", includeSettings)

        val docsArray = org.json.JSONArray()
        documents.forEach { doc ->
            val docObj = org.json.JSONObject()
            docObj.put("id", doc.id)
            docObj.put("title", doc.title)
            docObj.put("createdAt", doc.createdAt)
            docObj.put("updatedAt", doc.updatedAt)
            docObj.put("grandTotal", doc.grandTotal)
            docObj.put("headerNote", doc.headerNote)

            val linesArray = org.json.JSONArray()
            doc.lines.forEach { line ->
                val lineObj = org.json.JSONObject()
                lineObj.put("id", line.id)
                lineObj.put("lineType", line.lineType.name)
                lineObj.put("operator", line.operator)
                lineObj.put("rawValue", line.rawValue)
                lineObj.put("evaluatedNumber", line.evaluatedNumber)
                lineObj.put("runningTotal", line.runningTotal)
                lineObj.put("note", line.note)
                lineObj.put("variableName", line.variableName)
                lineObj.put("hasDividerBefore", line.hasDividerBefore)
                linesArray.put(lineObj)
            }
            docObj.put("lines", linesArray)
            docsArray.put(docObj)
        }
        root.put("documents", docsArray)
        return root.toString(2)
    }

    /**
     * Restore documents from backup JSON string
     */
    fun restoreBackupJson(jsonString: String): List<WorksheetDocument> {
        val list = mutableListOf<WorksheetDocument>()
        try {
            val root = org.json.JSONObject(jsonString)
            val docsArray = root.optJSONArray("documents") ?: return list
            for (i in 0 until docsArray.length()) {
                val docObj = docsArray.getJSONObject(i)
                val linesList = mutableListOf<WorksheetLine>()
                val linesArray = docObj.optJSONArray("lines") ?: org.json.JSONArray()
                for (j in 0 until linesArray.length()) {
                    val lObj = linesArray.getJSONObject(j)
                    linesList.add(
                        WorksheetLine(
                            id = lObj.optString("id", UUID.randomUUID().toString()),
                            lineType = try {
                                WorksheetLineType.valueOf(lObj.optString("lineType", WorksheetLineType.CALCULATION.name))
                            } catch (e: Exception) {
                                WorksheetLineType.CALCULATION
                            },
                            operator = lObj.optString("operator", "+"),
                            rawValue = lObj.optString("rawValue", "0"),
                            evaluatedNumber = lObj.optDouble("evaluatedNumber", 0.0),
                            runningTotal = lObj.optDouble("runningTotal", 0.0),
                            note = lObj.optString("note", ""),
                            variableName = if (lObj.has("variableName") && !lObj.isNull("variableName")) lObj.getString("variableName") else null,
                            hasDividerBefore = lObj.optBoolean("hasDividerBefore", false)
                        )
                    )
                }
                list.add(
                    WorksheetDocument(
                        id = docObj.optString("id", UUID.randomUUID().toString()),
                        title = docObj.optString("title", "Calculation"),
                        createdAt = docObj.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = docObj.optLong("updatedAt", System.currentTimeMillis()),
                        lines = linesList,
                        grandTotal = docObj.optDouble("grandTotal", 0.0),
                        headerNote = docObj.optString("headerNote", "")
                    )
                )
            }
        } catch (e: Exception) {
            // failed to parse
        }
        return list
    }

    /**
     * Pre-packaged professional templates matching user screenshots and everyday business needs.
     */
    fun getDefaultTemplates(): List<WorksheetTemplate> {
        return listOf(
            WorksheetTemplate(
                title = "Design Candle Cost",
                description = "Candle making cost per unit, volume multiplier & pickup discount",
                iconName = "LocalOffer",
                lines = listOf(
                    WorksheetLine(lineType = WorksheetLineType.COMMENT_HEADER, note = "Working with Variables"),
                    WorksheetLine(lineType = WorksheetLineType.COMMENT_HEADER, note = "Design candle cost calculation"),
                    WorksheetLine(lineType = WorksheetLineType.VARIABLE_SET, variableName = "Quantity", rawValue = "25", evaluatedNumber = 25.0),
                    WorksheetLine(lineType = WorksheetLineType.CALCULATION, operator = "+", rawValue = "12.50", evaluatedNumber = 12.50, note = "wax & wick"),
                    WorksheetLine(lineType = WorksheetLineType.CALCULATION, operator = "+", rawValue = "6.00", evaluatedNumber = 6.00, note = "labor"),
                    WorksheetLine(lineType = WorksheetLineType.CALCULATION, operator = "+", rawValue = "2.50", evaluatedNumber = 2.50, note = "jar & label"),
                    WorksheetLine(lineType = WorksheetLineType.SUB_TOTAL, operator = "+", rawValue = "21.00", evaluatedNumber = 21.00, note = "unit price"),
                    WorksheetLine(lineType = WorksheetLineType.CALCULATION, operator = "*", rawValue = "Quantity", note = ""),
                    WorksheetLine(lineType = WorksheetLineType.SUB_TOTAL, operator = "+", rawValue = "525.00", variableName = "Price", note = "Total Price"),
                    WorksheetLine(lineType = WorksheetLineType.COMMENT_HEADER, note = "Pickup option"),
                    WorksheetLine(lineType = WorksheetLineType.CALCULATION, operator = "+", rawValue = "Price", note = "Base Price"),
                    WorksheetLine(lineType = WorksheetLineType.PERCENTAGE, operator = "-", rawValue = "15.00", evaluatedNumber = 15.0, note = "pickup discount")
                )
            ),
            WorksheetTemplate(
                title = "Shopping & Groceries",
                description = "Weekly household shopping with itemized notes and voucher promo",
                iconName = "ShoppingCart",
                lines = listOf(
                    WorksheetLine(lineType = WorksheetLineType.COMMENT_HEADER, note = "CalcTape - Weekly Shopping"),
                    WorksheetLine(lineType = WorksheetLineType.CALCULATION, operator = "+", rawValue = "45.80", evaluatedNumber = 45.80, note = "Groceries & drinks"),
                    WorksheetLine(lineType = WorksheetLineType.CALCULATION, operator = "+", rawValue = "12.40", evaluatedNumber = 12.40, note = "Household items"),
                    WorksheetLine(lineType = WorksheetLineType.CALCULATION, operator = "+", rawValue = "7.50", evaluatedNumber = 7.50, note = "Magazines"),
                    WorksheetLine(lineType = WorksheetLineType.SUB_TOTAL, operator = "+", rawValue = "65.70", note = "Subtotal"),
                    WorksheetLine(lineType = WorksheetLineType.CALCULATION, operator = "-", rawValue = "5.00", evaluatedNumber = 5.00, note = "Voucher (promo)"),
                    WorksheetLine(lineType = WorksheetLineType.GRAND_TOTAL, operator = "+", rawValue = "60.70", note = "Total Due")
                )
            ),
            WorksheetTemplate(
                title = "Invoice & Tax Billing",
                description = "Hardware order with quantity multiplier, 19% VAT/GST and cash discount",
                iconName = "Receipt",
                lines = listOf(
                    WorksheetLine(lineType = WorksheetLineType.COMMENT_HEADER, note = "Order computers:"),
                    WorksheetLine(lineType = WorksheetLineType.CALCULATION, operator = "+", rawValue = "999.00", evaluatedNumber = 999.00, note = "$ single price"),
                    WorksheetLine(lineType = WorksheetLineType.CALCULATION, operator = "*", rawValue = "6.00", evaluatedNumber = 6.00, note = "number of items"),
                    WorksheetLine(lineType = WorksheetLineType.SUB_TOTAL, operator = "+", rawValue = "5994.00", note = "Gross Amount"),
                    WorksheetLine(lineType = WorksheetLineType.PERCENTAGE, operator = "+", rawValue = "19.00", evaluatedNumber = 19.0, note = "VAT / GST"),
                    WorksheetLine(lineType = WorksheetLineType.SUB_TOTAL, operator = "+", rawValue = "7132.86", note = "total"),
                    WorksheetLine(lineType = WorksheetLineType.COMMENT_HEADER, note = "Cash Discount:"),
                    WorksheetLine(lineType = WorksheetLineType.PERCENTAGE, operator = "-", rawValue = "3.00", evaluatedNumber = 3.0, note = "early pay discount"),
                    WorksheetLine(lineType = WorksheetLineType.GRAND_TOTAL, operator = "+", rawValue = "6918.87", note = "Final Payable")
                )
            ),
            WorksheetTemplate(
                title = "Travel Budget & Trip",
                description = "Flight, hotel, car rental, food and currency exchange buffer",
                iconName = "Flight",
                lines = listOf(
                    WorksheetLine(lineType = WorksheetLineType.COMMENT_HEADER, note = "Summer Trip Budget"),
                    WorksheetLine(lineType = WorksheetLineType.CALCULATION, operator = "+", rawValue = "340.00", evaluatedNumber = 340.00, note = "Roundtrip Flights"),
                    WorksheetLine(lineType = WorksheetLineType.CALCULATION, operator = "+", rawValue = "450.00", evaluatedNumber = 450.00, note = "Hotel (4 Nights)"),
                    WorksheetLine(lineType = WorksheetLineType.CALCULATION, operator = "+", rawValue = "120.00", evaluatedNumber = 120.00, note = "Car Rental & Fuel"),
                    WorksheetLine(lineType = WorksheetLineType.CALCULATION, operator = "+", rawValue = "200.00", evaluatedNumber = 200.00, note = "Dining & Food"),
                    WorksheetLine(lineType = WorksheetLineType.SUB_TOTAL, operator = "+", rawValue = "1110.00", note = "Trip Subtotal"),
                    WorksheetLine(lineType = WorksheetLineType.PERCENTAGE, operator = "+", rawValue = "10.00", evaluatedNumber = 10.0, note = "Emergency buffer"),
                    WorksheetLine(lineType = WorksheetLineType.GRAND_TOTAL, operator = "+", rawValue = "1221.00", note = "Target Savings")
                )
            ),
            WorksheetTemplate(
                title = "Project Hours & Timesheet",
                description = "Freelance consulting hours multiplied by hourly rate with deduction",
                iconName = "Schedule",
                lines = listOf(
                    WorksheetLine(lineType = WorksheetLineType.COMMENT_HEADER, note = "Freelance Monthly Timesheet"),
                    WorksheetLine(lineType = WorksheetLineType.VARIABLE_SET, variableName = "HourlyRate", rawValue = "45.00", evaluatedNumber = 45.0),
                    WorksheetLine(lineType = WorksheetLineType.CALCULATION, operator = "+", rawValue = "35.00", evaluatedNumber = 35.00, note = "Dev Hours Week 1-2"),
                    WorksheetLine(lineType = WorksheetLineType.CALCULATION, operator = "+", rawValue = "28.00", evaluatedNumber = 28.00, note = "Dev Hours Week 3-4"),
                    WorksheetLine(lineType = WorksheetLineType.SUB_TOTAL, operator = "+", rawValue = "63.00", note = "Total Hours"),
                    WorksheetLine(lineType = WorksheetLineType.CALCULATION, operator = "*", rawValue = "HourlyRate", note = "rate per hr"),
                    WorksheetLine(lineType = WorksheetLineType.SUB_TOTAL, operator = "+", rawValue = "2835.00", note = "Gross Billing"),
                    WorksheetLine(lineType = WorksheetLineType.PERCENTAGE, operator = "-", rawValue = "10.00", evaluatedNumber = 10.0, note = "TDS / Withholding Tax"),
                    WorksheetLine(lineType = WorksheetLineType.GRAND_TOTAL, operator = "+", rawValue = "2551.50", note = "Net Payout")
                )
            )
        )
    }
}
