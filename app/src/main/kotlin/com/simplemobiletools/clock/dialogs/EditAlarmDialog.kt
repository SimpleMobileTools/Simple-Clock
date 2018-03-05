package com.simplemobiletools.clock.dialogs

import android.app.TimePickerDialog
import android.support.v7.app.AlertDialog
import android.widget.TextView
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.activities.SimpleActivity
import com.simplemobiletools.clock.extensions.config
import com.simplemobiletools.clock.extensions.formatAlarmTime
import com.simplemobiletools.clock.models.Alarm
import com.simplemobiletools.commons.extensions.applyColorFilter
import com.simplemobiletools.commons.extensions.getDialogTheme
import com.simplemobiletools.commons.extensions.moveLastItemToFront
import com.simplemobiletools.commons.extensions.setupDialogStuff
import kotlinx.android.synthetic.main.dialog_edit_alarm.view.*

class EditAlarmDialog(val activity: SimpleActivity, val alarm: Alarm, val callback: () -> Unit) {
    val view = activity.layoutInflater.inflate(R.layout.dialog_edit_alarm, null)

    init {
        val textColor = activity.config.textColor
        updateAlarmTime()

        view.apply {
            edit_alarm_time.setOnClickListener {
                TimePickerDialog(context, context.getDialogTheme(), timeSetListener, alarm.timeInMinutes / 60, alarm.timeInMinutes % 60, context.config.use24hourFormat).show()
            }

            colorLeftDrawable(edit_alarm_sound, textColor)
            edit_alarm_sound.text = "Default alarm"
            edit_alarm_sound.setOnClickListener {

            }

            colorLeftDrawable(edit_alarm_vibrate, textColor)
            edit_alarm_vibrate.isChecked = alarm.vibrate
            edit_alarm_vibrate_holder.setOnClickListener {
                edit_alarm_vibrate.toggle()
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
                val drawableId = if (isDayChecked) R.drawable.circle_background_filled else R.drawable.circle_background_stroke
                val drawable = activity.resources.getDrawable(drawableId)
                drawable.applyColorFilter(textColor)
                day.background = drawable

                day.setTextColor(if (isDayChecked) context.config.backgroundColor else textColor)
                edit_alarm_days_holder.addView(day)
            }
        }

        AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok, { dialog, which -> dialogConfirmed() })
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
                    activity.setupDialogStuff(view, this) {

                    }
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
        callback()
    }

    private fun colorLeftDrawable(textView: TextView, color: Int) {
        val leftImage = textView.compoundDrawables.first()
        leftImage.applyColorFilter(color)
        textView.setCompoundDrawables(leftImage, null, null, null)
    }
}
