package com.example.model

import java.util.UUID

enum class WorksheetLineType {
    CALCULATION,      // + 12.50 wax & wick
    PERCENTAGE,       // - 15.00% | -78.75 pickup discount
    SUB_TOTAL,        // ------------------ 21.00 unit price
    GRAND_TOTAL,      // ================== 541.50 Total
    VARIABLE_SET,     // Quantity = 25 or Price = 525.00
    COMMENT_HEADER    // # Design candle cost calculation
}

data class WorksheetLine(
    val id: String = UUID.randomUUID().toString(),
    val lineType: WorksheetLineType = WorksheetLineType.CALCULATION,
    val operator: String = "+", // "+", "-", "*", "/", "%", "="
    val rawValue: String = "0", // "12.50", "25", "15"
    val evaluatedNumber: Double = 0.0,
    val percentageDelta: Double? = null, // for % lines: the calculated delta
    val runningTotal: Double = 0.0,
    val variableName: String? = null, // e.g. "Quantity", "Price"
    val note: String = "", // e.g. "wax & wick", "chai", "remarks"
    val hasDividerBefore: Boolean = false // whether a horizontal rule is drawn above this line
)

data class WorksheetVariable(
    val name: String,
    val value: Double,
    val note: String = ""
)

data class WorksheetSettings(
    val decimals: Int = 2,
    val thousandsSeparator: Boolean = true,
    val indianDigitGrouping: Boolean = true, // Example: 50,00,000.00
    val showLines: Boolean = false, // Horizontal lines between rows (ruled ledger lines)
    val lineColorName: String = "Light blue",
    val fontSize: Int = 16,
    val modernKeyStyle: Boolean = true,
    val keyboardSizePortrait: Float = 0.5f,
    val hapticsEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val showQuickInsert: Boolean = true,
    val spellCheck: Boolean = false,
    val sortOrder: String = "Date descending",
    val customKeyRate: String = "18",
    val customKeyType: String = "GST" // "GST" or "DISC"
)

data class WorksheetDocument(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Worksheet",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lines: List<WorksheetLine> = emptyList(),
    val grandTotal: Double = 0.0,
    val headerNote: String = "" // e.g. "Total - 70,000/-"
)

data class WorksheetTemplate(
    val title: String,
    val description: String,
    val iconName: String,
    val lines: List<WorksheetLine>
)
