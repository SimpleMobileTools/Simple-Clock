package com.simplemobiletools.clock.fragments

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.support.v4.app.Fragment
import android.support.v4.app.NotificationCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.activities.ReminderActivity
import com.simplemobiletools.clock.activities.SimpleActivity
import com.simplemobiletools.clock.dialogs.MyTimePickerDialogDialog
import com.simplemobiletools.clock.extensions.*
import com.simplemobiletools.clock.helpers.PICK_AUDIO_FILE_INTENT_ID
import com.simplemobiletools.clock.helpers.TIMER_NOTIF_ID
import com.simplemobiletools.commons.dialogs.SelectAlarmSoundDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.ALARM_SOUND_TYPE_ALARM
import com.simplemobiletools.commons.helpers.isLollipopPlus
import com.simplemobiletools.commons.helpers.isOreoPlus
import com.simplemobiletools.commons.models.AlarmSound
import kotlinx.android.synthetic.main.fragment_timer.view.*

class TimerFragment : Fragment() {
    private val UPDATE_INTERVAL = 1000L
    private val WAS_RUNNING = "was_running"
    private val CURRENT_TICKS = "current_ticks"
    private val TOTAL_TICKS = "total_ticks"

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
                SelectAlarmSoundDialog(activity as SimpleActivity, config.timerSoundUri, AudioManager.STREAM_ALARM, PICK_AUDIO_FILE_INTENT_ID,
                        ALARM_SOUND_TYPE_ALARM, true, onAlarmPicked = {
                    if (it != null) {
                        updateAlarmSound(it)
                    }
                }, onAlarmSoundDeleted = {
                    if (config.timerSoundUri == it.uri) {
                        val defaultAlarm = context.getDefaultAlarmSound(ALARM_SOUND_TYPE_ALARM)
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
        context!!.hideNotification(TIMER_NOTIF_ID)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isRunning && activity?.isChangingConfigurations == false) {
            context?.toast(R.string.timer_stopped)
        }
        isRunning = false
        updateHandler.removeCallbacks(updateRunnable)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.apply {
            putBoolean(WAS_RUNNING, isRunning)
            putInt(TOTAL_TICKS, totalTicks)
            putInt(CURRENT_TICKS, currentTicks)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.apply {
            isRunning = getBoolean(WAS_RUNNING, false)
            totalTicks = getInt(TOTAL_TICKS, 0)
            currentTicks = getInt(CURRENT_TICKS, 0)

            if (isRunning) {
                uptimeAtStart = SystemClock.uptimeMillis() - currentTicks * UPDATE_INTERVAL
                updateTimerState(false)
            }
        }
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
        updateTimerState(true)
    }

    private fun updateTimerState(setUptimeAtStart: Boolean) {
        updateIcons()
        context!!.hideTimerNotification()

        if (isRunning) {
            updateHandler.post(updateRunnable)
            view.timer_reset.beVisible()
            if (setUptimeAtStart) {
                uptimeAtStart = SystemClock.uptimeMillis()
            }
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
        } else if (diff > 0 && !isForegrounded && isRunning) {
            showNotification(formattedDuration)
        }

        return true
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun showNotification(formattedDuration: String) {
        val channelId = "simple_alarm_timer"
        val label = getString(R.string.timer)
        val notificationManager = context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (isOreoPlus()) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            NotificationChannel(channelId, label, importance).apply {
                setSound(null, null)
                notificationManager.createNotificationChannel(this)
            }
        }

        val builder = NotificationCompat.Builder(context)
                .setContentTitle(label)
                .setContentText(formattedDuration)
                .setSmallIcon(R.drawable.ic_timer)
                .setContentIntent(context!!.getOpenTimerTabIntent())
                .setPriority(Notification.PRIORITY_HIGH)
                .setSound(null)
                .setOngoing(true)
                .setAutoCancel(true)
                .setChannelId(channelId)

        if (isLollipopPlus()) {
            builder.setVisibility(Notification.VISIBILITY_PUBLIC)
        }

        notificationManager.notify(TIMER_NOTIF_ID, builder.build())
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
