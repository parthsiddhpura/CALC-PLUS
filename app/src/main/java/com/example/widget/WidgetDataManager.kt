package com.example.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.example.model.CalculationHistory
import com.example.model.ThemeId
import com.example.model.ThemePalette
import com.example.ui.theme.CalculatorThemes
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.pow
import kotlin.math.roundToInt

data class MyValueVariable(
    val id: String,
    val label: String,
    val displayValue: String,
    val numericValue: Double
)

data class FormulaPin(
    val title: String,
    val inputLabel: String,
    val inputValue: String,
    val outputLabel: String,
    val outputValue: String,
    val formulaExpression: String
)

data class ShortcutItem(
    val title: String,
    val modeKey: String,
    val symbol: String
)

object WidgetDataManager {
    private const val PREFS_NAME = "calc_plus_widget_prefs"

    // Keys
    private const val KEY_THEME = "widget_theme"
    private const val KEY_LAST_EXPR = "last_expression"
    private const val KEY_LAST_RESULT = "last_result"
    private const val KEY_HISTORY_JSON = "history_json"

    // Quick Calc
    private const val KEY_QUICK_CALC_BUFFER = "qc_buffer"
    private const val KEY_QUICK_CALC_RESULT = "qc_result"

    // Conversion
    private const val KEY_CONV_IS_UNIT = "conv_is_unit"
    private const val KEY_CONV_INPUT = "conv_input"
    private const val KEY_CONV_FROM = "conv_from"
    private const val KEY_CONV_TO = "conv_to"
    private const val KEY_CONV_RESULT = "conv_result"

    // GST
    private const val KEY_GST_AMOUNT = "gst_amount"
    private const val KEY_GST_RATE = "gst_rate"
    private const val KEY_GST_IS_ADD = "gst_is_add"

    // EMI
    private const val KEY_EMI_PRINCIPAL = "emi_principal"
    private const val KEY_EMI_RATE = "emi_rate"
    private const val KEY_EMI_TENURE = "emi_tenure"

    // Percentage
    private const val KEY_PERCENT_RATE = "pct_rate"
    private const val KEY_PERCENT_BASE = "pct_base"

    // Formula
    private const val KEY_FORMULA_INDEX = "formula_index"

    // Variables
    private const val KEY_VARIABLES_JSON = "variables_json"

    // Clipboard
    private const val KEY_CLIPBOARD_TEXT = "clipboard_text"
    private const val KEY_CLIPBOARD_NUMBER = "clipboard_number"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private const val APP_PREFS_NAME = "chromacalc_prefs"
    private const val KEY_APP_SAVED_THEME_ID = "saved_theme_id"

