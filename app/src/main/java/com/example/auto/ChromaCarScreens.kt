package com.example.auto

import androidx.car.app.CarContext
import androidx.car.app.CarToast
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.CarColor
import androidx.car.app.model.ForegroundCarColorSpan
import androidx.car.app.model.Header
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.SectionedItemList
import androidx.car.app.model.Template
import android.text.SpannableString
import android.text.Spanned
import com.example.data.CurrencyRepository
import com.example.domain.CalculatorEngine
import com.example.model.AngleMode
import com.example.widget.WidgetDataManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

/**
 * Main Dashboard Screen on Android Auto head unit.
 * Provides quick access to:
 * 1. Quick Calculator
 * 2. Live Currency & Forex Converter
 * 3. Fuel & Trip Cost Calculator
 * 4. GST Tax Calculator
 * 5. Recent Calculation History
 */
class ChromaCarMainScreen(carContext: CarContext) : Screen(carContext) {

    override fun onGetTemplate(): Template {
        val activeTheme = WidgetDataManager.getActiveAppTheme(carContext)
        val lastExpr = WidgetDataManager.getLastExpression(carContext)
        val lastResult = WidgetDataManager.getLastResult(carContext)

        val toolsList = ItemList.Builder()
            .addItem(
                Row.Builder()
                    .setTitle(colorize("Quick Calculator  •  $lastExpr = $lastResult", CarColor.YELLOW))
                    .addText("Instant car keypad presets, percentages & live expression solver")
                    .setBrowsable(true)
                    .setOnClickListener {
                        screenManager.push(QuickCalcCarScreen(carContext))
                    }
                    .build()
            )
            .addItem(
                Row.Builder()
                    .setTitle("Live Currency & Forex Converter")
                    .addText("Real-time USD, INR, EUR, GBP, AED, CAD & AUD conversions")
                    .setBrowsable(true)
                    .setOnClickListener {
                        screenManager.push(CurrencyCarScreen(carContext))
                    }
                    .build()
            )
            .addItem(
                Row.Builder()
                    .setTitle("Fuel & Highway Trip Calculator")
                    .addText("Calculate trip fuel liters, total fuel cost & ₹/km efficiency")
                    .setBrowsable(true)
                    .setOnClickListener {
                        screenManager.push(FuelTripCarScreen(carContext))
                    }
                    .build()
            )
            .addItem(
                Row.Builder()
                    .setTitle("GST Tax Calculator (5%, 12%, 18%, 28%)")
                    .addText("Instant CGST + SGST & inclusive/exclusive tax breakdown")
                    .setBrowsable(true)
                    .setOnClickListener {
                        screenManager.push(GstCarScreen(carContext))
                    }
                    .build()
            )
            .addItem(
                Row.Builder()
                    .setTitle("Recent Calculation History")
                    .addText("View and reuse calculations synced with your phone & widgets")
                    .setBrowsable(true)
                    .setOnClickListener {
                        screenManager.push(HistoryCarScreen(carContext))
                    }
                    .build()
            )
            .build()

        val statusList = ItemList.Builder()
            .addItem(
                Row.Builder()
                    .setTitle("Synced Theme: ${activeTheme.name}")
                    .addText("${activeTheme.subtitle} • Tap to cycle theme across App & Widgets")
                    .setOnClickListener {
                        WidgetDataManager.cycleThemeFamily(carContext)
                        val updated = WidgetDataManager.getActiveAppTheme(carContext)
                        CarToast.makeText(
                            carContext,
                            "Synced Theme: ${updated.name}",
                            CarToast.LENGTH_SHORT
                        ).show()
                        invalidate()
                    }
                    .build()
            )
            .build()

        return ListTemplate.Builder()
            .setSingleList(toolsList)
            .setTitle("CALC +  •  Android Auto")
            .setHeaderAction(Action.APP_ICON)
            .setActionStrip(
                ActionStrip.Builder()
                    .addAction(
                        Action.Builder()
                            .setTitle("Theme: ${activeTheme.name.take(12)}")
                            .setOnClickListener {
                                WidgetDataManager.cycleThemeFamily(carContext)
                                invalidate()
                            }
                            .build()
                    )
                    .build()
            )
            .build()
    }
}

