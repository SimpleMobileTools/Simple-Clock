package com.simplemobiletools.clock.services

import android.app.IntentService
import android.content.Context
import android.content.Intent
import com.simplemobiletools.clock.extensions.*
import com.simplemobiletools.clock.helpers.ALARM_ID
import com.simplemobiletools.clock.helpers.NOISE_CONTROL_KILL
import com.simplemobiletools.clock.helpers.NOISE_CONTROL_PAUSE
import com.simplemobiletools.clock.helpers.UID_ALARM_NOTIFICATION
import com.simplemobiletools.commons.helpers.MINUTE_SECONDS

class SnoozeService : IntentService("Snooze") {
    override fun onHandleIntent(intent: Intent) {
        val id = intent.getIntExtra(ALARM_ID, -1)
        val alarm = dbHelper.getAlarmWithId(id) ?: return
        hideNotification(id)
        this.sendNoiseControlIntent(UID_ALARM_NOTIFICATION, NOISE_CONTROL_PAUSE, id)
        setupAlarmClock(alarm, config.snoozeTime * MINUTE_SECONDS, false)
    }
}
