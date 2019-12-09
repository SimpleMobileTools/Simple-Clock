package com.simplemobiletools.clock.adapters

import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.activities.SimpleActivity
import com.simplemobiletools.clock.extensions.config
import com.simplemobiletools.clock.extensions.getFormattedDate
import com.simplemobiletools.clock.extensions.getFormattedTime
import com.simplemobiletools.clock.models.MyTimeZone
import com.simplemobiletools.commons.adapters.MyRecyclerViewAdapter
import com.simplemobiletools.commons.extensions.beGone
import com.simplemobiletools.commons.extensions.beVisible
import com.simplemobiletools.commons.views.MyRecyclerView
import kotlinx.android.synthetic.main.item_time_zone.view.*
import java.util.*

class TimeZonesAdapter(activity: SimpleActivity, var timeZones: ArrayList<MyTimeZone>, recyclerView: MyRecyclerView, itemClick: (Any) -> Unit) :
        MyRecyclerViewAdapter(activity, recyclerView, null, itemClick) {

    var todayDateString = activity.getFormattedDate(Calendar.getInstance())

    init {
        setupDragListener(true)
    }

    override fun getActionMenuId() = R.menu.cab_timezones

    override fun prepareActionMode(menu: Menu) {}

    override fun actionItemPressed(id: Int) {
        if (selectedKeys.isEmpty()) {
            return
        }

        when (id) {
            R.id.cab_delete -> deleteItems()
        }
    }

    override fun getSelectableItemCount() = timeZones.size

    override fun getIsItemSelectable(position: Int) = true

    override fun getItemSelectionKey(position: Int) = timeZones.getOrNull(position)?.id

    override fun getItemKeyPosition(key: Int) = timeZones.indexOfFirst { it.id == key }

    override fun onActionModeCreated() {}

    override fun onActionModeDestroyed() {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = createViewHolder(R.layout.item_time_zone, parent)

    override fun onBindViewHolder(holder: MyRecyclerViewAdapter.ViewHolder, position: Int) {
        val timeZone = timeZones[position]
        holder.bindView(timeZone, true, true) { itemView, layoutPosition ->
            setupView(itemView, timeZone)
        }
        bindViewHolder(holder)
    }

    override fun getItemCount() = timeZones.size

    fun updateItems(newItems: ArrayList<MyTimeZone>) {
        timeZones = newItems
        notifyDataSetChanged()
        finishActMode()
    }

    fun updateTimes() {
        notifyDataSetChanged()
    }

    private fun deleteItems() {
        val timeZonesToRemove = ArrayList<MyTimeZone>(selectedKeys.size)
        val timeZoneIDsToRemove = ArrayList<String>(selectedKeys.size)
        val positions = getSelectedItemPositions()
        getSelectedItems().forEach {
            timeZonesToRemove.add(it)
            timeZoneIDsToRemove.add(it.id.toString())
        }

        timeZones.removeAll(timeZonesToRemove)
        removeSelectedItems(positions)

        val selectedTimeZones = activity.config.selectedTimeZones
        val newTimeZones = selectedTimeZones.filter { !timeZoneIDsToRemove.contains(it) }.toHashSet()
        activity.config.selectedTimeZones = newTimeZones
    }

    private fun getSelectedItems() = timeZones.filter { selectedKeys.contains(it.id) } as ArrayList<MyTimeZone>

    private fun setupView(view: View, timeZone: MyTimeZone) {
        val currTimeZone = TimeZone.getTimeZone(timeZone.zoneName)
        val calendar = Calendar.getInstance(currTimeZone)
        var offset = calendar.timeZone.rawOffset
        val isDaylightSavingActive = currTimeZone.inDaylightTime(Date())
        if (isDaylightSavingActive) {
            offset += currTimeZone.dstSavings
        }
        val passedSeconds = ((calendar.timeInMillis + offset) / 1000).toInt()
        val formattedTime = activity.getFormattedTime(passedSeconds, false, false)
        val formattedDate = activity.getFormattedDate(calendar)

        val isSelected = selectedKeys.contains(timeZone.id)
        view.apply {
            time_zone_frame.isSelected = isSelected
            time_zone_title.text = timeZone.title
            time_zone_title.setTextColor(textColor)

            time_zone_time.text = formattedTime
            time_zone_time.setTextColor(textColor)

            if (formattedDate != todayDateString) {
                time_zone_date.beVisible()
                time_zone_date.text = formattedDate
                time_zone_date.setTextColor(textColor)
            } else {
                time_zone_date.beGone()
            }
        }
    }
}
