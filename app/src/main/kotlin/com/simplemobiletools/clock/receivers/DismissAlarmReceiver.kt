package com.simplemobiletools.clock.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.simplemobiletools.clock.extensions.*
import com.simplemobiletools.clock.helpers.ALARM_ID
import com.simplemobiletools.clock.helpers.NOTIFICATION_ID
import com.simplemobiletools.clock.models.Alarm
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import java.util.Calendar
import kotlin.math.pow

class DismissAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra(ALARM_ID, -1)
        val notificationId = intent.getIntExtra(NOTIFICATION_ID, -1)
        if (alarmId == -1) {
            return
        }

        context.hideNotification(notificationId)

        ensureBackgroundThread {
            context.dbHelper.getAlarmWithId(alarmId)?.let { alarm ->
                context.cancelAlarmClock(alarm)
                scheduleNextAlarm(alarm, context)
                if (alarm.days < 0) {
                    context.dbHelper.updateAlarmEnabledState(alarm.id, false)
                    context.updateWidgets()
                }
            }
        }
    }

    private fun scheduleNextAlarm(alarm: Alarm, context: Context) {
        val oldBitmask = alarm.days
        alarm.days = removeTodayFromBitmask(oldBitmask)
        context.scheduleNextAlarm(alarm, false)
        alarm.days = oldBitmask
    }

    private fun removeTodayFromBitmask(bitmask: Int): Int {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        val dayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7
        // boolean array with each day of the week set as per the bitmask provided
        val daysOfWeek = BooleanArray(7) { value ->
            var bitValue = bitmask
            for (i in 0 until value) {
                bitValue /= 2
            }
            bitValue % 2 == 1
        }

        daysOfWeek[dayOfWeek] = false // remove today

        // Convert the boolean array back to an integer (bitmask)
        var updatedBitmask = 0
        for (i in 0..6) {
            if (daysOfWeek[i]) {
                updatedBitmask += 2.0.pow(i).toInt()
            }
        }  // This will return a new bitmask without today included

        return updatedBitmask
    }
}