/**
 * 1. Quick Calculator Screen for Android Auto
 */
class QuickCalcCarScreen(
    carContext: CarContext,
    initialValue: Double = 100.0
) : Screen(carContext) {

    private var currentValue: Double = initialValue
    private var currentExpression: String = formatVal(initialValue)

    override fun onGetTemplate(): Template {
        val formattedResult = formatVal(currentValue)

        val displaySection = ItemList.Builder()
            .addItem(
                Row.Builder()
                    .setTitle(colorize("Result: $formattedResult", CarColor.GREEN))
                    .addText("Expression: $currentExpression  •  Tap to save to History & Widgets")
                    .setOnClickListener {
                        WidgetDataManager.updateCalculation(carContext, currentExpression, formattedResult)
                        CarToast.makeText(
                            carContext,
                            "Saved $currentExpression = $formattedResult",
                            CarToast.LENGTH_SHORT
                        ).show()
                    }
                    .build()
            )
            .build()

        val operationsSection = ItemList.Builder()
            .addItem(
                Row.Builder()
                    .setTitle("+ 100   |   Add 100")
                    .addText("New total: ${formatVal(currentValue + 100.0)}")
                    .setOnClickListener {
                        val prev = formatVal(currentValue)
                        currentValue += 100.0
                        currentExpression = "$prev + 100"
                        syncToWidgets()
                    }
                    .build()
            )
            .addItem(
                Row.Builder()
                    .setTitle("+ 1,000   |   Add 1,000")
                    .addText("New total: ${formatVal(currentValue + 1000.0)}")
                    .setOnClickListener {
                        val prev = formatVal(currentValue)
                        currentValue += 1000.0
                        currentExpression = "$prev + 1,000"
                        syncToWidgets()
                    }
                    .build()
            )
            .addItem(
                Row.Builder()
                    .setTitle("× 2   |   Double Value")
                    .addText("New total: ${formatVal(currentValue * 2.0)}")
                    .setOnClickListener {
                        val prev = formatVal(currentValue)
                        currentValue *= 2.0
                        currentExpression = "$prev × 2"
                        syncToWidgets()
                    }
                    .build()
            )
            .addItem(
                Row.Builder()
                    .setTitle("÷ 2   |   Split in Half")
                    .addText("New total: ${formatVal(currentValue / 2.0)}")
                    .setOnClickListener {
                        val prev = formatVal(currentValue)
                        currentValue /= 2.0
                        currentExpression = "$prev ÷ 2"
                        syncToWidgets()
                    }
                    .build()
            )
            .addItem(
                Row.Builder()
                    .setTitle("+ 18%   |   Add 18% Tax / Markup")
                    .addText("New total: ${formatVal(currentValue * 1.18)}")
                    .setOnClickListener {
                        val prev = formatVal(currentValue)
                        currentValue *= 1.18
                        currentExpression = "$prev + 18%"
                        syncToWidgets()
                    }
                    .build()
            )
            .addItem(
                Row.Builder()
                    .setTitle("- 10%   |   Apply 10% Discount")
                    .addText("New total: ${formatVal(currentValue * 0.90)}")
                    .setOnClickListener {
                        val prev = formatVal(currentValue)
                        currentValue *= 0.90
                        currentExpression = "$prev - 10%"
                        syncToWidgets()
                    }
                    .build()
            )
            .build()

        return ListTemplate.Builder()
            .addSectionedList(SectionedItemList.create(displaySection, "Live Display"))
            .addSectionedList(SectionedItemList.create(operationsSection, "One-Tap Operations"))
            .setTitle("Quick Calculator")
            .setHeaderAction(Action.BACK)
            .setActionStrip(
                ActionStrip.Builder()
                    .addAction(
                        Action.Builder()
                            .setTitle("Reset")
                            .setOnClickListener {
                                currentValue = 100.0
                                currentExpression = "100"
                                invalidate()
                            }
                            .build()
                    )
                    .build()
            )
            .build()
    }

    private fun syncToWidgets() {
        WidgetDataManager.updateCalculation(carContext, currentExpression, formatVal(currentValue))
        invalidate()
    }
}

