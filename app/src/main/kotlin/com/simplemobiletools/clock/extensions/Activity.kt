package com.simplemobiletools.clock.extensions

import android.app.Activity
import android.media.RingtoneManager
import android.view.WindowManager
import com.simplemobiletools.clock.models.AlarmSound
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.showErrorToast
import com.simplemobiletools.commons.helpers.PERMISSION_READ_STORAGE
import java.util.*

fun Activity.showOverLockscreen() {
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
}

fun BaseSimpleActivity.getAlarms(callback: (ArrayList<AlarmSound>) -> Unit) {
    val alarms = ArrayList<AlarmSound>()
    val manager = RingtoneManager(this)
    manager.setType(RingtoneManager.TYPE_ALARM)

    try {
        val cursor = manager.cursor
        val defaultAlarm = AlarmSound(getDefaultAlarmTitle(), getDefaultAlarmUri().toString())
        alarms.add(defaultAlarm)

        while (cursor.moveToNext()) {
            val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
            var uri = cursor.getString(RingtoneManager.URI_COLUMN_INDEX)
            val id = cursor.getString(RingtoneManager.ID_COLUMN_INDEX)
            if (!uri.endsWith(id)) {
                uri += "/$id"
            }
            val alarmSound = AlarmSound(title, uri)
            alarms.add(alarmSound)
        }
        callback(alarms)
    } catch (e: Exception) {
        if (e is SecurityException) {
            handlePermission(PERMISSION_READ_STORAGE) {
                if (it) {
                    getAlarms(callback)
                } else {
                    showErrorToast(e)
                    callback(ArrayList())
                }
            }
        } else {
            showErrorToast(e)
            callback(ArrayList())
        }
    }
}
