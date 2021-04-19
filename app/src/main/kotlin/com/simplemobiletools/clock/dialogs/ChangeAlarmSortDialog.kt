package com.simplemobiletools.clock.dialogs

import android.view.View
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.extensions.config
import com.simplemobiletools.clock.models.AlarmSort
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.setupDialogStuff
import kotlinx.android.synthetic.main.dialog_change_alarm_sort.view.*

class ChangeAlarmSortDialog(val activity: BaseSimpleActivity, val callback: (AlarmSort) -> Unit) {
    private var view: View = activity.layoutInflater.inflate(R.layout.dialog_change_alarm_sort, null).apply {
        val activeRadioButton = when (activity.config.alarmSort) {
            AlarmSort.CREATED_AT -> sorting_dialog_radio_creation_order
            AlarmSort.TIME_OF_DAY -> sorting_dialog_radio_alarm_time
        }
        activeRadioButton?.isChecked = true
    }

    init {
        AlertDialog.Builder(activity)
            .setPositiveButton(R.string.ok) { _, _ -> dialogConfirmed() }
            .setNegativeButton(R.string.cancel, null)
            .create().apply {
                activity.setupDialogStuff(view, this, R.string.sort_by)
            }
    }

    private fun dialogConfirmed() {
        val sort = when (view.sorting_dialog_radio_sorting.checkedRadioButtonId) {
            R.id.sorting_dialog_radio_creation_order -> AlarmSort.CREATED_AT
            R.id.sorting_dialog_radio_alarm_time -> AlarmSort.TIME_OF_DAY
            else -> AlarmSort.default()
        }
        activity.config.alarmSort = sort

        callback(sort)
    }
}
