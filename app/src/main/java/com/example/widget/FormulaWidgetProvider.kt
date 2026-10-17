package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R

class FormulaWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val theme = WidgetDataManager.getThemeFamily(context)
            val views = RemoteViews(context.packageName, R.layout.widget_formula)

            theme.applyRoot(views, R.id.widget_root)
            theme.applyScreen(views, R.id.btn_open_formula)
            theme.applyButton(views, R.id.btn_cycle_formula, isAccent = true)

            views.setTextColor(R.id.formula_name_text, theme.headerTextColorHex)
            views.setTextColor(R.id.formula_input_label, theme.subtextColorHex)
            views.setTextColor(R.id.formula_input_val, theme.textColorHex)
            views.setTextColor(R.id.formula_output_label, theme.accentColorHex)
            views.setTextColor(R.id.formula_output_val, theme.textColorHex)

            val formula = WidgetDataManager.getCurrentFormula(context)
            views.setTextViewText(R.id.formula_name_text, formula.title)
            views.setTextViewText(R.id.formula_input_label, formula.inputLabel)
            views.setTextViewText(R.id.formula_input_val, formula.inputValue)
            views.setTextViewText(R.id.formula_output_label, formula.outputLabel)
            views.setTextViewText(R.id.formula_output_val, formula.outputValue)

            val openIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(CalculatorAppWidgetProvider.EXTRA_MODE, "ENGINEERING")
            }
            val openPendingIntent = PendingIntent.getActivity(
                context, 801, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_open_formula, openPendingIntent)

            val cycleIntent = Intent(context, WidgetActionReceiver::class.java).apply {
                action = WidgetActionReceiver.ACTION_FORMULA_CYCLE
            }
            val cyclePendingIntent = PendingIntent.getBroadcast(
                context, 802, cycleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_cycle_formula, cyclePendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
