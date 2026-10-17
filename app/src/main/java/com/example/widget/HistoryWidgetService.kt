package com.example.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.R

class HistoryWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return HistoryRemoteViewsFactory(this.applicationContext)
    }
}

class HistoryRemoteViewsFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {
    private var items: List<String> = emptyList()

    override fun onCreate() {
        items = WidgetDataManager.getHistoryList(context)
    }

    override fun onDataSetChanged() {
        items = WidgetDataManager.getHistoryList(context)
    }

    override fun onDestroy() {
        items = emptyList()
    }

    override fun getCount(): Int = items.size

    override fun getViewAt(position: Int): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_history_item)
        if (position in items.indices) {
            val item = items[position]
            views.setTextViewText(R.id.history_item_text, item)

            val theme = WidgetDataManager.getDynamicTheme(context)
            views.setTextColor(R.id.history_item_text, theme.textColorHex)
            views.setTextColor(R.id.history_item_bullet, theme.accentColorHex)
            views.setTextColor(R.id.history_item_action, theme.accentColorHex)

            // Fill-in Intent to send clicked calculation to MainActivity
            val fillInIntent = Intent().apply {
                putExtra(WidgetActionReceiver.EXTRA_INITIAL_EXPR, item)
                putExtra(CalculatorAppWidgetProvider.EXTRA_MODE, "STANDARD")
            }
            views.setOnClickFillInIntent(R.id.history_item_root, fillInIntent)
        }
        return views
    }

    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount(): Int = 1
    override fun getItemId(position: Int): Long = position.toLong()
    override fun hasStableIds(): Boolean = true
}
