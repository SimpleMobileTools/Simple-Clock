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
import com.simplemobiletools.clock.extensions.dbHelper
import com.simplemobiletools.clock.models.Alarm
import com.simplemobiletools.commons.extensions.updateTextColors
import kotlinx.android.synthetic.main.fragment_alarm.view.*

class AlarmFragment : Fragment() {
    private val DEFAULT_ALARM_MINUTES = 480

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
                val newAlarm = Alarm(0, DEFAULT_ALARM_MINUTES, 0, false, false, "", "")
                openEditAlarm(newAlarm)
            }
        }

        setupAlarms()
    }

    private fun setupAlarms() {
        val alarms = context!!.dbHelper.getAlarms()
        val currAdapter = view.alarms_list.adapter
        if (currAdapter == null) {
            val alarmsAdapter = AlarmsAdapter(activity as SimpleActivity, alarms, view.alarms_list) {
                openEditAlarm(it as Alarm)
            }
            view.alarms_list.adapter = alarmsAdapter
        } else {
            (currAdapter as AlarmsAdapter).updateItems(alarms)
        }
    }

    private fun openEditAlarm(alarm: Alarm) {
        EditAlarmDialog(activity as SimpleActivity, alarm) {

        }
    }
}
