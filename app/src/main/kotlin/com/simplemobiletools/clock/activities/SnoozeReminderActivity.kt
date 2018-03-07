package com.simplemobiletools.clock.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.simplemobiletools.clock.extensions.config
import com.simplemobiletools.clock.extensions.dbHelper
import com.simplemobiletools.clock.extensions.hideNotification
import com.simplemobiletools.clock.extensions.setupAlarmClock
import com.simplemobiletools.clock.helpers.ALARM_ID
import com.simplemobiletools.commons.extensions.showPickIntervalDialog

class SnoozeReminderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = intent.getIntExtra(ALARM_ID, -1)
        val alarm = dbHelper.getAlarmWithId(id) ?: return
        hideNotification(id)
        showPickIntervalDialog(config.snoozeTime, true, cancelCallback = { dialogCancelled() }) {
            config.snoozeTime = it
            setupAlarmClock(alarm, it * 60)
            finishActivity()
        }
    }

    private fun dialogCancelled() {
        finishActivity()
    }

    private fun finishActivity() {
        finish()
        overridePendingTransition(0, 0)
    }
}
