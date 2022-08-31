package com.simplemobiletools.clock.dialogs

import android.media.AudioManager
import android.media.RingtoneManager
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.activities.SimpleActivity
import com.simplemobiletools.clock.extensions.*
import com.simplemobiletools.clock.helpers.PICK_AUDIO_FILE_INTENT_ID
import com.simplemobiletools.clock.models.Timer
import com.simplemobiletools.commons.dialogs.SelectAlarmSoundDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.models.AlarmSound
import kotlinx.android.synthetic.main.dialog_edit_timer.view.*

class EditTimerDialog(val activity: SimpleActivity, val timer: Timer, val callback: () -> Unit) {
    private val view = activity.layoutInflater.inflate(R.layout.dialog_edit_timer, null)
    private val textColor = activity.getProperTextColor()

    init {
        restoreLastAlarm()
        updateAlarmTime()

        view.apply {
            edit_timer_initial_time.colorCompoundDrawable(textColor)
            edit_timer_initial_time.text = timer.seconds.getFormattedDuration()
            edit_timer_initial_time.setTextColor(textColor)
            edit_timer_initial_time.setOnClickListener {
                changeDuration(timer)
            }

            edit_timer_vibrate_icon.setColorFilter(textColor)
            edit_timer_vibrate.isChecked = timer.vibrate
            edit_timer_vibrate.setTextColor(textColor)
            edit_timer_vibrate_holder.setOnClickListener {
                edit_timer_vibrate.toggle()
                timer.vibrate = edit_timer_vibrate.isChecked
                timer.channelId = null
            }

            edit_timer_sound.colorCompoundDrawable(textColor)
            edit_timer_sound.text = timer.soundTitle
            edit_timer_sound.setOnClickListener {
                SelectAlarmSoundDialog(activity, timer.soundUri, AudioManager.STREAM_ALARM, PICK_AUDIO_FILE_INTENT_ID,
                    RingtoneManager.TYPE_ALARM, true,
                    onAlarmPicked = { sound ->
                        if (sound != null) {
                            updateAlarmSound(sound)
                        }
                    },
                    onAlarmSoundDeleted = { sound ->
                        if (timer.soundUri == sound.uri) {
                            val defaultAlarm = context.getDefaultAlarmSound(RingtoneManager.TYPE_ALARM)
                            updateAlarmSound(defaultAlarm)
                        }

                        context.checkAlarmsWithDeletedSoundUri(sound.uri)
                    })
            }

            edit_timer_label_image.applyColorFilter(textColor)
            edit_timer.setText(timer.label)
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(view, this) { alertDialog ->
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        timer.label = view.edit_timer.value
                        activity.timerHelper.insertOrUpdateTimer(timer) {
                            activity.config.timerLastConfig = timer
                            callback()
                            alertDialog.dismiss()
                        }
                    }
                }
            }
    }

    private fun restoreLastAlarm() {
        if (timer.id == null) {
            activity.config.timerLastConfig?.let { lastConfig ->
                timer.label = lastConfig.label
                timer.seconds = lastConfig.seconds
                timer.soundTitle = lastConfig.soundTitle
                timer.soundUri = lastConfig.soundUri
                timer.vibrate = lastConfig.vibrate
            }
        }
    }

    private fun updateAlarmTime() {
        view.edit_timer_initial_time.text = activity.getFormattedTime(timer.seconds * 60, false, true)
    }

    private fun changeDuration(timer: Timer) {
        MyTimePickerDialogDialog(activity, timer.seconds) { seconds ->
            val timerSeconds = if (seconds <= 0) 10 else seconds
            timer.seconds = timerSeconds
            view.edit_timer_initial_time.text = timerSeconds.getFormattedDuration()
        }
    }

    fun updateAlarmSound(alarmSound: AlarmSound) {
        timer.soundTitle = alarmSound.title
        timer.soundUri = alarmSound.uri
        timer.channelId = null
        view.edit_timer_sound.text = alarmSound.title
    }
}
