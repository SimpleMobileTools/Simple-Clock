package com.simplemobiletools.clock.activities

import android.os.Bundle
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.extensions.config
import com.simplemobiletools.commons.extensions.*
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
        setupShowSeconds()
        setupDisplayOtherTimeZones()
        setupUseSameSnooze()
        setupSnoozeTime()
        setupVibrate()
        updateTextColors(settings_holder)
        setupSectionColors()
    }

    private fun setupSectionColors() {
        val adjustedPrimaryColor = getAdjustedPrimaryColor()
        arrayListOf(clock_tab_label, alarm_tab_label, stopwatch_tab_label).forEach {
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
            showPickIntervalDialog(config.snoozeTime, true) {
                config.snoozeTime = it
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

    private fun updateSnoozeText() {
        settings_snooze_time.text = formatMinutesToTimeString(config.snoozeTime)
    }
}
