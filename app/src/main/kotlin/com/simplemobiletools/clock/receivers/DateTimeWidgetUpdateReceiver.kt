package com.simplemobiletools.clock.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.simplemobiletools.clock.extensions.scheduleNextWidgetUpdate
import com.simplemobiletools.clock.extensions.updateWidgets

class DateTimeWidgetUpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        context.updateWidgets()
        context.scheduleNextWidgetUpdate()
    }
}
