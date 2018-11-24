package com.simplemobiletools.clock.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.simplemobiletools.clock.helpers.*

class NoiseControlReceiver(val systemSound: SystemSound, val uid: String, val alarmId: Int?) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmIdRight =
                if (intent.getIntExtra(ALARM_ID,-5) == -1)
                    true
                else if (alarmId != null)
                    intent.getIntExtra(ALARM_ID,-5) == alarmId
                else true
        val correctReceiver = intent.hasExtra(NOISE_CONTROL_UID) &&
                intent.getStringExtra(NOISE_CONTROL_UID) == uid &&
                alarmIdRight
        if (correctReceiver ){
            when (intent.getStringExtra(NOISE_CONTROL_COMMAND)){
                NOISE_CONTROL_START -> systemSound.start()
                NOISE_CONTROL_KILL -> {
                    systemSound.kill()
                    context.unregisterReceiver(this)
                }
                NOISE_CONTROL_PAUSE -> systemSound.pause()
                else -> {}
            }

        }
    }
}