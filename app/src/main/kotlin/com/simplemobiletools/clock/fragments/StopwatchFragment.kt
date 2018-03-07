package com.simplemobiletools.clock.fragments

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.extensions.config
import com.simplemobiletools.clock.extensions.formatStopwatchTime
import com.simplemobiletools.commons.extensions.beVisibleIf
import com.simplemobiletools.commons.extensions.getAdjustedPrimaryColor
import com.simplemobiletools.commons.extensions.getColoredDrawableWithColor
import com.simplemobiletools.commons.extensions.updateTextColors
import kotlinx.android.synthetic.main.fragment_stopwatch.view.*

class StopwatchFragment : Fragment() {
    private val UPDATE_INTERVAL = 10L

    private val updateHandler = Handler()
    private val mainLooper = Looper.getMainLooper()
    private var uptimeAtStart = 0L
    private var totalTicks = 0
    private var currentTicks = 0    // ticks that reset at pause
    private var isRunning = false

    lateinit var view: ViewGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        view = (inflater.inflate(R.layout.fragment_stopwatch, container, false) as ViewGroup).apply {
            stopwatch_time.setOnClickListener {
                togglePlayPause()
            }

            stopwatch_play_pause.setOnClickListener {
                togglePlayPause()
            }

            stopwatch_reset.setOnClickListener {

            }

            stopwatch_lap.setOnClickListener {

            }
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        setupStopwatch()
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        updateHandler.removeCallbacks(updateRunnable)
    }

    private fun setupStopwatch() {
        setupViews()
    }

    private fun setupViews() {
        context!!.apply {
            updateTextColors(view.stopwatch_fragment)
            view.stopwatch_play_pause.background = resources.getColoredDrawableWithColor(R.drawable.circle_background_filled, getAdjustedPrimaryColor())
        }
        updatePlayPauseIcon()
        updateDisplayedText()
    }

    private fun updatePlayPauseIcon() {
        val drawableId = if (isRunning) R.drawable.ic_pause else R.drawable.ic_play
        val iconColor = if (context!!.getAdjustedPrimaryColor() == Color.WHITE) Color.BLACK else context!!.config.textColor
        view.stopwatch_play_pause.setImageDrawable(resources.getColoredDrawableWithColor(drawableId, iconColor))
    }

    private fun togglePlayPause() {
        isRunning = !isRunning
        updatePlayPauseIcon()
        view.stopwatch_lap.beVisibleIf(isRunning)

        if (isRunning) {
            updateHandler.post(updateRunnable)
            uptimeAtStart = SystemClock.uptimeMillis()
        } else {
            val prevSessionsMS = (totalTicks - currentTicks) * UPDATE_INTERVAL
            val totalDuration = SystemClock.uptimeMillis() - uptimeAtStart + prevSessionsMS
            updateHandler.removeCallbacksAndMessages(null)
            view.stopwatch_time.text = totalDuration.formatStopwatchTime(true)
            currentTicks = 0
            totalTicks--
        }
    }

    private fun updateDisplayedText() {
        view.stopwatch_time.text = (totalTicks * UPDATE_INTERVAL).formatStopwatchTime(false)
    }

    private val updateRunnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                if (totalTicks % 10 == 0) {
                    mainLooper.run {
                        updateDisplayedText()
                    }
                }
                totalTicks++
                currentTicks++
                updateHandler.postAtTime(this, uptimeAtStart + currentTicks * UPDATE_INTERVAL)
            }
        }
    }
}
