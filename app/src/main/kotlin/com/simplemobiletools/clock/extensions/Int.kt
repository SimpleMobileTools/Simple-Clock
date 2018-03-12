package com.simplemobiletools.clock.extensions

fun Int.formatAlarmTime(): String {
    val hours = this / 60
    val minutes = this % 60
    val format = "%02d:%02d"
    return String.format(format, hours, minutes)
}