/**
 * 2. Live Currency & Forex Converter Screen for Android Auto
 */
class CurrencyCarScreen(carContext: CarContext) : Screen(carContext) {

    private val pairs = listOf(
        Pair("USD", "INR"),
        Pair("EUR", "INR"),
        Pair("GBP", "INR"),
        Pair("AED", "INR"),
        Pair("CAD", "INR"),
        Pair("AUD", "INR")
    )
    private var pairIndex = 0
    private var isReversed = false
    private var amount = 100.0
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        scope.launch {
            CurrencyRepository.fetchLiveRates()
            carContext.mainExecutor.execute { invalidate() }
        }
    }

    override fun onGetTemplate(): Template {
        val basePair = pairs[pairIndex]
        val fromCode = if (isReversed) basePair.second else basePair.first
        val toCode = if (isReversed) basePair.first else basePair.second

        val rate = CurrencyRepository.convert(1.0, fromCode, toCode)
        val converted = CurrencyRepository.convert(amount, fromCode, toCode)
        val fromInfo = CurrencyRepository.getCurrencyInfo(fromCode)
        val toInfo = CurrencyRepository.getCurrencyInfo(toCode)

        val listBuilder = ItemList.Builder()
            .addItem(
                Row.Builder()
                    .setTitle(
                        colorize(
                            "${fromInfo.symbol}${formatVal(amount)} $fromCode  =  ${toInfo.symbol}${formatVal(converted)} $toCode",
                            CarColor.GREEN
                        )
                    )
                    .addText("Live Rate: 1 $fromCode = ${String.format(Locale.US, "%.4f", rate)} $toCode")
                    .setOnClickListener {
                        WidgetDataManager.updateCalculation(
                            carContext,
                            "${fromInfo.symbol}${formatVal(amount)} $fromCode",
                            "${toInfo.symbol}${formatVal(converted)} $toCode"
                        )
                        CarToast.makeText(carContext, "Saved conversion to History", CarToast.LENGTH_SHORT).show()
                    }
                    .build()
            )
            .addItem(
                Row.Builder()
                    .setTitle("⇄ Swap Direction ($toCode → $fromCode)")
                    .addText("Tap to invert conversion direction")
                    .setOnClickListener {
                        isReversed = !isReversed
                        invalidate()
                    }
                    .build()
            )
            .addItem(
                Row.Builder()
                    .setTitle("Switch Currency Pair (${pairs[(pairIndex + 1) % pairs.size].first} / ${pairs[(pairIndex + 1) % pairs.size].second})")
                    .addText("Cycle through USD, EUR, GBP, AED, CAD, AUD")
                    .setOnClickListener {
                        pairIndex = (pairIndex + 1) % pairs.size
                        invalidate()
                    }
                    .build()
            )
            .addItem(
                Row.Builder()
                    .setTitle("Amount: 100 $fromCode  →  ${toInfo.symbol}${formatVal(CurrencyRepository.convert(100.0, fromCode, toCode))}")
                    .setOnClickListener {
                        amount = 100.0
                        invalidate()
                    }
                    .build()
            )
            .addItem(
                Row.Builder()
                    .setTitle("Amount: 500 $fromCode  →  ${toInfo.symbol}${formatVal(CurrencyRepository.convert(500.0, fromCode, toCode))}")
                    .setOnClickListener {
                        amount = 500.0
                        invalidate()
                    }
                    .build()
            )
            .addItem(
                Row.Builder()
                    .setTitle("Amount: 1,000 $fromCode  →  ${toInfo.symbol}${formatVal(CurrencyRepository.convert(1000.0, fromCode, toCode))}")
                    .setOnClickListener {
                        amount = 1000.0
                        invalidate()
                    }
                    .build()
            )

        return ListTemplate.Builder()
            .setSingleList(listBuilder.build())
            .setTitle("Live Currency Converter")
            .setHeaderAction(Action.BACK)
            .setActionStrip(
                ActionStrip.Builder()
                    .addAction(
                        Action.Builder()
                            .setTitle("Refresh")
                            .setOnClickListener {
                                scope.launch {
                                    CurrencyRepository.fetchLiveRates()
                                    carContext.mainExecutor.execute {
                                        CarToast.makeText(carContext, "Live Forex Rates Updated", CarToast.LENGTH_SHORT).show()
                                        invalidate()
                                    }
                                }
                            }
                            .build()
                    )
                    .build()
            )
            .build()
    }
}

