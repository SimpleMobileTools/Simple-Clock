package com.simplemobiletools.clock.helpers

import android.content.Context
import com.simplemobiletools.clock.extensions.config
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

    fun getTimer(timerId: Long, callback: (timer: Timer) -> Unit) {
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

    fun deleteTimer(id: Long, callback: () -> Unit = {}) {
        ensureBackgroundThread {
            timerDao.deleteTimer(id)
            callback.invoke()
        }
    }

    fun insertNewTimer(callback: () -> Unit = {}) {
        ensureBackgroundThread {
            val config = context.config
            timerDao.insertOrUpdateTimer(
                Timer(
                    id = null,
                    seconds = config.timerSeconds,
                    state = config.timerState,
                    vibrate = config.timerVibrate,
                    soundUri = config.timerSoundUri,
                    soundTitle = config.timerSoundTitle,
                    label = config.timerLabel ?: "",
                    createdAt = System.currentTimeMillis(),
                    channelId = config.timerChannelId,
                )
            )
            callback.invoke()
        }
    }
}
