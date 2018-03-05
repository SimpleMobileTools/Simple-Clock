package com.simplemobiletools.clock.dialogs

import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.support.v7.app.AlertDialog
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.activities.SimpleActivity
import com.simplemobiletools.clock.extensions.getAlarms
import com.simplemobiletools.clock.models.AlarmSound
import com.simplemobiletools.commons.extensions.setupDialogStuff
import kotlinx.android.synthetic.main.dialog_select_alarm_sound.view.*

class SelectAlarmSoundDialog(val activity: SimpleActivity, val currentUri: String, val callback: (alarmSound: AlarmSound) -> Unit) {
    private val view = activity.layoutInflater.inflate(R.layout.dialog_select_alarm_sound, null)
    private val alarms = activity.getAlarms()
    private var mediaPlayer = MediaPlayer()

    init {
        view.dialog_select_alarm_radio.apply {
            alarms.forEachIndexed { index, alarmSound ->
                val radioButton = (activity.layoutInflater.inflate(R.layout.item_select_alarm, null) as RadioButton).apply {
                    text = alarmSound.title
                    isChecked = alarmSound.uri == currentUri
                    id = index
                    setOnClickListener {
                        mediaPlayer.stop()
                        mediaPlayer = MediaPlayer().apply {
                            setAudioStreamType(AudioManager.STREAM_ALARM)
                            setDataSource(context, Uri.parse(alarmSound.uri))
                            prepare()
                            start()
                        }
                    }
                }

                addView(radioButton, RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            }
        }

        AlertDialog.Builder(activity)
                .setOnDismissListener { mediaPlayer.stop() }
                .setPositiveButton(R.string.ok, { dialog, which -> dialogConfirmed() })
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
                    activity.setupDialogStuff(view, this)
                    window.volumeControlStream = AudioManager.STREAM_ALARM
                }
    }

    private fun dialogConfirmed() {
        val checkedId = view.dialog_select_alarm_radio.checkedRadioButtonId
        callback(alarms[checkedId])
    }
}
