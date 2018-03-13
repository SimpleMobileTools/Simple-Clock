package com.simplemobiletools.clock.extensions

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.widget.Toast
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.helpers.*
import com.simplemobiletools.clock.models.Alarm
import com.simplemobiletools.clock.models.AlarmSound
import com.simplemobiletools.clock.models.MyTimeZone
import com.simplemobiletools.clock.receivers.AlarmReceiver
import com.simplemobiletools.clock.receivers.DateTimeWidgetUpdateReceiver
import com.simplemobiletools.commons.extensions.formatMinutesToTimeString
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.helpers.isKitkatPlus
import com.simplemobiletools.commons.helpers.isLollipopPlus
import java.util.*
import kotlin.math.pow

val Context.config: Config get() = Config.newInstance(applicationContext)

val Context.dbHelper: DBHelper get() = DBHelper.newInstance(applicationContext)

fun Context.getFormattedDate(calendar: Calendar): String {
    val dayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7    // make sure index 0 means monday
    val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
    val month = calendar.get(Calendar.MONTH)

    val dayString = resources.getStringArray(R.array.week_days)[dayOfWeek]
    val shortDayString = dayString.substring(0, Math.min(3, dayString.length))
    val monthString = resources.getStringArray(R.array.months)[month]
    return "$shortDayString, $dayOfMonth $monthString"
}

fun Context.getEditedTimeZonesMap(): HashMap<Int, String> {
    val editedTimeZoneTitles = config.editedTimeZoneTitles
    val editedTitlesMap = HashMap<Int, String>()
    editedTimeZoneTitles.forEach {
        val parts = it.split(EDITED_TIME_ZONE_SEPARATOR.toRegex(), 2)
        editedTitlesMap[parts[0].toInt()] = parts[1]
    }
    return editedTitlesMap
}

fun Context.getAllTimeZonesModified(): ArrayList<MyTimeZone> {
    val timeZones = getAllTimeZones()
    val editedTitlesMap = getEditedTimeZonesMap()
    timeZones.forEach {
        if (editedTitlesMap.keys.contains(it.id)) {
            it.title = editedTitlesMap[it.id]!!
        } else {
            it.title = it.title.substring(it.title.indexOf(' ')).trim()
        }
    }
    return timeZones
}

fun Context.getModifiedTimeZoneTitle(id: Int) = getAllTimeZonesModified().firstOrNull { it.id == id }?.title ?: getDefaultTimeZoneTitle(id)

fun Context.getAlarms(): ArrayList<AlarmSound> {
    val manager = RingtoneManager(this)
    manager.setType(RingtoneManager.TYPE_ALARM)
    val cursor = manager.cursor

    val alarms = ArrayList<AlarmSound>()
    val defaultAlarm = AlarmSound(getDefaultAlarmTitle(), getDefaultAlarmUri().toString())
    alarms.add(defaultAlarm)

    while (cursor.moveToNext()) {
        val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
        val uri = Uri.parse("${cursor.getString(RingtoneManager.URI_COLUMN_INDEX)}/${cursor.getString(RingtoneManager.ID_COLUMN_INDEX)}").toString()
        val alarmSound = AlarmSound(title, uri)
        alarms.add(alarmSound)
    }

    return alarms
}

fun Context.getDefaultAlarmUri() = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

fun Context.getDefaultAlarmTitle() = RingtoneManager.getRingtone(this, getDefaultAlarmUri()).getTitle(this)

fun Context.createNewAlarm(timeInMinutes: Int, weekDays: Int) = Alarm(0, timeInMinutes, weekDays, false, false, getDefaultAlarmTitle(), getDefaultAlarmUri().toString(), "")

