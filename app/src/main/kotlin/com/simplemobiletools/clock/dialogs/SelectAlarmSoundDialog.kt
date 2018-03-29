package com.simplemobiletools.clock.dialogs

import android.annotation.TargetApi
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.support.v7.app.AlertDialog
import android.view.ViewGroup
import android.widget.RadioGroup
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.activities.SimpleActivity
import com.simplemobiletools.clock.extensions.config
import com.simplemobiletools.clock.extensions.getAlarmSounds
import com.simplemobiletools.clock.helpers.PICK_AUDIO_FILE_INTENT_ID
import com.simplemobiletools.clock.models.AlarmSound
import com.simplemobiletools.commons.dialogs.RadioGroupDialog
import com.simplemobiletools.commons.extensions.getAdjustedPrimaryColor
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.extensions.showErrorToast
import com.simplemobiletools.commons.helpers.isKitkatPlus
import com.simplemobiletools.commons.models.RadioItem
import com.simplemobiletools.commons.views.MyCompatRadioButton
import kotlinx.android.synthetic.main.dialog_select_alarm_sound.view.*

class SelectAlarmSoundDialog(val activity: SimpleActivity, val currentUri: String, val audioStream: Int, val onAlarmPicked: (alarmSound: AlarmSound?) -> Unit,
                             val onAlarmSoundDeleted: (alarmSound: AlarmSound) -> Unit) {
    private val ADD_NEW_SOUND_ID = -2

    private val view = activity.layoutInflater.inflate(R.layout.dialog_select_alarm_sound, null)
    private var systemAlarmSounds = ArrayList<AlarmSound>()
    private var yourAlarmSounds = ArrayList<AlarmSound>()
    private var mediaPlayer = MediaPlayer()
    private val config = activity.config
    private val dialog: AlertDialog

    init {
        activity.getAlarmSounds {
            systemAlarmSounds = it
            gotSystemAlarms()
        }

        view.dialog_select_alarm_your_label.setTextColor(activity.getAdjustedPrimaryColor())
        view.dialog_select_alarm_system_label.setTextColor(activity.getAdjustedPrimaryColor())

        addYourAlarms()

        dialog = AlertDialog.Builder(activity)
                .setOnDismissListener { mediaPlayer.stop() }
                .setPositiveButton(R.string.ok, { dialog, which -> dialogConfirmed() })
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
                    activity.setupDialogStuff(view, this)
                    window.volumeControlStream = audioStream
                }
    }

    private fun addYourAlarms() {
        view.dialog_select_alarm_your_radio.removeAllViews()
        val token = object : TypeToken<ArrayList<AlarmSound>>() {}.type
        yourAlarmSounds = Gson().fromJson<ArrayList<AlarmSound>>(config.yourAlarmSounds, token) ?: ArrayList()
        yourAlarmSounds.add(AlarmSound(ADD_NEW_SOUND_ID, activity.getString(R.string.add_new_sound), ""))
        yourAlarmSounds.forEach {
            addAlarmSound(it, view.dialog_select_alarm_your_radio)
        }
    }

    private fun gotSystemAlarms() {
        systemAlarmSounds.forEach {
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

                if (holder == view.dialog_select_alarm_system_radio) {
                    view.dialog_select_alarm_your_radio.clearCheck()
                } else {
                    view.dialog_select_alarm_system_radio.clearCheck()
                }
            }

            if (alarmSound.id != -2 && holder == view.dialog_select_alarm_your_radio) {
                setOnLongClickListener {
                    val items = arrayListOf(RadioItem(1, context.getString(R.string.remove)))

                    RadioGroupDialog(activity, items) {
                        removeAlarmSound(alarmSound)
                    }
                    true
                }
            }
        }

        holder.addView(radioButton, RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private fun alarmClicked(alarmSound: AlarmSound) {
        if (alarmSound.id == ADD_NEW_SOUND_ID) {
            val action = if (isKitkatPlus()) Intent.ACTION_OPEN_DOCUMENT else Intent.ACTION_GET_CONTENT
            Intent(action).apply {
                type = "audio/*"
                activity.startActivityForResult(this, PICK_AUDIO_FILE_INTENT_ID)

                if (isKitkatPlus()) {
                    flags = flags or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                }
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

    private fun removeAlarmSound(alarmSound: AlarmSound) {
        val token = object : TypeToken<ArrayList<AlarmSound>>() {}.type
        yourAlarmSounds = Gson().fromJson<ArrayList<AlarmSound>>(config.yourAlarmSounds, token) ?: ArrayList()
        yourAlarmSounds.remove(alarmSound)
        config.yourAlarmSounds = Gson().toJson(yourAlarmSounds)
        addYourAlarms()

        if (alarmSound.id == view.dialog_select_alarm_your_radio.checkedRadioButtonId) {
            view.dialog_select_alarm_your_radio.clearCheck()
            view.dialog_select_alarm_system_radio.check(systemAlarmSounds.firstOrNull()?.id ?: 0)
        }

        onAlarmSoundDeleted(alarmSound)
    }

    private fun dialogConfirmed() {
        if (view.dialog_select_alarm_your_radio.checkedRadioButtonId != -1) {
            val checkedId = view.dialog_select_alarm_your_radio.checkedRadioButtonId
            onAlarmPicked(yourAlarmSounds.firstOrNull { it.id == checkedId })
        } else {
            val checkedId = view.dialog_select_alarm_system_radio.checkedRadioButtonId
            onAlarmPicked(systemAlarmSounds.firstOrNull { it.id == checkedId })
        }
    }
}
