package com.simplemobiletools.clock.models

sealed interface AlarmEvent {
    object Refresh : AlarmEvent
}
