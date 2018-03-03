package com.simplemobiletools.clock.adapters

import android.support.v7.widget.RecyclerView
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.activities.SimpleActivity
import com.simplemobiletools.clock.extensions.config
import com.simplemobiletools.clock.models.MyTimeZone
import com.simplemobiletools.commons.extensions.getAdjustedPrimaryColor
import com.simplemobiletools.commons.interfaces.MyAdapterListener
import kotlinx.android.synthetic.main.item_add_time_zone.view.*
import java.util.*

class SelectTimeZonesAdapter(val activity: SimpleActivity, val timeZones: ArrayList<MyTimeZone>) : RecyclerView.Adapter<SelectTimeZonesAdapter.ViewHolder>() {
    private val itemViews = SparseArray<View>()
    private val selectedPositions = HashSet<Int>()
    private val config = activity.config
    private val textColor = config.textColor
    private val backgroundColor = config.backgroundColor
    private val primaryColor = activity.getAdjustedPrimaryColor()

    init {
        val selectedTimeZones = config.selectedTimeZones
        timeZones.forEachIndexed { index, myTimeZone ->
            if (selectedTimeZones.contains(myTimeZone.id.toString())) {
                selectedPositions.add(index)
            }
        }
    }

    private fun toggleItemSelection(select: Boolean, pos: Int) {
        if (select) {
            if (itemViews[pos] != null) {
                selectedPositions.add(pos)
            }
        } else {
            selectedPositions.remove(pos)
        }

        itemViews[pos]?.add_time_zone_checkbox?.isChecked = select
    }

    private val adapterListener = object : MyAdapterListener {
        override fun toggleItemSelectionAdapter(select: Boolean, position: Int) {
            toggleItemSelection(select, position)
        }

        override fun getSelectedPositions() = selectedPositions

        override fun itemLongClicked(position: Int) {}
    }

    fun getSelectedItemsSet(): HashSet<String> {
        val selectedItemsSet = HashSet<String>(selectedPositions.size)
        selectedPositions.forEach { selectedItemsSet.add(timeZones[it].id.toString()) }
        return selectedItemsSet
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = activity.layoutInflater.inflate(R.layout.item_add_time_zone, parent, false)
        return ViewHolder(view, adapterListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val timeZone = timeZones[position]
        itemViews.put(position, holder.bindView(timeZone, textColor, primaryColor, backgroundColor))
        toggleItemSelection(selectedPositions.contains(position), position)
    }

    override fun getItemCount() = timeZones.size

    class ViewHolder(view: View, val adapterListener: MyAdapterListener) : RecyclerView.ViewHolder(view) {
        fun bindView(timeZone: MyTimeZone, textColor: Int, primaryColor: Int, backgroundColor: Int): View {
            itemView.apply {
                add_time_zone_title.text = timeZone.title
                add_time_zone_title.setTextColor(textColor)

                add_time_zone_checkbox.setColors(textColor, primaryColor, backgroundColor)
                add_time_zone_holder.setOnClickListener {
                    adapterListener.toggleItemSelectionAdapter(!add_time_zone_checkbox.isChecked, adapterPosition)
                }
            }

            return itemView
        }
    }
}
