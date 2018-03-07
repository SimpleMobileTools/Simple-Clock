package com.simplemobiletools.clock.extensions

import java.util.concurrent.TimeUnit

fun Long.formatStopwatchTime(): String {
    val hours = TimeUnit.MILLISECONDS.toHours(this)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(this) - TimeUnit.HOURS.toMinutes(hours)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(this) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(this))
    val ms = (this % 1000) / 10

    return when {
        hours > 0 -> {
            val format = "%02d:%02d:%02d.%02d"
            String.format(format, hours, minutes, seconds, ms)
        }
        minutes > 0 -> {
            val format = "%02d:%02d.%02d"
            String.format(format, minutes, seconds, ms)
        }
        else -> {
            val format = "%02d.%02d"
            String.format(format, seconds, ms)
        }
    }
}
