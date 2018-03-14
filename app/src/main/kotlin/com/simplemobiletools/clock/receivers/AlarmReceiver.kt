package com.simplemobiletools.clock.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import com.simplemobiletools.clock.activities.ReminderActivity
import com.simplemobiletools.clock.extensions.*
import com.simplemobiletools.clock.helpers.ALARM_ID

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra(ALARM_ID, -1)
        val alarm = context.dbHelper.getAlarmWithId(id) ?: return

        if (context.isScreenOn()) {
            context.showAlarmNotification(alarm)
            Handler().postDelayed({
                context.hideNotification(id)
            }, context.config.alarmMaxReminderSecs * 1000L)
        } else {
            Intent(context, ReminderActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(ALARM_ID, id)
                context.startActivity(this)
            }
        }
    }
}
