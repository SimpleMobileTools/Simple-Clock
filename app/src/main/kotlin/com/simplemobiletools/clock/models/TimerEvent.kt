package com.simplemobiletools.clock.models

sealed class TimerEvent(open val timerId: Int) {
    data class Reset(override val timerId: Int, val duration: Long) : TimerEvent(timerId)
    data class Start(override val timerId: Int, val duration: Long) : TimerEvent(timerId)
    data class Pause(override val timerId: Int, val duration: Long) : TimerEvent(timerId)
    data class Finish(override val timerId: Int, val duration: Long) : TimerEvent(timerId)
    data class Refresh(override val timerId: Int) : TimerEvent(timerId)
}
