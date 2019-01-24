package com.simplemobiletools.clock.adapters

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.activities.SimpleActivity
import com.simplemobiletools.clock.extensions.config
import com.simplemobiletools.clock.models.MyTimeZone
import com.simplemobiletools.commons.extensions.getAdjustedPrimaryColor
import kotlinx.android.synthetic.main.item_add_time_zone.view.*
import java.util.*

class SelectTimeZonesAdapter(val activity: SimpleActivity, val timeZones: ArrayList<MyTimeZone>) : RecyclerView.Adapter<SelectTimeZonesAdapter.ViewHolder>() {
    private val config = activity.config
    private val textColor = config.textColor
    private val backgroundColor = config.backgroundColor
    private val primaryColor = activity.getAdjustedPrimaryColor()
    var selectedKeys = HashSet<Int>()

    init {
        val selectedTimeZones = config.selectedTimeZones
        timeZones.forEachIndexed { index, myTimeZone ->
            if (selectedTimeZones.contains(myTimeZone.id.toString())) {
                selectedKeys.add(myTimeZone.id)
            }
        }
    }

    private fun toggleItemSelection(select: Boolean, pos: Int) {
        val itemKey = timeZones.getOrNull(pos)?.id ?: return

        if (select) {
            selectedKeys.add(itemKey)
        } else {
            selectedKeys.remove(itemKey)
        }

        notifyItemChanged(pos)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = activity.layoutInflater.inflate(R.layout.item_add_time_zone, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindView(timeZones[position], textColor, primaryColor, backgroundColor)
    }

    override fun getItemCount() = timeZones.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bindView(myTimeZone: MyTimeZone, textColor: Int, primaryColor: Int, backgroundColor: Int): View {
            val isSelected = selectedKeys.contains(myTimeZone.id)
            itemView.apply {
                add_time_zone_checkbox.isChecked = isSelected
                add_time_zone_title.text = myTimeZone.title
                add_time_zone_title.setTextColor(textColor)

                add_time_zone_checkbox.setColors(textColor, primaryColor, backgroundColor)
                add_time_zone_holder.setOnClickListener {
                    viewClicked(myTimeZone)
                }
            }

            return itemView
        }

        private fun viewClicked(myTimeZone: MyTimeZone) {
            val isSelected = selectedKeys.contains(myTimeZone.id)
            toggleItemSelection(!isSelected, adapterPosition)
        }
    }
}
