package com.simplemobiletools.clock.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.simplemobiletools.clock.extensions.dbHelper
import com.simplemobiletools.clock.extensions.scheduleNextAlarm

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        context.dbHelper.getAlarms().filter { it.isEnabled }.forEach {
            context.scheduleNextAlarm(it, false)
        }
    }
}
