package com.simplemobiletools.clock.dialogs

import android.media.MediaPlayer
import android.net.Uri
import android.support.v7.app.AlertDialog
import android.view.ViewGroup
import android.widget.RadioGroup
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.activities.SimpleActivity
import com.simplemobiletools.clock.extensions.config
import com.simplemobiletools.clock.extensions.getAlarms
import com.simplemobiletools.clock.models.AlarmSound
import com.simplemobiletools.commons.extensions.getAdjustedPrimaryColor
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.extensions.showErrorToast
import com.simplemobiletools.commons.views.MyCompatRadioButton
import kotlinx.android.synthetic.main.dialog_select_alarm_sound.view.*

class SelectAlarmSoundDialog(val activity: SimpleActivity, val currentUri: String, val audioStream: Int, val callback: (alarmSound: AlarmSound?) -> Unit) {
    private val view = activity.layoutInflater.inflate(R.layout.dialog_select_alarm_sound, null)
    private var alarms = ArrayList<AlarmSound>()
    private var mediaPlayer = MediaPlayer()

    init {
        activity.getAlarms {
            alarms = it
            gotAlarms()
        }

        view.dialog_select_alarm_system_label.setTextColor(activity.getAdjustedPrimaryColor())

        AlertDialog.Builder(activity)
                .setOnDismissListener { mediaPlayer.stop() }
                .setPositiveButton(R.string.ok, { dialog, which -> dialogConfirmed() })
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
                    activity.setupDialogStuff(view, this)
                    window.volumeControlStream = audioStream
                }
    }

    private fun gotAlarms() {
        val config = activity.config
        view.dialog_select_alarm_radio.apply {
            alarms.forEachIndexed { index, alarmSound ->
                val radioButton = (activity.layoutInflater.inflate(R.layout.item_select_alarm, null) as MyCompatRadioButton).apply {
                    text = alarmSound.title
                    isChecked = alarmSound.uri == currentUri
                    id = index
                    setColors(config.textColor, activity.getAdjustedPrimaryColor(), config.backgroundColor)
                    setOnClickListener {
                        try {
                            mediaPlayer.stop()
                            mediaPlayer = MediaPlayer().apply {
                                setAudioStreamType(audioStream)
                                setDataSource(context, Uri.parse(alarmSound.uri))
                                isLooping = true
                                prepare()
                                start()
                            }
                        } catch (e: Exception) {
                            activity.showErrorToast(e)
                        }
                    }
                }

                addView(radioButton, RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            }
        }
    }

    private fun dialogConfirmed() {
        val checkedId = view.dialog_select_alarm_radio.checkedRadioButtonId
        if (checkedId == -1) {
            callback(null)
        } else {
            callback(alarms[checkedId])
        }
    }
}
