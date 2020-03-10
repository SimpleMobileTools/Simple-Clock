package com.simplemobiletools.clock.services

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.extensions.config
import com.simplemobiletools.clock.extensions.getOpenTimerTabIntent
import com.simplemobiletools.clock.helpers.TIMER_RUNNING_NOTIF_ID
import com.simplemobiletools.commons.extensions.getFormattedDuration
import com.simplemobiletools.commons.helpers.isOreoPlus
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class TimerService : Service() {

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

        return START_NOT_STICKY
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: TimerStopService) {
        stopService()
    }

    private fun stopService() {
        if (isOreoPlus()) {
            stopForeground(true)
        } else {
            stopSelf()
        }
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
            val importance = NotificationManager.IMPORTANCE_DEFAULT
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
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setSound(null)
                .setOngoing(true)
                .setAutoCancel(true)
                .setChannelId(channelId)

        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        return builder.build()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun startTimerService(context: Context) {
    if (isOreoPlus()) {
        context.startForegroundService(Intent(context, TimerService::class.java))
    } else {
        context.startService(Intent(context, TimerService::class.java))
    }
}

object TimerStopService
