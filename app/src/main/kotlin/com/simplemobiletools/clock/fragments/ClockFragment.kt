package com.simplemobiletools.clock.fragments

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.simplemobiletools.commons.extensions.updateTextColors
import kotlinx.android.synthetic.main.fragment_clock.view.*
import java.util.*

class ClockFragment(context: Context, attributeSet: AttributeSet) : RelativeLayout(context, attributeSet) {
    private val ONE_SECOND = 1000L
    private var passedSeconds = 0

    private val updateHandler = Handler()

    override fun onFinishInflate() {
        super.onFinishInflate()
        context.updateTextColors(clock_fragment)
        val offset = Calendar.getInstance().timeZone.rawOffset
        passedSeconds = ((Calendar.getInstance().timeInMillis + offset) / 1000).toInt()
        updateCurrentTime()
    }

    private fun updateCurrentTime() {
        val hours = (passedSeconds / 3600) % 24
        val minutes = (passedSeconds / 60) % 60
        val seconds = passedSeconds % 60
        val format = "%02d:%02d:%02d"
        clock_time.text = String.format(format, hours, minutes, seconds)

        updateHandler.postDelayed({
            passedSeconds++
            updateCurrentTime()
        }, ONE_SECOND)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        updateHandler.removeCallbacksAndMessages(null)
    }
}
