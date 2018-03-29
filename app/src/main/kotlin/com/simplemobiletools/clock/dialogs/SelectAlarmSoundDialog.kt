package com.simplemobiletools.clock.dialogs

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.support.v7.app.AlertDialog
import android.view.ViewGroup
import android.widget.RadioGroup
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.activities.SimpleActivity
import com.simplemobiletools.clock.extensions.config
import com.simplemobiletools.clock.extensions.getAlarmSounds
import com.simplemobiletools.clock.helpers.PICK_AUDIO_FILE_INTENT_ID
import com.simplemobiletools.clock.models.AlarmSound
import com.simplemobiletools.commons.extensions.getAdjustedPrimaryColor
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.extensions.showErrorToast
import com.simplemobiletools.commons.views.MyCompatRadioButton
import kotlinx.android.synthetic.main.dialog_select_alarm_sound.view.*

class SelectAlarmSoundDialog(val activity: SimpleActivity, val currentUri: String, val audioStream: Int, val callback: (alarmSound: AlarmSound?) -> Unit) {
    private val ADD_NEW_SOUND_ID = -1

    private val view = activity.layoutInflater.inflate(R.layout.dialog_select_alarm_sound, null)
    private var alarmSounds = ArrayList<AlarmSound>()
    private var mediaPlayer = MediaPlayer()
    private val config = activity.config
    private val dialog: AlertDialog

    init {
        activity.getAlarmSounds {
            alarmSounds = it
            gotSystemAlarms()
        }

        view.dialog_select_alarm_your_label.setTextColor(activity.getAdjustedPrimaryColor())
        view.dialog_select_alarm_system_label.setTextColor(activity.getAdjustedPrimaryColor())

        val newAlarmSound = AlarmSound(ADD_NEW_SOUND_ID, activity.getString(R.string.add_new_sound), "")
        addAlarmSound(newAlarmSound, view.dialog_select_alarm_your_radio)

        dialog = AlertDialog.Builder(activity)
                .setOnDismissListener { mediaPlayer.stop() }
                .setPositiveButton(R.string.ok, { dialog, which -> dialogConfirmed() })
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
                    activity.setupDialogStuff(view, this)
                    window.volumeControlStream = audioStream
                }
    }

    private fun gotSystemAlarms() {
        alarmSounds.forEach {
            addAlarmSound(it, view.dialog_select_alarm_system_radio)
        }
    }

    private fun addAlarmSound(alarmSound: AlarmSound, holder: ViewGroup) {
        val radioButton = (activity.layoutInflater.inflate(R.layout.item_select_alarm, null) as MyCompatRadioButton).apply {
            text = alarmSound.title
            isChecked = alarmSound.uri == currentUri
            id = alarmSound.id
            setColors(config.textColor, activity.getAdjustedPrimaryColor(), config.backgroundColor)
            setOnClickListener {
                alarmClicked(alarmSound)
            }
        }

        holder.addView(radioButton, RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
    }

    private fun alarmClicked(alarmSound: AlarmSound) {
        if (alarmSound.id == ADD_NEW_SOUND_ID) {
            Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "audio/*"
                activity.startActivityForResult(this, PICK_AUDIO_FILE_INTENT_ID)
            }
            dialog.dismiss()
        } else {
            try {
                mediaPlayer.stop()
                mediaPlayer = MediaPlayer().apply {
                    setAudioStreamType(audioStream)
                    setDataSource(activity, Uri.parse(alarmSound.uri))
                    isLooping = true
                    prepare()
                    start()
                }
            } catch (e: Exception) {
                activity.showErrorToast(e)
            }
        }
    }

    private fun dialogConfirmed() {
        val checkedId = view.dialog_select_alarm_system_radio.checkedRadioButtonId
        if (checkedId == -1) {
            callback(null)
        } else {
            callback(alarmSounds.firstOrNull { it.id == checkedId })
        }
    }
}
