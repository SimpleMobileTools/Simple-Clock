package com.simplemobiletools.clock.activities

import android.content.Intent
import android.os.Bundle
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.extensions.config
import com.simplemobiletools.clock.extensions.updateWidgets
import com.simplemobiletools.clock.helpers.DEFAULT_MAX_ALARM_REMINDER_SECS
import com.simplemobiletools.clock.helpers.DEFAULT_MAX_TIMER_REMINDER_SECS
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.IS_CUSTOMIZING_COLORS
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

        setupPurchaseThankYou()
        setupCustomizeColors()
        setupUseEnglish()
        setupPreventPhoneFromSleeping()
        setupHourFormat()
        setupSundayFirst()
        setupShowSeconds()
        setupAlarmMaxReminder()
        setupUseSameSnooze()
        setupSnoozeTime()
        setupVibrate()
        setupTimerMaxReminder()
        setupIncreaseVolumeGradually()
        setupUseTextShadow()
        setupCustomizeWidgetColors()
        updateTextColors(settings_holder)
        setupSectionColors()
    }

    private fun setupSectionColors() {
        val adjustedPrimaryColor = getAdjustedPrimaryColor()
        arrayListOf(clock_tab_label, alarm_tab_label, stopwatch_tab_label, timer_tab_label, widgets_label).forEach {
            it.setTextColor(adjustedPrimaryColor)
        }
    }

    private fun setupPurchaseThankYou() {
        settings_purchase_thank_you_holder.beVisibleIf(!isThankYouInstalled())
        settings_purchase_thank_you_holder.setOnClickListener {
            launchPurchaseThankYouIntent()
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
            System.exit(0)
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
            updateWidgets()
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

    private fun setupIncreaseVolumeGradually() {
        settings_increase_volume_gradually.isChecked = config.increaseVolumeGradually
        settings_increase_volume_gradually_holder.setOnClickListener {
            settings_increase_volume_gradually.toggle()
            config.increaseVolumeGradually = settings_increase_volume_gradually.isChecked
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

    private fun setupCustomizeWidgetColors() {
        settings_customize_widget_colors_holder.setOnClickListener {
            Intent(this, WidgetDateTimeConfigureActivity::class.java).apply {
                putExtra(IS_CUSTOMIZING_COLORS, true)
                startActivity(this)
            }
        }
    }
}
