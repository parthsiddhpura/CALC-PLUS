package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R

class VariableWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val theme = WidgetDataManager.getThemeFamily(context)
            val views = RemoteViews(context.packageName, R.layout.widget_variable)

            theme.applyRoot(views, R.id.widget_root)
            theme.applyThemeCycleButton(views, R.id.btn_theme_cycle)
            views.setTextColor(R.id.widget_title, theme.headerTextColorHex)

            val variables = WidgetDataManager.getMyValues(context)
            val rows = listOf(
                Pair(R.id.btn_var_1, Pair(R.id.var_label_1, R.id.var_val_1)),
                Pair(R.id.btn_var_2, Pair(R.id.var_label_2, R.id.var_val_2)),
                Pair(R.id.btn_var_3, Pair(R.id.var_label_3, R.id.var_val_3)),
                Pair(R.id.btn_var_4, Pair(R.id.var_label_4, R.id.var_val_4))
            )

            for (i in rows.indices) {
                val (rowLayoutId, textPair) = rows[i]
                theme.applyScreen(views, rowLayoutId)
                views.setTextColor(textPair.first, theme.subtextColorHex)
                views.setTextColor(textPair.second, theme.accentColorHex)

                if (i in variables.indices) {
                    val v = variables[i]
                    views.setTextViewText(textPair.first, v.label)
                    views.setTextViewText(textPair.second, v.displayValue)

                    val numStr = if (v.numericValue % 1.0 == 0.0) {
                        v.numericValue.toLong().toString()
                    } else {
                        v.numericValue.toString()
                    }
                    val intent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra(CalculatorAppWidgetProvider.EXTRA_MODE, "STANDARD")
                        putExtra(WidgetActionReceiver.EXTRA_INITIAL_EXPR, numStr)
                    }
                    val pi = PendingIntent.getActivity(
                        context, 900 + i, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(rowLayoutId, pi)
                }
            }

            val themeIntent = Intent(context, WidgetActionReceiver::class.java).apply {
                action = WidgetActionReceiver.ACTION_CYCLE_THEME
            }
            val themePendingIntent = PendingIntent.getBroadcast(
                context, 910, themeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_theme_cycle, themePendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
