package com.simplemobiletools.clock.activities

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.SeekBar
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.extensions.config
import com.simplemobiletools.clock.extensions.getFormattedDate
import com.simplemobiletools.clock.extensions.getFormattedTime
import com.simplemobiletools.clock.helpers.MyWidgetDateTimeProvider
import com.simplemobiletools.clock.helpers.getPassedSeconds
import com.simplemobiletools.commons.dialogs.ColorPickerDialog
import com.simplemobiletools.commons.extensions.adjustAlpha
import com.simplemobiletools.commons.extensions.setFillWithStroke
import com.simplemobiletools.commons.helpers.IS_CUSTOMIZING_COLORS
import kotlinx.android.synthetic.main.widget_config_date_time.*
import java.util.*

class WidgetDateTimeConfigureActivity : SimpleActivity() {
    private var mBgAlpha = 0f
    private var mWidgetId = 0
    private var mBgColorWithoutTransparency = 0
    private var mBgColor = 0
    private var mTextColorWithoutTransparency = 0
    private var mTextColor = 0

    public override fun onCreate(savedInstanceState: Bundle?) {
        useDynamicTheme = false
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)
        setContentView(R.layout.widget_config_date_time)
        initVariables()

        val isCustomizingColors = intent.extras?.getBoolean(IS_CUSTOMIZING_COLORS) ?: false
        mWidgetId = intent.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (mWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID && !isCustomizingColors) {
            finish()
        }

        config_save.setOnClickListener { saveConfig() }
        config_bg_color.setOnClickListener { pickBackgroundColor() }
        config_text_color.setOnClickListener { pickTextColor() }

        val primaryColor = config.primaryColor
        config_bg_seekbar.setColors(mTextColor, primaryColor, primaryColor)
    }

    override fun onResume() {
        super.onResume()
        window.decorView.setBackgroundColor(0)
    }

    private fun initVariables() {
        mTextColorWithoutTransparency = config.widgetTextColor
        updateColors()

        mBgColor = config.widgetBgColor
        if (mBgColor == 1) {
            mBgColor = Color.BLACK
            mBgAlpha = .2f
        } else {
            mBgAlpha = Color.alpha(mBgColor) / 255.toFloat()
        }

        mBgColorWithoutTransparency = Color.rgb(Color.red(mBgColor), Color.green(mBgColor), Color.blue(mBgColor))
        config_bg_seekbar.setOnSeekBarChangeListener(bgSeekbarChangeListener)
        config_bg_seekbar.progress = (mBgAlpha * 100).toInt()
        updateBgColor()
        updateCurrentDateTime()
    }

    private fun updateCurrentDateTime() {
        val calendar = Calendar.getInstance()
        config_time.text = getFormattedTime(getPassedSeconds(), false, false).toString()
        config_date.text = getFormattedDate(calendar)

        if (config.useTextShadow) {
            config_time.setShadowLayer(1f, 0f, 1f, Color.BLACK)
            config_date.setShadowLayer(1f, 0f, 1f, Color.BLACK)
        }
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
            widgetTextColor = mTextColorWithoutTransparency
        }
    }

    private fun pickBackgroundColor() {
        ColorPickerDialog(this, mBgColorWithoutTransparency) { wasPositivePressed, color ->
            if (wasPositivePressed) {
                mBgColorWithoutTransparency = color
                updateBgColor()
            }
        }
    }

    private fun pickTextColor() {
        ColorPickerDialog(this, mTextColor) { wasPositivePressed, color ->
            if (wasPositivePressed) {
                mTextColorWithoutTransparency = color
                updateColors()
            }
        }
    }

    private fun requestWidgetUpdate() {
        Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null, this, MyWidgetDateTimeProvider::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(mWidgetId))
            sendBroadcast(this)
        }
    }

    private fun updateColors() {
        mTextColor = mTextColorWithoutTransparency
        config_text_color.setFillWithStroke(mTextColor, Color.BLACK)
        config_save.setTextColor(mTextColor)
        config_time.setTextColor(mTextColor)
        config_date.setTextColor(mTextColor)
    }

    private fun updateBgColor() {
        mBgColor = mBgColorWithoutTransparency.adjustAlpha(mBgAlpha)
        config_bg_color.setFillWithStroke(mBgColor, Color.BLACK)
        config_save.setBackgroundColor(mBgColor)
        config_date_time_wrapper.setBackgroundColor(mBgColor)
    }

    private val bgSeekbarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            mBgAlpha = progress.toFloat() / 100.toFloat()
            updateBgColor()
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {

        }
    }
}
