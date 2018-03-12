package com.simplemobiletools.clock.activities

import android.os.Bundle
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.extensions.config
import com.simplemobiletools.clock.extensions.updateWidgets
import com.simplemobiletools.clock.helpers.DEFAULT_MAX_ALARM_REMINDER_SECS
import com.simplemobiletools.clock.helpers.DEFAULT_MAX_TIMER_REMINDER_SECS
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.MINUTE_SECONDS
import kotlinx.android.synthetic.main.activity_settings.*
import java.util.*

class SettingsActivity : SimpleActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }

    override fun onResume() {
        super.onResume()

        setupCustomizeColors()
        setupUseEnglish()
        setupAvoidWhatsNew()
        setupPreventPhoneFromSleeping()
        setupHourFormat()
        setupSundayFirst()
        setupShowSeconds()
        setupDisplayOtherTimeZones()
        setupAlarmMaxReminder()
        setupUseSameSnooze()
        setupSnoozeTime()
        setupVibrate()
        setupTimerMaxReminder()
        setupUseTextShadow()
        updateTextColors(settings_holder)
        setupSectionColors()
    }

    private fun setupSectionColors() {
        val adjustedPrimaryColor = getAdjustedPrimaryColor()
        arrayListOf(clock_tab_label, alarm_tab_label, stopwatch_tab_label, timer_tab_label, widgets_label).forEach {
            it.setTextColor(adjustedPrimaryColor)
        }
    }

    private fun setupCustomizeColors() {
        settings_customize_colors_holder.setOnClickListener {
            startCustomizationActivity()
        }
    }

    private fun setupUseEnglish() {
        settings_use_english_holder.beVisibleIf(config.wasUseEnglishToggled || Locale.getDefault().language != "en")
        settings_use_english.isChecked = config.useEnglish
        settings_use_english_holder.setOnClickListener {
            settings_use_english.toggle()
            config.useEnglish = settings_use_english.isChecked
            useEnglishToggled()
        }
    }

    private fun setupAvoidWhatsNew() {
        settings_avoid_whats_new.isChecked = config.avoidWhatsNew
        settings_avoid_whats_new_holder.setOnClickListener {
            settings_avoid_whats_new.toggle()
            config.avoidWhatsNew = settings_avoid_whats_new.isChecked
        }
    }

    private fun setupPreventPhoneFromSleeping() {
        settings_prevent_phone_from_sleeping.isChecked = config.preventPhoneFromSleeping
        settings_prevent_phone_from_sleeping_holder.setOnClickListener {
            settings_prevent_phone_from_sleeping.toggle()
            config.preventPhoneFromSleeping = settings_prevent_phone_from_sleeping.isChecked
        }
    }

    private fun setupHourFormat() {
        settings_hour_format.isChecked = config.use24HourFormat
        settings_hour_format_holder.setOnClickListener {
            settings_hour_format.toggle()
            config.use24HourFormat = settings_hour_format.isChecked
        }
    }

    private fun setupSundayFirst() {
        settings_sunday_first.isChecked = config.isSundayFirst
        settings_sunday_first_holder.setOnClickListener {
            settings_sunday_first.toggle()
            config.isSundayFirst = settings_sunday_first.isChecked
        }
    }

    private fun setupShowSeconds() {
        settings_show_seconds.isChecked = config.showSeconds
        settings_show_seconds_holder.setOnClickListener {
            settings_show_seconds.toggle()
            config.showSeconds = settings_show_seconds.isChecked
        }
    }

    private fun setupDisplayOtherTimeZones() {
        settings_display_other_timezones.isChecked = config.displayOtherTimeZones
        settings_display_other_timezones_holder.setOnClickListener {
            settings_display_other_timezones.toggle()
            config.displayOtherTimeZones = settings_display_other_timezones.isChecked
        }
    }

    private fun setupAlarmMaxReminder() {
        updateAlarmMaxReminderText()
        settings_alarm_max_reminder_holder.setOnClickListener {
            showPickSecondsDialog(config.alarmMaxReminderSecs, true, true) {
                config.alarmMaxReminderSecs = if (it != 0) it else DEFAULT_MAX_ALARM_REMINDER_SECS
                updateAlarmMaxReminderText()
            }
        }
    }

    private fun setupUseSameSnooze() {
        settings_snooze_time_holder.beVisibleIf(config.useSameSnooze)
        settings_use_same_snooze.isChecked = config.useSameSnooze
        settings_use_same_snooze_holder.setOnClickListener {
            settings_use_same_snooze.toggle()
            config.useSameSnooze = settings_use_same_snooze.isChecked
            settings_snooze_time_holder.beVisibleIf(config.useSameSnooze)
        }
    }

    private fun setupSnoozeTime() {
        updateSnoozeText()
        settings_snooze_time_holder.setOnClickListener {
            showPickSecondsDialog(config.snoozeTime * MINUTE_SECONDS, true) {
                config.snoozeTime = it / MINUTE_SECONDS
                updateSnoozeText()
            }
        }
    }

    private fun setupVibrate() {
        settings_vibrate.isChecked = config.vibrateOnButtonPress
        settings_vibrate_holder.setOnClickListener {
            settings_vibrate.toggle()
            config.vibrateOnButtonPress = settings_vibrate.isChecked
        }
    }

    private fun setupTimerMaxReminder() {
        updateTimerMaxReminderText()
        settings_timer_max_reminder_holder.setOnClickListener {
            showPickSecondsDialog(config.timerMaxReminderSecs, true, true) {
                config.timerMaxReminderSecs = if (it != 0) it else DEFAULT_MAX_TIMER_REMINDER_SECS
                updateTimerMaxReminderText()
            }
        }
    }

    private fun setupUseTextShadow() {
        settings_use_text_shadow.isChecked = config.useTextShadow
        settings_use_text_shadow_holder.setOnClickListener {
            settings_use_text_shadow.toggle()
            config.useTextShadow = settings_use_text_shadow.isChecked
            updateWidgets()
        }
    }

    private fun updateSnoozeText() {
        settings_snooze_time.text = formatMinutesToTimeString(config.snoozeTime)
    }

    private fun updateAlarmMaxReminderText() {
        settings_alarm_max_reminder.text = formatSecondsToTimeString(config.alarmMaxReminderSecs)
    }

    private fun updateTimerMaxReminderText() {
        settings_timer_max_reminder.text = formatSecondsToTimeString(config.timerMaxReminderSecs)
    }
}
