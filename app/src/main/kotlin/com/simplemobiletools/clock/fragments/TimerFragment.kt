package com.simplemobiletools.clock.fragments

import android.content.Intent
import android.graphics.Color
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.activities.ReminderActivity
import com.simplemobiletools.clock.activities.SimpleActivity
import com.simplemobiletools.clock.dialogs.MyTimePickerDialogDialog
import com.simplemobiletools.clock.dialogs.SelectAlarmSoundDialog
import com.simplemobiletools.clock.extensions.*
import com.simplemobiletools.clock.models.AlarmSound
import com.simplemobiletools.commons.extensions.*
import kotlinx.android.synthetic.main.fragment_timer.view.*

class TimerFragment : Fragment() {
    private val UPDATE_INTERVAL = 1000L

    private var isRunning = false
    private var uptimeAtStart = 0L
    private var initialSecs = 0
    private var totalTicks = 0
    private var currentTicks = 0
    private var updateHandler = Handler()
    private var isForegrounded = true

    lateinit var view: ViewGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val config = context!!.config
        view = (inflater.inflate(R.layout.fragment_timer, container, false) as ViewGroup).apply {
            timer_time.setOnClickListener {
                togglePlayPause()
            }

            timer_play_pause.setOnClickListener {
                togglePlayPause()
            }

            timer_reset.setOnClickListener {
                context!!.hideTimerNotification()
                resetTimer()
            }

            timer_initial_time.setOnClickListener {
                MyTimePickerDialogDialog(activity as SimpleActivity, config.timerSeconds) {
                    val seconds = if (it <= 0) 10 else it
                    config.timerSeconds = seconds
                    timer_initial_time.text = seconds.getFormattedDuration()
                    if (!isRunning) {
                        resetTimer()
                    }
                }
            }

            timer_vibrate_holder.setOnClickListener {
                timer_vibrate.toggle()
                config.timerVibrate = timer_vibrate.isChecked
            }

            timer_sound.setOnClickListener {
                SelectAlarmSoundDialog(activity as SimpleActivity, config.timerSoundUri, AudioManager.STREAM_SYSTEM, onAlarmPicked = {
                    if (it != null) {
                        updateAlarmSound(it)
                    }
                }, onAlarmSoundDeleted = {
                    if (config.timerSoundUri == it.uri) {
                        val defaultAlarm = AlarmSound(0, context.getDefaultAlarmTitle(), context.getDefaultAlarmUri().toString())
                        updateAlarmSound(defaultAlarm)
                    }
                    context.checkAlarmsWithDeletedSoundUri(it.uri)
                })
            }
        }

        initialSecs = config.timerSeconds
        updateDisplayedText()
        return view
    }

    override fun onStart() {
        super.onStart()
        isForegrounded = true
    }

    override fun onResume() {
        super.onResume()
        setupViews()
    }

    override fun onStop() {
        super.onStop()
        isForegrounded = false
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isRunning) {
            context?.toast(R.string.timer_stopped)
        }
        isRunning = false
        updateHandler.removeCallbacks(updateRunnable)
    }

    fun updateAlarmSound(alarmSound: AlarmSound) {
        context!!.config.timerSoundTitle = alarmSound.title
        context!!.config.timerSoundUri = alarmSound.uri
        view.timer_sound.text = alarmSound.title
    }

    private fun setupViews() {
        val config = context!!.config
        val textColor = config.textColor
        view.apply {
            context!!.updateTextColors(timer_fragment)
            timer_play_pause.background = resources.getColoredDrawableWithColor(R.drawable.circle_background_filled, context!!.getAdjustedPrimaryColor())
            timer_reset.applyColorFilter(textColor)

            timer_initial_time.text = config.timerSeconds.getFormattedDuration()
            timer_initial_time.colorLeftDrawable(textColor)

            timer_vibrate.isChecked = config.timerVibrate
            timer_vibrate.colorLeftDrawable(textColor)

            timer_sound.text = config.timerSoundTitle
            timer_sound.colorLeftDrawable(textColor)
        }

        updateIcons()
        updateDisplayedText()
    }

    private fun togglePlayPause() {
        isRunning = !isRunning
        updateIcons()
        context!!.hideTimerNotification()

        if (isRunning) {
            updateHandler.post(updateRunnable)
            uptimeAtStart = SystemClock.uptimeMillis()
            view.timer_reset.beVisible()
        } else {
            updateHandler.removeCallbacksAndMessages(null)
            currentTicks = 0
            totalTicks--
        }
    }

    private fun updateIcons() {
        val drawableId = if (isRunning) R.drawable.ic_pause else R.drawable.ic_play
        val iconColor = if (context!!.getAdjustedPrimaryColor() == Color.WHITE) Color.BLACK else context!!.config.textColor
        view.timer_play_pause.setImageDrawable(resources.getColoredDrawableWithColor(drawableId, iconColor))
    }

    private fun resetTimer() {
        updateHandler.removeCallbacks(updateRunnable)
        isRunning = false
        currentTicks = 0
        totalTicks = 0
        initialSecs = context!!.config.timerSeconds
        updateDisplayedText()
        updateIcons()
        view.timer_reset.beGone()
    }

    private fun updateDisplayedText(): Boolean {
        val diff = initialSecs - totalTicks
        var formattedDuration = Math.abs(diff).getFormattedDuration()

        if (diff < 0) {
            formattedDuration = "-$formattedDuration"
            if (!isForegrounded) {
                resetTimer()
                return false
            }
        }

        view.timer_time.text = formattedDuration
        if (diff == 0) {
            if (context?.isScreenOn() == true) {
                context!!.showTimerNotification(false)
                Handler().postDelayed({
                    context?.hideTimerNotification()
                }, context?.config!!.timerMaxReminderSecs * 1000L)
            } else {
                Intent(context, ReminderActivity::class.java).apply {
                    activity?.startActivity(this)
                }
            }
        }
        return true
    }

    private val updateRunnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                if (updateDisplayedText()) {
                    currentTicks++
                    totalTicks++
                    updateHandler.postAtTime(this, uptimeAtStart + currentTicks * UPDATE_INTERVAL)
                }
            }
        }
    }
}
