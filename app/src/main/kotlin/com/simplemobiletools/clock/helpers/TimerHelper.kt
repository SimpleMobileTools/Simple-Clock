package com.simplemobiletools.clock.helpers

import android.content.Context
import android.media.RingtoneManager
import com.simplemobiletools.clock.extensions.timerDb
import com.simplemobiletools.clock.models.Timer
import com.simplemobiletools.clock.models.TimerState
import com.simplemobiletools.commons.extensions.getDefaultAlarmSound
import com.simplemobiletools.commons.extensions.getDefaultAlarmTitle
import com.simplemobiletools.commons.helpers.ensureBackgroundThread

class TimerHelper(val context: Context) {
    private val timerDao = context.timerDb

    fun getTimers(callback: (notes: List<Timer>) -> Unit) {
        ensureBackgroundThread {
            callback.invoke(timerDao.getTimers())
        }
    }

    fun insertOrUpdateTimer(timer: Timer, callback: () -> Unit = {}) {
        ensureBackgroundThread {
            timerDao.insertOrUpdateTimer(timer)
            callback.invoke()
        }
    }

    fun deleteTimer(id: Long, callback: () -> Unit = {}) {
        ensureBackgroundThread {
            timerDao.deleteTimer(id)
            callback.invoke()
        }
    }

    fun insertNewTimer(callback: () -> Unit = {}) {
        ensureBackgroundThread {
            timerDao.insertOrUpdateTimer(
                Timer(id = null,
                    seconds = DEFAULT_TIME,
                    TimerState.Idle,
                    false,
                    context.getDefaultAlarmSound(RingtoneManager.TYPE_ALARM).uri,
                    context.getDefaultAlarmTitle(RingtoneManager.TYPE_ALARM),
                    "",
                    DEFAULT_MAX_TIMER_REMINDER_SECS.toString())
            )

            callback.invoke()
        }
    }
}
