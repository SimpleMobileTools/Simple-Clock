package com.simplemobiletools.clock.dialogs

import com.simplemobiletools.clock.activities.SimpleActivity
import com.simplemobiletools.clock.databinding.DialogMyTimePickerBinding
import com.simplemobiletools.commons.extensions.getAlertDialogBuilder
import com.simplemobiletools.commons.extensions.getProperTextColor
import com.simplemobiletools.commons.extensions.setupDialogStuff

class MyTimePickerDialogDialog(val activity: SimpleActivity, val initialSeconds: Int, val callback: (result: Int) -> Unit) {
    private val binding = DialogMyTimePickerBinding.inflate(activity.layoutInflater)

    init {
        binding.apply {
            val textColor = activity.getProperTextColor()
            arrayOf(myTimePickerHours, myTimePickerMinutes, myTimePickerSeconds).forEach {
                it.textColor = textColor
                it.selectedTextColor = textColor
                it.dividerColor = textColor
            }

            myTimePickerHours.value = initialSeconds / 3600
            myTimePickerMinutes.value = (initialSeconds) / 60 % 60
            myTimePickerSeconds.value = initialSeconds % 60
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(com.simplemobiletools.commons.R.string.ok) { dialog, which -> dialogConfirmed() }
            .setNegativeButton(com.simplemobiletools.commons.R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this)
            }
    }

    private fun dialogConfirmed() {
        binding.apply {
            val hours = myTimePickerHours.value
            val minutes = myTimePickerMinutes.value
            val seconds = myTimePickerSeconds.value
            callback(hours * 3600 + minutes * 60 + seconds)
        }
    }
}
