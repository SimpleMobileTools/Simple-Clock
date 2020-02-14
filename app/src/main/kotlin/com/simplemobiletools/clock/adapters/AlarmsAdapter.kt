package com.simplemobiletools.clock.adapters

import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.activities.SimpleActivity
import com.simplemobiletools.clock.extensions.config
import com.simplemobiletools.clock.extensions.dbHelper
import com.simplemobiletools.clock.extensions.getFormattedTime
import com.simplemobiletools.clock.interfaces.ToggleAlarmInterface
import com.simplemobiletools.clock.models.Alarm
import com.simplemobiletools.commons.adapters.MyRecyclerViewAdapter
import com.simplemobiletools.commons.dialogs.ConfirmationDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.views.MyRecyclerView
import kotlinx.android.synthetic.main.item_alarm.view.*
import java.util.*

class AlarmsAdapter(activity: SimpleActivity, var alarms: ArrayList<Alarm>, val toggleAlarmInterface: ToggleAlarmInterface,
                    recyclerView: MyRecyclerView, itemClick: (Any) -> Unit) : MyRecyclerViewAdapter(activity, recyclerView, null, itemClick) {
    private val adjustedPrimaryColor = activity.getAdjustedPrimaryColor()

    init {
        setupDragListener(true)
    }

    override fun getActionMenuId() = R.menu.cab_alarms

    override fun prepareActionMode(menu: Menu) {}

    override fun actionItemPressed(id: Int) {
        if (selectedKeys.isEmpty()) {
            return
        }

        when (id) {
            R.id.cab_delete -> deleteItems()
        }
    }

    override fun getSelectableItemCount() = alarms.size

    override fun getIsItemSelectable(position: Int) = true

    override fun getItemSelectionKey(position: Int) = alarms.getOrNull(position)?.id

    override fun getItemKeyPosition(key: Int) = alarms.indexOfFirst { it.id == key }

    override fun onActionModeCreated() {}

    override fun onActionModeDestroyed() {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = createViewHolder(R.layout.item_alarm, parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val alarm = alarms[position]
        holder.bindView(alarm, true, true) { itemView, layoutPosition ->
            setupView(itemView, alarm)
        }
        bindViewHolder(holder)
    }

    override fun getItemCount() = alarms.size

    fun updateItems(newItems: ArrayList<Alarm>) {
        alarms = newItems
        notifyDataSetChanged()
        finishActMode()
    }

    private fun deleteItems() {
        val alarmsToRemove = ArrayList<Alarm>()
        val positions = getSelectedItemPositions()
        getSelectedItems().forEach {
            alarmsToRemove.add(it)
        }

        alarms.removeAll(alarmsToRemove)
        removeSelectedItems(positions)
        activity.dbHelper.deleteAlarms(alarmsToRemove)
    }

    private fun getSelectedItems() = alarms.filter { selectedKeys.contains(it.id) } as ArrayList<Alarm>

    private fun setupView(view: View, alarm: Alarm) {
        val isSelected = selectedKeys.contains(alarm.id)
        view.apply {
            alarm_frame.isSelected = isSelected
            alarm_time.text = activity.getFormattedTime(alarm.timeInMinutes * 60, false, true)
            alarm_time.setTextColor(textColor)

            alarm_days.text = activity.getSelectedDaysString(alarm.days)
            alarm_days.setTextColor(textColor)

            alarm_label.text = alarm.label
            alarm_label.setTextColor(textColor)
            alarm_label.beVisibleIf(alarm.label.isNotEmpty())

            alarm_switch.isChecked = alarm.isEnabled
            alarm_switch.setColors(textColor, adjustedPrimaryColor, backgroundColor)
            alarm_switch.setOnClickListener {
                if (alarm.days > 0) {
                    if (activity.config.wasAlarmWarningShown) {
                        toggleAlarmInterface.alarmToggled(alarm.id, alarm_switch.isChecked)
                    } else {
                        ConfirmationDialog(activity, messageId = R.string.alarm_warning, positive = R.string.ok, negative = 0) {
                            activity.config.wasAlarmWarningShown = true
                            toggleAlarmInterface.alarmToggled(alarm.id, alarm_switch.isChecked)
                        }
                    }
                } else {
                    activity.toast(R.string.no_days_selected)
                    alarm_switch.isChecked = false
                }
            }

            val layoutParams = alarm_switch.layoutParams as RelativeLayout.LayoutParams
            layoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, if (alarm_label.isVisible()) alarm_label.id else alarm_days.id)
        }
    }
}
