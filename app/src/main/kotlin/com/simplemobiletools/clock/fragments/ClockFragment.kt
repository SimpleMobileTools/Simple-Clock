package com.simplemobiletools.clock.fragments

import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.extensions.config
import com.simplemobiletools.commons.extensions.beVisibleIf
import com.simplemobiletools.commons.extensions.updateTextColors
import kotlinx.android.synthetic.main.fragment_clock.view.*
import java.util.*

class ClockFragment : Fragment() {
    private val ONE_SECOND = 1000L

    private var passedSeconds = 0
    private var isFirstResume = true
    private var calendar = Calendar.getInstance()
    private val updateHandler = Handler()

    lateinit var view: ViewGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        view = inflater.inflate(R.layout.fragment_clock, container, false) as ViewGroup
        val offset = calendar.timeZone.rawOffset
        passedSeconds = ((calendar.timeInMillis + offset) / 1000).toInt()
        updateCurrentTime()
        updateDate()
        setupViews()
        return view
    }

    override fun onResume() {
        super.onResume()

        if (!isFirstResume) {
            setupViews()
        }
        isFirstResume = false
    }

    override fun onDestroy() {
        super.onDestroy()
        updateHandler.removeCallbacksAndMessages(null)
    }

    private fun setupViews() {
        val displayOtherTimeZones = context!!.config.displayOtherTimeZones
        view.apply {
            context!!.updateTextColors(clock_fragment)
            time_zones_list.beVisibleIf(displayOtherTimeZones)
            clock_fab.beVisibleIf(displayOtherTimeZones)
            clock_fab.setOnClickListener {
                fabClicked()
            }
        }
    }

    private fun updateCurrentTime() {
        val hours = (passedSeconds / 3600) % 24
        val minutes = (passedSeconds / 60) % 60
        val seconds = passedSeconds % 60
        var format = "%02d:%02d"

        val formattedText = if (context!!.config.showSeconds) {
            format += ":%02d"
            String.format(format, hours, minutes, seconds)
        } else {
            String.format(format, hours, minutes)
        }

        view.clock_time.text = formattedText

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

        val dayString = context!!.resources.getStringArray(R.array.week_days)[dayOfWeek]
        val shortDayString = dayString.substring(0, Math.min(3, dayString.length))
        val monthString = context!!.resources.getStringArray(R.array.months)[month]

        val dateString = "$shortDayString, $dayOfMonth $monthString"
        view.clock_date.text = dateString
    }

    private fun fabClicked() {

    }
}
