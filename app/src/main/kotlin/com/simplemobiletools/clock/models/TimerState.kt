package com.simplemobiletools.clock.models

sealed class TimerState {
    object Idle : TimerState()
    data class Start(val duration: Long) : TimerState()
    data class Running(val duration: Long, val tick: Long) : TimerState()
    data class Pause(val duration: Long) : TimerState()
    data class Paused(val duration: Long, val tick: Long) : TimerState()
    data class Finish(val duration: Long) : TimerState()
    object Finished : TimerState()
}
