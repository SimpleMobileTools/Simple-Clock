package com.simplemobiletools.clock.dialogs

import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.activities.SimpleActivity
import com.simplemobiletools.clock.extensions.config
import com.simplemobiletools.commons.extensions.setupDialogStuff
import kotlinx.android.synthetic.main.dialog_my_time_picker.view.*

class MyTimePickerDialogDialog(val activity: SimpleActivity, val initialSeconds: Int, val callback: (result: Int) -> Unit) {
    private var view = activity.layoutInflater.inflate(R.layout.dialog_my_time_picker, null)

    init {
        view.apply {
            val textColor = activity.config.textColor
            arrayOf(my_time_picker_hours, my_time_picker_minutes, my_time_picker_seconds).forEach {
                it.textColor = textColor
                it.selectedTextColor = textColor
                it.dividerColor = textColor
            }

            my_time_picker_hours.value = initialSeconds / 3600
            my_time_picker_minutes.value = (initialSeconds) / 60 % 60
            my_time_picker_seconds.value = initialSeconds % 60
        }

        AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok, { dialog, which -> dialogConfirmed() })
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
                    activity.setupDialogStuff(view, this)
                }
    }

    private fun dialogConfirmed() {
        view.apply {
            val hours = my_time_picker_hours.value
            val minutes = my_time_picker_minutes.value
            val seconds = my_time_picker_seconds.value
            callback(hours * 3600 + minutes * 60 + seconds)
        }
    }
}
