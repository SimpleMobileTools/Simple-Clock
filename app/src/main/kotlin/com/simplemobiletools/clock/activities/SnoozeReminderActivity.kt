package com.simplemobiletools.clock.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.simplemobiletools.clock.extensions.config
import com.simplemobiletools.clock.extensions.dbHelper
import com.simplemobiletools.clock.extensions.hideNotification
import com.simplemobiletools.clock.extensions.setupAlarmClock
import com.simplemobiletools.clock.helpers.ALARM_ID
import com.simplemobiletools.clock.helpers.HIDE_REMINDER_ACTIVITY
import com.simplemobiletools.commons.extensions.showPickSecondsDialog
import com.simplemobiletools.commons.helpers.MINUTE_SECONDS

class SnoozeReminderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = intent.getIntExtra(ALARM_ID, -1)
        val alarm = dbHelper.getAlarmWithId(id) ?: return
        hideNotification(id)
        showPickSecondsDialog(config.snoozeTime * MINUTE_SECONDS, true, cancelCallback = { dialogCancelled() }) {
            config.snoozeTime = it * 60
            setupAlarmClock(alarm, it)
            finishActivity()
        }
    }

    private fun dialogCancelled() {
        checkReminderActivityHiding()
        finishActivity()
    }

    private fun finishActivity() {
        checkReminderActivityHiding()
        finish()
        overridePendingTransition(0, 0)
    }

    private fun checkReminderActivityHiding() {
        if (intent.getBooleanExtra(HIDE_REMINDER_ACTIVITY, false)) {
            Intent(this, ReminderActivity::class.java).apply {
                startActivity(this)
            }
        }
    }
}
