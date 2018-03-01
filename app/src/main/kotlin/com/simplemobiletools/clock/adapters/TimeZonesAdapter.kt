package com.simplemobiletools.clock.adapters

import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.activities.SimpleActivity
import com.simplemobiletools.clock.extensions.getFormattedDate
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

    override fun getActionMenuId() = R.menu.cab_timezones

    override fun prepareActionMode(menu: Menu) {}

    override fun prepareItemSelection(view: View) {}

    override fun markItemSelection(select: Boolean, view: View?) {
        view?.time_zone_frame?.isSelected = select
    }

    override fun actionItemPressed(id: Int) {}

    override fun getSelectableItemCount() = timeZones.size

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int) = createViewHolder(R.layout.item_time_zone, parent)

    override fun onBindViewHolder(holder: MyRecyclerViewAdapter.ViewHolder, position: Int) {
        val contact = timeZones[position]
        val view = holder.bindView(contact, true) { itemView, layoutPosition ->
            setupView(itemView, contact)
        }
        bindViewHolder(holder, position, view)
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

    private fun setupView(view: View, timeZone: MyTimeZone) {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone(timeZone.zoneName))
        val offset = calendar.timeZone.rawOffset
        val passedSeconds = ((calendar.timeInMillis + offset) / 1000).toInt()
        val hours = (passedSeconds / 3600) % 24
        val minutes = (passedSeconds / 60) % 60
        val format = "%02d:%02d"
        val formattedTime = String.format(format, hours, minutes)
        val formattedDate = activity.getFormattedDate(calendar)

        view.apply {
            time_zone_title.text = timeZone.title.substring(timeZone.title.indexOf(' '))
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
