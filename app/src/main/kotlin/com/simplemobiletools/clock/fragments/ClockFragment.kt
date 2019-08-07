package com.simplemobiletools.clock.fragments

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.activities.SimpleActivity
import com.simplemobiletools.clock.adapters.TimeZonesAdapter
import com.simplemobiletools.clock.dialogs.AddTimeZonesDialog
import com.simplemobiletools.clock.dialogs.EditTimeZoneDialog
import com.simplemobiletools.clock.extensions.*
import com.simplemobiletools.clock.helpers.getPassedSeconds
import com.simplemobiletools.clock.models.MyTimeZone
import com.simplemobiletools.commons.extensions.beVisibleIf
import com.simplemobiletools.commons.extensions.updateTextColors
import kotlinx.android.synthetic.main.fragment_clock.view.*
import java.util.*

class ClockFragment : Fragment() {
    private val ONE_SECOND = 1000L

    private var passedSeconds = 0
    private var calendar = Calendar.getInstance()
    private val updateHandler = Handler()

    private var storedTextColor = 0

    lateinit var view: ViewGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        storeStateVariables()
        view = inflater.inflate(R.layout.fragment_clock, container, false) as ViewGroup
        return view
    }

    override fun onResume() {
        super.onResume()
        setupDateTime()

        val configTextColor = context!!.config.textColor
        if (storedTextColor != configTextColor) {
            (view.time_zones_list.adapter as? TimeZonesAdapter)?.updateTextColor(configTextColor)
        }
    }

    override fun onPause() {
        super.onPause()
        updateHandler.removeCallbacksAndMessages(null)
        storeStateVariables()
    }

    private fun storeStateVariables() {
        storedTextColor = context!!.config.textColor
    }

    private fun setupDateTime() {
        calendar = Calendar.getInstance()
        passedSeconds = getPassedSeconds()
        updateCurrentTime()
        updateDate()
        updateAlarm()
        setupViews()
    }

    private fun setupViews() {
        view.apply {
            context!!.updateTextColors(clock_fragment)
            clock_fab.setOnClickListener {
                fabClicked()
            }

            updateTimeZones()
        }
    }

    private fun updateCurrentTime() {
        val hours = (passedSeconds / 3600) % 24
        val minutes = (passedSeconds / 60) % 60
        val seconds = passedSeconds % 60
        view.clock_time.text = context!!.getFormattedTime(passedSeconds, context!!.config.showSeconds, true)

        if (!context!!.config.use24HourFormat) {
            view.clock_time.textSize = resources.getDimension(R.dimen.clock_text_size_smaller) / resources.displayMetrics.density
        }

        if (seconds == 0) {
            if (hours == 0 && minutes == 0) {
                updateDate()
            }

            (view.time_zones_list.adapter as? TimeZonesAdapter)?.updateTimes()
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
        (view.time_zones_list.adapter as? TimeZonesAdapter)?.todayDateString = formattedDate
    }

    fun updateAlarm() {
        view.apply {
            val nextAlarm = context!!.getNextAlarm()
            clock_alarm.beVisibleIf(nextAlarm.isNotEmpty())
            clock_alarm.text = nextAlarm
            clock_alarm.colorLeftDrawable(context!!.config.textColor)
        }
    }

    private fun updateTimeZones() {
        val selectedTimeZones = context!!.config.selectedTimeZones
        view.time_zones_list.beVisibleIf(selectedTimeZones.isNotEmpty())
        if (selectedTimeZones.isEmpty()) {
            return
        }

        val selectedTimeZoneIDs = selectedTimeZones.map { it.toInt() }
        val timeZones = context!!.getAllTimeZonesModified().filter { selectedTimeZoneIDs.contains(it.id) } as ArrayList<MyTimeZone>
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
