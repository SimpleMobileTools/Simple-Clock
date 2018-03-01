package com.simplemobiletools.clock.fragments

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet

abstract class MyViewPagerFragment(context: Context, attributeSet: AttributeSet) : CoordinatorLayout(context, attributeSet) {
    abstract fun onActivityResume()
}
