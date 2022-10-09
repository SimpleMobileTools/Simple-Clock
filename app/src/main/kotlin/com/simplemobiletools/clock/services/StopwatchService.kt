package com.simplemobiletools.clock.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.extensions.getFormattedDuration
import com.simplemobiletools.clock.extensions.getOpenStopwatchTabIntent
import com.simplemobiletools.clock.helpers.STOPWATCH_RUNNING_NOTIF_ID
import com.simplemobiletools.clock.helpers.Stopwatch
import com.simplemobiletools.clock.helpers.Stopwatch.State
import com.simplemobiletools.clock.helpers.Stopwatch.UpdateListener
import com.simplemobiletools.commons.helpers.isNougatPlus
import com.simplemobiletools.commons.helpers.isOreoPlus
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class StopwatchService : Service() {

    private val bus = EventBus.getDefault()
    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private var isStopping = false

    override fun onCreate() {
        super.onCreate()
        bus.register(this)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationBuilder = getServiceNotificationBuilder(
            getString(R.string.app_name),
            getString(R.string.stopwatch)
        )
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        isStopping = false
        startForeground(
            STOPWATCH_RUNNING_NOTIF_ID,
            notificationBuilder.build()
        )
        Stopwatch.addUpdateListener(updateListener)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        bus.unregister(this)
        Stopwatch.removeUpdateListener(updateListener)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: StopwatchStopService) {
        isStopping = true
        stopForeground(true)
    }

    private fun getServiceNotificationBuilder(
        title: String,
        contentText: String
    ): NotificationCompat.Builder {
        val channelId = "simple_alarm_stopwatch"
        val label = getString(R.string.stopwatch)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        if (isOreoPlus()) {
            NotificationChannel(channelId, label, importance).apply {
                setSound(null, null)
                notificationManager.createNotificationChannel(this)
            }
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_stopwatch_vector)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSound(null)
            .setOngoing(true)
            .setAutoCancel(true)
            .setContentIntent(getOpenStopwatchTabIntent())
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
    }

    private fun updateNotification(totalTime: Long) {
        val formattedDuration = totalTime.getFormattedDuration()
        notificationBuilder.setContentTitle(formattedDuration).setContentText(getString(R.string.stopwatch))
        notificationManager.notify(STOPWATCH_RUNNING_NOTIF_ID, notificationBuilder.build())
    }

    private val updateListener = object : UpdateListener {
        override fun onUpdate(totalTime: Long, lapTime: Long, useLongerMSFormat: Boolean) {
            if (!isStopping) {
                updateNotification(totalTime)
            }
        }

        override fun onStateChanged(state: State) {
            if (state == State.STOPPED && isNougatPlus()) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            }
        }
    }
}

fun startStopwatchService(context: Context) {
    Handler(Looper.getMainLooper()).post {
        ContextCompat.startForegroundService(context, Intent(context, StopwatchService::class.java))
    }
}

object StopwatchStopService
