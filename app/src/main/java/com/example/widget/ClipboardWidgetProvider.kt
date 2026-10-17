package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R

class ClipboardWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val theme = WidgetDataManager.getThemeFamily(context)
            val views = RemoteViews(context.packageName, R.layout.widget_clipboard)

            theme.applyRoot(views, R.id.widget_root)
            theme.applyScreen(views, R.id.btn_clipboard_card)
            theme.applyButton(views, R.id.btn_clip_calc, isAccent = true)
            theme.applyButton(views, R.id.btn_clip_convert, isAccent = false)
            theme.applyButton(views, R.id.btn_clip_gst, isAccent = false)
            theme.applyButton(views, R.id.btn_clip_pct, isAccent = false)
            theme.applyButton(views, R.id.btn_clip_save, isAccent = false)
            theme.applyButton(views, R.id.btn_clip_share, isAccent = false)

            views.setTextColor(R.id.clipboard_val_text, theme.textColorHex)

            val text = WidgetDataManager.getClipboardText(context)
            views.setTextViewText(R.id.clipboard_val_text, text)

            val cardIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(CalculatorAppWidgetProvider.EXTRA_MODE, "STANDARD")
                putExtra(WidgetActionReceiver.EXTRA_INITIAL_EXPR, WidgetDataManager.getClipboardNumber(context))
            }
            val cardPi = PendingIntent.getActivity(
                context, 1101, cardIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_clipboard_card, cardPi)

            bindAction(context, views, R.id.btn_clip_calc, "CALCULATE", 1110)
            bindAction(context, views, R.id.btn_clip_convert, "CONVERT", 1111)
            bindAction(context, views, R.id.btn_clip_gst, "GST", 1112)
            bindAction(context, views, R.id.btn_clip_pct, "PERCENTAGE", 1113)
            bindAction(context, views, R.id.btn_clip_save, "SAVE", 1114)
            bindAction(context, views, R.id.btn_clip_share, "SHARE", 1115)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun bindAction(context: Context, views: RemoteViews, viewId: Int, target: String, reqCode: Int) {
            val intent = Intent(context, WidgetActionReceiver::class.java).apply {
                action = WidgetActionReceiver.ACTION_CLIPBOARD_DO
                putExtra(WidgetActionReceiver.EXTRA_CLIPBOARD_TARGET, target)
            }
            val pi = PendingIntent.getBroadcast(
                context, reqCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(viewId, pi)
        }
    }
}
