package com.simplemobiletools.clock.dialogs

import android.app.TimePickerDialog
import android.graphics.drawable.Drawable
import android.support.v7.app.AlertDialog
import android.widget.TextView
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.activities.SimpleActivity
import com.simplemobiletools.clock.extensions.config
import com.simplemobiletools.clock.extensions.formatAlarmTime
import com.simplemobiletools.clock.models.Alarm
import com.simplemobiletools.commons.extensions.*
import kotlinx.android.synthetic.main.dialog_edit_alarm.view.*

class EditAlarmDialog(val activity: SimpleActivity, val alarm: Alarm, val callback: () -> Unit) {
    private val view = activity.layoutInflater.inflate(R.layout.dialog_edit_alarm, null)
    private val textColor = activity.config.textColor

    init {
        updateAlarmTime()

        view.apply {
            edit_alarm_time.setOnClickListener {
                TimePickerDialog(context, context.getDialogTheme(), timeSetListener, alarm.timeInMinutes / 60, alarm.timeInMinutes % 60, context.config.use24hourFormat).show()
            }

            colorLeftDrawable(edit_alarm_sound)
            edit_alarm_sound.text = alarm.soundTitle
            edit_alarm_sound.setOnClickListener {

            }

            colorLeftDrawable(edit_alarm_vibrate)
            edit_alarm_vibrate.isChecked = alarm.vibrate
            edit_alarm_vibrate_holder.setOnClickListener {
                edit_alarm_vibrate.toggle()
                alarm.vibrate = edit_alarm_vibrate.isChecked
            }

            edit_alarm_label_image.applyColorFilter(textColor)

            val dayLetters = activity.resources.getStringArray(R.array.week_day_letters).toList() as ArrayList<String>
            if (activity.config.isSundayFirst) {
                dayLetters.moveLastItemToFront()
            }

            for (i in 0..6) {
                val pow = Math.pow(2.0, i.toDouble()).toInt()
                val day = activity.layoutInflater.inflate(R.layout.alarm_day, edit_alarm_days_holder, false) as TextView
                day.text = dayLetters[i]

                val isDayChecked = alarm.days and pow != 0
                day.background = getProperDayDrawable(isDayChecked)

                day.setTextColor(if (isDayChecked) context.config.backgroundColor else textColor)
                day.setOnClickListener {
                    val selectDay = alarm.days and pow == 0
                    if (selectDay) {
                        alarm.days = alarm.days.addBit(pow)
                    } else {
                        alarm.days = alarm.days.removeBit(pow)
                    }
                    day.background = getProperDayDrawable(selectDay)
                    day.setTextColor(if (selectDay) context.config.backgroundColor else textColor)
                }

                edit_alarm_days_holder.addView(day)
            }
        }

        AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok, { dialog, which -> dialogConfirmed() })
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
                    activity.setupDialogStuff(view, this)
                }
    }

    private val timeSetListener = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
        alarm.timeInMinutes = hourOfDay * 60 + minute
        updateAlarmTime()
    }

    private fun updateAlarmTime() {
        view.edit_alarm_time.text = alarm.timeInMinutes.formatAlarmTime()
    }

    private fun dialogConfirmed() {
        alarm.label = view.edit_alarm_label.value
        callback()
    }

    private fun colorLeftDrawable(textView: TextView) {
        val leftImage = textView.compoundDrawables.first()
        leftImage.applyColorFilter(textColor)
        textView.setCompoundDrawables(leftImage, null, null, null)
    }

    private fun getProperDayDrawable(selected: Boolean): Drawable {
        val drawableId = if (selected) R.drawable.circle_background_filled else R.drawable.circle_background_stroke
        val drawable = activity.resources.getDrawable(drawableId)
        drawable.applyColorFilter(textColor)
        return drawable
    }
}
