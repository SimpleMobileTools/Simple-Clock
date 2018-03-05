package com.simplemobiletools.clock.extensions

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.helpers.*
import com.simplemobiletools.clock.models.Alarm
import com.simplemobiletools.clock.models.AlarmSound
import com.simplemobiletools.clock.models.MyTimeZone
import java.util.*

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
    val defaultAlarm = AlarmSound(getDefaultAlarmTitle(this), getDefaultAlarmUri().toString())
    alarms.add(defaultAlarm)

    while (cursor.moveToNext()) {
        val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
        val uri = Uri.parse("${cursor.getString(RingtoneManager.URI_COLUMN_INDEX)}/${cursor.getString(RingtoneManager.ID_COLUMN_INDEX)}").toString()
        val alarmSound = AlarmSound(title, uri)
        alarms.add(alarmSound)
    }

    return alarms
}

private fun getDefaultAlarmUri() = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

private fun getDefaultAlarmTitle(context: Context) = RingtoneManager.getRingtone(context, getDefaultAlarmUri()).getTitle(context)

fun Context.createNewAlarm(timeInMinutes: Int, weekDays: Int) = Alarm(0, timeInMinutes, weekDays, false, false, getDefaultAlarmTitle(this), getDefaultAlarmUri().toString(), "")
