package com.simplemobiletools.clock.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.simplemobiletools.clock.activities.MainActivity
import com.simplemobiletools.clock.activities.SimpleActivity
import com.simplemobiletools.clock.adapters.AlarmsAdapter
import com.simplemobiletools.clock.databinding.FragmentAlarmBinding
import com.simplemobiletools.clock.dialogs.ChangeAlarmSortDialog
import com.simplemobiletools.clock.dialogs.EditAlarmDialog
import com.simplemobiletools.clock.extensions.*
import com.simplemobiletools.clock.helpers.*
import com.simplemobiletools.clock.interfaces.ToggleAlarmInterface
import com.simplemobiletools.clock.models.Alarm
import com.simplemobiletools.commons.extensions.getProperBackgroundColor
import com.simplemobiletools.commons.extensions.getProperTextColor
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.extensions.updateTextColors
import com.simplemobiletools.commons.helpers.SORT_BY_DATE_CREATED
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.commons.models.AlarmSound

class AlarmFragment : Fragment(), ToggleAlarmInterface {
    private var alarms = ArrayList<Alarm>()
    private var currentEditAlarmDialog: EditAlarmDialog? = null

    private lateinit var binding: FragmentAlarmBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAlarmBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        setupViews()
    }

    fun showSortingDialog() {
        ChangeAlarmSortDialog(activity as SimpleActivity) {
            setupAlarms()
        }
    }

    private fun setupViews() {
        binding.apply {
            requireContext().updateTextColors(alarmFragment)
            alarmFab.setOnClickListener {
                val newAlarm = root.context.createNewAlarm(DEFAULT_ALARM_MINUTES, 0)
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
        context?.getEnabledAlarms { enabledAlarms ->
            if (enabledAlarms.isNullOrEmpty()) {
                alarms.forEach {
                    if (it.days == TODAY_BIT && it.isEnabled && it.timeInMinutes <= getCurrentDayMinutes()) {
                        it.isEnabled = false
                        ensureBackgroundThread {
                            context?.dbHelper?.updateAlarmEnabledState(it.id, false)
                        }
                    }
                }
            }
        }

        val currAdapter = binding.alarmsList.adapter
        if (currAdapter == null) {
            AlarmsAdapter(activity as SimpleActivity, alarms, this, binding.alarmsList) {
                openEditAlarm(it as Alarm)
            }.apply {
                binding.alarmsList.adapter = this
            }
        } else {
            (currAdapter as AlarmsAdapter).apply {
                updatePrimaryColor()
                updateBackgroundColor(requireContext().getProperBackgroundColor())
                updateTextColor(requireContext().getProperTextColor())
                updateItems(this@AlarmFragment.alarms)
            }
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
        (activity as SimpleActivity).handleFullScreenNotificationsPermission { granted ->
            if (granted) {
                if (requireContext().dbHelper.updateAlarmEnabledState(id, isEnabled)) {
                    val alarm = alarms.firstOrNull { it.id == id } ?: return@handleFullScreenNotificationsPermission
                    alarm.isEnabled = isEnabled
                    checkAlarmState(alarm)
                } else {
                    requireActivity().toast(com.simplemobiletools.commons.R.string.unknown_error_occurred)
                }
                requireContext().updateWidgets()
            } else {
                setupAlarms()
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
