package com.simplemobiletools.clock.receivers

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import androidx.core.app.NotificationCompat
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.activities.ReminderActivity
import com.simplemobiletools.clock.extensions.*
import com.simplemobiletools.clock.helpers.ALARM_ID
import com.simplemobiletools.clock.helpers.ALARM_NOTIF_ID
import com.simplemobiletools.commons.helpers.isOreoPlus

class AlarmReceiver : BroadcastReceiver() {

    @SuppressLint("NewApi")
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra(ALARM_ID, -1)
        val alarm = context.dbHelper.getAlarmWithId(id) ?: return

        if (context.isScreenOn()) {
            context.showAlarmNotification(alarm)
            Handler().postDelayed({
                context.hideNotification(id)
            }, context.config.alarmMaxReminderSecs * 1000L)
        } else {
            if (isOreoPlus()) {
                val notificationManager = context.getSystemService(NotificationManager::class.java)
                if (notificationManager.getNotificationChannel("Alarm") == null) {
                    val channel = NotificationChannel("Alarm", "Alarm", NotificationManager.IMPORTANCE_HIGH)
                    channel.setBypassDnd(true)
                    notificationManager.createNotificationChannel(channel)
                }

                val pendingIntent = PendingIntent.getActivity(context, 0, Intent(context, ReminderActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    putExtra(ALARM_ID, id)
                }, PendingIntent.FLAG_UPDATE_CURRENT)

                val builder = NotificationCompat.Builder(context, "Alarm")
                        .setSmallIcon(R.drawable.ic_alarm_vector)
                        .setContentTitle(context.getString(R.string.alarm))
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_ALARM)
                        .setFullScreenIntent(pendingIntent, true)

                notificationManager.notify(ALARM_NOTIF_ID, builder.build())
            } else {
                Intent(context, ReminderActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    putExtra(ALARM_ID, id)
                    context.startActivity(this)
                }
            }
        }
    }
}
