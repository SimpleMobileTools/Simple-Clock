package com.simplemobiletools.clock.helpers

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.activities.SplashActivity
import com.simplemobiletools.clock.extensions.config
import com.simplemobiletools.clock.extensions.getFormattedDate
import com.simplemobiletools.clock.extensions.getFormattedTime
import com.simplemobiletools.clock.extensions.scheduleNextWidgetUpdate
import com.simplemobiletools.commons.extensions.setBackgroundColor
import com.simplemobiletools.commons.extensions.setText
import java.util.*

class MyWidgetDateTimeProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        performUpdate(context)
        context.scheduleNextWidgetUpdate()
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        context.scheduleNextWidgetUpdate()
    }

    private fun performUpdate(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        appWidgetManager.getAppWidgetIds(getComponentName(context)).forEach {
            val layout = if (context.config.useTextShadow) R.layout.widget_date_time_with_shadow else R.layout.widget_date_time
            RemoteViews(context.packageName, layout).apply {
                updateTexts(context, this)
                updateColors(context, this)
                setupAppOpenIntent(context, this)
                appWidgetManager.updateAppWidget(it, this)
            }
        }
    }

    private fun updateTexts(context: Context, views: RemoteViews) {
        val calendar = Calendar.getInstance()
        views.apply {
            setText(R.id.widget_time, getPassedSeconds().getFormattedTime(false))
            setText(R.id.widget_date, context.getFormattedDate(calendar))
        }
    }

    private fun updateColors(context: Context, views: RemoteViews) {
        val config = context.config
        val widgetBgColor = config.widgetBgColor
        val widgetTextColor = config.widgetTextColor

        views.apply {
            setBackgroundColor(R.id.widget_date_time_holder, widgetBgColor)
            setTextColor(R.id.widget_time, widgetTextColor)
            setTextColor(R.id.widget_date, widgetTextColor)
        }
    }

    private fun getComponentName(context: Context) = ComponentName(context, this::class.java)

    private fun setupAppOpenIntent(context: Context, views: RemoteViews) {
        Intent(context, SplashActivity::class.java).apply {
            putExtra(OPEN_TAB, TAB_CLOCK)
            val pendingIntent = PendingIntent.getActivity(context, OPEN_APP_INTENT_ID, this, PendingIntent.FLAG_UPDATE_CURRENT)
            views.setOnClickPendingIntent(R.id.widget_date_time_holder, pendingIntent)
        }
    }
}
