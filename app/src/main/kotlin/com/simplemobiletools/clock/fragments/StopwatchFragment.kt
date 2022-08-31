package com.simplemobiletools.clock.fragments

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.activities.SimpleActivity
import com.simplemobiletools.clock.adapters.StopwatchAdapter
import com.simplemobiletools.clock.extensions.config
import com.simplemobiletools.clock.extensions.formatStopwatchTime
import com.simplemobiletools.clock.helpers.SORT_BY_LAP
import com.simplemobiletools.clock.helpers.SORT_BY_LAP_TIME
import com.simplemobiletools.clock.helpers.SORT_BY_TOTAL_TIME
import com.simplemobiletools.clock.helpers.Stopwatch
import com.simplemobiletools.clock.models.Lap
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.SORT_DESCENDING
import kotlinx.android.synthetic.main.fragment_stopwatch.view.*

class StopwatchFragment : Fragment() {

    private var storedTextColor = 0

    lateinit var stopwatchAdapter: StopwatchAdapter
    lateinit var view: ViewGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        storeStateVariables()
        val sorting = requireContext().config.stopwatchLapsSort
        Lap.sorting = sorting
        view = (inflater.inflate(R.layout.fragment_stopwatch, container, false) as ViewGroup).apply {
            stopwatch_time.setOnClickListener {
                togglePlayPause()
            }

            stopwatch_play_pause.setOnClickListener {
                togglePlayPause()
            }

            stopwatch_reset.setOnClickListener {
                resetStopwatch()
            }

            stopwatch_sorting_indicator_1.setOnClickListener {
                changeSorting(SORT_BY_LAP)
            }

            stopwatch_sorting_indicator_2.setOnClickListener {
                changeSorting(SORT_BY_LAP_TIME)
            }

            stopwatch_sorting_indicator_3.setOnClickListener {
                changeSorting(SORT_BY_TOTAL_TIME)
            }

            stopwatch_lap.setOnClickListener {
                stopwatch_sorting_indicators_holder.beVisible()
                Stopwatch.lap()
                updateLaps()
            }

            stopwatchAdapter = StopwatchAdapter(activity as SimpleActivity, ArrayList(), stopwatch_list) {
                if (it is Int) {
                    changeSorting(it)
                }
            }
            stopwatch_list.adapter = stopwatchAdapter
        }

        updateSortingIndicators(sorting)
        return view
    }

    override fun onResume() {
        super.onResume()
        setupViews()

        val configTextColor = requireContext().getProperTextColor()
        if (storedTextColor != configTextColor) {
            stopwatchAdapter.updateTextColor(configTextColor)
        }

        Stopwatch.addUpdateListener(updateListener)
        updateLaps()
        view.stopwatch_sorting_indicators_holder.beVisibleIf(Stopwatch.laps.isNotEmpty())
        if (Stopwatch.laps.isNotEmpty()) {
            updateSorting(Lap.sorting)
        }
    }

    override fun onPause() {
        super.onPause()
        storeStateVariables()
        Stopwatch.removeUpdateListener(updateListener)
    }

    private fun storeStateVariables() {
        storedTextColor = requireContext().getProperTextColor()
    }

    private fun setupViews() {
        val properPrimaryColor = requireContext().getProperPrimaryColor()
        view.apply {
            requireContext().updateTextColors(stopwatch_fragment)
            stopwatch_play_pause.background = resources.getColoredDrawableWithColor(R.drawable.circle_background_filled, properPrimaryColor)
            stopwatch_reset.applyColorFilter(requireContext().getProperTextColor())
        }
    }

    private fun updateIcons(state: Stopwatch.State) {
        val drawableId = if (state == Stopwatch.State.RUNNING) R.drawable.ic_pause_vector else R.drawable.ic_play_vector
        val iconColor = if (requireContext().getProperPrimaryColor() == Color.WHITE) Color.BLACK else Color.WHITE
        view.stopwatch_play_pause.setImageDrawable(resources.getColoredDrawableWithColor(drawableId, iconColor))
    }

    private fun togglePlayPause() {
        (activity as SimpleActivity).handleNotificationPermission {
            if (it) {
                Stopwatch.toggle(true)
            } else {
                activity?.toast(R.string.no_post_notifications_permissions)
            }
        }
    }

    private fun updateDisplayedText(totalTime: Long, lapTime: Long, useLongerMSFormat: Boolean) {
        view.stopwatch_time.text = totalTime.formatStopwatchTime(useLongerMSFormat)
        if (Stopwatch.laps.isNotEmpty() && lapTime != -1L) {
            stopwatchAdapter.updateLastField(lapTime, totalTime)
        }
    }

    private fun resetStopwatch() {
        Stopwatch.reset()

        updateLaps()
        view.apply {
            stopwatch_reset.beGone()
            stopwatch_lap.beGone()
            stopwatch_time.text = 0L.formatStopwatchTime(false)
            stopwatch_sorting_indicators_holder.beInvisible()
        }
    }

    private fun changeSorting(clickedValue: Int) {
        val sorting = if (Lap.sorting and clickedValue != 0) {
            Lap.sorting.flipBit(SORT_DESCENDING)
        } else {
            clickedValue or SORT_DESCENDING
        }
        updateSorting(sorting)
    }

    private fun updateSorting(sorting: Int) {
        updateSortingIndicators(sorting)
        Lap.sorting = sorting
        requireContext().config.stopwatchLapsSort = sorting
        updateLaps()
    }

    private fun updateSortingIndicators(sorting: Int) {
        var bitmap = requireContext().resources.getColoredBitmap(R.drawable.ic_sorting_triangle_vector, requireContext().getProperPrimaryColor())
        view.apply {
            stopwatch_sorting_indicator_1.beInvisibleIf(sorting and SORT_BY_LAP == 0)
            stopwatch_sorting_indicator_2.beInvisibleIf(sorting and SORT_BY_LAP_TIME == 0)
            stopwatch_sorting_indicator_3.beInvisibleIf(sorting and SORT_BY_TOTAL_TIME == 0)

            val activeIndicator = when {
                sorting and SORT_BY_LAP != 0 -> stopwatch_sorting_indicator_1
                sorting and SORT_BY_LAP_TIME != 0 -> stopwatch_sorting_indicator_2
                else -> stopwatch_sorting_indicator_3
            }

            if (sorting and SORT_DESCENDING == 0) {
                val matrix = Matrix()
                matrix.postScale(1f, -1f)
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            }
            activeIndicator.setImageBitmap(bitmap)
        }
    }

    private fun updateLaps() {
        stopwatchAdapter.updateItems(Stopwatch.laps)
    }

    private val updateListener = object : Stopwatch.UpdateListener {
        override fun onUpdate(totalTime: Long, lapTime: Long, useLongerMSFormat: Boolean) {
            updateDisplayedText(totalTime, lapTime, useLongerMSFormat)
        }

        override fun onStateChanged(state: Stopwatch.State) {
            updateIcons(state)
            view.stopwatch_lap.beVisibleIf(state == Stopwatch.State.RUNNING)
            view.stopwatch_reset.beVisibleIf(state != Stopwatch.State.STOPPED)
        }
    }
}
