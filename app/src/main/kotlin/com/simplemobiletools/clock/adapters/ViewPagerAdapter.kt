package com.simplemobiletools.clock.adapters

import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.activities.SimpleActivity
import com.simplemobiletools.clock.fragments.MyViewPagerFragment
import com.simplemobiletools.clock.helpers.TABS_COUNT

class ViewPagerAdapter(val activity: SimpleActivity) : PagerAdapter() {
    private val fragments = HashMap<Int, MyViewPagerFragment>()

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val layout = getFragment(position)
        val fragment = activity.layoutInflater.inflate(layout, container, false) as MyViewPagerFragment
        fragments[position] = fragment
        container.addView(fragment)
        return fragment
    }

    override fun destroyItem(container: ViewGroup, position: Int, item: Any) {
        container.removeView(item as View)
    }

    override fun getCount() = TABS_COUNT
    override fun isViewFromObject(view: View, item: Any) = view == item

    private fun getFragment(position: Int) = when (position) {
        0 -> R.layout.fragment_clock
        1 -> R.layout.fragment_alarm
        else -> R.layout.fragment_stopwatch
    }

    fun activityResumed() {
        fragments.values.forEach {
            it.onActivityResume()
        }
    }
}
