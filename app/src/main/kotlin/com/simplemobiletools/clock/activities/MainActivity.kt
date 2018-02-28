package com.simplemobiletools.clock.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import com.simplemobiletools.clock.BuildConfig
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.extensions.config
import com.simplemobiletools.commons.extensions.restartActivity
import com.simplemobiletools.commons.helpers.LICENSE_KOTLIN
import com.simplemobiletools.commons.models.FAQItem

class MainActivity : SimpleActivity() {
    private var storedUseEnglish = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        storeStateVariables()
    }

    override fun onResume() {
        super.onResume()
        if (storedUseEnglish != config.useEnglish) {
            restartActivity()
            return
        }

        if (config.preventPhoneFromSleeping) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    override fun onPause() {
        super.onPause()
        storeStateVariables()
        if (config.preventPhoneFromSleeping) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> launchSettings()
            R.id.about -> launchAbout()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun storeStateVariables() {
        storedUseEnglish = config.useEnglish
    }

    private fun launchSettings() {
        startActivity(Intent(applicationContext, SettingsActivity::class.java))
    }

    private fun launchAbout() {
        val faqItems = arrayListOf(
                FAQItem(R.string.faq_1_title_commons, R.string.faq_1_text_commons),
                FAQItem(R.string.faq_2_title_commons, R.string.faq_2_text_commons),
                FAQItem(R.string.faq_4_title_commons, R.string.faq_4_text_commons)
        )

        startAboutActivity(R.string.app_name, LICENSE_KOTLIN, BuildConfig.VERSION_NAME, faqItems)
    }
}
