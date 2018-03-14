package com.simplemobiletools.clock.services

import android.app.IntentService
import android.content.Intent
import com.simplemobiletools.clock.activities.ReminderActivity
import com.simplemobiletools.clock.extensions.config
import com.simplemobiletools.clock.extensions.dbHelper
import com.simplemobiletools.clock.extensions.hideNotification
import com.simplemobiletools.clock.extensions.setupAlarmClock
import com.simplemobiletools.clock.helpers.ALARM_ID
import com.simplemobiletools.clock.helpers.HIDE_REMINDER_ACTIVITY

class SnoozeService : IntentService("Snooze") {
    override fun onHandleIntent(intent: Intent) {
        val id = intent.getIntExtra(ALARM_ID, -1)
        val alarm = dbHelper.getAlarmWithId(id) ?: return
        hideNotification(id)
        setupAlarmClock(alarm, config.snoozeTime * 60)

        if (intent.getBooleanExtra(HIDE_REMINDER_ACTIVITY, false)) {
            Intent(this, ReminderActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(this)
            }
        }
    }
}
