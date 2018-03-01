package com.simplemobiletools.clock.fragments

import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.activities.SimpleActivity
import com.simplemobiletools.clock.adapters.TimeZonesAdapter
import com.simplemobiletools.clock.dialogs.AddTimeZonesDialog
import com.simplemobiletools.clock.extensions.config
import com.simplemobiletools.clock.helpers.getAllTimeZones
import com.simplemobiletools.clock.models.MyTimeZone
import com.simplemobiletools.commons.extensions.beVisibleIf
import com.simplemobiletools.commons.extensions.updateTextColors
import kotlinx.android.synthetic.main.fragment_clock.view.*
import java.util.*

class ClockFragment : Fragment() {
    private val ONE_SECOND = 1000L

    private var passedSeconds = 0
    private var isFirstResume = true
    private var displayOtherTimeZones = false
    private var calendar = Calendar.getInstance()
    private val updateHandler = Handler()

    lateinit var view: ViewGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        view = inflater.inflate(R.layout.fragment_clock, container, false) as ViewGroup
        val offset = calendar.timeZone.rawOffset
        passedSeconds = ((calendar.timeInMillis + offset) / 1000).toInt()
        displayOtherTimeZones = context!!.config.displayOtherTimeZones
        updateCurrentTime()
        updateDate()
        setupViews()
        return view
    }

    override fun onResume() {
        super.onResume()

        displayOtherTimeZones = context!!.config.displayOtherTimeZones
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
        view.apply {
            context!!.updateTextColors(clock_fragment)
            time_zones_list.beVisibleIf(displayOtherTimeZones)
            clock_fab.beVisibleIf(displayOtherTimeZones)
            clock_fab.setOnClickListener {
                fabClicked()
            }

            if (displayOtherTimeZones) {
                updateTimeZones()
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

        if (seconds == 0) {
            if (displayOtherTimeZones) {
                (view.time_zones_list.adapter as? TimeZonesAdapter)?.updateTimes()
            }

            if (hours == 0 && minutes == 0) {
                updateDate()
            }
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

    private fun updateTimeZones() {
        if (!displayOtherTimeZones) {
            return
        }

        val selectedTimeZoneIDs = context!!.config.selectedTimeZones.map { it.toInt() }
        val timeZones = getAllTimeZones().filter { selectedTimeZoneIDs.contains(it.id) } as ArrayList<MyTimeZone>
        val currAdapter = view.time_zones_list.adapter
        if (currAdapter == null) {
            val timeZonesAdapter = TimeZonesAdapter(activity as SimpleActivity, timeZones, view.time_zones_list) {}
            view.time_zones_list.adapter = timeZonesAdapter
        } else {
            (currAdapter as TimeZonesAdapter).updateItems(timeZones)
        }
    }

    private fun fabClicked() {
        AddTimeZonesDialog(activity as SimpleActivity) {
            updateTimeZones()
        }
    }
}
