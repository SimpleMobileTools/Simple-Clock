package com.simplemobiletools.clock.fragments

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.extensions.config
import com.simplemobiletools.commons.extensions.beVisibleIf
import com.simplemobiletools.commons.extensions.getAdjustedPrimaryColor
import com.simplemobiletools.commons.extensions.underlineText
import com.simplemobiletools.commons.extensions.updateTextColors
import kotlinx.android.synthetic.main.fragment_clock.view.*
import java.util.*

class ClockFragment(context: Context, attributeSet: AttributeSet) : MyViewPagerFragment(context, attributeSet) {
    private val ONE_SECOND = 1000L
    private var passedSeconds = 0
    private var calendar = Calendar.getInstance()

    private val updateHandler = Handler()

    override fun onFinishInflate() {
        super.onFinishInflate()
        val offset = calendar.timeZone.rawOffset
        passedSeconds = ((calendar.timeInMillis + offset) / 1000).toInt()
        updateCurrentTime()
        updateDate()

        time_zones_placeholder_2.apply {
            underlineText()
            setOnClickListener {
                placeholderClicked()
            }
        }

        setupViews()
    }

    private fun setupViews() {
        time_zones_holder.beVisibleIf(context.config.displayOtherTimeZones)
        context.updateTextColors(clock_fragment)
        time_zones_placeholder_2.setTextColor(context.getAdjustedPrimaryColor())
    }

    private fun updateCurrentTime() {
        val hours = (passedSeconds / 3600) % 24
        val minutes = (passedSeconds / 60) % 60
        val seconds = passedSeconds % 60
        var format = "%02d:%02d"

        val formattedText = if (context.config.showSeconds) {
            format += ":%02d"
            String.format(format, hours, minutes, seconds)
        } else {
            String.format(format, hours, minutes)
        }

        clock_time.text = formattedText

        if (hours == 0 && minutes == 0 && seconds == 0) {
            updateDate()
        }

        updateHandler.postDelayed({
            passedSeconds++
            updateCurrentTime()
        }, ONE_SECOND)
    }

    private fun updateDate() {
        calendar = Calendar.getInstance()
        val dayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7    // make sure index 0 means monday
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH)

        val dayString = context.resources.getStringArray(R.array.week_days)[dayOfWeek]
        val shortDayString = dayString.substring(0, Math.min(3, dayString.length))
        val monthString = context.resources.getStringArray(R.array.months)[month]

        val dateString = "$shortDayString, $dayOfMonth $monthString"
        clock_date.text = dateString
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        updateHandler.removeCallbacksAndMessages(null)
    }

    private fun placeholderClicked() {

    }

    override fun onActivityResume() {
        setupViews()
    }
}
