package com.simplemobiletools.clock.activities

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.SeekBar
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.extensions.config
import com.simplemobiletools.clock.helpers.MyDigitalTimeWidgetProvider
import com.simplemobiletools.commons.dialogs.ColorPickerDialog
import com.simplemobiletools.commons.dialogs.FeatureLockedDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.IS_CUSTOMIZING_COLORS
import kotlinx.android.synthetic.main.widget_config_digital.*

class WidgetDigitalConfigureActivity : SimpleActivity() {
    private var mBgAlpha = 0f
    private var mWidgetId = 0
    private var mBgColor = 0
    private var mTextColor = 0
    private var mBgColorWithoutTransparency = 0
    private var mFeatureLockedDialog: FeatureLockedDialog? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        useDynamicTheme = false
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)
        setContentView(R.layout.widget_config_digital)
        initVariables()

        val isCustomizingColors = intent.extras?.getBoolean(IS_CUSTOMIZING_COLORS) ?: false
        mWidgetId = intent.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (mWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID && !isCustomizingColors) {
            finish()
        }

        config_digital_save.setOnClickListener { saveConfig() }
        config_digital_bg_color.setOnClickListener { pickBackgroundColor() }
        config_digital_text_color.setOnClickListener { pickTextColor() }

        val primaryColor = getProperPrimaryColor()
        config_digital_bg_seekbar.setColors(mTextColor, primaryColor, primaryColor)

        if (!isCustomizingColors && !isOrWasThankYouInstalled()) {
            mFeatureLockedDialog = FeatureLockedDialog(this) {
                if (!isOrWasThankYouInstalled()) {
                    finish()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setupToolbar(config_toolbar)
        if (mFeatureLockedDialog != null && isOrWasThankYouInstalled()) {
            mFeatureLockedDialog?.dismissDialog()
        }
    }

    private fun initVariables() {
        mBgColor = config.widgetBgColor
        mBgAlpha = Color.alpha(mBgColor) / 255.toFloat()
        mBgColorWithoutTransparency = Color.rgb(Color.red(mBgColor), Color.green(mBgColor), Color.blue(mBgColor))

        config_digital_bg_seekbar.setOnSeekBarChangeListener(bgSeekbarChangeListener)
        config_digital_bg_seekbar.progress = (mBgAlpha * 100).toInt()
        updateBackgroundColor()

        mTextColor = config.widgetTextColor
        updateTextColor()
    }

    private fun saveConfig() {
        storeWidgetColors()
        requestWidgetUpdate()

        Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetId)
            setResult(Activity.RESULT_OK, this)
        }
        finish()
    }

    private fun storeWidgetColors() {
        config.apply {
            widgetBgColor = mBgColor
            widgetTextColor = mTextColor
        }
    }

    private fun pickBackgroundColor() {
        ColorPickerDialog(this, mBgColorWithoutTransparency) { wasPositivePressed, color ->
            if (wasPositivePressed) {
                mBgColorWithoutTransparency = color
                updateBackgroundColor()
            }
        }
    }

    private fun pickTextColor() {
        ColorPickerDialog(this, mTextColor) { wasPositivePressed, color ->
            if (wasPositivePressed) {
                mTextColor = color
                updateTextColor()
            }
        }
    }

    private fun requestWidgetUpdate() {
        Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null, this, MyDigitalTimeWidgetProvider::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(mWidgetId))
            sendBroadcast(this)
        }
    }

    private fun updateTextColor() {
        config_digital_text_color.setFillWithStroke(mTextColor, mTextColor)
        config_digital_time.setTextColor(mTextColor)
        config_digital_date.setTextColor(mTextColor)
        config_digital_save.setTextColor(getProperPrimaryColor().getContrastColor())
    }

    private fun updateBackgroundColor() {
        mBgColor = mBgColorWithoutTransparency.adjustAlpha(mBgAlpha)
        config_digital_bg_color.setFillWithStroke(mBgColor, mBgColor)
        config_digital_background.applyColorFilter(mBgColor)
        config_digital_save.backgroundTintList = ColorStateList.valueOf(getProperPrimaryColor())
    }

    private val bgSeekbarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            mBgAlpha = progress.toFloat() / 100.toFloat()
            updateBackgroundColor()
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {}

        override fun onStopTrackingTouch(seekBar: SeekBar) {}
    }
}
