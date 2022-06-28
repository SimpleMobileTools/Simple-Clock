package com.simplemobiletools.clock.extensions

import androidx.viewpager2.widget.ViewPager2

// todo: maybe move to simple-commons later
fun ViewPager2.onPageChangeListener(pageChangedAction: (newPosition: Int) -> Unit) {
    registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrollStateChanged(state: Int) {}

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

        override fun onPageSelected(position: Int) {
            pageChangedAction(position)
        }
    })
}
