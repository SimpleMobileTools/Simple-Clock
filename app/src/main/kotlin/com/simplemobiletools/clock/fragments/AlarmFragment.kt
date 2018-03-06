package com.simplemobiletools.clock.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.activities.SimpleActivity
import com.simplemobiletools.clock.adapters.AlarmsAdapter
import com.simplemobiletools.clock.dialogs.EditAlarmDialog
import com.simplemobiletools.clock.extensions.cancelAlarmClock
import com.simplemobiletools.clock.extensions.createNewAlarm
import com.simplemobiletools.clock.extensions.dbHelper
import com.simplemobiletools.clock.extensions.scheduleNextAlarm
import com.simplemobiletools.clock.helpers.DEFAULT_ALARM_MINUTES
import com.simplemobiletools.clock.interfaces.ToggleAlarmInterface
import com.simplemobiletools.clock.models.Alarm
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.extensions.updateTextColors
import kotlinx.android.synthetic.main.fragment_alarm.view.*
import java.util.*

class AlarmFragment : Fragment(), ToggleAlarmInterface {
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
            checkAlarmState(alarm)
        }
    }

    override fun alarmToggled(id: Int, isEnabled: Boolean) {
        if (context!!.dbHelper.updateAlarmEnabledState(id, isEnabled)) {
            val alarm = alarms.firstOrNull { it.id == id } ?: return
            alarm.isEnabled = isEnabled
            checkAlarmState(alarm)
        } else {
            activity!!.toast(R.string.unknown_error_occurred)
        }
    }

    private fun checkAlarmState(alarm: Alarm) {
        if (alarm.isEnabled) {
            context!!.scheduleNextAlarm(alarm, true)
        } else {
            context!!.cancelAlarmClock(alarm)
        }
    }
}
