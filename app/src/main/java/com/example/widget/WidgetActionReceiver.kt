package com.example.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.MainActivity

class WidgetActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return

        when (action) {
            ACTION_QUICK_CALC_KEY -> {
                val key = intent.getStringExtra(EXTRA_KEY) ?: return
                WidgetDataManager.handleQuickCalcKey(context, key)
            }
            ACTION_CONVERSION_SWAP -> {
                WidgetDataManager.swapConversion(context)
            }
            ACTION_CONVERSION_TOGGLE_TYPE -> {
                WidgetDataManager.toggleConversionMode(context)
            }
            ACTION_GST_TOGGLE_MODE -> {
                WidgetDataManager.toggleGstMode(context)
            }
            ACTION_GST_CYCLE_RATE -> {
                WidgetDataManager.cycleGstRate(context)
            }
            ACTION_GST_ADJUST_AMOUNT -> {
                val delta = intent.getDoubleExtra(EXTRA_DELTA, 1000.0)
                WidgetDataManager.adjustGstAmount(context, delta)
            }
            ACTION_EMI_ADJUST_TENURE -> {
                val delta = intent.getIntExtra(EXTRA_DELTA_INT, 1)
                WidgetDataManager.adjustEmiTenure(context, delta)
            }
            ACTION_EMI_ADJUST_RATE -> {
                val delta = intent.getDoubleExtra(EXTRA_DELTA, 0.5)
                WidgetDataManager.adjustEmiRate(context, delta)
            }
            ACTION_PERCENT_ADJUST_RATE -> {
                val delta = intent.getDoubleExtra(EXTRA_DELTA, 5.0)
                WidgetDataManager.adjustPercentageRate(context, delta)
            }
            ACTION_PERCENT_ADJUST_BASE -> {
                val delta = intent.getDoubleExtra(EXTRA_DELTA, 1000.0)
                WidgetDataManager.adjustPercentageBase(context, delta)
            }
            ACTION_FORMULA_CYCLE -> {
                WidgetDataManager.cycleFormula(context)
            }
            ACTION_CYCLE_THEME -> {
                WidgetDataManager.cycleThemeFamily(context)
            }
            ACTION_CLIPBOARD_DO -> {
                val targetAction = intent.getStringExtra(EXTRA_CLIPBOARD_TARGET) ?: "CALCULATE"
                val number = WidgetDataManager.getClipboardNumber(context)

                when (targetAction) {
                    "CALCULATE" -> {
                        val openIntent = Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            putExtra(CalculatorAppWidgetProvider.EXTRA_MODE, "STANDARD")
                            putExtra(EXTRA_INITIAL_EXPR, number)
                        }
                        context.startActivity(openIntent)
                    }
                    "CONVERT" -> {
                        val openIntent = Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            putExtra(CalculatorAppWidgetProvider.EXTRA_MODE, "CURRENCY_CONVERTER")
                            putExtra(EXTRA_INITIAL_EXPR, number)
                        }
                        context.startActivity(openIntent)
                    }
                    "GST" -> {
                        val openIntent = Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            putExtra(CalculatorAppWidgetProvider.EXTRA_MODE, "GST_CALCULATOR")
                            putExtra(EXTRA_INITIAL_EXPR, number)
                        }
                        context.startActivity(openIntent)
                    }
                    "PERCENTAGE" -> {
                        val numVal = number.toDoubleOrNull() ?: 1000.0
                        WidgetDataManager.adjustPercentageBase(context, numVal - WidgetDataManager.getPercentageBase(context))
                        Toast.makeText(context, "Loaded into Percentage Widget: $number", Toast.LENGTH_SHORT).show()
                    }
                    "SHARE" -> {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "CALC + Copied Value: ${WidgetDataManager.getClipboardText(context)}")
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        val chooser = Intent.createChooser(shareIntent, "Share Number").apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(chooser)
                    }
                    "SAVE" -> {
                        WidgetDataManager.addToHistory(context, "Saved Value: ${WidgetDataManager.getClipboardText(context)}")
                        Toast.makeText(context, "Saved to CALC + History", Toast.LENGTH_SHORT).show()
                        WidgetDataManager.notifyAllWidgets(context)
                    }
                }
            }
            ACTION_EDIT_WIDGET_VALUE -> {
                val widgetType = intent.getStringExtra(EXTRA_WIDGET_TYPE) ?: "GST"
                val editIntent = Intent(context, WidgetValueEditorActivity::class.java).apply {
                    putExtra(WidgetValueEditorActivity.EXTRA_WIDGET_TYPE, widgetType)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                context.startActivity(editIntent)
            }
        }
    }

    companion object {
        const val ACTION_QUICK_CALC_KEY = "com.example.widget.ACTION_QUICK_CALC_KEY"
        const val ACTION_CONVERSION_SWAP = "com.example.widget.ACTION_CONVERSION_SWAP"
        const val ACTION_CONVERSION_TOGGLE_TYPE = "com.example.widget.ACTION_CONVERSION_TOGGLE_TYPE"
        const val ACTION_GST_TOGGLE_MODE = "com.example.widget.ACTION_GST_TOGGLE_MODE"
        const val ACTION_GST_CYCLE_RATE = "com.example.widget.ACTION_GST_CYCLE_RATE"
        const val ACTION_GST_ADJUST_AMOUNT = "com.example.widget.ACTION_GST_ADJUST_AMOUNT"
        const val ACTION_EMI_ADJUST_TENURE = "com.example.widget.ACTION_EMI_ADJUST_TENURE"
        const val ACTION_EMI_ADJUST_RATE = "com.example.widget.ACTION_EMI_ADJUST_RATE"
        const val ACTION_PERCENT_ADJUST_RATE = "com.example.widget.ACTION_PERCENT_ADJUST_RATE"
        const val ACTION_PERCENT_ADJUST_BASE = "com.example.widget.ACTION_PERCENT_ADJUST_BASE"
        const val ACTION_FORMULA_CYCLE = "com.example.widget.ACTION_FORMULA_CYCLE"
        const val ACTION_CYCLE_THEME = "com.example.widget.ACTION_CYCLE_THEME"
        const val ACTION_CLIPBOARD_DO = "com.example.widget.ACTION_CLIPBOARD_DO"
        const val ACTION_EDIT_WIDGET_VALUE = "com.example.widget.ACTION_EDIT_WIDGET_VALUE"

        const val EXTRA_KEY = "extra_key_char"
        const val EXTRA_DELTA = "extra_delta_double"
        const val EXTRA_DELTA_INT = "extra_delta_int"
        const val EXTRA_CLIPBOARD_TARGET = "extra_clipboard_target"
        const val EXTRA_INITIAL_EXPR = "extra_initial_expression"
        const val EXTRA_WIDGET_TYPE = "extra_widget_type"
    }
}
