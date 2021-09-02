package com.simplemobiletools.clock.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.activities.SimpleActivity
import com.simplemobiletools.clock.adapters.TimerAdapter
import com.simplemobiletools.clock.extensions.config
import com.simplemobiletools.clock.extensions.hideTimerNotification
import com.simplemobiletools.clock.extensions.secondsToMillis
import com.simplemobiletools.clock.extensions.timerHelper
import com.simplemobiletools.clock.models.Timer
import com.simplemobiletools.clock.models.TimerEvent
import com.simplemobiletools.clock.models.TimerState
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.models.AlarmSound
import kotlinx.android.synthetic.main.fragment_timer.timer_view_pager
import kotlinx.android.synthetic.main.fragment_timer.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class TimerFragment : Fragment() {
    private val INVALID_POSITION = -1
    private lateinit var view: ViewGroup
    private lateinit var timerAdapter: TimerAdapter
    private var timerPositionToScrollTo = INVALID_POSITION

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        view = (inflater.inflate(R.layout.fragment_timer, container, false) as ViewGroup).apply {
            timerAdapter = TimerAdapter(requireActivity() as SimpleActivity) {
                refreshTimers()
            }

            timer_view_pager.adapter = timerAdapter
            //set empty page transformer to disable item animations
            timer_view_pager.setPageTransformer { _, _ -> }
            timer_view_pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    updateViews(position)
                }
            })

            timer_add.setOnClickListener {
                activity?.hideKeyboard(it)
                activity?.timerHelper?.insertNewTimer {
                    refreshTimers(true)
                }
            }

            activity?.updateTextColors(timer_fragment)

            val textColor = requireContext().config.textColor
            timer_play_pause.background =
                resources.getColoredDrawableWithColor(R.drawable.circle_background_filled, requireActivity().getAdjustedPrimaryColor())
            timer_play_pause.applyColorFilter(if (activity?.getAdjustedPrimaryColor() == Color.WHITE) Color.BLACK else Color.WHITE)
            timer_reset.applyColorFilter(textColor)

            timer_play_pause.setOnClickListener {
                val timer = timerAdapter.getItemAt(timer_view_pager.currentItem)
                when (val state = timer.state) {
                    is TimerState.Idle -> EventBus.getDefault().post(TimerEvent.Start(timer.id!!, timer.seconds.secondsToMillis))
                    is TimerState.Paused -> EventBus.getDefault().post(TimerEvent.Start(timer.id!!, state.tick))
                    is TimerState.Running -> EventBus.getDefault().post(TimerEvent.Pause(timer.id!!, state.tick))
                    is TimerState.Finished -> EventBus.getDefault().post(TimerEvent.Start(timer.id!!, timer.seconds.secondsToMillis))
                }
            }

            timer_reset.setOnClickListener {
                val timer = timerAdapter.getItemAt(timer_view_pager.currentItem)
                stopTimer(timer)
            }

            timer_delete.setOnClickListener {
                val timer = timerAdapter.getItemAt(timer_view_pager.currentItem)
                activity?.timerHelper?.deleteTimer(timer.id!!) {
                    refreshTimers()
                }
            }

            refreshTimers()
        }
        return view
    }

    private fun updateViews(position: Int) {
        activity?.runOnUiThread {
            if (timerAdapter.itemCount > 0) {
                val timer = timerAdapter.getItemAt(position)
                updateViewStates(timer.state)
                view.timer_play_pause.beVisible()
            } else {
                view.timer_delete.beGone()
                view.timer_play_pause.beGone()
                view.timer_reset.beGone()
            }
        }
    }

    private fun refreshTimers(scrollToLatest: Boolean = false) {
        activity?.timerHelper?.getTimers { timers ->
            Log.d(TAG, "refreshTimers: $timers")
            timerAdapter.submitList(timers) {
                Log.e(TAG, "submitted list: timerPositionToScrollTo=$timerPositionToScrollTo")
                if (timerPositionToScrollTo != INVALID_POSITION && timerAdapter.itemCount > timerPositionToScrollTo) {
                    Log.e(TAG, "scrolling to position=$timerPositionToScrollTo")
                    view.timer_view_pager.setCurrentItem(timerPositionToScrollTo, false)
                    timerPositionToScrollTo = INVALID_POSITION
                } else if (scrollToLatest) {
                    view.timer_view_pager.setCurrentItem(0, false)
                }
                updateViews(timer_view_pager.currentItem)
            }
        }
    }

    private fun stopTimer(timer: Timer) {
        EventBus.getDefault().post(TimerEvent.Reset(timer.id!!, timer.seconds.secondsToMillis))
        activity?.hideTimerNotification()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: TimerEvent.Refresh) {
        Log.d(TAG, "onMessageEvent: $event")
        refreshTimers()
    }

    private fun updateViewStates(state: TimerState) {
        val resetPossible = state is TimerState.Running || state is TimerState.Paused || state is TimerState.Finished
        view.timer_reset.beVisibleIf(resetPossible)
        view.timer_delete.beVisibleIf(!resetPossible && timerAdapter.itemCount > 1)

        val drawableId = if (state is TimerState.Running) {
            R.drawable.ic_pause_vector
        } else {
            R.drawable.ic_play_vector
        }

        val iconColor = if (activity?.getAdjustedPrimaryColor() == Color.WHITE) {
            Color.BLACK
        } else {
            Color.WHITE
        }

        view.timer_play_pause.setImageDrawable(resources.getColoredDrawableWithColor(drawableId, iconColor))
    }

    fun updateAlarmSound(alarmSound: AlarmSound) {
        val timer = timerAdapter.getItemAt(timer_view_pager.currentItem)
        activity?.timerHelper?.insertOrUpdateTimer(timer.copy(soundTitle = alarmSound.title, soundUri = alarmSound.uri)) {
            refreshTimers()
        }
    }

    fun updatePosition(timerId: Long) {
        Log.e(TAG, "updatePosition TIMER: $timerId")
        activity?.timerHelper?.getTimers { timers ->
            val position = timers.indexOfFirst { it.id == timerId }
            Log.e(TAG, "updatePosition POSITION: $position")
            if (position != INVALID_POSITION) {
                activity?.runOnUiThread {
                    if (timerAdapter.itemCount > position) {
                        Log.e(TAG, "updatePosition now: $position")
                        view.timer_view_pager.setCurrentItem(position, false)
                    } else {
                        Log.e(TAG, "updatePosition later: $position")
                        timerPositionToScrollTo = position
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "TimerFragment"
    }
}
