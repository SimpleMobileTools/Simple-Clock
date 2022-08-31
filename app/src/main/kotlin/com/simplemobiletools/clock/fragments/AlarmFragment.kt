package com.simplemobiletools.clock.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.activities.MainActivity
import com.simplemobiletools.clock.activities.SimpleActivity
import com.simplemobiletools.clock.adapters.AlarmsAdapter
import com.simplemobiletools.clock.dialogs.ChangeAlarmSortDialog
import com.simplemobiletools.clock.dialogs.EditAlarmDialog
import com.simplemobiletools.clock.extensions.*
import com.simplemobiletools.clock.helpers.*
import com.simplemobiletools.clock.interfaces.ToggleAlarmInterface
import com.simplemobiletools.clock.models.Alarm
import com.simplemobiletools.commons.extensions.getProperTextColor
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.extensions.updateTextColors
import com.simplemobiletools.commons.helpers.SORT_BY_DATE_CREATED
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.commons.models.AlarmSound
import kotlinx.android.synthetic.main.fragment_alarm.view.*

class AlarmFragment : Fragment(), ToggleAlarmInterface {
    private var alarms = ArrayList<Alarm>()
    private var currentEditAlarmDialog: EditAlarmDialog? = null

    private var storedTextColor = 0

    lateinit var view: ViewGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        storeStateVariables()
        view = inflater.inflate(R.layout.fragment_alarm, container, false) as ViewGroup
        return view
    }

    override fun onResume() {
        super.onResume()
        setupViews()

        val configTextColor = requireContext().getProperTextColor()
        if (storedTextColor != configTextColor) {
            (view.alarms_list.adapter as AlarmsAdapter).updateTextColor(configTextColor)
        }
    }

    override fun onPause() {
        super.onPause()
        storeStateVariables()
    }

    fun showSortingDialog() {
        ChangeAlarmSortDialog(activity as SimpleActivity) {
            setupAlarms()
        }
    }

    private fun storeStateVariables() {
        storedTextColor = requireContext().getProperTextColor()
    }

    private fun setupViews() {
        view.apply {
            requireContext().updateTextColors(alarm_fragment)
            alarm_fab.setOnClickListener {
                val newAlarm = context.createNewAlarm(DEFAULT_ALARM_MINUTES, 0)
                newAlarm.isEnabled = true
                newAlarm.days = getTomorrowBit()
                openEditAlarm(newAlarm)
            }
        }

        setupAlarms()
    }

    private fun setupAlarms() {
        alarms = context?.dbHelper?.getAlarms() ?: return

        when (requireContext().config.alarmSort) {
            SORT_BY_ALARM_TIME -> alarms.sortBy { it.timeInMinutes }
            SORT_BY_DATE_CREATED -> alarms.sortBy { it.id }
            SORT_BY_DATE_AND_TIME -> alarms.sortWith(compareBy<Alarm> {
                requireContext().firstDayOrder(it.days)
            }.thenBy {
                it.timeInMinutes
            })
        }

        if (context?.getNextAlarm()?.isEmpty() == true) {
            alarms.forEach {
                if (it.days == TODAY_BIT && it.isEnabled && it.timeInMinutes <= getCurrentDayMinutes()) {
                    it.isEnabled = false
                    ensureBackgroundThread {
                        context?.dbHelper?.updateAlarmEnabledState(it.id, false)
                    }
                }
            }
        }

        val currAdapter = view.alarms_list.adapter
        if (currAdapter == null) {
            AlarmsAdapter(activity as SimpleActivity, alarms, this, view.alarms_list) {
                openEditAlarm(it as Alarm)
            }.apply {
                view.alarms_list.adapter = this
            }
        } else {
            (currAdapter as AlarmsAdapter).updateItems(alarms)
        }
    }

    private fun openEditAlarm(alarm: Alarm) {
        currentEditAlarmDialog = EditAlarmDialog(activity as SimpleActivity, alarm) {
            alarm.id = it
            currentEditAlarmDialog = null
            setupAlarms()
            checkAlarmState(alarm)
        }
    }

    override fun alarmToggled(id: Int, isEnabled: Boolean) {
        (activity as SimpleActivity).handleNotificationPermission {
            if (it) {
                if (requireContext().dbHelper.updateAlarmEnabledState(id, isEnabled)) {
                    val alarm = alarms.firstOrNull { it.id == id } ?: return@handleNotificationPermission
                    alarm.isEnabled = isEnabled
                    checkAlarmState(alarm)
                } else {
                    requireActivity().toast(R.string.unknown_error_occurred)
                }
                requireContext().updateWidgets()
            } else {
                activity?.toast(R.string.no_post_notifications_permissions)
            }
        }
    }

    private fun checkAlarmState(alarm: Alarm) {
        if (alarm.isEnabled) {
            context?.scheduleNextAlarm(alarm, true)
        } else {
            context?.cancelAlarmClock(alarm)
        }
        (activity as? MainActivity)?.updateClockTabAlarm()
    }

    fun updateAlarmSound(alarmSound: AlarmSound) {
        currentEditAlarmDialog?.updateSelectedAlarmSound(alarmSound)
    }
}
