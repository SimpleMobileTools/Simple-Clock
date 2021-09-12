package com.simplemobiletools.clock.helpers

import android.content.Context
import com.simplemobiletools.clock.extensions.timerDb
import com.simplemobiletools.clock.models.Timer
import com.simplemobiletools.commons.helpers.ensureBackgroundThread

class TimerHelper(val context: Context) {
    private val timerDao = context.timerDb

    fun getTimers(callback: (timers: List<Timer>) -> Unit) {
        ensureBackgroundThread {
            callback.invoke(timerDao.getTimers())
        }
    }

    fun getTimer(timerId: Int, callback: (timer: Timer) -> Unit) {
        ensureBackgroundThread {
            callback.invoke(timerDao.getTimer(timerId))
        }
    }

    fun insertOrUpdateTimer(timer: Timer, callback: () -> Unit = {}) {
        ensureBackgroundThread {
            timerDao.insertOrUpdateTimer(timer)
            callback.invoke()
        }
    }

    fun deleteTimer(id: Int, callback: () -> Unit = {}) {
        ensureBackgroundThread {
            timerDao.deleteTimer(id)
            callback.invoke()
        }
    }

    fun deleteTimers(timers: List<Timer>, callback: () -> Unit = {}) {
        ensureBackgroundThread {
            timerDao.deleteTimers(timers)
            callback.invoke()
        }
    }
}
