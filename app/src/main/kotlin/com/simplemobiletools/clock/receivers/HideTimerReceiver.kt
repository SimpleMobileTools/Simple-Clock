package com.simplemobiletools.clock.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.simplemobiletools.clock.extensions.hideTimerNotification
import com.simplemobiletools.clock.models.TimerState
import org.greenrobot.eventbus.EventBus

class HideTimerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        context.hideTimerNotification()
        EventBus.getDefault().post(TimerState.Idle)
    }
}
