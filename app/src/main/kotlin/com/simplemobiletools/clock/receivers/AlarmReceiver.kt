package com.simplemobiletools.clock.receivers

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Handler
import android.support.v4.app.NotificationCompat
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.activities.MainActivity
import com.simplemobiletools.clock.activities.SnoozeReminderActivity
import com.simplemobiletools.clock.extensions.*
import com.simplemobiletools.clock.helpers.ALARM_ID
import com.simplemobiletools.clock.models.Alarm
import com.simplemobiletools.clock.services.SnoozeService
import com.simplemobiletools.commons.extensions.getAdjustedPrimaryColor
import com.simplemobiletools.commons.helpers.isLollipopPlus
import com.simplemobiletools.commons.helpers.isOreoPlus

class AlarmReceiver : BroadcastReceiver() {
    private val NOTIFICATION_DISAPPEAR_MS = 600000L

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra(ALARM_ID, -1)
        val alarm = context.dbHelper.getAlarmWithId(id) ?: return

        val pendingIntent = getPendingIntent(context, alarm)
        val notification = getNotification(context, pendingIntent, alarm)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(alarm.id, notification)
        context.scheduleNextAlarm(alarm, false)

        Handler().postDelayed({
            context.hideNotification(id)
        }, NOTIFICATION_DISAPPEAR_MS)
    }

    @SuppressLint("NewApi")
    private fun getNotification(context: Context, pendingIntent: PendingIntent, alarm: Alarm): Notification {
        val channelId = "alarm_channel"
        if (isOreoPlus()) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val name = context.resources.getString(R.string.alarm)
            val importance = NotificationManager.IMPORTANCE_HIGH
            NotificationChannel(channelId, name, importance).apply {
                enableLights(true)
                lightColor = context.getAdjustedPrimaryColor()
                enableVibration(alarm.vibrate)
                notificationManager.createNotificationChannel(this)
            }
        }

        val builder = NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.alarm))
                .setContentText(alarm.timeInMinutes.formatAlarmTime())
                .setSmallIcon(R.drawable.ic_alarm)
                .setContentIntent(pendingIntent)
                .setPriority(Notification.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .setAutoCancel(true)
                .setSound(Uri.parse(alarm.soundUri), AudioManager.STREAM_ALARM)
                .setChannelId(channelId)
                .addAction(R.drawable.ic_snooze, context.getString(R.string.snooze), getSnoozePendingIntent(context, alarm))

        if (isLollipopPlus()) {
            builder.setVisibility(Notification.VISIBILITY_PUBLIC)
        }

        if (alarm.vibrate) {
            val vibrateArray = LongArray(1200) { 500 }
            builder.setVibrate(vibrateArray)
        }

        return builder.build()
    }

    private fun getSnoozePendingIntent(context: Context, alarm: Alarm): PendingIntent {
        val snoozeClass = if (context.config.useSameSnooze) SnoozeService::class.java else SnoozeReminderActivity::class.java
        val intent = Intent(context, snoozeClass).setAction("Snooze")
        intent.putExtra(ALARM_ID, alarm.id)
        return if (context.config.useSameSnooze) {
            PendingIntent.getService(context, alarm.id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        } else {
            PendingIntent.getActivity(context, alarm.id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }

    private fun getPendingIntent(context: Context, alarm: Alarm): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
        return PendingIntent.getActivity(context, alarm.id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
}
