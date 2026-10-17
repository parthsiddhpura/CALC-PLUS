package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R

class QuickCalcWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val theme = WidgetDataManager.getThemeFamily(context)
            val views = RemoteViews(context.packageName, R.layout.widget_quick_calc)

            // Apply exact App Theme Styling
            theme.applyRoot(views, R.id.widget_root)
            theme.applyScreen(views, R.id.btn_open_full_calc)
            theme.applyThemeCycleButton(views, R.id.btn_theme_cycle)
            views.setTextColor(R.id.widget_title, theme.headerTextColorHex)
            views.setTextColor(R.id.widget_result, theme.textColorHex)
            views.setTextColor(R.id.widget_expression, theme.subtextColorHex)

            // Keypad Button Theme Mirroring
            theme.applyButton(views, R.id.btn_key_c, isAccent = true)
            theme.applyButton(views, R.id.btn_key_div, isAccent = true)
            theme.applyButton(views, R.id.btn_key_mul, isAccent = true)
            theme.applyButton(views, R.id.btn_key_sub, isAccent = true)
            theme.applyButton(views, R.id.btn_key_add, isAccent = true)
            theme.applyButton(views, R.id.btn_key_eq, isAccent = true)
            theme.applyButton(views, R.id.btn_key_7, isAccent = false)
            theme.applyButton(views, R.id.btn_key_8, isAccent = false)
            theme.applyButton(views, R.id.btn_key_9, isAccent = false)
            theme.applyButton(views, R.id.btn_key_4, isAccent = false)
            theme.applyButton(views, R.id.btn_key_5, isAccent = false)
            theme.applyButton(views, R.id.btn_key_0, isAccent = false)

            // Data
            val buffer = WidgetDataManager.getQuickCalcBuffer(context)
            val result = WidgetDataManager.getQuickCalcResult(context)
            views.setTextViewText(R.id.widget_expression, if (buffer.isNotEmpty()) buffer else "Tap for full app")
            views.setTextViewText(R.id.widget_result, result)

            // Tap screen -> Open standard calculator
            val openIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(CalculatorAppWidgetProvider.EXTRA_MODE, "STANDARD")
                if (result != "0" && result != "Error") {
                    putExtra(WidgetActionReceiver.EXTRA_INITIAL_EXPR, result)
                }
            }
            val openPendingIntent = PendingIntent.getActivity(
                context, 201, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_open_full_calc, openPendingIntent)

            // Cycle Theme Button
            val themeIntent = Intent(context, WidgetActionReceiver::class.java).apply {
                action = WidgetActionReceiver.ACTION_CYCLE_THEME
            }
            val themePendingIntent = PendingIntent.getBroadcast(
                context, 202, themeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_theme_cycle, themePendingIntent)

            // Keypad pending intents
            bindKey(context, views, R.id.btn_key_c, "C", 210)
            bindKey(context, views, R.id.btn_key_div, "÷", 211)
            bindKey(context, views, R.id.btn_key_mul, "×", 212)
            bindKey(context, views, R.id.btn_key_sub, "-", 213)
            bindKey(context, views, R.id.btn_key_7, "7", 214)
            bindKey(context, views, R.id.btn_key_8, "8", 215)
            bindKey(context, views, R.id.btn_key_9, "9", 216)
            bindKey(context, views, R.id.btn_key_add, "+", 217)
            bindKey(context, views, R.id.btn_key_4, "4", 218)
            bindKey(context, views, R.id.btn_key_5, "5", 219)
            bindKey(context, views, R.id.btn_key_0, "0", 220)
            bindKey(context, views, R.id.btn_key_eq, "=", 221)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun bindKey(context: Context, views: RemoteViews, viewId: Int, keyChar: String, reqCode: Int) {
            val intent = Intent(context, WidgetActionReceiver::class.java).apply {
                action = WidgetActionReceiver.ACTION_QUICK_CALC_KEY
                putExtra(WidgetActionReceiver.EXTRA_KEY, keyChar)
            }
            val pi = PendingIntent.getBroadcast(
                context, reqCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(viewId, pi)
        }
    }
}
