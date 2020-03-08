package com.simplemobiletools.clock.fragments

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.activities.SimpleActivity
import com.simplemobiletools.clock.dialogs.MyTimePickerDialogDialog
import com.simplemobiletools.clock.extensions.*
import com.simplemobiletools.clock.helpers.Config
import com.simplemobiletools.clock.helpers.PICK_AUDIO_FILE_INTENT_ID
import com.simplemobiletools.clock.helpers.TIMER_NOTIF_ID
import com.simplemobiletools.clock.workers.*
import com.simplemobiletools.commons.dialogs.SelectAlarmSoundDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.ALARM_SOUND_TYPE_ALARM
import com.simplemobiletools.commons.helpers.isOreoPlus
import com.simplemobiletools.commons.models.AlarmSound
import kotlinx.android.synthetic.main.fragment_timer.view.*
import java.util.concurrent.TimeUnit


class TimerFragment : Fragment() {

    lateinit var view: ViewGroup
    private var timer: CountDownTimer? = null
    private var isRunning = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        view = (inflater.inflate(R.layout.fragment_timer, container, false) as ViewGroup).apply {
            val config = requiredActivity.config
            val textColor = config.textColor

            requiredActivity.updateTextColors(timer_fragment)
            timer_play_pause.background = resources.getColoredDrawableWithColor(R.drawable.circle_background_filled, context!!.getAdjustedPrimaryColor())
            timer_reset.applyColorFilter(textColor)

            timer_initial_time.text = config.timerSeconds.getFormattedDuration()
            timer_initial_time.colorLeftDrawable(textColor)

            timer_vibrate.isChecked = config.timerVibrate
            timer_vibrate.colorLeftDrawable(textColor)

            timer_sound.text = config.timerSoundTitle
            timer_sound.colorLeftDrawable(textColor)

            timer_time.setOnClickListener {
                if (isRunning) {
                    pauseTimer(config)
                } else {
                    startTimer(config)
                }
            }

            timer_play_pause.setOnClickListener {
                if (isRunning) {
                    pauseTimer(config)
                } else {
                    startTimer(config)
                }
            }

            timer_reset.setOnClickListener {
                cancelTimerWorker()
                requiredActivity.hideTimerNotification()

                config.timerTickStamp = 0L
                config.timerStartStamp = 0L
                requiredActivity.toast(R.string.timer_stopped)
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

            WorkManager.getInstance(requiredActivity).getWorkInfosByTagLiveData(TIMER_WORKER_KEY).observe(requiredActivity, Observer { workInfo ->
                val workerState = workInfo?.firstOrNull()?.state
                isRunning = (workerState == WorkInfo.State.ENQUEUED)

                updateIcons(isRunning)
                timer_reset.beVisibleIf(isRunning)
                timer?.cancel()

                when (workerState) {
                    WorkInfo.State.ENQUEUED -> {
                        val duration = config.timerSeconds.toLong() * 1000 //MS

                        timer = object : CountDownTimer(duration, 1000) {
                            override fun onTick(millisUntilFinished: Long) {
                                timer_time.text = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished).toInt().getFormattedDuration()
                            }

                            override fun onFinish() {}
                        }.start()
                    }

                    else -> {
                        timer_time.text = 0.getFormattedDuration()
                    }
                }
            })

            cancelTimerWorker()
        }

        return view
    }

    private fun startTimer(config: Config) {
        val isTimerNoTick = config.timerTickStamp == 0L

        if (isTimerNoTick) {
            config.timerStartStamp = System.currentTimeMillis()

            val selectedDuration = config.timerSeconds
            val formattedTimestamp = config.timerStartStamp.timestampFormat("HH:mm:ss")

            enqueueTimerWorker(TimeUnit.SECONDS.toMillis(selectedDuration.toLong()))
            showNotification("(${selectedDuration.getFormattedDuration()}) $formattedTimestamp")
        } else {
            val duration = config.timerSeconds.toLong() * 1000 //MS
            val selectedDuration = (config.timerStartStamp + duration) - (config.timerTickStamp - config.timerStartStamp)
            val formattedTimestamp = config.timerStartStamp.timestampFormat("HH:mm:ss")

            enqueueTimerWorker(TimeUnit.SECONDS.toMillis(selectedDuration.toLong()))
            showNotification("(${selectedDuration.toInt().getFormattedDuration()}) $formattedTimestamp")
        }
    }

    private fun pauseTimer(config: Config) {
        cancelTimerWorker()
        requiredActivity.hideTimerNotification()

        config.timerTickStamp = System.currentTimeMillis()

        val tick = config.timerTickStamp
        val duration = config.timerSeconds.toLong() * 1000 //MS
        val startedAt = config.timerStartStamp
        val distance = duration - (tick - startedAt)
        view.timer_time.text = distance.toInt().getFormattedDuration()
    }

    fun updateAlarmSound(alarmSound: AlarmSound) {
        requiredActivity.config.timerSoundTitle = alarmSound.title
        requiredActivity.config.timerSoundUri = alarmSound.uri
        view.timer_sound.text = alarmSound.title
    }

    private fun updateIcons(isRunning: Boolean) {
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
