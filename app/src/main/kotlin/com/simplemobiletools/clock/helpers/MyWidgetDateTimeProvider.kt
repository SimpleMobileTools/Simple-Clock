package com.simplemobiletools.clock.helpers

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.widget.RemoteViews
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.activities.SplashActivity
import com.simplemobiletools.clock.extensions.*
import com.simplemobiletools.commons.extensions.getColoredBitmap
import com.simplemobiletools.commons.extensions.setBackgroundColor
import com.simplemobiletools.commons.extensions.setText
import com.simplemobiletools.commons.extensions.setVisibleIf
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
        val use24HourFormat = context.config.use24HourFormat

        val timeText = context.getFormattedTime(getPassedSeconds(), false, false).toString()
        views.apply {
            if (use24HourFormat) {
                setText(R.id.widget_time, timeText)
            } else {
                val timeParts = timeText.split(" ")
                setText(R.id.widget_time, timeParts[0])
                setText(R.id.widget_time_am_pm, " ${timeParts[1]}")
            }
            setText(R.id.widget_date, context.getFormattedDate(calendar))
            setVisibleIf(R.id.widget_time_am_pm, !use24HourFormat)

            val nextAlarm = getFormattedNextAlarm(context)
            setVisibleIf(R.id.widget_alarm_holder, nextAlarm.isNotEmpty())
            setText(R.id.widget_next_alarm, nextAlarm)
        }
    }

    private fun updateColors(context: Context, views: RemoteViews) {
        val config = context.config
        val widgetBgColor = config.widgetBgColor
        val widgetTextColor = config.widgetTextColor

        views.apply {
            setBackgroundColor(R.id.widget_date_time_holder, widgetBgColor)
            setTextColor(R.id.widget_time, widgetTextColor)
            setTextColor(R.id.widget_time_am_pm, widgetTextColor)
            setTextColor(R.id.widget_date, widgetTextColor)
            setTextColor(R.id.widget_next_alarm, widgetTextColor)

            if (context.config.useTextShadow) {
                val bitmap = getMultiplyColoredBitmap(R.drawable.ic_clock_shadowed, widgetTextColor, context)
                setImageViewBitmap(R.id.widget_next_alarm_image, bitmap)
            } else {
                setImageViewBitmap(R.id.widget_next_alarm_image, context.resources.getColoredBitmap(R.drawable.ic_clock, widgetTextColor))
            }
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

    private fun getFormattedNextAlarm(context: Context): String {
        val nextAlarm = context.getNextAlarm()
        if (nextAlarm.isEmpty()) {
            return ""
        }

        val isIn24HoursFormat = !nextAlarm.endsWith(".")
        return when {
            context.config.use24HourFormat && !isIn24HoursFormat -> {
                val dayTime = nextAlarm.split(" ")
                val times = dayTime[1].split(":")
                val hours = times[0].toInt()
                val minutes = times[1].toInt()
                val seconds = 0
                val isAM = dayTime[2].startsWith("a", true)
                val newHours = when {
                    hours == 12 && isAM -> 0
                    hours == 12 && !isAM -> 12
                    !isAM -> hours + 12
                    else -> hours
                }
                formatTime(false, true, newHours, minutes, seconds)
            }
            !context.config.use24HourFormat && isIn24HoursFormat -> {
                val times = nextAlarm.split(" ")[1].split(":")
                val hours = times[0].toInt()
                val minutes = times[1].toInt()
                val seconds = 0
                context.formatTo12HourFormat(false, hours, minutes, seconds)
            }
            else -> nextAlarm
        }
    }

    fun getMultiplyColoredBitmap(resourceId: Int, newColor: Int, context: Context): Bitmap {
        val options = BitmapFactory.Options()
        options.inMutable = true
        val bmp = BitmapFactory.decodeResource(context.resources, resourceId, options)
        val paint = Paint()
        val filter = PorterDuffColorFilter(newColor, PorterDuff.Mode.MULTIPLY)
        paint.colorFilter = filter
        val canvas = Canvas(bmp)
        canvas.drawBitmap(bmp, 0f, 0f, paint)
        return bmp
    }
}
