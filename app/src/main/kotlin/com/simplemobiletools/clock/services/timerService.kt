package com.simplemobiletools.clock.services

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.extensions.config
import com.simplemobiletools.clock.extensions.getOpenTimerTabIntent
import com.simplemobiletools.clock.extensions.getTimerNotification
import com.simplemobiletools.clock.extensions.secondsToMillis
import com.simplemobiletools.clock.helpers.TIMER_NOTIF_ID
import com.simplemobiletools.clock.helpers.TIMER_RUNNING_NOTIF_ID
import com.simplemobiletools.commons.extensions.getFormattedDuration
import com.simplemobiletools.commons.helpers.isOreoPlus
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

fun Context.startTimerService() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(Intent(this, TimerService::class.java))
    } else {
        startService(Intent(this, TimerService::class.java))
    }
}

class TimerService : Service() {

    private var timer: CountDownTimer? = null
    private var lastTick = 0L
    private val bus = EventBus.getDefault()

    override fun onCreate() {
        super.onCreate()
        bus.register(this)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        val formattedDuration = config.timerSeconds.getFormattedDuration()
        startForeground(TIMER_RUNNING_NOTIF_ID, notification(formattedDuration))

        when (val state = config.timerState) {
            is TimerState.Idle -> bus.post(TimerState.Start(config.timerSeconds.secondsToMillis))
            is TimerState.Paused -> bus.post(TimerState.Start(state.tick))
            is TimerState.Running -> bus.post(TimerState.Pause(state.tick))
            else -> {}
        }

        return START_NOT_STICKY
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(state: TimerState.Idle) {
        config.timerState = state
        timer?.cancel()
        stopService()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(state: TimerState.Start) {
        timer = object : CountDownTimer(state.duration, 1000) {
            override fun onTick(tick: Long) {
                lastTick = tick

                val newState = TimerState.Running(state.duration, tick)
                bus.post(newState)
                config.timerState = newState
            }

            override fun onFinish() {
                bus.post(TimerState.Finish(state.duration))
            }
        }.start()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: TimerState.Finish) {
        val pendingIntent = getOpenTimerTabIntent()
        val notification = getTimerNotification(pendingIntent, false) //MAYBE IN FUTURE ADD TIME TO NOTIFICATION
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(TIMER_NOTIF_ID, notification)

        bus.post(TimerState.Idle)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: TimerState.Pause) {
        bus.post(TimerState.Paused(event.duration, lastTick))
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(state: TimerState.Paused) {
        config.timerState = state
        timer?.cancel()
        stopService()
    }

    private fun stopService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) stopForeground(true)
        else stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        bus.unregister(this)
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun notification(formattedDuration: String): Notification {
        val channelId = "simple_alarm_timer"
        val label = getString(R.string.timer)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (isOreoPlus()) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            NotificationChannel(channelId, label, importance).apply {
                setSound(null, null)
                notificationManager.createNotificationChannel(this)
            }
        }

        val builder = NotificationCompat.Builder(this)
                .setContentTitle(label)
                .setContentText(formattedDuration)
                .setSmallIcon(R.drawable.ic_timer)
                .setContentIntent(this.getOpenTimerTabIntent())
                .setPriority(Notification.PRIORITY_HIGH)
                .setSound(null)
                .setOngoing(true)
                .setAutoCancel(true)
                .setChannelId(channelId)

        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        return builder.build()
    }
}

data class StateWrapper(val state: TimerState)

sealed class TimerState {
    object Idle: TimerState()
    data class Start(val duration: Long): TimerState()
    data class Running(val duration: Long, val tick: Long): TimerState()
    data class Pause(val duration: Long): TimerState()
    data class Paused(val duration: Long, val tick: Long): TimerState()
    data class Finish(val duration: Long): TimerState()
}
