package com.simplemobiletools.clock.dialogs

import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.activities.SimpleActivity
import com.simplemobiletools.clock.adapters.SelectTimeZonesAdapter
import com.simplemobiletools.clock.extensions.config
import com.simplemobiletools.clock.helpers.getAllTimeZones
import com.simplemobiletools.commons.extensions.setupDialogStuff
import kotlinx.android.synthetic.main.dialog_select_time_zones.view.*

class AddTimeZonesDialog(val activity: SimpleActivity, private val callback: () -> Unit) {
    private var view = activity.layoutInflater.inflate(R.layout.dialog_select_time_zones, null)

    init {
        view.select_time_zones_list.adapter = SelectTimeZonesAdapter(activity, getAllTimeZones())

        AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok) { dialog, which -> dialogConfirmed() }
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
                    activity.setupDialogStuff(view, this)
                }
    }

    private fun dialogConfirmed() {
        val adapter = view?.select_time_zones_list?.adapter as? SelectTimeZonesAdapter
        val selectedTimeZones = adapter?.selectedKeys?.map { it.toString() }?.toHashSet() ?: LinkedHashSet()
        activity.config.selectedTimeZones = selectedTimeZones
        callback()
    }
}
