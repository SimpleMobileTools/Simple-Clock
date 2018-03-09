package com.simplemobiletools.clock.dialogs

import android.support.v7.app.AlertDialog
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.activities.SimpleActivity
import com.simplemobiletools.clock.extensions.config
import com.simplemobiletools.commons.extensions.setupDialogStuff
import kotlinx.android.synthetic.main.dialog_my_time_picker.view.*

class MyTimePickerDialogDialog(val activity: SimpleActivity, val initialSeconds: Int, private val callback: () -> Unit) {
    private var view = activity.layoutInflater.inflate(R.layout.dialog_my_time_picker, null)

    init {
        view.apply {
            val textColor = activity.config.textColor
            arrayOf(my_time_picker_hours, my_time_picker_minutes, my_time_picker_seconds).forEach {
                it.textColor = textColor
                it.selectedTextColor = textColor
                it.dividerColor = textColor
            }
        }

        AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok, { dialog, which -> dialogConfirmed() })
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
                    activity.setupDialogStuff(view, this)
                }
    }

    private fun dialogConfirmed() {
        callback()
    }
}
