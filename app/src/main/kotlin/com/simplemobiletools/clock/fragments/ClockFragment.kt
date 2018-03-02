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
import com.simplemobiletools.clock.dialogs.EditTimeZoneDialog
import com.simplemobiletools.clock.extensions.config
import com.simplemobiletools.clock.extensions.getFormattedDate
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
        setupDateTime()
        return view
    }

    override fun onResume() {
        super.onResume()

        displayOtherTimeZones = context!!.config.displayOtherTimeZones
        if (!isFirstResume) {
            setupDateTime()
        }

        isFirstResume = false
    }

    override fun onPause() {
        super.onPause()
        updateHandler.removeCallbacksAndMessages(null)
    }

    private fun setupDateTime() {
        calendar = Calendar.getInstance()
        val offset = calendar.timeZone.rawOffset
        passedSeconds = ((calendar.timeInMillis + offset) / 1000).toInt()
        displayOtherTimeZones = context!!.config.displayOtherTimeZones
        updateCurrentTime()
        updateDate()
        setupViews()
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
            if (hours == 0 && minutes == 0) {
                updateDate()
            }

            if (displayOtherTimeZones) {
                (view.time_zones_list.adapter as? TimeZonesAdapter)?.updateTimes()
            }
        }

        updateHandler.postDelayed({
            passedSeconds++
            updateCurrentTime()
        }, ONE_SECOND)
    }

    private fun updateDate() {
        calendar = Calendar.getInstance()
        val formattedDate = context!!.getFormattedDate(calendar)
        view.clock_date.text = formattedDate

        if (displayOtherTimeZones) {
            (view.time_zones_list.adapter as? TimeZonesAdapter)?.todayDateString = formattedDate
        }
    }

    private fun updateTimeZones() {
        if (!displayOtherTimeZones) {
            return
        }

        val selectedTimeZoneIDs = context!!.config.selectedTimeZones.map { it.toInt() }
        val timeZones = getAllTimeZones().filter { selectedTimeZoneIDs.contains(it.id) } as ArrayList<MyTimeZone>
        val currAdapter = view.time_zones_list.adapter
        if (currAdapter == null) {
            TimeZonesAdapter(activity as SimpleActivity, timeZones, view.time_zones_list) {
                EditTimeZoneDialog(activity as SimpleActivity, it as MyTimeZone) {
                    updateTimeZones()
                }
            }.apply {
                view.time_zones_list.adapter = this
            }
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
