package com.simplemobiletools.clock.activities

import android.content.Intent
import com.simplemobiletools.clock.helpers.*
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

            intent?.action == STOPWATCH_TOGGLE_ACTION -> {
                Intent(this, MainActivity::class.java).apply {
                    putExtra(OPEN_TAB, TAB_STOPWATCH)
                    putExtra(TOGGLE_STOPWATCH, intent.getBooleanExtra(TOGGLE_STOPWATCH, false))
                    startActivity(this)
                }
            }

            intent.extras?.containsKey(OPEN_TAB) == true -> {
                Intent(this, MainActivity::class.java).apply {
                    putExtra(OPEN_TAB, intent.getIntExtra(OPEN_TAB, TAB_CLOCK))
                    putExtra(TIMER_ID, intent.getIntExtra(TIMER_ID, INVALID_TIMER_ID))
                    startActivity(this)
                }
            }

            else -> startActivity(Intent(this, MainActivity::class.java))
        }
        finish()
    }
}
