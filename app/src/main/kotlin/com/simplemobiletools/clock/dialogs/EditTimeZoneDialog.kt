package com.simplemobiletools.clock.dialogs

import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.activities.SimpleActivity
import com.simplemobiletools.clock.extensions.config
import com.simplemobiletools.clock.extensions.getEditedTimeZonesMap
import com.simplemobiletools.clock.extensions.getModifiedTimeZoneTitle
import com.simplemobiletools.clock.helpers.EDITED_TIME_ZONE_SEPARATOR
import com.simplemobiletools.clock.helpers.getDefaultTimeZoneTitle
import com.simplemobiletools.clock.models.MyTimeZone
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.extensions.showKeyboard
import com.simplemobiletools.commons.extensions.value
import kotlinx.android.synthetic.main.dialog_edit_time_zone.view.*

class EditTimeZoneDialog(val activity: SimpleActivity, val myTimeZone: MyTimeZone, val callback: () -> Unit) {

    init {
        val view = activity.layoutInflater.inflate(R.layout.dialog_edit_time_zone, null).apply {
            edit_time_zone_title.setText(activity.getModifiedTimeZoneTitle(myTimeZone.id))
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
        val editedTitlesMap = activity.getEditedTimeZonesMap()

        if (newTitle.isEmpty()) {
            editedTitlesMap.remove(myTimeZone.id)
        } else {
            editedTitlesMap[myTimeZone.id] = newTitle
        }

        val newTitlesSet = HashSet<String>()
        for ((key, value) in editedTitlesMap) {
            newTitlesSet.add("$key$EDITED_TIME_ZONE_SEPARATOR$value")
        }

        activity.config.editedTimeZoneTitles = newTitlesSet
        callback()
    }
}