    private fun getAppPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(APP_PREFS_NAME, Context.MODE_PRIVATE)
    }

    // --- Active App Theme Synchronization (Mirrors exact app theme on all widgets) ---
    fun getActiveAppThemeId(context: Context): ThemeId {
        val savedName = getAppPrefs(context).getString(KEY_APP_SAVED_THEME_ID, null)
            ?: getPrefs(context).getString(KEY_THEME, ThemeId.BATMAN_DARK_KNIGHT.name)
        return try {
            ThemeId.valueOf(savedName ?: ThemeId.BATMAN_DARK_KNIGHT.name)
        } catch (e: Exception) {
            ThemeId.BATMAN_DARK_KNIGHT
        }
    }

    fun getActiveAppTheme(context: Context): ThemePalette {
        val basePalette = CalculatorThemes.getThemeById(getActiveAppThemeId(context))
        val appPrefs = getAppPrefs(context)
        val localAccentLong = if (appPrefs.contains("custom_accent_color")) {
            appPrefs.getLong("custom_accent_color", 0L).takeIf { it != 0L }
        } else null
        return if (localAccentLong != null) {
            val customAccent = androidx.compose.ui.graphics.Color(localAccentLong.toInt())
            basePalette.copy(
                accentColor = customAccent,
                operatorButtonBg = customAccent
            )
        } else {
            basePalette
        }
    }

    fun getThemeFamily(context: Context): WidgetDynamicTheme {
        return WidgetDynamicTheme.fromThemePalette(getActiveAppTheme(context))
    }

    fun getDynamicTheme(context: Context): WidgetDynamicTheme {
        return getThemeFamily(context)
    }

    fun syncAppTheme(context: Context, themeId: ThemeId) {
        getAppPrefs(context).edit().putString(KEY_APP_SAVED_THEME_ID, themeId.name).apply()
        getPrefs(context).edit().putString(KEY_THEME, themeId.name).apply()
        notifyAllWidgets(context)
    }

    fun setThemeFamily(context: Context, family: WidgetThemeFamily) {
        val targetId = when (family) {
            WidgetThemeFamily.DARK_KNIGHT -> ThemeId.BATMAN_DARK_KNIGHT
            WidgetThemeFamily.MINIMAL -> ThemeId.MONOCHROME_LUXURY
            WidgetThemeFamily.RETRO -> ThemeId.MAC_CLASSIC_1984
        }
        syncAppTheme(context, targetId)
    }

    fun cycleThemeFamily(context: Context) {
        val allThemes = CalculatorThemes.allThemes
        val currentId = getActiveAppThemeId(context)
        val currentIndex = allThemes.indexOfFirst { it.id == currentId }
        val nextIndex = if (currentIndex >= 0) (currentIndex + 1) % allThemes.size else 0
        syncAppTheme(context, allThemes[nextIndex].id)
    }

    // --- Last Calculation ---
    fun getLastExpression(context: Context): String =
        getPrefs(context).getString(KEY_LAST_EXPR, "125 × 18") ?: "125 × 18"

    fun getLastResult(context: Context): String =
        getPrefs(context).getString(KEY_LAST_RESULT, "2,250") ?: "2,250"

    fun updateCalculation(context: Context, expression: String, result: String) {
        getPrefs(context).edit()
            .putString(KEY_LAST_EXPR, expression)
            .putString(KEY_LAST_RESULT, result)
            .apply()
        addToHistory(context, "$expression = $result")
        notifyAllWidgets(context)
    }

    // --- History ---
    fun getHistoryList(context: Context): List<String> {
        val json = getPrefs(context).getString(KEY_HISTORY_JSON, null)
        if (json.isNullOrBlank()) {
            return listOf(
                "125 × 18 = 2,250",
                "₹50,000 + 18% = ₹59,000",
                "√144 = 12",
                "450 × 12 = 5,400",
                "₹1,20,000 ÷ 12 = ₹10,000",
                "85 × 2.5 = 212.5"
            )
        }
        return try {
            val arr = JSONArray(json)
            val list = mutableListOf<String>()
            for (i in 0 until arr.length()) {
                list.add(arr.getString(i))
            }
            list
        } catch (e: Exception) {
            listOf("125 × 18 = 2,250", "₹50,000 + 18% = ₹59,000", "√144 = 12")
        }
    }

    fun addToHistory(context: Context, item: String) {
        val current = getHistoryList(context).toMutableList()
        current.removeAll { it == item }
        current.add(0, item)
        val trimmed = current.take(20)
        val arr = JSONArray(trimmed)
        getPrefs(context).edit().putString(KEY_HISTORY_JSON, arr.toString()).apply()
    }

    // --- Quick Calc Mini Keypad ---
    fun getQuickCalcBuffer(context: Context): String =
        getPrefs(context).getString(KEY_QUICK_CALC_BUFFER, "") ?: ""

    fun getQuickCalcResult(context: Context): String =
        getPrefs(context).getString(KEY_QUICK_CALC_RESULT, "0") ?: "0"

    fun handleQuickCalcKey(context: Context, key: String) {
        val prefs = getPrefs(context)
        var buffer = prefs.getString(KEY_QUICK_CALC_BUFFER, "") ?: ""
        var result = prefs.getString(KEY_QUICK_CALC_RESULT, "0") ?: "0"

        when (key) {
            "C" -> {
                buffer = ""
                result = "0"
            }
            "=" -> {
                if (buffer.isNotEmpty()) {
                    try {
                        val eval = evaluateMiniExpression(buffer)
                        result = eval
                    } catch (e: Exception) {
                        result = "Error"
                    }
                }
            }
            "+" , "-", "×", "÷" -> {
                if (buffer.isNotEmpty() && !"+-×÷".contains(buffer.last())) {
                    buffer += key
                } else if (buffer.isEmpty() && result != "0" && result != "Error") {
                    buffer = result + key
                }
            }
            else -> {
                // Digits or dot
                buffer += key
                try {
                    val eval = evaluateMiniExpression(buffer)
                    if (eval != "Error") result = eval
                } catch (e: Exception) {}
            }
        }

        prefs.edit()
            .putString(KEY_QUICK_CALC_BUFFER, buffer)
            .putString(KEY_QUICK_CALC_RESULT, result)
            .apply()
        notifyAllWidgets(context)
    }

    private fun evaluateMiniExpression(expr: String): String {
        val clean = expr.replace("×", "*").replace("÷", "/")
        return try {
            // Simple 2-operand evaluator for mini widget
            val ops = listOf('+', '-', '*', '/')
            var opIdx = -1
            var op = ' '
            for (i in 1 until clean.length) {
                if (clean[i] in ops) {
                    opIdx = i
                    op = clean[i]
                    break
                }
            }
            if (opIdx != -1) {
                val left = clean.substring(0, opIdx).toDoubleOrNull() ?: 0.0
                val right = clean.substring(opIdx + 1).toDoubleOrNull() ?: return ""
                val res = when (op) {
                    '+' -> left + right
                    '-' -> left - right
                    '*' -> left * right
                    '/' -> if (right != 0.0) left / right else return "Error"
                    else -> left
                }
                formatNum(res)
            } else {
                formatNum(clean.toDoubleOrNull() ?: 0.0)
            }
        } catch (e: Exception) {
            "Error"
        }
    }

    // --- Quick Conversion ---
    fun isUnitConversion(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_CONV_IS_UNIT, false)

    fun getConversionInput(context: Context): String {
        return if (isUnitConversion(context)) {
            getPrefs(context).getString(KEY_CONV_INPUT, "10 km") ?: "10 km"
        } else {
            getPrefs(context).getString(KEY_CONV_INPUT, "100 USD") ?: "100 USD"
        }
    }

    fun getConversionResult(context: Context): String {
        return if (isUnitConversion(context)) {
            getPrefs(context).getString(KEY_CONV_RESULT, "6.21 mi") ?: "6.21 mi"
        } else {
            getPrefs(context).getString(KEY_CONV_RESULT, "₹8,730") ?: "₹8,730"
        }
    }

    fun toggleConversionMode(context: Context) {
        val current = isUnitConversion(context)
        getPrefs(context).edit().putBoolean(KEY_CONV_IS_UNIT, !current).apply()
        notifyAllWidgets(context)
    }

    fun swapConversion(context: Context) {
        val prefs = getPrefs(context)
        val isUnit = prefs.getBoolean(KEY_CONV_IS_UNIT, false)
        if (isUnit) {
            val curInput = prefs.getString(KEY_CONV_INPUT, "10 km") ?: "10 km"
            if (curInput.contains("km")) {
                prefs.edit()
                    .putString(KEY_CONV_INPUT, "10 mi")
                    .putString(KEY_CONV_RESULT, "16.09 km")
                    .apply()
            } else {
                prefs.edit()
                    .putString(KEY_CONV_INPUT, "10 km")
                    .putString(KEY_CONV_RESULT, "6.21 mi")
                    .apply()
            }
        } else {
            val curInput = prefs.getString(KEY_CONV_INPUT, "100 USD") ?: "100 USD"
            if (curInput.contains("USD")) {
                prefs.edit()
                    .putString(KEY_CONV_INPUT, "₹8,730")
                    .putString(KEY_CONV_RESULT, "100 USD")
                    .apply()
            } else {
                prefs.edit()
                    .putString(KEY_CONV_INPUT, "100 USD")
                    .putString(KEY_CONV_RESULT, "₹8,730")
                    .apply()
            }
        }
        notifyAllWidgets(context)
    }

    // --- GST Widget ---
    fun getGstAmount(context: Context): Double =
        getPrefs(context).getFloat(KEY_GST_AMOUNT, 25000f).toDouble()

    fun getGstRate(context: Context): Double =
        getPrefs(context).getFloat(KEY_GST_RATE, 18f).toDouble()

    fun isGstAdd(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_GST_IS_ADD, true)

    fun getGstCalculatedTotal(context: Context): Double {
        val amount = getGstAmount(context)
        val rate = getGstRate(context)
        val isAdd = isGstAdd(context)
        return if (isAdd) {
            amount + (amount * rate / 100.0)
        } else {
            amount / (1.0 + rate / 100.0)
        }
    }

    fun toggleGstMode(context: Context) {
        val isAdd = isGstAdd(context)
        getPrefs(context).edit().putBoolean(KEY_GST_IS_ADD, !isAdd).apply()
        notifyAllWidgets(context)
    }

    fun cycleGstRate(context: Context) {
        val current = getGstRate(context)
        val slabs = listOf(5.0, 12.0, 18.0, 28.0)
        val next = slabs[(slabs.indexOf(current) + 1).coerceAtLeast(0) % slabs.size]
        getPrefs(context).edit().putFloat(KEY_GST_RATE, next.toFloat()).apply()
        notifyAllWidgets(context)
    }

    fun adjustGstAmount(context: Context, delta: Double) {
        val current = (getGstAmount(context) + delta).coerceAtLeast(1000.0)
        getPrefs(context).edit().putFloat(KEY_GST_AMOUNT, current.toFloat()).apply()
        notifyAllWidgets(context)
    }

    // --- EMI Widget ---
    fun getEmiPrincipal(context: Context): Double =
        getPrefs(context).getFloat(KEY_EMI_PRINCIPAL, 1000000f).toDouble()

    fun getEmiRate(context: Context): Double =
        getPrefs(context).getFloat(KEY_EMI_RATE, 8.5f).toDouble()

    fun getEmiTenureYears(context: Context): Int =
        getPrefs(context).getInt(KEY_EMI_TENURE, 5)

    fun calculateEmi(context: Context): Double {
        val p = getEmiPrincipal(context)
        val r = (getEmiRate(context) / 12.0) / 100.0
        val n = getEmiTenureYears(context) * 12.0
        if (r == 0.0) return p / n
        return (p * r * (1.0 + r).pow(n)) / ((1.0 + r).pow(n) - 1.0)
    }

    fun adjustEmiTenure(context: Context, deltaYears: Int) {
        val current = (getEmiTenureYears(context) + deltaYears).coerceIn(1, 30)
        getPrefs(context).edit().putInt(KEY_EMI_TENURE, current).apply()
        notifyAllWidgets(context)
    }

    fun adjustEmiRate(context: Context, deltaRate: Double) {
        val current = (getEmiRate(context) + deltaRate).coerceIn(1.0, 25.0)
        getPrefs(context).edit().putFloat(KEY_EMI_RATE, current.toFloat()).apply()
        notifyAllWidgets(context)
    }

    // --- Percentage Widget ---
    fun getPercentageRate(context: Context): Double =
        getPrefs(context).getFloat(KEY_PERCENT_RATE, 15f).toDouble()

    fun getPercentageBase(context: Context): Double =
        getPrefs(context).getFloat(KEY_PERCENT_BASE, 8500f).toDouble()

    fun getPercentageResult(context: Context): Double =
        (getPercentageBase(context) * getPercentageRate(context)) / 100.0

    fun adjustPercentageRate(context: Context, delta: Double) {
        val current = (getPercentageRate(context) + delta).coerceIn(1.0, 100.0)
        getPrefs(context).edit().putFloat(KEY_PERCENT_RATE, current.toFloat()).apply()
        notifyAllWidgets(context)
    }

    fun adjustPercentageBase(context: Context, delta: Double) {
        val current = (getPercentageBase(context) + delta).coerceAtLeast(100.0)
        getPrefs(context).edit().putFloat(KEY_PERCENT_BASE, current.toFloat()).apply()
        notifyAllWidgets(context)
    }

    // --- Direct Widget Value Customizers (Interactive Tap-to-Edit Dialogs) ---
    fun setGstValues(context: Context, amount: Double, rate: Double, isAdd: Boolean) {
        getPrefs(context).edit()
            .putFloat(KEY_GST_AMOUNT, amount.coerceAtLeast(0.0).toFloat())
            .putFloat(KEY_GST_RATE, rate.coerceIn(0.0, 100.0).toFloat())
            .putBoolean(KEY_GST_IS_ADD, isAdd)
            .apply()
        notifyAllWidgets(context)
    }

    fun setEmiValues(context: Context, principal: Double, rate: Double, tenureYears: Int) {
        getPrefs(context).edit()
            .putFloat(KEY_EMI_PRINCIPAL, principal.coerceAtLeast(1000.0).toFloat())
            .putFloat(KEY_EMI_RATE, rate.coerceIn(0.1, 50.0).toFloat())
            .putInt(KEY_EMI_TENURE, tenureYears.coerceIn(1, 40))
            .apply()
        notifyAllWidgets(context)
    }

    fun setPercentageValues(context: Context, base: Double, rate: Double) {
        getPrefs(context).edit()
            .putFloat(KEY_PERCENT_BASE, base.coerceAtLeast(0.0).toFloat())
            .putFloat(KEY_PERCENT_RATE, rate.coerceIn(0.0, 1000.0).toFloat())
            .apply()
        notifyAllWidgets(context)
    }

    fun setQuickCalcValues(context: Context, buffer: String, result: String) {
        getPrefs(context).edit()
            .putString(KEY_QUICK_CALC_BUFFER, buffer)
            .putString(KEY_QUICK_CALC_RESULT, result)
            .apply()
        notifyAllWidgets(context)
    }

    // --- Formula Widget ---
    private val predefinedFormulas = listOf(
        FormulaPin("Circle Area", "Radius", "10 cm", "Area", "314.16 cm²", "π × r²"),
        FormulaPin("Cylinder Vol", "r=5, h=10", "50 cm²", "Volume", "785.4 cm³", "π × r² × h"),
        FormulaPin("Ohm's Law", "12V / 4Ω", "12V", "Current", "3.0 Amperes", "I = V / R"),
        FormulaPin("Compound Int", "₹1,00,000 @ 10%", "2 Years", "Returns", "₹1,21,000", "P(1+r/n)^nt")
    )

    fun getFormulaIndex(context: Context): Int =
        getPrefs(context).getInt(KEY_FORMULA_INDEX, 0)

    fun getCurrentFormula(context: Context): FormulaPin {
        val idx = getFormulaIndex(context) % predefinedFormulas.size
        return predefinedFormulas[idx]
    }

    fun cycleFormula(context: Context) {
        val current = getFormulaIndex(context)
        val next = (current + 1) % predefinedFormulas.size
        getPrefs(context).edit().putInt(KEY_FORMULA_INDEX, next).apply()
        notifyAllWidgets(context)
    }

    // --- Variable Widget ("My Values") ---
    fun getMyValues(context: Context): List<MyValueVariable> {
        val json = getPrefs(context).getString(KEY_VARIABLES_JSON, null)
        if (json.isNullOrBlank()) {
            return listOf(
                MyValueVariable("1", "Salary", "₹85,000", 85000.0),
                MyValueVariable("2", "Rent", "₹18,000", 18000.0),
                MyValueVariable("3", "Tax", "18%", 18.0),
                MyValueVariable("4", "Fuel", "₹104/L", 104.0)
            )
        }
        return try {
            val arr = JSONArray(json)
            val list = mutableListOf<MyValueVariable>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    MyValueVariable(
                        id = obj.getString("id"),
                        label = obj.getString("label"),
                        displayValue = obj.getString("displayValue"),
                        numericValue = obj.getDouble("numericValue")
                    )
                )
            }
            list
        } catch (e: Exception) {
            listOf(
                MyValueVariable("1", "Salary", "₹85,000", 85000.0),
                MyValueVariable("2", "Rent", "₹18,000", 18000.0),
                MyValueVariable("3", "Tax", "18%", 18.0),
                MyValueVariable("4", "Fuel", "₹104/L", 104.0)
            )
        }
    }

    fun saveMyValues(context: Context, values: List<MyValueVariable>) {
        val arr = JSONArray()
        values.forEach { v ->
            val obj = JSONObject().apply {
                put("id", v.id)
                put("label", v.label)
                put("displayValue", v.displayValue)
                put("numericValue", v.numericValue)
            }
            arr.put(obj)
        }
        getPrefs(context).edit().putString(KEY_VARIABLES_JSON, arr.toString()).apply()
        notifyAllWidgets(context)
    }

    // --- Clipboard Widget ---
    fun getClipboardText(context: Context): String =
        getPrefs(context).getString(KEY_CLIPBOARD_TEXT, "₹25,500") ?: "₹25,500"

    fun getClipboardNumber(context: Context): String =
        getPrefs(context).getString(KEY_CLIPBOARD_NUMBER, "25500") ?: "25500"

    fun updateClipboard(context: Context, text: String) {
        val cleanNumber = text.replace("[^0-9.]".toRegex(), "")
        getPrefs(context).edit()
            .putString(KEY_CLIPBOARD_TEXT, text)
            .putString(KEY_CLIPBOARD_NUMBER, if (cleanNumber.isNotEmpty()) cleanNumber else "0")
            .apply()
        notifyAllWidgets(context)
    }

    // --- Shortcuts Grid ---
    fun getShortcuts(): List<ShortcutItem> {
        return listOf(
            ShortcutItem("CALC", "STANDARD", "🔢"),
            ShortcutItem("GST", "GST_CALCULATOR", "🧾"),
            ShortcutItem("EMI", "EMI_LOAN", "🏦"),
            ShortcutItem("BMI", "BMI_CALCULATOR", "⚖️"),
            ShortcutItem("AGE", "AGE_CALCULATOR", "🎂"),
            ShortcutItem("₹→$", "CURRENCY_CONVERTER", "💱")
        )
    }

    // --- Formatting Utilities ---
    fun formatCurrency(amount: Double): String {
        return "₹" + NumberFormat.getNumberInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 0
        }.format(amount)
    }

    fun formatNum(num: Double): String {
        return if (num % 1.0 == 0.0) {
            num.toLong().toString()
        } else {
            String.format(Locale.US, "%.2f", num).trimEnd('0').trimEnd('.')
        }
    }

    // --- Broadcast Refresh ---
    fun notifyAllWidgets(context: Context) {
        val providers = listOf(
            CalculatorAppWidgetProvider::class.java,
            QuickCalcWidgetProvider::class.java,
            HistoryWidgetProvider::class.java,
            QuickConversionWidgetProvider::class.java,
            GstWidgetProvider::class.java,
            EmiWidgetProvider::class.java,
            ShortcutGridWidgetProvider::class.java,
            FormulaWidgetProvider::class.java,
            VariableWidgetProvider::class.java,
            PercentageWidgetProvider::class.java,
            ClipboardWidgetProvider::class.java
        )

        val appWidgetManager = AppWidgetManager.getInstance(context)
        for (providerClass in providers) {
            try {
                val component = ComponentName(context, providerClass)
                val ids = appWidgetManager.getAppWidgetIds(component)
                if (ids.isNotEmpty()) {
                    val updateIntent = Intent(context, providerClass).apply {
                        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                    }
                    context.sendBroadcast(updateIntent)
                }
            } catch (e: Exception) {
                // Ignore any widget notification error
            }
        }

        // Notify History ListView to refresh items
        try {
            val historyComponent = ComponentName(context, HistoryWidgetProvider::class.java)
            val historyIds = appWidgetManager.getAppWidgetIds(historyComponent)
            if (historyIds.isNotEmpty()) {
                appWidgetManager.notifyAppWidgetViewDataChanged(historyIds, com.example.R.id.widget_history_list)
            }
        } catch (e: Exception) {}
    }
}
