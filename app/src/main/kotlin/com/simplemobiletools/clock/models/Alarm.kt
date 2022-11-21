package com.simplemobiletools.clock.models

data class Alarm(var id: Int,var pid: Int, var timeInMinutes: Int, var days: Int, var isEnabled: Boolean, var isDismissed: Boolean, var vibrate: Boolean, var soundTitle: String,
                 var soundUri: String, var label: String)
