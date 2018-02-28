package com.simplemobiletools.clock.fragments

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.simplemobiletools.commons.extensions.updateTextColors
import kotlinx.android.synthetic.main.fragment_stopwatch.view.*

class StopwatchFragment(context: Context, attributeSet: AttributeSet) : RelativeLayout(context, attributeSet) {
    override fun onFinishInflate() {
        super.onFinishInflate()
        context.updateTextColors(stopwatch_fragment)
    }
}
