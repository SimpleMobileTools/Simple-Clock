package com.simplemobiletools.clock.models

data class Alarm(val id: Int, var timeInMinutes: Int, var days: Int, var isEnabled: Boolean, var vibrate: Boolean, var soundUri: String, var label: String)
