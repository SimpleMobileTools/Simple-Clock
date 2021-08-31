package com.simplemobiletools.clock.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.activities.SimpleActivity
import com.simplemobiletools.clock.adapters.TimerAdapter
import com.simplemobiletools.clock.dialogs.MyTimePickerDialogDialog
import com.simplemobiletools.clock.extensions.config
import com.simplemobiletools.clock.extensions.hideTimerNotification
import com.simplemobiletools.clock.extensions.timerHelper
import com.simplemobiletools.clock.models.Timer
import com.simplemobiletools.clock.models.TimerState
import com.simplemobiletools.commons.extensions.*
import kotlinx.android.synthetic.main.fragment_timer.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class TimerFragment : Fragment() {

    lateinit var view: ViewGroup
    private lateinit var timerAdapter: TimerAdapter

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
//                    is TimerState.Idle -> EventBus.getDefault().post(TimerState.Start(timer.seconds.secondsToMillis))
//                    is TimerState.Paused -> EventBus.getDefault().post(TimerState.Start(state.tick))
//                    is TimerState.Running -> EventBus.getDefault().post(TimerState.Pause(state.tick))
//                    is TimerState.Finished -> EventBus.getDefault().post(TimerState.Start(timer.seconds.secondsToMillis))
                    else -> {
                    }
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
        val timer = timerAdapter.getItemAt(position)
        //check if timer is running to update view
    }

    private fun refreshTimers(scrollToLast: Boolean = false) {
        activity?.timerHelper?.getTimers { timers ->
            timerAdapter.submitList(timers)
            activity?.runOnUiThread {
                view.timer_delete.beVisibleIf(timers.size > 1)
                if (scrollToLast) {
                    view.timer_view_pager.currentItem = timers.lastIndex
                }
            }
        }
    }

    private fun stopTimer(timer: Timer) {
        EventBus.getDefault().post(TimerState.Idle)
        activity?.hideTimerNotification()
//        view.timer_time.text = activity?.config?.timerSeconds?.getFormattedDuration()
    }

    private fun changeDuration() {
        MyTimePickerDialogDialog(activity as SimpleActivity, requireContext().config.timerSeconds) { seconds ->
            val timerSeconds = if (seconds <= 0) 10 else seconds
            activity?.config?.timerSeconds = timerSeconds
            val duration = timerSeconds.getFormattedDuration()
//            view.timer_initial_time.text = duration

//            if (view.timer_reset.isGone()) {
//                stopTimer()
//            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(state: TimerState.Idle) {
//        view.timer_time.text = requiredActivity.config.timerSeconds.getFormattedDuration()
        updateViewStates(state)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(state: TimerState.Running) {
//        view.timer_time.text = state.tick.div(1000F).roundToInt().getFormattedDuration()
        updateViewStates(state)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(state: TimerState.Paused) {
        updateViewStates(state)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(state: TimerState.Finished) {
//        view.timer_time.text = 0.getFormattedDuration()
        updateViewStates(state)
    }

    private fun updateViewStates(state: TimerState) {
        val resetPossible = state is TimerState.Running || state is TimerState.Paused || state is TimerState.Finished
        view.timer_reset.beVisibleIf(resetPossible)

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
//
//    fun updateAlarmSound(alarmSound: AlarmSound) {
//        activity?.config?.timerSoundTitle = alarmSound.title
//        activity?.config?.timerSoundUri = alarmSound.uri
//        view.timer_sound.text = alarmSound.title
//    }
}
