package com.simplemobiletools.clock.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.simplemobiletools.clock.extensions.hideNotification
import com.simplemobiletools.clock.helpers.TIMER_NOTIF_ID

class TimerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        context.hideNotification(TIMER_NOTIF_ID)
    }
}
