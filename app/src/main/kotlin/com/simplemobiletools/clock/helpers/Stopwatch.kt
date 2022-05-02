package com.simplemobiletools.clock.helpers

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import com.simplemobiletools.clock.models.Lap
import java.util.concurrent.CopyOnWriteArraySet

private const val UPDATE_INTERVAL = 10L

object Stopwatch {

    private val updateHandler = Handler(Looper.getMainLooper())
    private var uptimeAtStart = 0L
    private var totalTicks = 0
    private var currentTicks = 0    // ticks that reset at pause
    private var lapTicks = 0
    private var currentLap = 1
    val laps = ArrayList<Lap>()
    var state = State.STOPPED
        private set(value) {
            field = value
            for (listener in updateListeners) {
                listener.onStateChanged(value)
            }
        }
    private var updateListeners = CopyOnWriteArraySet<UpdateListener>()

    fun reset() {
        updateHandler.removeCallbacksAndMessages(null)
        state = State.STOPPED
        currentTicks = 0
        totalTicks = 0
        currentLap = 1
        lapTicks = 0
        laps.clear()
    }

    fun toggle(setUptimeAtStart: Boolean) {
        if (state != State.RUNNING) {
            state = State.RUNNING
            updateHandler.post(updateRunnable)
            if (setUptimeAtStart) {
                uptimeAtStart = SystemClock.uptimeMillis()
            }
        } else {
            state = State.PAUSED
            val prevSessionsMS = (totalTicks - currentTicks) * UPDATE_INTERVAL
            val totalDuration = SystemClock.uptimeMillis() - uptimeAtStart + prevSessionsMS
            updateHandler.removeCallbacksAndMessages(null)
            currentTicks = 0
            totalTicks--
            for (listener in updateListeners) {
                listener.onUpdate(totalDuration, -1, true)
            }
        }
    }

    fun lap() {
        if (laps.isEmpty()) {
            val lap = Lap(currentLap++, lapTicks * UPDATE_INTERVAL, totalTicks * UPDATE_INTERVAL)
            laps.add(0, lap)
            lapTicks = 0
        } else {
            laps.first().apply {
                lapTime = lapTicks * UPDATE_INTERVAL
                totalTime = totalTicks * UPDATE_INTERVAL
            }
        }

        val lap = Lap(currentLap++, lapTicks * UPDATE_INTERVAL, totalTicks * UPDATE_INTERVAL)
        laps.add(0, lap)
        lapTicks = 0
    }

    /**
     * Add a update listener to the stopwatch. The listener gets the current state
     * immediately after adding. To avoid memory leaks the listener should be removed
     * from the stopwatch.
     * @param updateListener the listener
     */
    fun addUpdateListener(updateListener: UpdateListener) {
        updateListeners.add(updateListener)
        updateListener.onUpdate(
            totalTicks * UPDATE_INTERVAL,
            lapTicks * UPDATE_INTERVAL,
            state != State.STOPPED
        )
        updateListener.onStateChanged(state)
    }

    /**
     * Remove the listener from the stopwatch
     * @param updateListener the listener
     */
    fun removeUpdateListener(updateListener: UpdateListener) {
        updateListeners.remove(updateListener)
    }

    private val updateRunnable = object : Runnable {
        override fun run() {
            if (state == State.RUNNING) {
                if (totalTicks % 10 == 0) {
                    for (listener in updateListeners) {
                        listener.onUpdate(
                            totalTicks * UPDATE_INTERVAL,
                            lapTicks * UPDATE_INTERVAL,
                            false
                        )
                    }
                }
                totalTicks++
                currentTicks++
                lapTicks++
                updateHandler.postAtTime(this, uptimeAtStart + currentTicks * UPDATE_INTERVAL)
            }
        }
    }

    enum class State {
        RUNNING,
        PAUSED,
        STOPPED
    }

    interface UpdateListener {
        fun onUpdate(totalTime: Long, lapTime: Long, useLongerMSFormat: Boolean)
        fun onStateChanged(state: State)
    }
}
