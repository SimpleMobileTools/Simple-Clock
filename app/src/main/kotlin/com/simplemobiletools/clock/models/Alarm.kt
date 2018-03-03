package com.simplemobiletools.clock.models

data class Alarm(val timeInMinutes: Int, var days: Int, val isEnabled: Boolean, val vibrate: Boolean, val soundUri: String, val label: String)
