package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R

class EmiWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val theme = WidgetDataManager.getThemeFamily(context)
            val views = RemoteViews(context.packageName, R.layout.widget_emi)

            theme.applyRoot(views, R.id.widget_root)
            theme.applyScreen(views, R.id.btn_open_emi_calc)
            theme.applyThemeCycleButton(views, R.id.btn_theme_cycle)
            theme.applyButton(views, R.id.btn_emi_tenure_sub, isAccent = false)
            theme.applyButton(views, R.id.btn_emi_tenure_add, isAccent = false)
            theme.applyButton(views, R.id.btn_emi_rate_sub, isAccent = false)
            theme.applyButton(views, R.id.btn_emi_rate_add, isAccent = true)

            views.setTextColor(R.id.widget_title, theme.headerTextColorHex)
            views.setTextColor(R.id.emi_principal_text, theme.textColorHex)
            views.setTextColor(R.id.emi_terms_text, theme.subtextColorHex)
            views.setTextColor(R.id.emi_result_text, theme.accentColorHex)

            val principal = WidgetDataManager.getEmiPrincipal(context)
            val rate = WidgetDataManager.getEmiRate(context)
            val tenure = WidgetDataManager.getEmiTenureYears(context)
            val emi = WidgetDataManager.calculateEmi(context)

            views.setTextViewText(R.id.emi_principal_text, WidgetDataManager.formatCurrency(principal))
            views.setTextViewText(R.id.emi_terms_text, "${WidgetDataManager.formatNum(rate)}% • $tenure Years")
            views.setTextViewText(R.id.emi_result_text, "${WidgetDataManager.formatCurrency(emi)} / mo")

            val editIntent = Intent(context, WidgetValueEditorActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(WidgetValueEditorActivity.EXTRA_WIDGET_TYPE, "EMI")
            }
            val editPendingIntent = PendingIntent.getActivity(
                context, 601, editIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_open_emi_calc, editPendingIntent)

            val tenureSubIntent = Intent(context, WidgetActionReceiver::class.java).apply {
                action = WidgetActionReceiver.ACTION_EMI_ADJUST_TENURE
                putExtra(WidgetActionReceiver.EXTRA_DELTA_INT, -1)
            }
            val tenureSubPendingIntent = PendingIntent.getBroadcast(
                context, 602, tenureSubIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_emi_tenure_sub, tenureSubPendingIntent)

            val tenureAddIntent = Intent(context, WidgetActionReceiver::class.java).apply {
                action = WidgetActionReceiver.ACTION_EMI_ADJUST_TENURE
                putExtra(WidgetActionReceiver.EXTRA_DELTA_INT, 1)
            }
            val tenureAddPendingIntent = PendingIntent.getBroadcast(
                context, 603, tenureAddIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_emi_tenure_add, tenureAddPendingIntent)

            val rateSubIntent = Intent(context, WidgetActionReceiver::class.java).apply {
                action = WidgetActionReceiver.ACTION_EMI_ADJUST_RATE
                putExtra(WidgetActionReceiver.EXTRA_DELTA, -0.5)
            }
            val rateSubPendingIntent = PendingIntent.getBroadcast(
                context, 604, rateSubIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_emi_rate_sub, rateSubPendingIntent)

            val rateAddIntent = Intent(context, WidgetActionReceiver::class.java).apply {
                action = WidgetActionReceiver.ACTION_EMI_ADJUST_RATE
                putExtra(WidgetActionReceiver.EXTRA_DELTA, 0.5)
            }
            val rateAddPendingIntent = PendingIntent.getBroadcast(
                context, 605, rateAddIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_emi_rate_add, rateAddPendingIntent)

            val themeIntent = Intent(context, WidgetActionReceiver::class.java).apply {
                action = WidgetActionReceiver.ACTION_CYCLE_THEME
            }
            val themePendingIntent = PendingIntent.getBroadcast(
                context, 606, themeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_theme_cycle, themePendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