/**
 * 3. Fuel & Highway Trip Cost Calculator Screen for Android Auto
 */
class FuelTripCarScreen(carContext: CarContext) : Screen(carContext) {

    private var distanceKm = 150.0
    private var mileageKmPerL = 16.0
    private var fuelPricePerL = 102.0

    override fun onGetTemplate(): Template {
        val fuelLiters = distanceKm / mileageKmPerL.coerceAtLeast(1.0)
        val totalCost = fuelLiters * fuelPricePerL
        val costPerKm = totalCost / distanceKm.coerceAtLeast(1.0)

        val listBuilder = ItemList.Builder()
            .addItem(
                Row.Builder()
                    .setTitle(colorize("Total Trip Fuel Cost: ₹${formatVal(totalCost)}", CarColor.YELLOW))
                    .addText(
                        "Fuel Required: ${String.format(Locale.US, "%.2f", fuelLiters)} L  •  Cost/km: ₹${String.format(Locale.US, "%.2f", costPerKm)}/km"
                    )
                    .setOnClickListener {
                        WidgetDataManager.updateCalculation(
                            carContext,
                            "${distanceKm.toInt()}km @ ${mileageKmPerL.toInt()}km/L",
                            "₹${formatVal(totalCost)}"
                        )
                        CarToast.makeText(carContext, "Trip cost saved to History", CarToast.LENGTH_SHORT).show()
                    }
                    .build()
            )
            .addItem(
                Row.Builder()
                    .setTitle("Trip Distance: ${distanceKm.toInt()} km  (Tap +50 km)")
                    .addText("Cycles 50 km to 1,000 km")
                    .setOnClickListener {
                        distanceKm = if (distanceKm >= 1000.0) 50.0 else distanceKm + 50.0
                        invalidate()
                    }
                    .build()
            )
            .addItem(
                Row.Builder()
                    .setTitle("Vehicle Mileage: ${String.format(Locale.US, "%.1f", mileageKmPerL)} km/L  (Tap +2 km/L)")
                    .addText("Cycles 8 km/L to 30 km/L")
                    .setOnClickListener {
                        mileageKmPerL = if (mileageKmPerL >= 30.0) 8.0 else mileageKmPerL + 2.0
                        invalidate()
                    }
                    .build()
            )
            .addItem(
                Row.Builder()
                    .setTitle("Fuel Price: ₹${String.format(Locale.US, "%.1f", fuelPricePerL)} / Liter  (Tap +₹2)")
                    .addText("Cycles ₹85/L to ₹115/L")
                    .setOnClickListener {
                        fuelPricePerL = if (fuelPricePerL >= 115.0) 85.0 else fuelPricePerL + 2.0
                        invalidate()
                    }
                    .build()
            )

        return ListTemplate.Builder()
            .setSingleList(listBuilder.build())
            .setTitle("Fuel & Trip Cost Calculator")
            .setHeaderAction(Action.BACK)
            .setActionStrip(
                ActionStrip.Builder()
                    .addAction(
                        Action.Builder()
                            .setTitle("Reset")
                            .setOnClickListener {
                                distanceKm = 150.0
                                mileageKmPerL = 16.0
                                fuelPricePerL = 102.0
                                invalidate()
                            }
                            .build()
                    )
                    .build()
            )
            .build()
    }
}

/**
 * 4. GST Tax Calculator Screen for Android Auto
 */
class GstCarScreen(carContext: CarContext) : Screen(carContext) {

