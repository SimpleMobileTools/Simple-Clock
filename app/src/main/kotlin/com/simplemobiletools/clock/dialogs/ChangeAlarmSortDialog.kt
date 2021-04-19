package com.simplemobiletools.clock.dialogs

import android.view.View
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.extensions.config
import com.simplemobiletools.clock.helpers.SORT_BY_ALARM_TIME
import com.simplemobiletools.clock.helpers.SORT_BY_CREATION_ORDER
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.setupDialogStuff
import kotlinx.android.synthetic.main.dialog_change_alarm_sort.view.*

class ChangeAlarmSortDialog(val activity: BaseSimpleActivity, val callback: () -> Unit) {
    private var view: View = activity.layoutInflater.inflate(R.layout.dialog_change_alarm_sort, null).apply {
        val activeRadioButton = when (activity.config.alarmSort) {
            SORT_BY_ALARM_TIME -> sorting_dialog_radio_alarm_time
            else -> sorting_dialog_radio_creation_order
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
            R.id.sorting_dialog_radio_alarm_time -> SORT_BY_ALARM_TIME
            else -> SORT_BY_CREATION_ORDER
        }

        activity.config.alarmSort = sort
        callback()
    }
}
