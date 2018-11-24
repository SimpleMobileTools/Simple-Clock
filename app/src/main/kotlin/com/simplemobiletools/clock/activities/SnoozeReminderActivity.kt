package com.simplemobiletools.clock.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.simplemobiletools.clock.extensions.*
import com.simplemobiletools.clock.helpers.ALARM_ID
import com.simplemobiletools.clock.helpers.NOISE_CONTROL_KILL
import com.simplemobiletools.clock.helpers.NOISE_CONTROL_PAUSE
import com.simplemobiletools.clock.helpers.UID_ALARM_NOTIFICATION
import com.simplemobiletools.commons.extensions.showPickSecondsDialog
import com.simplemobiletools.commons.helpers.MINUTE_SECONDS

class SnoozeReminderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = intent.getIntExtra(ALARM_ID, -1)
        val alarm = dbHelper.getAlarmWithId(id) ?: return
        hideNotification(id)
        this.sendNoiseControlIntent(UID_ALARM_NOTIFICATION, NOISE_CONTROL_PAUSE, id)
        showPickSecondsDialog(config.snoozeTime * MINUTE_SECONDS, true, cancelCallback = { dialogCancelled() }) {
            config.snoozeTime = it / MINUTE_SECONDS
            setupAlarmClock(alarm, it, false)
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
