package com.simplemobiletools.clock.activities

import android.os.Bundle
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.extensions.showOverLockscreen
import com.simplemobiletools.clock.helpers.REMINDER_TEXT
import com.simplemobiletools.clock.helpers.REMINDER_TITLE
import com.simplemobiletools.commons.extensions.updateTextColors
import kotlinx.android.synthetic.main.activity_reminder.*

class ReminderActivity : SimpleActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder)
        showOverLockscreen()
        updateTextColors(reminder_holder)
        reminder_title.text = intent.getStringExtra(REMINDER_TITLE)
        reminder_text.text = intent.getStringExtra(REMINDER_TEXT)
    }
}
