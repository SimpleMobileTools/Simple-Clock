package com.simplemobiletools.clock.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.activities.SimpleActivity
import com.simplemobiletools.clock.adapters.TimerAdapter
import com.simplemobiletools.clock.dialogs.EditTimerDialog
import com.simplemobiletools.clock.extensions.config
import com.simplemobiletools.clock.extensions.createNewTimer
import com.simplemobiletools.clock.extensions.timerHelper
import com.simplemobiletools.clock.helpers.DisabledItemChangeAnimator
import com.simplemobiletools.clock.models.Timer
import com.simplemobiletools.clock.models.TimerEvent
import com.simplemobiletools.commons.extensions.hideKeyboard
import com.simplemobiletools.commons.extensions.updateTextColors
import com.simplemobiletools.commons.models.AlarmSound
import kotlinx.android.synthetic.main.fragment_timer.timer_fragment
import kotlinx.android.synthetic.main.fragment_timer.view.timer_add
import kotlinx.android.synthetic.main.fragment_timer.view.timers_list
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class TimerFragment : Fragment() {
    private val INVALID_POSITION = -1
    private lateinit var view: ViewGroup
    private lateinit var timerAdapter: TimerAdapter
    private var timerPositionToScrollTo = INVALID_POSITION
    private var storedTextColor = 0
    private var currentEditAlarmDialog: EditTimerDialog? = null

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
            timerAdapter = TimerAdapter(requireActivity() as SimpleActivity, timers_list, ::refreshTimers, ::openEditTimer)

            storeStateVariables()

            timers_list.adapter = timerAdapter
            timers_list.itemAnimator = DisabledItemChangeAnimator()

            timer_add.setOnClickListener {
                activity?.run {
                    hideKeyboard()
                    openEditTimer(createNewTimer())
                }
            }

            refreshTimers()
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        requireContext().updateTextColors(timer_fragment)
        val configTextColor = requireContext().config.textColor
        if (storedTextColor != configTextColor) {
            (view.timers_list.adapter as TimerAdapter).updateTextColor(configTextColor)
        }
    }

    override fun onPause() {
        super.onPause()
        storeStateVariables()
    }

    private fun refreshTimers(scrollToLatest: Boolean = false) {
        activity?.timerHelper?.getTimers { timers ->
            timerAdapter.submitList(timers) {
                view.timers_list.post {
                    if (getView() != null) {
                        if (timerPositionToScrollTo != INVALID_POSITION && timerAdapter.itemCount > timerPositionToScrollTo) {
                            view.timers_list.scrollToPosition(timerPositionToScrollTo)
                            timerPositionToScrollTo = INVALID_POSITION
                        } else if (scrollToLatest) {
                            view.timers_list.scrollToPosition(timers.lastIndex)
                        }
                    }
                }
            }
        }
    }

    private fun storeStateVariables() {
        storedTextColor = requireContext().config.textColor
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: TimerEvent.Refresh) {
        refreshTimers()
    }

    fun updateAlarmSound(alarmSound: AlarmSound) {
        currentEditAlarmDialog?.updateAlarmSound(alarmSound)
    }

    fun updatePosition(timerId: Int) {
        activity?.timerHelper?.getTimers { timers ->
            val position = timers.indexOfFirst { it.id == timerId }
            if (position != INVALID_POSITION) {
                activity?.runOnUiThread {
                    if (timerAdapter.itemCount > position) {
                        view.timers_list.scrollToPosition(position)
                    } else {
                        timerPositionToScrollTo = position
                    }
                }
            }
        }
    }

    private fun openEditTimer(timer: Timer) {
        currentEditAlarmDialog = EditTimerDialog(activity as SimpleActivity, timer) {
            currentEditAlarmDialog = null
            refreshTimers()
        }
    }
}
