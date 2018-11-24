package com.simplemobiletools.clock.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.simplemobiletools.clock.extensions.hideTimerNotification
import com.simplemobiletools.clock.extensions.sendNoiseControlIntent
import com.simplemobiletools.clock.helpers.NOISE_CONTROL_KILL
import com.simplemobiletools.clock.helpers.UID_TIMER_NOTIFICATION

class HideTimerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        context.hideTimerNotification()
        context.sendNoiseControlIntent(UID_TIMER_NOTIFICATION, NOISE_CONTROL_KILL, null)
    }
}
