package com.simplemobiletools.clock.helpers

import android.content.Context
import com.simplemobiletools.clock.extensions.gson.gson
import com.simplemobiletools.clock.models.Alarm
import com.simplemobiletools.clock.models.StateWrapper
import com.simplemobiletools.clock.models.TimerState
import com.simplemobiletools.commons.extensions.getDefaultAlarmTitle
import com.simplemobiletools.commons.extensions.getDefaultAlarmUri
import com.simplemobiletools.commons.helpers.ALARM_SOUND_TYPE_ALARM
import com.simplemobiletools.commons.helpers.BaseConfig

class Config(context: Context) : BaseConfig(context) {
    companion object {
        fun newInstance(context: Context) = Config(context)
    }

    var showSeconds: Boolean
        get() = prefs.getBoolean(SHOW_SECONDS, true)
        set(showSeconds) = prefs.edit().putBoolean(SHOW_SECONDS, showSeconds).apply()

    var selectedTimeZones: Set<String>
        get() = prefs.getStringSet(SELECTED_TIME_ZONES, HashSet())!!
        set(selectedTimeZones) = prefs.edit().putStringSet(SELECTED_TIME_ZONES, selectedTimeZones).apply()

    var editedTimeZoneTitles: Set<String>
        get() = prefs.getStringSet(EDITED_TIME_ZONE_TITLES, HashSet())!!
        set(editedTimeZoneTitles) = prefs.edit().putStringSet(EDITED_TIME_ZONE_TITLES, editedTimeZoneTitles).apply()

    var timerSeconds: Int
        get() = prefs.getInt(TIMER_SECONDS, 300)
        set(lastTimerSeconds) = prefs.edit().putInt(TIMER_SECONDS, lastTimerSeconds).apply()

    var timerState: TimerState
        get() = prefs.getString(TIMER_STATE, null)?.let { state ->
            gson.fromJson(state, StateWrapper::class.java)
        }?.state ?: TimerState.Idle
        set(state) = prefs.edit().putString(TIMER_STATE, gson.toJson(StateWrapper(state))).apply()

    var timerVibrate: Boolean
        get() = prefs.getBoolean(TIMER_VIBRATE, false)
        set(timerVibrate) = prefs.edit().putBoolean(TIMER_VIBRATE, timerVibrate).apply()

    var timerSoundUri: String
        get() = prefs.getString(TIMER_SOUND_URI, context.getDefaultAlarmUri(ALARM_SOUND_TYPE_ALARM).toString())!!
        set(timerSoundUri) = prefs.edit().putString(TIMER_SOUND_URI, timerSoundUri).apply()

    var timerSoundTitle: String
        get() = prefs.getString(TIMER_SOUND_TITLE, context.getDefaultAlarmTitle(ALARM_SOUND_TYPE_ALARM))!!
        set(timerSoundTitle) = prefs.edit().putString(TIMER_SOUND_TITLE, timerSoundTitle).apply()

    var timerMaxReminderSecs: Int
        get() = prefs.getInt(TIMER_MAX_REMINDER_SECS, DEFAULT_MAX_TIMER_REMINDER_SECS)
        set(timerMaxReminderSecs) = prefs.edit().putInt(TIMER_MAX_REMINDER_SECS, timerMaxReminderSecs).apply()

    var timerLabel: String?
        get() = prefs.getString(TIMER_LABEL, null)
        set(label) = prefs.edit().putString(TIMER_LABEL, label).apply()

    var alarmMaxReminderSecs: Int
        get() = prefs.getInt(ALARM_MAX_REMINDER_SECS, DEFAULT_MAX_ALARM_REMINDER_SECS)
        set(alarmMaxReminderSecs) = prefs.edit().putInt(ALARM_MAX_REMINDER_SECS, alarmMaxReminderSecs).apply()

    var useTextShadow: Boolean
        get() = prefs.getBoolean(USE_TEXT_SHADOW, true)
        set(useTextShadow) = prefs.edit().putBoolean(USE_TEXT_SHADOW, useTextShadow).apply()

    var increaseVolumeGradually: Boolean
        get() = prefs.getBoolean(INCREASE_VOLUME_GRADUALLY, true)
        set(increaseVolumeGradually) = prefs.edit().putBoolean(INCREASE_VOLUME_GRADUALLY, increaseVolumeGradually).apply()

    var alarmLastConfig: Alarm?
        get() = prefs.getString(ALARM_LAST_CONFIG, null)?.let { lastAlarm ->
            gson.fromJson(lastAlarm, Alarm::class.java)
        }
        set(alarm) = prefs.edit().putString(ALARM_LAST_CONFIG, gson.toJson(alarm)).apply()

    var timerChannelId: String?
        get() = prefs.getString(TIMER_CHANNEL_ID, null)
        set(id) = prefs.edit().putString(TIMER_CHANNEL_ID, id).apply()

}
