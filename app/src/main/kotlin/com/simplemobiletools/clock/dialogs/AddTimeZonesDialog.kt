package com.simplemobiletools.clock.dialogs

import com.simplemobiletools.clock.activities.SimpleActivity
import com.simplemobiletools.clock.adapters.SelectTimeZonesAdapter
import com.simplemobiletools.clock.databinding.DialogSelectTimeZonesBinding
import com.simplemobiletools.clock.extensions.config
import com.simplemobiletools.clock.helpers.getAllTimeZones
import com.simplemobiletools.commons.extensions.getAlertDialogBuilder
import com.simplemobiletools.commons.extensions.setupDialogStuff

class AddTimeZonesDialog(val activity: SimpleActivity, private val callback: () -> Unit) {
    private val binding = DialogSelectTimeZonesBinding.inflate(activity.layoutInflater)

    init {
        binding.selectTimeZonesList.adapter = SelectTimeZonesAdapter(activity, getAllTimeZones())

        activity.getAlertDialogBuilder()
            .setPositiveButton(com.simplemobiletools.commons.R.string.ok) { dialog, which -> dialogConfirmed() }
            .setNegativeButton(com.simplemobiletools.commons.R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this)
            }
    }

    private fun dialogConfirmed() {
        val adapter = binding.selectTimeZonesList.adapter as? SelectTimeZonesAdapter
        val selectedTimeZones = adapter?.selectedKeys?.map { it.toString() }?.toHashSet() ?: LinkedHashSet()
        activity.config.selectedTimeZones = selectedTimeZones
        callback()
    }
}
