package com.simplemobiletools.clock.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.activities.SimpleActivity
import com.simplemobiletools.clock.adapters.AlarmsAdapter
import com.simplemobiletools.clock.dialogs.EditAlarmDialog
import com.simplemobiletools.clock.extensions.createNewAlarm
import com.simplemobiletools.clock.extensions.dbHelper
import com.simplemobiletools.clock.interfaces.ToggleAlarmInterface
import com.simplemobiletools.clock.models.Alarm
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.extensions.updateTextColors
import kotlinx.android.synthetic.main.fragment_alarm.view.*
import java.util.*
import kotlin.math.pow

class AlarmFragment : Fragment(), ToggleAlarmInterface {
    private val DEFAULT_ALARM_MINUTES = 480
    private val DAY_MINUTES = 1440

    private var alarms = ArrayList<Alarm>()
    lateinit var view: ViewGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        view = inflater.inflate(R.layout.fragment_alarm, container, false) as ViewGroup
        return view
    }

    override fun onResume() {
        super.onResume()
        setupViews()
    }

    private fun setupViews() {
        view.apply {
            context!!.updateTextColors(alarm_fragment)
            alarm_fab.setOnClickListener {
                val newAlarm = context.createNewAlarm(DEFAULT_ALARM_MINUTES, 0)
                openEditAlarm(newAlarm)
            }
        }

        setupAlarms()
    }

    private fun setupAlarms() {
        alarms = context!!.dbHelper.getAlarms()
        val currAdapter = view.alarms_list.adapter
        if (currAdapter == null) {
            val alarmsAdapter = AlarmsAdapter(activity as SimpleActivity, alarms, this, view.alarms_list) {
                openEditAlarm(it as Alarm)
            }
            view.alarms_list.adapter = alarmsAdapter
        } else {
            (currAdapter as AlarmsAdapter).updateItems(alarms)
        }
    }

    private fun openEditAlarm(alarm: Alarm) {
        EditAlarmDialog(activity as SimpleActivity, alarm) {
            setupAlarms()
        }
    }

    override fun alarmToggled(id: Int, isEnabled: Boolean) {
        if (context!!.dbHelper.updateAlarmEnabledState(id, isEnabled)) {
            val alarm = alarms.firstOrNull { it.id == id } ?: return
            alarm.isEnabled = isEnabled
            if (isEnabled) {
                getClosestTriggerTimestamp(alarm)
            }
        } else {
            activity!!.toast(R.string.unknown_error_occurred)
        }
    }

    private fun getClosestTriggerTimestamp(alarm: Alarm) {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        for (i in 0..7) {
            val currentDay = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7
            val isCorrectDay = alarm.days and 2.0.pow(currentDay).toInt() != 0
            val currentTimeInMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
            if (isCorrectDay && (alarm.timeInMinutes > currentTimeInMinutes || i > 0)) {
                val triggerInMinutes = alarm.timeInMinutes - currentTimeInMinutes + (i * DAY_MINUTES)
                showRemainingTimeMessage(triggerInMinutes)
                break
            } else {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }
        }
    }

    private fun showRemainingTimeMessage(triggerInMinutes: Int) {
        val days = triggerInMinutes / DAY_MINUTES
        val hours = (triggerInMinutes % DAY_MINUTES) / 60
        val minutes = triggerInMinutes % 60
        val timesString = StringBuilder()
        if (days > 0) {
            val daysString = String.format(activity!!.resources.getQuantityString(R.plurals.days, days, days))
            timesString.append("$daysString, ")
        }

        if (hours > 0) {
            val hoursString = String.format(activity!!.resources.getQuantityString(R.plurals.hours, hours, hours))
            timesString.append("$hoursString, ")
        }

        if (minutes > 0) {
            val minutesString = String.format(activity!!.resources.getQuantityString(R.plurals.minutes, minutes, minutes))
            timesString.append(minutesString)
        }

        val fullString = String.format(activity!!.getString(R.string.alarm_goes_off_in), timesString.toString().trim().trimEnd(','))
        activity!!.toast(fullString, Toast.LENGTH_LONG)
    }
}
