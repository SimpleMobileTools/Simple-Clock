package com.simplemobiletools.clock.fragments

import android.graphics.Color
import android.media.AudioManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.activities.SimpleActivity
import com.simplemobiletools.clock.dialogs.MyTimePickerDialogDialog
import com.simplemobiletools.clock.extensions.*
import com.simplemobiletools.clock.helpers.PICK_AUDIO_FILE_INTENT_ID
import com.simplemobiletools.clock.models.TimerState
import com.simplemobiletools.commons.dialogs.SelectAlarmSoundDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.ALARM_SOUND_TYPE_ALARM
import com.simplemobiletools.commons.models.AlarmSound
import kotlinx.android.synthetic.main.fragment_timer.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.math.roundToInt

class TimerFragment : Fragment() {

    lateinit var view: ViewGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        view = (inflater.inflate(R.layout.fragment_timer, container, false) as ViewGroup).apply {
            val config = requiredActivity.config
            val textColor = config.textColor

            timer_time.text = config.timerSeconds.getFormattedDuration()
            timer_label.setText(config.timerLabel)

            requiredActivity.updateTextColors(timer_fragment)
            timer_play_pause.background = resources.getColoredDrawableWithColor(R.drawable.circle_background_filled, context!!.getAdjustedPrimaryColor())
            timer_play_pause.applyColorFilter(if (context!!.getAdjustedPrimaryColor() == Color.WHITE) Color.BLACK else Color.WHITE)
            timer_reset.applyColorFilter(textColor)

            timer_initial_time.text = config.timerSeconds.getFormattedDuration()
            timer_initial_time.colorLeftDrawable(textColor)

            timer_vibrate.isChecked = config.timerVibrate
            timer_vibrate.colorLeftDrawable(textColor)

            timer_sound.text = config.timerSoundTitle
            timer_sound.colorLeftDrawable(textColor)

            timer_time.setOnClickListener {
                stopTimer()
            }

            timer_play_pause.setOnClickListener {
                val state = config.timerState

                when (state) {
                    is TimerState.Idle -> EventBus.getDefault().post(TimerState.Start(config.timerSeconds.secondsToMillis))
                    is TimerState.Paused -> EventBus.getDefault().post(TimerState.Start(state.tick))
                    is TimerState.Running -> EventBus.getDefault().post(TimerState.Pause(state.tick))
                    is TimerState.Finished -> EventBus.getDefault().post(TimerState.Start(config.timerSeconds.secondsToMillis))
                    else -> {
                    }
                }
            }

            timer_reset.setOnClickListener {
                stopTimer()
            }

            timer_initial_time.setOnClickListener {
                MyTimePickerDialogDialog(activity as SimpleActivity, config.timerSeconds) { seconds ->
                    val timerSeconds = if (seconds <= 0) 10 else seconds
                    config.timerSeconds = timerSeconds
                    timer_initial_time.text = timerSeconds.getFormattedDuration()
                }
            }

            timer_vibrate_holder.setOnClickListener {
                timer_vibrate.toggle()
                config.timerVibrate = timer_vibrate.isChecked
                config.timerChannelId = null
            }

            timer_sound.setOnClickListener {
                SelectAlarmSoundDialog(activity as SimpleActivity, config.timerSoundUri, AudioManager.STREAM_ALARM, PICK_AUDIO_FILE_INTENT_ID,
                        ALARM_SOUND_TYPE_ALARM, true,
                        onAlarmPicked = { sound ->
                            if (sound != null) {
                                updateAlarmSound(sound)
                            }
                        },
                        onAlarmSoundDeleted = { sound ->
                            if (config.timerSoundUri == sound.uri) {
                                val defaultAlarm = context.getDefaultAlarmSound(ALARM_SOUND_TYPE_ALARM)
                                updateAlarmSound(defaultAlarm)
                            }

                            context.checkAlarmsWithDeletedSoundUri(sound.uri)
                        })
            }

            timer_label.onTextChangeListener { text ->
                config.timerLabel = text
            }
        }

        return view
    }

    private fun stopTimer() {
        EventBus.getDefault().post(TimerState.Idle)
        requiredActivity.hideTimerNotification()
        view.timer_time.text = requiredActivity.config.timerSeconds.getFormattedDuration()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(state: TimerState.Idle) {
        view.timer_time.text = requiredActivity.config.timerSeconds.getFormattedDuration()
        updateViewStates(state)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(state: TimerState.Running) {
        view.timer_time.text = state.tick.div(1000F).roundToInt().getFormattedDuration()
        updateViewStates(state)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(state: TimerState.Paused) {
        updateViewStates(state)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(state: TimerState.Finished) {
        view.timer_time.text = 0.getFormattedDuration()
        updateViewStates(state)
    }

    private fun updateViewStates(state: TimerState) {
        val resetPossible = state is TimerState.Running || state is TimerState.Paused || state is TimerState.Finished
        view.timer_reset.beVisibleIf(resetPossible)

        val drawableId = if (state is TimerState.Running) {
            R.drawable.ic_pause_vector
        } else {
            R.drawable.ic_play_vector
        }

        val iconColor = if (requiredActivity.getAdjustedPrimaryColor() == Color.WHITE) {
            Color.BLACK
        } else {
            Color.WHITE
        }

        view.timer_play_pause.setImageDrawable(resources.getColoredDrawableWithColor(drawableId, iconColor))
    }

    fun updateAlarmSound(alarmSound: AlarmSound) {
        requiredActivity.config.timerSoundTitle = alarmSound.title
        requiredActivity.config.timerSoundUri = alarmSound.uri
        view.timer_sound.text = alarmSound.title
    }
}
