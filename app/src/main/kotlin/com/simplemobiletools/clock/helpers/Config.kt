package com.simplemobiletools.clock.helpers

import android.content.Context
import com.simplemobiletools.commons.helpers.BaseConfig

class Config(context: Context) : BaseConfig(context) {
    companion object {
        fun newInstance(context: Context) = Config(context)
    }

    var showSeconds: Boolean
        get() = prefs.getBoolean(SHOW_SECONDS, true)
        set(showSeconds) = prefs.edit().putBoolean(SHOW_SECONDS, showSeconds).apply()

    var displayOtherTimeZones: Boolean
        get() = prefs.getBoolean(DISPLAY_OTHER_TIME_ZONES, true)
        set(displayOtherTimeZones) = prefs.edit().putBoolean(DISPLAY_OTHER_TIME_ZONES, displayOtherTimeZones).apply()

    var selectedTimeZones: Set<String>
        get() = prefs.getStringSet(SELECTED_TIME_ZONES, HashSet())
        set(selectedTimeZones) = prefs.edit().putStringSet(SELECTED_TIME_ZONES, selectedTimeZones).apply()

    var editedTimeZoneTitles: Set<String>
        get() = prefs.getStringSet(EDITED_TIME_ZONE_TITLES, HashSet())
        set(editedTimeZoneTitles) = prefs.edit().putStringSet(EDITED_TIME_ZONE_TITLES, editedTimeZoneTitles).apply()
}
