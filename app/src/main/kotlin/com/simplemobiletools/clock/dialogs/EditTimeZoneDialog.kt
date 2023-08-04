package com.simplemobiletools.clock.dialogs

import com.simplemobiletools.clock.activities.SimpleActivity
import com.simplemobiletools.clock.databinding.DialogEditTimeZoneBinding
import com.simplemobiletools.clock.extensions.config
import com.simplemobiletools.clock.extensions.getEditedTimeZonesMap
import com.simplemobiletools.clock.extensions.getModifiedTimeZoneTitle
import com.simplemobiletools.clock.helpers.EDITED_TIME_ZONE_SEPARATOR
import com.simplemobiletools.clock.helpers.getDefaultTimeZoneTitle
import com.simplemobiletools.clock.models.MyTimeZone
import com.simplemobiletools.commons.extensions.getAlertDialogBuilder
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.extensions.showKeyboard
import com.simplemobiletools.commons.extensions.value

class EditTimeZoneDialog(val activity: SimpleActivity, val myTimeZone: MyTimeZone, val callback: () -> Unit) {

    init {
        val binding = DialogEditTimeZoneBinding.inflate(activity.layoutInflater).apply {
            editTimeZoneTitle.setText(activity.getModifiedTimeZoneTitle(myTimeZone.id))
            editTimeZoneLabel.setText(getDefaultTimeZoneTitle(myTimeZone.id))
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(com.simplemobiletools.commons.R.string.ok) { dialog, which -> dialogConfirmed(binding.editTimeZoneTitle.value) }
            .setNegativeButton(com.simplemobiletools.commons.R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this) { alertDialog ->
                    alertDialog.showKeyboard(binding.editTimeZoneTitle)
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
