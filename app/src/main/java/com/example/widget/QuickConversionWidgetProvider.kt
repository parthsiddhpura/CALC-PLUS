package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R

class QuickConversionWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val theme = WidgetDataManager.getThemeFamily(context)
            val views = RemoteViews(context.packageName, R.layout.widget_quick_conversion)

            theme.applyRoot(views, R.id.widget_root)
            theme.applyScreen(views, R.id.btn_open_converter)
            theme.applyThemeCycleButton(views, R.id.btn_theme_cycle)
            theme.applyButton(views, R.id.btn_toggle_unit_type, isAccent = false)
            theme.applyButton(views, R.id.btn_swap_conv, isAccent = true)
            theme.applyButton(views, R.id.btn_open_app_conv, isAccent = false)

            views.setTextColor(R.id.widget_title, theme.headerTextColorHex)
            views.setTextColor(R.id.conv_input_text, theme.textColorHex)
            views.setTextColor(R.id.conv_result_text, theme.accentColorHex)

            val input = WidgetDataManager.getConversionInput(context)
            val result = WidgetDataManager.getConversionResult(context)
            views.setTextViewText(R.id.conv_input_text, input)
            views.setTextViewText(R.id.conv_result_text, result)

            val isUnit = WidgetDataManager.isUnitConversion(context)
            val targetMode = if (isUnit) "UNIT_CONVERTER" else "CURRENCY_CONVERTER"
            val openIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(CalculatorAppWidgetProvider.EXTRA_MODE, targetMode)
            }
            val openPendingIntent = PendingIntent.getActivity(
                context, 401, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_open_converter, openPendingIntent)
            views.setOnClickPendingIntent(R.id.btn_open_app_conv, openPendingIntent)

            val swapIntent = Intent(context, WidgetActionReceiver::class.java).apply {
                action = WidgetActionReceiver.ACTION_CONVERSION_SWAP
            }
            val swapPendingIntent = PendingIntent.getBroadcast(
                context, 402, swapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_swap_conv, swapPendingIntent)

            val modeIntent = Intent(context, WidgetActionReceiver::class.java).apply {
                action = WidgetActionReceiver.ACTION_CONVERSION_TOGGLE_TYPE
            }
            val modePendingIntent = PendingIntent.getBroadcast(
                context, 403, modeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_toggle_unit_type, modePendingIntent)

            val themeIntent = Intent(context, WidgetActionReceiver::class.java).apply {
                action = WidgetActionReceiver.ACTION_CYCLE_THEME
            }
            val themePendingIntent = PendingIntent.getBroadcast(
                context, 404, themeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_theme_cycle, themePendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
