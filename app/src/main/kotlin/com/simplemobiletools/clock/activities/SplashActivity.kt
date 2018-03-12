package com.simplemobiletools.clock.activities

import android.content.Intent
import com.simplemobiletools.clock.helpers.OPEN_TAB
import com.simplemobiletools.clock.helpers.TAB_ALARM
import com.simplemobiletools.commons.activities.BaseSplashActivity

class SplashActivity : BaseSplashActivity() {
    override fun initActivity() {
        when {
            intent?.action == "android.intent.action.SHOW_ALARMS" -> {
                Intent(this, MainActivity::class.java).apply {
                    putExtra(OPEN_TAB, TAB_ALARM)
                    startActivity(this)
                }
            }
            else -> startActivity(Intent(this, MainActivity::class.java))
        }
        finish()
    }
}