    override fun onGetTemplate(): Template {
        val baseAmount = WidgetDataManager.getGstAmount(carContext)
        val rate = WidgetDataManager.getGstRate(carContext)
        val isAdd = WidgetDataManager.isGstAdd(carContext)
        val total = WidgetDataManager.getGstCalculatedTotal(carContext)
        val taxAmount = if (isAdd) total - baseAmount else baseAmount - total
        val halfTax = taxAmount / 2.0

        val listBuilder = ItemList.Builder()
            .addItem(
                Row.Builder()
                    .setTitle(
                        colorize(
                            if (isAdd) "Gross Total (+${rate.toInt()}% GST): ${WidgetDataManager.formatCurrency(total)}"
                            else "Net Pre-Tax (-${rate.toInt()}% GST): ${WidgetDataManager.formatCurrency(total)}",
                            CarColor.GREEN
                        )
                    )
                    .addText(
                        "Total GST: ${WidgetDataManager.formatCurrency(taxAmount)} (CGST: ${WidgetDataManager.formatCurrency(halfTax)} + SGST: ${WidgetDataManager.formatCurrency(halfTax)})"
                    )
                    .build()
            )
            .addItem(
                Row.Builder()
                    .setTitle("GST Slab: ${rate.toInt()}%  (Tap to Cycle 5% / 12% / 18% / 28%)")
                    .addText("Half Rate CGST/SGST: ${rate / 2.0}% each")
                    .setOnClickListener {
                        WidgetDataManager.cycleGstRate(carContext)
                        invalidate()
                    }
                    .build()
            )
            .addItem(
                Row.Builder()
                    .setTitle("Mode: ${if (isAdd) "Add GST (+ Exclusive)" else "Remove GST (- Inclusive)"}")
                    .addText("Tap to toggle between +GST and -GST")
                    .setOnClickListener {
                        WidgetDataManager.toggleGstMode(carContext)
                        invalidate()
                    }
                    .build()
            )
            .addItem(
                Row.Builder()
                    .setTitle("Base Amount: ${WidgetDataManager.formatCurrency(baseAmount)}  (Tap +₹5,000)")
                    .addText("Increases base amount by ₹5,000 and updates GST widget")
                    .setOnClickListener {
                        WidgetDataManager.adjustGstAmount(carContext, 5000.0)
                        invalidate()
                    }
                    .build()
            )
            .addItem(
                Row.Builder()
                    .setTitle("Decrease Base Amount (-₹5,000)")
                    .addText("Decreases base amount by ₹5,000")
                    .setOnClickListener {
                        WidgetDataManager.adjustGstAmount(carContext, -5000.0)
                        invalidate()
                    }
                    .build()
            )

        return ListTemplate.Builder()
            .setSingleList(listBuilder.build())
            .setTitle("GST Tax Calculator")
            .setHeaderAction(Action.BACK)
            .build()
    }
}

/**
 * 5. Recent Calculation History Screen for Android Auto
 */
class HistoryCarScreen(carContext: CarContext) : Screen(carContext) {

    override fun onGetTemplate(): Template {
        val historyItems = WidgetDataManager.getHistoryList(carContext).take(6)
        val listBuilder = ItemList.Builder()

        if (historyItems.isEmpty()) {
            listBuilder.addItem(
                Row.Builder()
                    .setTitle("No recent calculations yet")
                    .addText("Calculations from your phone or car will appear here")
                    .build()
            )
        } else {
            for (item in historyItems) {
                val parts = item.split("=")
                val expr = parts.firstOrNull()?.trim() ?: item
                val res = parts.getOrNull(1)?.trim() ?: ""
                val numericVal = res.replace("[^0-9.-]".toRegex(), "").toDoubleOrNull() ?: 100.0

                listBuilder.addItem(
                    Row.Builder()
                        .setTitle(item)
                        .addText("Tap to load ${if (res.isNotEmpty()) res else expr} into Quick Calculator")
                        .setBrowsable(true)
                        .setOnClickListener {
                            screenManager.push(QuickCalcCarScreen(carContext, numericVal))
                        }
                        .build()
                )
            }
        }

        return ListTemplate.Builder()
            .setSingleList(listBuilder.build())
            .setTitle("Recent Calculations")
            .setHeaderAction(Action.BACK)
            .build()
    }
}

private fun formatVal(v: Double): String {
    return NumberFormat.getNumberInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 2
        minimumFractionDigits = 0
    }.format(v)
}

private fun colorize(text: String, color: CarColor): CharSequence {
    val ss = SpannableString(text)
    ss.setSpan(ForegroundCarColorSpan.create(color), 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    return ss
}
