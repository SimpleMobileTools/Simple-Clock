package com.simplemobiletools.clock.dialogs

import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.databinding.DialogChangeAlarmSortBinding
import com.simplemobiletools.clock.extensions.config
import com.simplemobiletools.clock.helpers.SORT_BY_ALARM_TIME
import com.simplemobiletools.clock.helpers.SORT_BY_CREATION_ORDER
import com.simplemobiletools.clock.helpers.SORT_BY_DATE_AND_TIME
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.getAlertDialogBuilder
import com.simplemobiletools.commons.extensions.setupDialogStuff

class ChangeAlarmSortDialog(val activity: BaseSimpleActivity, val callback: () -> Unit) {
    private val binding = DialogChangeAlarmSortBinding.inflate(activity.layoutInflater).apply {
        val activeRadioButton = when (activity.config.alarmSort) {
            SORT_BY_ALARM_TIME -> sortingDialogRadioAlarmTime
            SORT_BY_DATE_AND_TIME -> sortingDialogRadioDayAndTime
            else -> sortingDialogRadioCreationOrder
        }
        activeRadioButton.isChecked = true
    }

    init {
        activity.getAlertDialogBuilder()
            .setPositiveButton(com.simplemobiletools.commons.R.string.ok) { _, _ -> dialogConfirmed() }
            .setNegativeButton(com.simplemobiletools.commons.R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this, com.simplemobiletools.commons.R.string.sort_by)
            }
    }

    private fun dialogConfirmed() {
        val sort = when (binding.sortingDialogRadioSorting.checkedRadioButtonId) {
            R.id.sorting_dialog_radio_alarm_time -> SORT_BY_ALARM_TIME
            R.id.sorting_dialog_radio_day_and_time -> SORT_BY_DATE_AND_TIME
            else -> SORT_BY_CREATION_ORDER
        }

        activity.config.alarmSort = sort
        callback()
    }
}
