package com.simplemobiletools.clock.fragments

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.activities.SimpleActivity
import com.simplemobiletools.clock.dialogs.MyTimePickerDialogDialog
import com.simplemobiletools.clock.extensions.colorLeftDrawable
import com.simplemobiletools.clock.extensions.config
import com.simplemobiletools.commons.extensions.*
import kotlinx.android.synthetic.main.fragment_timer.view.*

class TimerFragment : Fragment() {
    private val UPDATE_INTERVAL = 1000L

    private var isRunning = false
    private var uptimeAtStart = 0L
    private var initialSecs = 0
    private var totalTicks = 0
    private var currentTicks = 0
    private var updateHandler = Handler()

    lateinit var view: ViewGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        view = (inflater.inflate(R.layout.fragment_timer, container, false) as ViewGroup).apply {
            timer_time.setOnClickListener {
                togglePlayPause()
            }

            timer_play_pause.setOnClickListener {
                togglePlayPause()
            }

            timer_reset.setOnClickListener {
                resetTimer()
            }

            timer_initial_time.setOnClickListener {

            }

            timer_vibrate_holder.setOnClickListener {
                timer_vibrate.toggle()
                context!!.config.timerVibrate = timer_vibrate.isChecked
            }

            timer_sound.setOnClickListener {

            }
        }

        initialSecs = context!!.config.timerSeconds
        updateDisplayedText()
        return view
    }

    override fun onResume() {
        super.onResume()
        setupViews()
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        updateHandler.removeCallbacks(updateRunnable)
    }

    private fun setupViews() {
        val config = context!!.config
        val textColor = config.textColor
        view.apply {
            context!!.updateTextColors(timer_fragment)
            timer_play_pause.background = resources.getColoredDrawableWithColor(R.drawable.circle_background_filled, context!!.getAdjustedPrimaryColor())
            timer_reset.applyColorFilter(textColor)

            timer_initial_time.text = config.timerSeconds.getFormattedDuration()
            timer_initial_time.colorLeftDrawable(textColor)
            timer_initial_time.setOnClickListener {
                MyTimePickerDialogDialog(activity as SimpleActivity, config.timerSeconds) {
                    config.timerSeconds = it
                    timer_initial_time.text = it.getFormattedDuration()
                    if (!isRunning) {
                        resetTimer()
                    }
                }
            }

            timer_vibrate.isChecked = config.timerVibrate
            timer_vibrate.colorLeftDrawable(textColor)

            timer_sound.text = config.timerSoundTitle
            timer_sound.colorLeftDrawable(textColor)
        }

        updateIcons()
        updateDisplayedText()
    }

    private fun togglePlayPause() {
        isRunning = !isRunning
        updateIcons()

        if (isRunning) {
            updateHandler.post(updateRunnable)
            uptimeAtStart = SystemClock.uptimeMillis()
            view.timer_reset.beVisible()
        } else {
            updateHandler.removeCallbacksAndMessages(null)
            currentTicks = 0
            totalTicks--
        }
    }

    private fun updateIcons() {
        val drawableId = if (isRunning) R.drawable.ic_pause else R.drawable.ic_play
        val iconColor = if (context!!.getAdjustedPrimaryColor() == Color.WHITE) Color.BLACK else context!!.config.textColor
        view.timer_play_pause.setImageDrawable(resources.getColoredDrawableWithColor(drawableId, iconColor))
    }

    private fun resetTimer() {
        updateHandler.removeCallbacksAndMessages(null)
        isRunning = false
        currentTicks = 0
        totalTicks = 0
        initialSecs = context!!.config.timerSeconds
        updateDisplayedText()
        updateIcons()
        view.timer_reset.beGone()
    }

    private fun updateDisplayedText() {
        view.timer_time.text = (initialSecs - totalTicks).getFormattedDuration()
    }

    private val updateRunnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                updateDisplayedText()
                currentTicks++
                totalTicks++
                updateHandler.postAtTime(this, uptimeAtStart + currentTicks * UPDATE_INTERVAL)
            }
        }
    }
}
