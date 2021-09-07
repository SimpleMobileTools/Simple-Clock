package com.simplemobiletools.clock.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.activities.SimpleActivity
import com.simplemobiletools.clock.dialogs.MyTimePickerDialogDialog
import com.simplemobiletools.clock.extensions.*
import com.simplemobiletools.clock.models.Timer
import com.simplemobiletools.clock.models.TimerEvent
import com.simplemobiletools.clock.models.TimerState
import com.simplemobiletools.commons.extensions.*
import kotlinx.android.synthetic.main.item_timer.view.*
import org.greenrobot.eventbus.EventBus

class TimerAdapter(
    private val activity: SimpleActivity,
    private val onRefresh: () -> Unit,
    private val onClick: (Timer) -> Unit,
) : ListAdapter<Timer, TimerAdapter.TimerViewHolder>(diffUtil) {

    companion object {
        private val diffUtil = object : DiffUtil.ItemCallback<Timer>() {
            override fun areItemsTheSame(oldItem: Timer, newItem: Timer): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Timer, newItem: Timer): Boolean {
                return oldItem == newItem
            }
        }
    }

    private val config = activity.config
    private var textColor = config.textColor
    private var adjustedPrimaryColor = activity.getAdjustedPrimaryColor()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimerViewHolder {
        return TimerViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_timer, parent, false))
    }

    override fun onBindViewHolder(holder: TimerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun updateTextColor(textColor: Int) {
        this.textColor = textColor
        onRefresh.invoke()
    }

    inner class TimerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(timer: Timer) {
            itemView.apply {
                post {
                    timer_play_pause.background = activity.resources.getColoredDrawableWithColor(R.drawable.circle_background_filled, adjustedPrimaryColor)
                    timer_play_pause.applyColorFilter(if (adjustedPrimaryColor == Color.WHITE) Color.BLACK else Color.WHITE)
                    timer_reset.applyColorFilter(textColor)
                    timer_delete.applyColorFilter(textColor)
                }
                timer_label.setTextColor(textColor)
                timer_label.setHintTextColor(textColor.adjustAlpha(0.7f))
                //only update when different to prevent flickering and unnecessary updates
                if (timer_label.text.toString() != timer.label) {
                    timer_label.setText(timer.label)
                }


                timer_time.setTextColor(textColor)
                timer_time.text = when (timer.state) {
                    is TimerState.Finished -> 0.getFormattedDuration()
                    is TimerState.Idle -> timer.seconds.getFormattedDuration()
                    is TimerState.Paused -> timer.state.tick.getFormattedDuration()
                    is TimerState.Running -> timer.state.tick.getFormattedDuration()
                }
                timer_time.setOnClickListener {
                    changeDuration(timer)
                }

                timer_delete.applyColorFilter(textColor)
                timer_delete.setOnClickListener {
                    activity.timerHelper.deleteTimer(timer.id!!) {
                        onRefresh.invoke()
                    }
                }

                timer_reset.applyColorFilter(textColor)
                timer_reset.setOnClickListener {
                    stopTimer(timer)
                }


                timer_play_pause.setOnClickListener {
                    when (val state = timer.state) {
                        is TimerState.Idle -> EventBus.getDefault().post(TimerEvent.Start(timer.id!!, timer.seconds.secondsToMillis))
                        is TimerState.Paused -> EventBus.getDefault().post(TimerEvent.Start(timer.id!!, state.tick))
                        is TimerState.Running -> EventBus.getDefault().post(TimerEvent.Pause(timer.id!!, state.tick))
                        is TimerState.Finished -> EventBus.getDefault().post(TimerEvent.Start(timer.id!!, timer.seconds.secondsToMillis))
                    }
                }

                updateViewStates(timer.state)

                setOnClickListener {
                    onClick.invoke(timer)
                }
            }
        }

        private fun updateViewStates(state: TimerState) {
            val resetPossible = state is TimerState.Running || state is TimerState.Paused || state is TimerState.Finished
            itemView.timer_reset.beInvisibleIf(!resetPossible)
            itemView.timer_delete.beInvisibleIf(!(!resetPossible && itemCount > 1))
            val drawableId = if (state is TimerState.Running) R.drawable.ic_pause_vector else R.drawable.ic_play_vector
            val iconColor = if (adjustedPrimaryColor == Color.WHITE) Color.BLACK else Color.WHITE
            itemView.timer_play_pause.setImageDrawable(activity.resources.getColoredDrawableWithColor(drawableId, iconColor))
        }
    }

    private fun changeDuration(timer: Timer) {
        MyTimePickerDialogDialog(activity, timer.seconds) { seconds ->
            val timerSeconds = if (seconds <= 0) 10 else seconds
            updateTimer(timer.copy(seconds = timerSeconds))
        }
    }

    private fun updateTimer(timer: Timer, refresh: Boolean = true) {
        activity.timerHelper.insertOrUpdateTimer(timer) {
            if (refresh) {
                onRefresh.invoke()
            }
        }
    }

    private fun stopTimer(timer: Timer) {
        EventBus.getDefault().post(TimerEvent.Reset(timer.id!!, timer.seconds.secondsToMillis))
        activity.hideTimerNotification()
    }

}
