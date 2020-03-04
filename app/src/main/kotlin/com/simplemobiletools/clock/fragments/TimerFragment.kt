package com.simplemobiletools.clock.fragments

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.activities.ReminderActivity
import com.simplemobiletools.clock.activities.SimpleActivity
import com.simplemobiletools.clock.dialogs.MyTimePickerDialogDialog
import com.simplemobiletools.clock.extensions.*
import com.simplemobiletools.clock.helpers.PICK_AUDIO_FILE_INTENT_ID
import com.simplemobiletools.clock.helpers.TIMER_NOTIF_ID
import com.simplemobiletools.clock.workers.cancelTimerWorker
import com.simplemobiletools.clock.workers.enqueueTimerWorker
import com.simplemobiletools.clock.workers.timerRequestId
import com.simplemobiletools.commons.dialogs.SelectAlarmSoundDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.ALARM_SOUND_TYPE_ALARM
import com.simplemobiletools.commons.helpers.isOreoPlus
import com.simplemobiletools.commons.models.AlarmSound
import kotlinx.android.synthetic.main.fragment_timer.*
import kotlinx.android.synthetic.main.fragment_timer.view.*
import kotlinx.android.synthetic.main.fragment_timer.view.timer_time
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class TimerFragment : Fragment() {

    private val UPDATE_INTERVAL = 1000L
    private val WAS_RUNNING = "was_running"
    private val CURRENT_TICKS = "current_ticks"
    private val TOTAL_TICKS = "total_ticks"

    private var isRunning = false
    private var initialSecs = 0
    private var totalTicks = 0
    private var currentTicks = 0

    lateinit var view: ViewGroup

    @InternalCoroutinesApi
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val config = requiredActivity.config
        view = (inflater.inflate(R.layout.fragment_timer, container, false) as ViewGroup).apply {
            timer_time.setOnClickListener {
                val selectedDuration = config.timerSeconds
                enqueueTimerWorker(TimeUnit.SECONDS.toMillis(selectedDuration.toLong()))
                showNotification(selectedDuration.getFormattedDuration())
            }

            timer_play_pause.setOnClickListener {
                val selectedDuration = config.timerSeconds
                enqueueTimerWorker(TimeUnit.SECONDS.toMillis(selectedDuration.toLong()))
                showNotification(selectedDuration.getFormattedDuration())
            }

            timer_reset.setOnClickListener {
                cancelTimerWorker()
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
        }

        initialSecs = config.timerSeconds

        WorkManager.getInstance(activity!!).getWorkInfoByIdLiveData(timerRequestId)
            .observe(this, Observer { workInfo ->
                Log.e("test", workInfo.toString())

                when (workInfo.state) {
                    WorkInfo.State.SUCCEEDED -> {
                        showNotification("-")
                    }
                    WorkInfo.State.FAILED -> {}
                    WorkInfo.State.CANCELLED -> {}
                    else -> {}
                }
            })

        val a = lifecycleScope.launch {
            (config.timerSeconds downTo 0).asFlow().onEach { delay(1000) }.collect {
                Log.e("test", it.toString())
                timer_time.text = it.getFormattedDuration()
            }
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        setupViews()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState.apply {
            putBoolean(WAS_RUNNING, isRunning)
            putInt(TOTAL_TICKS, totalTicks)
            putInt(CURRENT_TICKS, currentTicks)
        })
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.apply {
            isRunning = getBoolean(WAS_RUNNING, false)
            totalTicks = getInt(TOTAL_TICKS, 0)
            currentTicks = getInt(CURRENT_TICKS, 0)
        }
    }

    fun updateAlarmSound(alarmSound: AlarmSound) {
        requiredActivity.config.timerSoundTitle = alarmSound.title
        requiredActivity.config.timerSoundUri = alarmSound.uri
        view.timer_sound.text = alarmSound.title
    }

    private fun setupViews() {
        val config = requiredActivity.config
        val textColor = config.textColor

        view.apply {
            requiredActivity.updateTextColors(timer_fragment)
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
    }

    private fun updateIcons() {
        val drawableId = if (isRunning) R.drawable.ic_pause_vector else R.drawable.ic_play_vector
        val iconColor = if (requiredActivity.getAdjustedPrimaryColor() == Color.WHITE) Color.BLACK else requiredActivity.config.textColor
        view.timer_play_pause.setImageDrawable(resources.getColoredDrawableWithColor(drawableId, iconColor))
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun showNotification(formattedDuration: String) {
        val channelId = "simple_alarm_timer"
        val label = getString(R.string.timer)
        val notificationManager = requiredActivity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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

        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        notificationManager.notify(TIMER_NOTIF_ID, builder.build())
    }
}

//    private fun resetTimer() {
//        updateHandler.removeCallbacks(updateRunnable)
//        isRunning = false
//        currentTicks = 0
//        totalTicks = 0
//        initialSecs = context!!.config.timerSeconds
//        updateDisplayedText()
//        updateIcons()
//        view.timer_reset.beGone()
//        requiredActivity.hideTimerNotification()
//        context!!.hideNotification(TIMER_NOTIF_ID)
//        context?.toast(R.string.timer_stopped)
//    }

//    private fun updateDisplayedText(): Boolean {
//        val diff = initialSecs - totalTicks
//        var formattedDuration = Math.abs(diff).getFormattedDuration()
//
//        if (diff < 0) {
//            formattedDuration = "-$formattedDuration"
//            if (!isForegrounded) {
//                resetTimer()
//                return false
//            }
//        }
//
//        view.timer_time.text = formattedDuration
//        if (diff == 0) {
//            if (context?.isScreenOn() == true) {
//                context!!.showTimerNotification(false)
//                Handler().postDelayed({
//                    context?.hideTimerNotification()
//                }, context?.config!!.timerMaxReminderSecs * 1000L)
//            } else {
//                Intent(context, ReminderActivity::class.java).apply {
//                    activity?.startActivity(this)
//                }
//            }
//        } else if (diff > 0 && !isForegrounded && isRunning) {
//            showNotification(formattedDuration)
//        }
//
//        return true
//    }
