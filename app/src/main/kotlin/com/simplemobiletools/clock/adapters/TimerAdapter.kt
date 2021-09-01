package com.simplemobiletools.clock.adapters

import android.media.AudioManager
import android.media.RingtoneManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.activities.SimpleActivity
import com.simplemobiletools.clock.dialogs.MyTimePickerDialogDialog
import com.simplemobiletools.clock.extensions.checkAlarmsWithDeletedSoundUri
import com.simplemobiletools.clock.extensions.colorLeftDrawable
import com.simplemobiletools.clock.extensions.config
import com.simplemobiletools.clock.extensions.timerHelper
import com.simplemobiletools.clock.helpers.PICK_AUDIO_FILE_INTENT_ID
import com.simplemobiletools.clock.models.Timer
import com.simplemobiletools.clock.models.TimerState
import com.simplemobiletools.commons.dialogs.SelectAlarmSoundDialog
import com.simplemobiletools.commons.extensions.getDefaultAlarmSound
import com.simplemobiletools.commons.extensions.getFormattedDuration
import com.simplemobiletools.commons.extensions.onTextChangeListener
import com.simplemobiletools.commons.models.AlarmSound
import kotlin.math.roundToInt
import kotlinx.android.synthetic.main.item_timer.view.*

class TimerAdapter(
    private val activity: SimpleActivity,
    private val onRefresh: () -> Unit,
) : ListAdapter<Timer, TimerAdapter.TimerViewHolder>(diffUtil) {

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

        init {
            itemView.timer_label.onTextChangeListener { text ->
                Log.w(TAG, "timer_label")
                updateTimer(getItemAt(adapterPosition).copy(label = text), false)
            }

            itemView.post {
                val textColor = activity.config.textColor
                itemView.timer_initial_time.colorLeftDrawable(textColor)
                itemView.timer_vibrate.colorLeftDrawable(textColor)
                itemView.timer_sound.colorLeftDrawable(textColor)
            }
        }

        fun bind(timer: Timer) {
            itemView.apply {
                //only update when different to prevent flickering and unnecessary updates
                if (timer_label.text.toString() != timer.label) {
                    timer_label.setText(timer.label)
                }

                timer_initial_time.text = timer.seconds.getFormattedDuration()

                timer_vibrate.isChecked = timer.vibrate

                timer_sound.text = timer.soundTitle

                timer_time.setOnClickListener {
                    changeDuration(timer)
                }

                timer_initial_time.setOnClickListener {
                    changeDuration(timer)
                }

                timer_vibrate_holder.setOnClickListener {
                    Log.w(TAG, "toggle")
                    timer_vibrate.toggle()
                    updateTimer(timer.copy(vibrate = timer_vibrate.isChecked), false)
                }

                timer_sound.setOnClickListener {
                    SelectAlarmSoundDialog(activity, timer.soundUri, AudioManager.STREAM_ALARM, PICK_AUDIO_FILE_INTENT_ID,
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


                when (timer.state) {
                    is TimerState.Finished -> {
                        timer_time.text = 0.getFormattedDuration()
                    }

                    is TimerState.Idle -> {
                        timer_time.text = timer.seconds.getFormattedDuration()
                    }

                    is TimerState.Paused -> {
                        timer_time.text = timer.state.tick.div(1000F).roundToInt().getFormattedDuration()
                    }

                    is TimerState.Running -> {
                        timer_time.text = timer.state.tick.div(1000F).roundToInt().getFormattedDuration()
                    }
                }
            }
        }
    }

    private fun changeDuration(timer: Timer) {
        MyTimePickerDialogDialog(activity, timer.seconds) { seconds ->
            val timerSeconds = if (seconds <= 0) 10 else seconds
            Log.w(TAG, "changeDuration")
            updateTimer(timer.copy(seconds = timerSeconds))
        }
    }

    fun updateAlarmSound(timer: Timer, alarmSound: AlarmSound) {
        Log.w(TAG, "updateAlarmSound: $timer")
        updateTimer(timer.copy(soundTitle = alarmSound.title, soundUri = alarmSound.uri))
    }

    private fun updateTimer(timer: Timer, refresh: Boolean = true) {
        Log.w(TAG, "updateTimer: $timer")
        activity.timerHelper.insertOrUpdateTimer(timer)
        if (refresh) {
            onRefresh.invoke()
        }
    }

    companion object {
        private const val TAG = "TimerAdapter"
        private val diffUtil = object : DiffUtil.ItemCallback<Timer>() {
            override fun areItemsTheSame(oldItem: Timer, newItem: Timer): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Timer, newItem: Timer): Boolean {
                return oldItem == newItem
            }
        }
    }
}
