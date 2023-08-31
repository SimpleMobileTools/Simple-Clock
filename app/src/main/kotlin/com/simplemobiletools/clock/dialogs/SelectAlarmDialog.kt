package com.simplemobiletools.clock.dialogs

import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.clock.databinding.DialogSelectAlarmBinding
import com.simplemobiletools.clock.databinding.ItemRadioButtonBinding
import com.simplemobiletools.clock.models.Alarm
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.*

class SelectAlarmDialog(
    val activity: BaseSimpleActivity,
    val alarms: List<Alarm>,
    val titleResId: Int,
    val onAlarmPicked: (alarm: Alarm?) -> Unit
) {
    private val binding = DialogSelectAlarmBinding.inflate(activity.layoutInflater, null, false)
    private var dialog: AlertDialog? = null

    init {
        addYourAlarms()

        activity.getAlertDialogBuilder()
            .setOnDismissListener { onAlarmPicked(null) }
            .setPositiveButton(com.simplemobiletools.commons.R.string.ok) { _, _ -> dialogConfirmed() }
            .setNegativeButton(com.simplemobiletools.commons.R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this, titleResId) { alertDialog ->
                    dialog = alertDialog
                }
            }
    }

    private fun addYourAlarms() {
        binding.dialogSelectAlarmRadio.removeAllViews()
        alarms.forEach { addAlarm(it) }
    }

    private fun addAlarm(alarm: Alarm) {
        val radioButton = ItemRadioButtonBinding.inflate(activity.layoutInflater).root.apply {
            text = alarm.label
            isChecked = false
            id = alarm.id
            setColors(activity.getProperTextColor(), activity.getProperPrimaryColor(), activity.getProperBackgroundColor())
        }

        binding.dialogSelectAlarmRadio.addView(radioButton, RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
    }

    private fun dialogConfirmed() {
        val checkedId = binding.dialogSelectAlarmRadio.checkedRadioButtonId
        onAlarmPicked(alarms.firstOrNull { it.id == checkedId })
    }
}
