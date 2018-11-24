package com.simplemobiletools.clock.activities

import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.view.animation.AnimationUtils
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.extensions.*
import com.simplemobiletools.clock.helpers.ALARM_ID
import com.simplemobiletools.clock.helpers.SystemSound
import com.simplemobiletools.clock.helpers.getPassedSeconds
import com.simplemobiletools.clock.models.Alarm
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.MINUTE_SECONDS
import kotlinx.android.synthetic.main.activity_reminder.*

class ReminderActivity : SimpleActivity() {

    private val maxReminderDurationHandler = Handler()
    private val swipeGuideFadeHandler = Handler()
    private var isAlarmReminder = false
    private var didVibrate = false
    private var alarm: Alarm? = null
    private var dragDownX = 0f
    private var systemSound: SystemSound? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder)
        showOverLockscreen()
        updateTextColors(reminder_holder)

        val id = intent.getIntExtra(ALARM_ID, -1)
        isAlarmReminder = id != -1
        if (id != -1) {
            alarm = dbHelper.getAlarmWithId(id) ?: return
        }

        val label = if (isAlarmReminder) {
            if (alarm!!.label.isEmpty()) {
                getString(R.string.alarm)
            } else {
                alarm!!.label
            }
        } else {
            getString(R.string.timer)
        }

        reminder_title.text = label
        reminder_text.text = if (isAlarmReminder) getFormattedTime(getPassedSeconds(), false, false) else getString(R.string.time_expired)

        val maxDuration = if (isAlarmReminder) config.alarmMaxReminderSecs else config.timerMaxReminderSecs
        maxReminderDurationHandler.postDelayed({
            finishActivity()
        }, maxDuration * 1000L)

        setupButtons()
        setupAudio()
    }

    private fun setupButtons() {
        if (isAlarmReminder) {
            setupAlarmButtons()
        } else {
            setupTimerButtons()
        }
    }

    private fun setupAlarmButtons() {
        reminder_stop.beGone()
        reminder_draggable_background.startAnimation(AnimationUtils.loadAnimation(this, R.anim.pulsing_animation))
        reminder_draggable_background.applyColorFilter(getAdjustedPrimaryColor())
        reminder_snooze.applyColorFilter(config.textColor)

        var minDragX = 0f
        var maxDragX = 0f
        var initialDraggableX = 0f

        reminder_dismiss.onGlobalLayout {
            minDragX = reminder_snooze.left.toFloat()
            maxDragX = reminder_dismiss.left.toFloat()
            initialDraggableX = reminder_draggable.left.toFloat()
        }

        reminder_draggable.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dragDownX = event.x
                    reminder_draggable_background.animate().alpha(0f)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    dragDownX = 0f
                    if (!didVibrate) {
                        reminder_draggable.animate().x(initialDraggableX).withEndAction {
                            reminder_draggable_background.animate().alpha(0.2f)
                        }

                        reminder_guide.animate().alpha(1f).start()
                        swipeGuideFadeHandler.removeCallbacksAndMessages(null)
                        swipeGuideFadeHandler.postDelayed({
                            reminder_guide.animate().alpha(0f).start()
                        }, 2000L)
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    reminder_draggable.x = Math.min(maxDragX, Math.max(minDragX, event.rawX - dragDownX))
                    if (reminder_draggable.x >= maxDragX - 50f) {
                        if (!didVibrate) {
                            reminder_draggable.performHapticFeedback()
                            didVibrate = true
                            finishActivity()
                        }
                    } else if (reminder_draggable.x <= minDragX + 50f) {
                        if (!didVibrate) {
                            reminder_draggable.performHapticFeedback()
                            didVibrate = true
                            snoozeAlarm()
                        }
                    }
                }
            }
            true
        }
    }

    private fun setupTimerButtons() {
        reminder_stop.background = resources.getColoredDrawableWithColor(R.drawable.circle_background_filled, getAdjustedPrimaryColor())
        arrayOf(reminder_snooze, reminder_draggable_background, reminder_draggable, reminder_dismiss).forEach {
            it.beGone()
        }

        reminder_stop.setOnClickListener {
            finishActivity()
        }
    }

    private fun setupAudio() {
        val soundUri = Uri.parse(if (alarm != null) alarm!!.soundUri else config.timerSoundUri)
        systemSound = SystemSound(this, soundUri);
        systemSound?.start()
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        finishActivity()
    }

    override fun onDestroy() {
        super.onDestroy()
        maxReminderDurationHandler.removeCallbacksAndMessages(null)
        swipeGuideFadeHandler.removeCallbacksAndMessages(null)
    }



    private fun snoozeAlarm() {
        systemSound?.pause()

        if (config.useSameSnooze) {
            setupAlarmClock(alarm!!, config.snoozeTime * MINUTE_SECONDS, false)
            finishActivity()
        } else {
            showPickSecondsDialog(config.snoozeTime * MINUTE_SECONDS, true, cancelCallback = { finishActivity() }) {
                config.snoozeTime = it / MINUTE_SECONDS
                setupAlarmClock(alarm!!, it, false)
                finishActivity()
            }
        }
    }

    private fun finishActivity() {
        systemSound?.kill()
        finish()
        overridePendingTransition(0, 0)
    }
}
