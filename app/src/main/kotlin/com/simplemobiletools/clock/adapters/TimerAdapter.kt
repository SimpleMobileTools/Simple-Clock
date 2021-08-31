package com.simplemobiletools.clock.adapters

import android.media.AudioManager
import android.media.RingtoneManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.activities.SimpleActivity
import com.simplemobiletools.clock.dialogs.MyTimePickerDialogDialog
import com.simplemobiletools.clock.extensions.*
import com.simplemobiletools.clock.helpers.PICK_AUDIO_FILE_INTENT_ID
import com.simplemobiletools.clock.models.Timer
import com.simplemobiletools.clock.models.TimerState
import com.simplemobiletools.commons.dialogs.SelectAlarmSoundDialog
import com.simplemobiletools.commons.extensions.getDefaultAlarmSound
import com.simplemobiletools.commons.extensions.getFormattedDuration
import com.simplemobiletools.commons.extensions.onTextChangeListener
import com.simplemobiletools.commons.models.AlarmSound
import kotlinx.android.synthetic.main.item_timer.view.*
import org.greenrobot.eventbus.EventBus

class TimerAdapter(
    private val activity: SimpleActivity,
    private val onRefresh: () -> Unit,

    ) : ListAdapter<Timer, TimerAdapter.TimerViewHolder>(diffUtil) {

    private val config = activity.config

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimerViewHolder {
        return TimerViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_timer, parent, false))
    }

    override fun onBindViewHolder(holder: TimerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun getItemAt(position: Int): Timer {
        return getItem(position)
    }

    inner class TimerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(timer: Timer) {
            itemView.apply {
                timer_time.text = timer.seconds.getFormattedDuration()
                timer_label.setText(timer.label)

                timer_time.text = timer.seconds.getFormattedDuration()
                timer_label.setText(timer.label)

                val textColor = activity.config.textColor

                timer_initial_time.text = timer.seconds.getFormattedDuration()
                timer_initial_time.colorLeftDrawable(textColor)

                timer_vibrate.isChecked = timer.vibrate
                timer_vibrate.colorLeftDrawable(textColor)

                timer_sound.text = timer.soundTitle
                timer_sound.colorLeftDrawable(textColor)

                timer_time.setOnClickListener {
                    stopTimer(timer)
                }

                timer_time.setOnClickListener {
                    changeDuration(timer)
                }

                timer_initial_time.setOnClickListener {
                    changeDuration(timer)
                }

                timer_vibrate_holder.setOnClickListener {
                    timer_vibrate.toggle()
                    updateTimer(timer.copy(vibrate = timer_vibrate.isChecked), false)
                }

                timer_sound.setOnClickListener {
                    SelectAlarmSoundDialog(activity, config.timerSoundUri, AudioManager.STREAM_ALARM, PICK_AUDIO_FILE_INTENT_ID,
                        RingtoneManager.TYPE_ALARM, true,
                        onAlarmPicked = { sound ->
                            if (sound != null) {
                                updateAlarmSound(timer, sound)
                            }
                        },
                        onAlarmSoundDeleted = { sound ->
                            if (timer.soundUri == sound.uri) {
                                val defaultAlarm = context.getDefaultAlarmSound(RingtoneManager.TYPE_ALARM)
                                updateAlarmSound(timer, defaultAlarm)
                            }

                            context.checkAlarmsWithDeletedSoundUri(sound.uri)
                        })
                }

                timer_label.onTextChangeListener { text ->
                    updateTimer(timer.copy(label = text), false)
                }
            }
        }
    }

    private fun stopTimer(timer: Timer) {
        EventBus.getDefault().post(TimerState.Idle)
        activity.hideTimerNotification()
    }

    private fun changeDuration(timer: Timer) {
        MyTimePickerDialogDialog(activity, timer.seconds) { seconds ->
            val timerSeconds = if (seconds <= 0) 10 else seconds
            updateTimer(timer.copy(seconds = timerSeconds))
        }
    }

    fun updateAlarmSound(timer: Timer, alarmSound: AlarmSound) {
        updateTimer(timer.copy(soundTitle = alarmSound.title, soundUri = alarmSound.uri))
    }

    private fun updateTimer(timer: Timer, refresh: Boolean = true) {
        activity.timerHelper.insertOrUpdateTimer(timer)
        if (refresh) {
            onRefresh.invoke()
        }
    }


    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<Timer>() {
            override fun areItemsTheSame(oldItem: Timer, newItem: Timer): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Timer, newItem: Timer): Boolean {
                return oldItem == newItem
            }
        }
    }
}
