package com.simplemobiletools.clock.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.simplemobiletools.clock.extensions.hideNotification
import com.simplemobiletools.clock.extensions.sendNoiseControlIntent
import com.simplemobiletools.clock.helpers.ALARM_ID
import com.simplemobiletools.clock.helpers.NOISE_CONTROL_KILL
import com.simplemobiletools.clock.helpers.UID_ALARM_NOTIFICATION

class HideAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra(ALARM_ID, -1)
        context.hideNotification(id)
        context.sendNoiseControlIntent(UID_ALARM_NOTIFICATION, NOISE_CONTROL_KILL, id)
    }
}
