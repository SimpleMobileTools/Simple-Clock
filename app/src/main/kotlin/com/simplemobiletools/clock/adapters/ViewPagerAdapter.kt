package com.simplemobiletools.clock.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.simplemobiletools.clock.fragments.AlarmFragment
import com.simplemobiletools.clock.fragments.ClockFragment
import com.simplemobiletools.clock.fragments.StopwatchFragment
import com.simplemobiletools.clock.fragments.TimerFragment
import com.simplemobiletools.clock.helpers.TABS_COUNT
import com.simplemobiletools.clock.helpers.TAB_ALARM
import com.simplemobiletools.clock.helpers.TAB_CLOCK
import com.simplemobiletools.clock.helpers.TAB_TIMER
import com.simplemobiletools.commons.models.AlarmSound

class ViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    private val fragments = HashMap<Int, Fragment>()

    override fun createFragment(position: Int): Fragment {
        val fragment = getFragment(position)
        fragments[position] = fragment
        return fragment
    }

    override fun getItemCount() = TABS_COUNT

    private fun getFragment(position: Int) = when (position) {
        0 -> ClockFragment()
        1 -> AlarmFragment()
        2 -> StopwatchFragment()
        3 -> TimerFragment()
        else -> throw RuntimeException("Trying to fetch unknown fragment id $position")
    }

    fun showAlarmSortDialog() {
        (fragments[TAB_ALARM] as? AlarmFragment)?.showSortingDialog()
    }

    fun updateClockTabAlarm() {
        (fragments[TAB_CLOCK] as? ClockFragment)?.updateAlarm()
    }

    fun updateAlarmTabAlarmSound(alarmSound: AlarmSound) {
        (fragments[TAB_ALARM] as? AlarmFragment)?.updateAlarmSound(alarmSound)
    }

    fun updateTimerTabAlarmSound(alarmSound: AlarmSound) {
        (fragments[TAB_TIMER] as? TimerFragment)?.updateAlarmSound(alarmSound)
    }

    fun updateTimerPosition(timerId: Int) {
        (fragments[TAB_TIMER] as? TimerFragment)?.updatePosition(timerId)
    }
}
