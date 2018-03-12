package com.simplemobiletools.clock.fragments

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.support.v4.app.Fragment
import android.support.v4.app.NotificationCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.activities.SimpleActivity
import com.simplemobiletools.clock.activities.SplashActivity
import com.simplemobiletools.clock.dialogs.MyTimePickerDialogDialog
import com.simplemobiletools.clock.dialogs.SelectAlarmSoundDialog
import com.simplemobiletools.clock.extensions.colorLeftDrawable
import com.simplemobiletools.clock.extensions.config
import com.simplemobiletools.clock.extensions.hideNotification
import com.simplemobiletools.clock.helpers.OPEN_TAB
import com.simplemobiletools.clock.helpers.TAB_TIMER
import com.simplemobiletools.clock.helpers.TIMER_NOTIF_ID
import com.simplemobiletools.clock.receivers.TimerReceiver
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.isLollipopPlus
import com.simplemobiletools.commons.helpers.isOreoPlus
import kotlinx.android.synthetic.main.fragment_timer.view.*

class TimerFragment : Fragment() {
    private val UPDATE_INTERVAL = 1000L

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
                resetTimer()
            }

            timer_initial_time.setOnClickListener {
                MyTimePickerDialogDialog(activity as SimpleActivity, config.timerSeconds) {
                    config.timerSeconds = it
                    timer_initial_time.text = it.getFormattedDuration()
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
                SelectAlarmSoundDialog(activity as SimpleActivity, config.timerSoundUri, AudioManager.STREAM_SYSTEM) {
                    config.timerSoundTitle = it.title
                    config.timerSoundUri = it.uri
                    timer_sound.text = it.title
                }
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
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isRunning) {
            context?.toast(R.string.timer_stopped)
        }
        isRunning = false
        updateHandler.removeCallbacks(updateRunnable)
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
        updateIcons()

        if (isRunning) {
            updateHandler.post(updateRunnable)
            uptimeAtStart = SystemClock.uptimeMillis()
            view.timer_reset.beVisible()
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
            val pendingIntent = getOpenAppIntent(context!!)
            val notification = getNotification(context!!, pendingIntent)
            val notificationManager = context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(TIMER_NOTIF_ID, notification)

            Handler().postDelayed({
                context?.hideNotification(TIMER_NOTIF_ID)
            }, context?.config!!.timerMaxReminderSecs * 1000L)
        }
        return true
    }

    @SuppressLint("NewApi")
    private fun getNotification(context: Context, pendingIntent: PendingIntent): Notification {
        val channelId = "timer_channel"
        if (isOreoPlus()) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val name = context.resources.getString(R.string.timer)
            val importance = NotificationManager.IMPORTANCE_HIGH
            NotificationChannel(channelId, name, importance).apply {
                enableLights(true)
                lightColor = context.getAdjustedPrimaryColor()
                enableVibration(context.config.timerVibrate)
                notificationManager.createNotificationChannel(this)
            }
        }

        val builder = NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.timer))
                .setContentText(context.getString(R.string.time_expired))
                .setSmallIcon(R.drawable.ic_timer)
                .setContentIntent(pendingIntent)
                .setPriority(Notification.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .setAutoCancel(true)
                .setSound(Uri.parse(context.config.timerSoundUri), AudioManager.STREAM_SYSTEM)
                .setChannelId(channelId)
                .addAction(R.drawable.ic_cross, context.getString(R.string.dismiss), getTimerPendingIntent(context))

        if (isLollipopPlus()) {
            builder.setVisibility(Notification.VISIBILITY_PUBLIC)
        }

        if (context.config.timerVibrate) {
            val vibrateArray = LongArray(120) { 500 }
            builder.setVibrate(vibrateArray)
        }

        return builder.build()
    }

    private fun getTimerPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, TimerReceiver::class.java)
        return PendingIntent.getBroadcast(context, TIMER_NOTIF_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun getOpenAppIntent(context: Context): PendingIntent {
        val intent = Intent(context, SplashActivity::class.java)
        intent.putExtra(OPEN_TAB, TAB_TIMER)
        return PendingIntent.getActivity(context, TIMER_NOTIF_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT)
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