fun Context.scheduleNextAlarm(alarm: Alarm, showToast: Boolean) {
    val calendar = Calendar.getInstance()
    calendar.firstDayOfWeek = Calendar.MONDAY
    for (i in 0..7) {
        val currentDay = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7
        val isCorrectDay = alarm.days and 2.0.pow(currentDay).toInt() != 0
        val currentTimeInMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        if (isCorrectDay && (alarm.timeInMinutes > currentTimeInMinutes || i > 0)) {
            val triggerInMinutes = alarm.timeInMinutes - currentTimeInMinutes + (i * DAY_MINUTES)
            setupAlarmClock(alarm, triggerInMinutes * 60 - calendar.get(Calendar.SECOND))

            if (showToast) {
                showRemainingTimeMessage(triggerInMinutes)
            }
            break
        } else {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
    }
}

fun Context.showRemainingTimeMessage(totalMinutes: Int) {
    val fullString = String.format(getString(R.string.alarm_goes_off_in), formatMinutesToTimeString(totalMinutes))
    toast(fullString, Toast.LENGTH_LONG)
}

@SuppressLint("NewApi")
fun Context.setupAlarmClock(alarm: Alarm, triggerInSeconds: Int) {
    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val targetMS = System.currentTimeMillis() + triggerInSeconds * 1000
    val pendingIntent = getOpenAppIntent(alarm)

    if (isLollipopPlus()) {
        val info = AlarmManager.AlarmClockInfo(targetMS, pendingIntent)
        alarmManager.setAlarmClock(info, pendingIntent)
    }
}

fun Context.getOpenAppIntent(alarm: Alarm): PendingIntent {
    val intent = Intent(this, AlarmReceiver::class.java)
    intent.putExtra(ALARM_ID, alarm.id)
    return PendingIntent.getBroadcast(this, alarm.id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
}

fun Context.cancelAlarmClock(alarm: Alarm) {
    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.cancel(getOpenAppIntent(alarm))
}

fun Context.hideNotification(id: Int) {
    val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    manager.cancel(id)
}

fun Context.updateWidgets() {
    val widgetsCnt = AppWidgetManager.getInstance(applicationContext).getAppWidgetIds(ComponentName(applicationContext, MyWidgetDateTimeProvider::class.java))
    if (widgetsCnt.isNotEmpty()) {
        val ids = intArrayOf(R.xml.widget_date_time_info)
        Intent(applicationContext, MyWidgetDateTimeProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            sendBroadcast(this)
        }
    }
}

@SuppressLint("NewApi")
fun Context.scheduleNextWidgetUpdate() {
    val widgetsCnt = AppWidgetManager.getInstance(applicationContext).getAppWidgetIds(ComponentName(applicationContext, MyWidgetDateTimeProvider::class.java))
    if (widgetsCnt.isEmpty()) {
        return
    }

    val intent = Intent(this, DateTimeWidgetUpdateReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(this, UPDATE_WIDGET_INTENT_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT)

    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val triggerAtMillis = System.currentTimeMillis() + getMSTillNextMinute()
    when {
        isKitkatPlus() -> alarmManager.setExact(AlarmManager.RTC, triggerAtMillis, pendingIntent)
        else -> alarmManager.set(AlarmManager.RTC, triggerAtMillis, pendingIntent)
    }
}

fun Context.getFormattedTime(passedSeconds: Int, showSeconds: Boolean, makeAmPmSmaller: Boolean): SpannableString {
    val use24HourFormat = config.use24HourFormat
    val hours = (passedSeconds / 3600) % 24
    val minutes = (passedSeconds / 60) % 60
    val seconds = passedSeconds % 60

    return if (!use24HourFormat) {
        val formattedTime = formatTo12HourFormat(showSeconds, hours, minutes, seconds)
        val spannableTime = SpannableString(formattedTime)
        val amPmMultiplier = if (makeAmPmSmaller) 0.4f else 1f
        spannableTime.setSpan(RelativeSizeSpan(amPmMultiplier), spannableTime.length - 5, spannableTime.length, 0)
        spannableTime
    } else {
        val formattedTime = formatTime(showSeconds, use24HourFormat, hours, minutes, seconds)
        SpannableString(formattedTime)
    }
}

fun Context.formatTo12HourFormat(showSeconds: Boolean, hours: Int, minutes: Int, seconds: Int): String {
    val appendable = getString(if (hours >= 12) R.string.p_m else R.string.a_m)
    val newHours = if (hours == 0 || hours == 12) 12 else hours % 12
    return "${formatTime(showSeconds, false, newHours, minutes, seconds)} $appendable"
}
