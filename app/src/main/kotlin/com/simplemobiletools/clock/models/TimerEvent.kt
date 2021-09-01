package com.simplemobiletools.clock.models

sealed class TimerEvent(open val timerId: Long) {
    data class Reset(override val timerId: Long, val duration: Long) : TimerEvent(timerId)
    data class Start(override val timerId: Long, val duration: Long) : TimerEvent(timerId)
    data class Pause(override val timerId: Long, val duration: Long) : TimerEvent(timerId)
    data class Finish(override val timerId: Long, val duration: Long) : TimerEvent(timerId)
    data class Refresh(override val timerId: Long) : TimerEvent(timerId)
}
