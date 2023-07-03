package com.simplemobiletools.clock.receivers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.extensions.getDismissAlarmPendingIntent
import com.simplemobiletools.clock.extensions.getOpenAlarmTabIntent
import com.simplemobiletools.clock.helpers.*
import com.simplemobiletools.commons.helpers.isOreoPlus
import java.text.SimpleDateFormat
import java.util.*

class EarlyAlarmDismissalReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra(ALARM_ID, -1)
        if (alarmId == -1) {
            return
        }

        val alarmTime = intent.getIntExtra(ALARM_TIME, -1)
        triggerEarlyDismissalNotification(context, alarmTime, alarmId)
    }

    private fun triggerEarlyDismissalNotification(context: Context, alarmTime: Int, alarmId: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (isOreoPlus()) {
            NotificationChannel(EARLY_ALARM_DISMISSAL_CHANNEL_ID, context.getString(R.string.early_alarm_dismissal), NotificationManager.IMPORTANCE_DEFAULT).apply {
                setBypassDnd(true)
                setSound(null, null)
                notificationManager.createNotificationChannel(this)
            }
        }

        val dismissIntent = context.getDismissAlarmPendingIntent(alarmId, EARLY_ALARM_NOTIF_ID)
        val contentIntent = context.getOpenAlarmTabIntent()
        val notification = NotificationCompat.Builder(context)
            .setContentTitle(context.getString(R.string.upcoming_alarm))
            .setContentText(getNotificationTimeString(alarmTime))
            .setSmallIcon(R.drawable.ic_alarm_vector)
            .setPriority(Notification.PRIORITY_LOW)
            .addAction(0, context.getString(R.string.dismiss), dismissIntent)
            .setContentIntent(contentIntent)
            .setSound(null)
            .setAutoCancel(true)
            .setChannelId(EARLY_ALARM_DISMISSAL_CHANNEL_ID)
            .build()

        notificationManager.notify(EARLY_ALARM_NOTIF_ID, notification)
    }

    /**
     * Gets the time at which the alarm is going to fire.
     * eg: "Sun 1:30 pm"
     */
    private fun getNotificationTimeString(alarmTime: Int): String {
        val calendar = Calendar.getInstance()
        val triggerTime = ((alarmTime - getCurrentDayMinutes()) * 60) - calendar.get(Calendar.SECOND)
        val targetMs = System.currentTimeMillis() + (triggerTime * 1000)
        val sdf = SimpleDateFormat("EEE h:mm a", Locale.getDefault())
        return sdf.format(Date(targetMs))
    }

}
