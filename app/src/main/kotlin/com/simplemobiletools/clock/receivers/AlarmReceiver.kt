package com.simplemobiletools.clock.receivers

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Handler
import androidx.core.app.NotificationCompat
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.activities.ReminderActivity
import com.simplemobiletools.clock.activities.SplashActivity
import com.simplemobiletools.clock.extensions.*
import com.simplemobiletools.clock.helpers.ALARM_ID
import com.simplemobiletools.clock.helpers.ALARM_NOTIF_ID
import com.simplemobiletools.commons.extensions.showErrorToast
import com.simplemobiletools.commons.helpers.isOreoPlus

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra(ALARM_ID, -1)
        val alarm = context.dbHelper.getAlarmWithId(id) ?: return
        val isUpcomingAlarm = context.dbHelper.getAlarmWithParentId(id) == null
        val alarmReminderSecs = if (isUpcomingAlarm) {
            context.config.alarmMaxReminderSecs * 1000L
        }
        else {
            context.config.upcomingAlarmMaxReminderSecs.toLong()
        }

        if (context.isScreenOn()) {
            context.showAlarmNotification(alarm)
            Handler().postDelayed({
                context.hideNotification(id)
            }, alarmReminderSecs)
        } else {
            if (isOreoPlus()) {
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()

                val notificationManager = context.getSystemService(NotificationManager::class.java)
                if (notificationManager.getNotificationChannel("Alarm") == null) {
                    NotificationChannel("Alarm", "Alarm", NotificationManager.IMPORTANCE_HIGH).apply {
                        setBypassDnd(true)
                        if(!isUpcomingAlarm) {
                            setSound(Uri.parse(alarm.soundUri), audioAttributes)
                        }
                        notificationManager.createNotificationChannel(this)
                    }
                }

                var pendingIntent = PendingIntent.getActivity(context, 0, Intent(context, ReminderActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    putExtra(ALARM_ID, id)
                }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

                if(isUpcomingAlarm) {
                    pendingIntent = PendingIntent.getActivity(context, 0, Intent(context, SplashActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                }

                val builder = NotificationCompat.Builder(context, "Alarm")
                    .setSmallIcon(R.drawable.ic_alarm_vector)
                    .setContentTitle(context.getString(R.string.alarm))
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setFullScreenIntent(pendingIntent, true)

                try {
                    notificationManager.notify(ALARM_NOTIF_ID, builder.build())
                } catch (e: Exception) {
                    context.showErrorToast(e)
                }
            } else {
                if(isUpcomingAlarm) {
                    Intent(context, SplashActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        putExtra(ALARM_ID, id)
                        context.startActivity(this)
                    }
                }else{
                    Intent(context, ReminderActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        putExtra(ALARM_ID, id)
                        context.startActivity(this)
                    }
                }
            }
        }
    }
}
