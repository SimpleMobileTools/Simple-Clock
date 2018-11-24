package com.simplemobiletools.clock.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import com.simplemobiletools.clock.extensions.sendNoiseControlIntent
import com.simplemobiletools.clock.helpers.NOISE_CONTROL_KILL
import com.simplemobiletools.clock.helpers.NOISE_CONTROL_PAUSE
import com.simplemobiletools.clock.helpers.UID_ALARM_NOTIFICATION
import com.simplemobiletools.clock.helpers.UID_TIMER_NOTIFICATION

class HeadphonesUnplugged: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val headsetIn = audioManager.isWiredHeadsetOn
        if (!headsetIn){
            context.sendNoiseControlIntent(UID_TIMER_NOTIFICATION, NOISE_CONTROL_KILL, null)
            Thread.sleep(300) // Buffer until the noise is started on an alarm
            context.sendNoiseControlIntent(UID_ALARM_NOTIFICATION, NOISE_CONTROL_PAUSE, -1)
        }
    }
}