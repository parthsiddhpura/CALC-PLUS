package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R

class GstWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val theme = WidgetDataManager.getThemeFamily(context)
            val views = RemoteViews(context.packageName, R.layout.widget_gst)

            theme.applyRoot(views, R.id.widget_root)
            theme.applyScreen(views, R.id.btn_open_gst_calc)
            theme.applyButton(views, R.id.btn_cycle_gst_rate, isAccent = true)
            theme.applyButton(views, R.id.btn_gst_add, isAccent = true)
            theme.applyButton(views, R.id.btn_gst_sub, isAccent = false)
            theme.applyButton(views, R.id.btn_gst_amount_inc, isAccent = false)

            views.setTextColor(R.id.widget_title, theme.headerTextColorHex)
            views.setTextColor(R.id.gst_base_text, theme.subtextColorHex)
            views.setTextColor(R.id.gst_rate_label, theme.accentColorHex)
            views.setTextColor(R.id.gst_total_text, theme.textColorHex)

            val amount = WidgetDataManager.getGstAmount(context)
            val rate = WidgetDataManager.getGstRate(context)
            val isAdd = WidgetDataManager.isGstAdd(context)
            val total = WidgetDataManager.getGstCalculatedTotal(context)

            views.setTextViewText(R.id.gst_base_text, WidgetDataManager.formatCurrency(amount))
            views.setTextViewText(R.id.gst_rate_label, "GST ${if (isAdd) "+" else "-"}${rate.toInt()}%")
            views.setTextViewText(R.id.gst_total_text, WidgetDataManager.formatCurrency(total))
            views.setTextViewText(R.id.btn_cycle_gst_rate, "${rate.toInt()}% ▾")

            val editIntent = Intent(context, WidgetValueEditorActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(WidgetValueEditorActivity.EXTRA_WIDGET_TYPE, "GST")
            }
            val editPendingIntent = PendingIntent.getActivity(
                context, 501, editIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_open_gst_calc, editPendingIntent)

            val addIntent = Intent(context, WidgetActionReceiver::class.java).apply {
                action = WidgetActionReceiver.ACTION_GST_TOGGLE_MODE
            }
            val addPendingIntent = PendingIntent.getBroadcast(
                context, 502, addIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_gst_add, addPendingIntent)

            val subIntent = Intent(context, WidgetActionReceiver::class.java).apply {
                action = WidgetActionReceiver.ACTION_GST_TOGGLE_MODE
            }
            val subPendingIntent = PendingIntent.getBroadcast(
                context, 503, subIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_gst_sub, subPendingIntent)

            val rateIntent = Intent(context, WidgetActionReceiver::class.java).apply {
                action = WidgetActionReceiver.ACTION_GST_CYCLE_RATE
            }
            val ratePendingIntent = PendingIntent.getBroadcast(
                context, 504, rateIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_cycle_gst_rate, ratePendingIntent)

            val incIntent = Intent(context, WidgetActionReceiver::class.java).apply {
                action = WidgetActionReceiver.ACTION_GST_ADJUST_AMOUNT
                putExtra(WidgetActionReceiver.EXTRA_DELTA, 1000.0)
            }
            val incPendingIntent = PendingIntent.getBroadcast(
                context, 505, incIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_gst_amount_inc, incPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
