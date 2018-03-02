package com.simplemobiletools.clock.dialogs

import android.support.v7.app.AlertDialog
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.activities.SimpleActivity
import com.simplemobiletools.clock.extensions.config
import com.simplemobiletools.clock.helpers.getDefaultTimeZoneTitle
import com.simplemobiletools.clock.models.MyTimeZone
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.extensions.showKeyboard
import com.simplemobiletools.commons.extensions.value
import kotlinx.android.synthetic.main.dialog_edit_time_zone.view.*

class EditTimeZoneDialog(val activity: SimpleActivity, val myTimeZone: MyTimeZone, val callback: () -> Unit) {

    init {
        val view = activity.layoutInflater.inflate(R.layout.dialog_edit_time_zone, null).apply {
            edit_time_zone_title.setText(myTimeZone.title)
            edit_time_zone_value.text = getDefaultTimeZoneTitle(myTimeZone.id)
        }

        AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok, { dialog, which -> dialogConfirmed(view.edit_time_zone_title.value) })
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
                    activity.setupDialogStuff(view, this) {
                        showKeyboard(view.edit_time_zone_title)
                    }
                }
    }

    private fun dialogConfirmed(newTitle: String) {
        val editedTimeZoneTitles = activity.config.editedTimeZoneTitles
        val editedTitlesMap = HashMap<Int, String>()
        editedTimeZoneTitles.forEach {
            val parts = it.split(":".toRegex(), 2)
        }
        callback()
    }
}
