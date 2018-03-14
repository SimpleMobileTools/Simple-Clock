package com.simplemobiletools.clock.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.extensions.*
import com.simplemobiletools.clock.helpers.ALARM_ID
import com.simplemobiletools.clock.models.Alarm
import com.simplemobiletools.commons.extensions.getAdjustedPrimaryColor
import com.simplemobiletools.commons.extensions.getColoredDrawableWithColor
import com.simplemobiletools.commons.extensions.updateTextColors
import kotlinx.android.synthetic.main.activity_reminder.*

class ReminderActivity : SimpleActivity() {
    private val hideNotificationHandler = Handler()
    private var isAlarmReminder = false
    private var alarm: Alarm? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder)
        showOverLockscreen()
        updateTextColors(reminder_holder)

        val id = intent.getIntExtra(ALARM_ID, -1)
        isAlarmReminder = id != -1
        if (id != -1) {
            alarm = dbHelper.getAlarmWithId(id) ?: return
        }

        reminder_title.text = getString(if (isAlarmReminder) R.string.alarm else R.string.timer)
        reminder_text.text = if (isAlarmReminder) getFormattedTime(alarm!!.timeInMinutes * 60, false, false) else getString(R.string.time_expired)
        reminder_stop.background = resources.getColoredDrawableWithColor(R.drawable.circle_background_filled, getAdjustedPrimaryColor())
        reminder_stop.setOnClickListener {
            finish()
        }

        Handler().postDelayed({
            if (isAlarmReminder) {
                showAlarmNotification(alarm!!)
            } else {
                showTimerNotification(true)
            }

            val maxDuration = if (isAlarmReminder) config.alarmMaxReminderSecs else config.timerMaxReminderSecs
            hideNotificationHandler.postDelayed({
                finish()
            }, maxDuration * 1000L)
        }, 1000L)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        finish()
    }

    override fun onStop() {
        super.onStop()
        if (isAlarmReminder) {
            hideNotification(alarm?.id ?: 0)
        } else {
            hideTimerNotification()
        }
        hideNotificationHandler.removeCallbacksAndMessages(null)
    }
}
