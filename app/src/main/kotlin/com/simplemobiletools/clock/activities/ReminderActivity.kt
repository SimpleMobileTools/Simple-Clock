package com.simplemobiletools.clock.activities

import android.os.Bundle
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.extensions.hideTimerNotification
import com.simplemobiletools.clock.extensions.showOverLockscreen
import com.simplemobiletools.clock.extensions.showTimerNotification
import com.simplemobiletools.commons.extensions.updateTextColors
import kotlinx.android.synthetic.main.activity_reminder.*

class ReminderActivity : SimpleActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder)
        showOverLockscreen()
        updateTextColors(reminder_holder)
        reminder_title.text = getString(R.string.timer)
        reminder_text.text = getString(R.string.time_expired)
        showTimerNotification()
    }

    override fun onDestroy() {
        super.onDestroy()
        hideTimerNotification()
    }
}
