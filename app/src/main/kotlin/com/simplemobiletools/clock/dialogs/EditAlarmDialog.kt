package com.simplemobiletools.clock.dialogs

import android.app.TimePickerDialog
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.media.RingtoneManager
import android.text.format.DateFormat
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.activities.SimpleActivity
import com.simplemobiletools.clock.databinding.DialogEditAlarmBinding
import com.simplemobiletools.clock.extensions.*
import com.simplemobiletools.clock.helpers.PICK_AUDIO_FILE_INTENT_ID
import com.simplemobiletools.clock.helpers.TODAY_BIT
import com.simplemobiletools.clock.helpers.TOMORROW_BIT
import com.simplemobiletools.clock.helpers.getCurrentDayMinutes
import com.simplemobiletools.clock.models.Alarm
import com.simplemobiletools.commons.dialogs.ConfirmationDialog
import com.simplemobiletools.commons.dialogs.SelectAlarmSoundDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.models.AlarmSound

class EditAlarmDialog(val activity: SimpleActivity, val alarm: Alarm, val onDismiss: () -> Unit = {}, val callback: (alarmId: Int) -> Unit) {
    private val binding = DialogEditAlarmBinding.inflate(activity.layoutInflater)
    private val textColor = activity.getProperTextColor()

    init {
        restoreLastAlarm()
        updateAlarmTime()

        binding.apply {
            editAlarmTime.setOnClickListener {
                if (activity.config.isUsingSystemTheme) {
                    val timeFormat = if (DateFormat.is24HourFormat(activity)) {
                        TimeFormat.CLOCK_24H
                    } else {
                        TimeFormat.CLOCK_12H
                    }

                    val timePicker = MaterialTimePicker.Builder()
                        .setTimeFormat(timeFormat)
                        .setHour(alarm.timeInMinutes / 60)
                        .setMinute(alarm.timeInMinutes % 60)
                        .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                        .build()

                    timePicker.addOnPositiveButtonClickListener {
                        timePicked(timePicker.hour, timePicker.minute)
                    }

                    timePicker.show(activity.supportFragmentManager, "")
                } else {
                    TimePickerDialog(
                        root.context,
                        root.context.getTimePickerDialogTheme(),
                        timeSetListener,
                        alarm.timeInMinutes / 60,
                        alarm.timeInMinutes % 60,
                        DateFormat.is24HourFormat(activity)
                    ).show()
                }
            }

            editAlarmSound.colorCompoundDrawable(textColor)
            editAlarmSound.text = alarm.soundTitle
            editAlarmSound.setOnClickListener {
                SelectAlarmSoundDialog(activity, alarm.soundUri, AudioManager.STREAM_ALARM, PICK_AUDIO_FILE_INTENT_ID, RingtoneManager.TYPE_ALARM, true,
                    onAlarmPicked = {
                        if (it != null) {
                            updateSelectedAlarmSound(it)
                        }
                    }, onAlarmSoundDeleted = {
                        if (alarm.soundUri == it.uri) {
                            val defaultAlarm = root.context.getDefaultAlarmSound(RingtoneManager.TYPE_ALARM)
                            updateSelectedAlarmSound(defaultAlarm)
                        }
                        activity.checkAlarmsWithDeletedSoundUri(it.uri)
                    })
            }

            editAlarmVibrateIcon.setColorFilter(textColor)
            editAlarmVibrate.isChecked = alarm.vibrate
            editAlarmVibrateHolder.setOnClickListener {
                editAlarmVibrate.toggle()
                alarm.vibrate = editAlarmVibrate.isChecked
            }

            editAlarmLabelImage.applyColorFilter(textColor)
            editAlarm.setText(alarm.label)

            val dayLetters = activity.resources.getStringArray(com.simplemobiletools.commons.R.array.week_day_letters).toList() as ArrayList<String>
            val dayIndexes = arrayListOf(0, 1, 2, 3, 4, 5, 6)
            if (activity.config.isSundayFirst) {
                dayIndexes.moveLastItemToFront()
            }

            dayIndexes.forEach {
                val pow = Math.pow(2.0, it.toDouble()).toInt()
                val day = activity.layoutInflater.inflate(R.layout.alarm_day, editAlarmDaysHolder, false) as TextView
                day.text = dayLetters[it]

                val isDayChecked = alarm.days > 0 && alarm.days and pow != 0
                day.background = getProperDayDrawable(isDayChecked)

                day.setTextColor(if (isDayChecked) root.context.getProperBackgroundColor() else textColor)
                day.setOnClickListener {
                    if (alarm.days < 0) {
                        alarm.days = 0
                    }

                    val selectDay = alarm.days and pow == 0
                    if (selectDay) {
                        alarm.days = alarm.days.addBit(pow)
                    } else {
                        alarm.days = alarm.days.removeBit(pow)
                    }
                    day.background = getProperDayDrawable(selectDay)
                    day.setTextColor(if (selectDay) root.context.getProperBackgroundColor() else textColor)
                    checkDaylessAlarm()
                }

                editAlarmDaysHolder.addView(day)
            }
        }

        activity.getAlertDialogBuilder()
            .setOnDismissListener { onDismiss() }
            .setPositiveButton(com.simplemobiletools.commons.R.string.ok, null)
            .setNegativeButton(com.simplemobiletools.commons.R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this) { alertDialog ->
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        if (!activity.config.wasAlarmWarningShown) {
                            ConfirmationDialog(
                                activity,
                                messageId = com.simplemobiletools.commons.R.string.alarm_warning,
                                positive = com.simplemobiletools.commons.R.string.ok,
                                negative = 0
                            ) {
                                activity.config.wasAlarmWarningShown = true
                                it.performClick()
                            }

                            return@setOnClickListener
                        }

                        if (alarm.days <= 0) {
                            alarm.days = if (alarm.timeInMinutes > getCurrentDayMinutes()) {
                                TODAY_BIT
                            } else {
                                TOMORROW_BIT
                            }
                        }

                        alarm.label = binding.editAlarm.value
                        alarm.isEnabled = true
                        alarm.oneShot = false

                        var alarmId = alarm.id
                        activity.handleFullScreenNotificationsPermission { granted ->
                            if (granted) {
                                if (alarm.id == 0) {
                                    alarmId = activity.dbHelper.insertAlarm(alarm)
                                    if (alarmId == -1) {
                                        activity.toast(com.simplemobiletools.commons.R.string.unknown_error_occurred)
                                    }
                                } else {
                                    if (!activity.dbHelper.updateAlarm(alarm)) {
                                        activity.toast(com.simplemobiletools.commons.R.string.unknown_error_occurred)
                                    }
                                }

                                activity.config.alarmLastConfig = alarm
                                callback(alarmId)
                                alertDialog.dismiss()
                            }
                        }
                    }
                }
            }
    }

    private fun restoreLastAlarm() {
        if (alarm.id == 0) {
            activity.config.alarmLastConfig?.let { lastConfig ->
                alarm.label = lastConfig.label
                alarm.days = lastConfig.days
                alarm.soundTitle = lastConfig.soundTitle
                alarm.soundUri = lastConfig.soundUri
                alarm.timeInMinutes = lastConfig.timeInMinutes
                alarm.vibrate = lastConfig.vibrate
            }
        }
    }

    private val timeSetListener = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
        timePicked(hourOfDay, minute)
    }

    private fun timePicked(hours: Int, minutes: Int) {
        alarm.timeInMinutes = hours * 60 + minutes
        updateAlarmTime()
    }

    private fun updateAlarmTime() {
        binding.editAlarmTime.text = activity.getFormattedTime(alarm.timeInMinutes * 60, false, true)
        checkDaylessAlarm()
    }

    private fun checkDaylessAlarm() {
        if (alarm.days <= 0) {
            val textId = if (alarm.timeInMinutes > getCurrentDayMinutes()) {
                com.simplemobiletools.commons.R.string.today
            } else {
                com.simplemobiletools.commons.R.string.tomorrow
            }

            binding.editAlarmDaylessLabel.text = "(${activity.getString(textId)})"
        }
        binding.editAlarmDaylessLabel.beVisibleIf(alarm.days <= 0)
    }

    private fun getProperDayDrawable(selected: Boolean): Drawable {
        val drawableId = if (selected) R.drawable.circle_background_filled else R.drawable.circle_background_stroke
        val drawable = activity.resources.getDrawable(drawableId)
        drawable.applyColorFilter(textColor)
        return drawable
    }

    fun updateSelectedAlarmSound(alarmSound: AlarmSound) {
        alarm.soundTitle = alarmSound.title
        alarm.soundUri = alarmSound.uri
        binding.editAlarmSound.text = alarmSound.title
    }
}
