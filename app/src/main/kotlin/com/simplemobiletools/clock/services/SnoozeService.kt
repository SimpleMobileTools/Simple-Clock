package com.simplemobiletools.clock.services

import android.app.IntentService
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import com.simplemobiletools.clock.extensions.config
import com.simplemobiletools.clock.extensions.dbHelper
import com.simplemobiletools.clock.extensions.setupAlarmClock
import com.simplemobiletools.clock.helpers.ALARM_ID

class SnoozeService : IntentService("Snooze") {
    override fun onHandleIntent(intent: Intent) {
        val id = intent.getIntExtra(ALARM_ID, -1)
        val alarm = dbHelper.getAlarmWithId(id) ?: return
        setupAlarmClock(alarm, config.snoozeTime * 60)

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(alarm.id)
    }
}
