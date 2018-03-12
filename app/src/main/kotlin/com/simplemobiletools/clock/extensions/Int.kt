package com.simplemobiletools.clock.extensions

fun Int.formatAlarmTime(): String {
    val hours = this / 60
    val minutes = this % 60
    val format = "%02d:%02d"
    return String.format(format, hours, minutes)
}

fun Int.getFormattedTime(showSeconds: Boolean): String {
    val hours = (this / 3600) % 24
    val minutes = (this / 60) % 60
    val seconds = this % 60
    var format = "%02d:%02d"

    return if (showSeconds) {
        format += ":%02d"
        String.format(format, hours, minutes, seconds)
    } else {
        String.format(format, hours, minutes)
    }
}
