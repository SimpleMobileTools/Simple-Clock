package com.simplemobiletools.clock.extensions

import android.content.Context
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.helpers.Config
import java.util.*

val Context.config: Config get() = Config.newInstance(applicationContext)

fun Context.getFormattedDate(calendar: Calendar): String {
    val dayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7    // make sure index 0 means monday
    val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
    val month = calendar.get(Calendar.MONTH)

    val dayString = resources.getStringArray(R.array.week_days)[dayOfWeek]
    val shortDayString = dayString.substring(0, Math.min(3, dayString.length))
    val monthString = resources.getStringArray(R.array.months)[month]
    return "$shortDayString, $dayOfMonth $monthString"
}
