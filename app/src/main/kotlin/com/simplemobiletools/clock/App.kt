package com.simplemobiletools.clock

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.facebook.stetho.Stetho
import com.simplemobiletools.clock.activities.SplashActivity
import com.simplemobiletools.clock.extensions.*
import com.simplemobiletools.clock.helpers.OPEN_TAB
import com.simplemobiletools.clock.helpers.Stopwatch
import com.simplemobiletools.clock.helpers.Stopwatch.State
import com.simplemobiletools.clock.helpers.TAB_STOPWATCH
import com.simplemobiletools.clock.helpers.TOGGLE_STOPWATCH
import com.simplemobiletools.clock.helpers.STOPWATCH_TOGGLE_ACTION
import com.simplemobiletools.clock.helpers.STOPWATCH_SHORTCUT_ID
import com.simplemobiletools.clock.models.TimerEvent
import com.simplemobiletools.clock.models.TimerState
import com.simplemobiletools.clock.services.StopwatchStopService
import com.simplemobiletools.clock.services.TimerStopService
import com.simplemobiletools.clock.services.startStopwatchService
import com.simplemobiletools.clock.services.startTimerService
import com.simplemobiletools.commons.extensions.checkUseEnglish
import com.simplemobiletools.commons.extensions.showErrorToast
import com.simplemobiletools.commons.helpers.isNougatMR1Plus
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class App : Application(), LifecycleObserver {

    private var countDownTimers = mutableMapOf<Int, CountDownTimer>()

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        EventBus.getDefault().register(this)

        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this)
        }

        checkUseEnglish()
        if (isNougatMR1Plus()) {
            val shortcutManager = getSystemService(ShortcutManager::class.java)
            val intent = Intent(this, SplashActivity::class.java).apply {
                putExtra(OPEN_TAB, TAB_STOPWATCH)
                putExtra(TOGGLE_STOPWATCH, true)
                action = STOPWATCH_TOGGLE_ACTION
            }
            val shortcut = ShortcutInfo.Builder(this, STOPWATCH_SHORTCUT_ID)
                .setShortLabel(getString(R.string.stopwatch))
                .setLongLabel(getString(R.string.start_stopwatch))
                .setIcon(Icon.createWithResource(this, R.drawable.ic_stopwatch_vector))
                .setIntent(
                    intent
                )
                .build()
            shortcutManager.dynamicShortcuts = listOf(shortcut)
        }
    }

    override fun onTerminate() {
        EventBus.getDefault().unregister(this)
        super.onTerminate()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun onAppBackgrounded() {
        timerHelper.getTimers { timers ->
            if (timers.any { it.state is TimerState.Running }) {
                startTimerService(this)
            }
        }
        if (Stopwatch.state == State.RUNNING) {
            startStopwatchService(this)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun onAppForegrounded() {
        EventBus.getDefault().post(TimerStopService)
        timerHelper.getTimers { timers ->
            val runningTimers = timers.filter { it.state is TimerState.Running }
            runningTimers.forEach { timer ->
                if (countDownTimers[timer.id] == null) {
                    EventBus.getDefault().post(TimerEvent.Start(timer.id!!, (timer.state as TimerState.Running).tick))
                }
            }
        }
        if (Stopwatch.state == State.RUNNING) {
            EventBus.getDefault().post(StopwatchStopService)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: TimerEvent.Reset) {
        updateTimerState(event.timerId, TimerState.Idle)
        countDownTimers[event.timerId]?.cancel()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: TimerEvent.Delete) {
        countDownTimers[event.timerId]?.cancel()
        timerHelper.deleteTimer(event.timerId) {
            EventBus.getDefault().post(TimerEvent.Refresh)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: TimerEvent.Start) {
        val countDownTimer = object : CountDownTimer(event.duration, 1000) {
            override fun onTick(tick: Long) {
                updateTimerState(event.timerId, TimerState.Running(event.duration, tick))
            }

            override fun onFinish() {
                EventBus.getDefault().post(TimerEvent.Finish(event.timerId, event.duration))
                EventBus.getDefault().post(TimerStopService)
            }
        }.start()
        countDownTimers[event.timerId] = countDownTimer
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: TimerEvent.Finish) {
        timerHelper.getTimer(event.timerId) { timer ->
            val pendingIntent = getOpenTimerTabIntent(event.timerId)
            val notification = getTimerNotification(timer, pendingIntent, false)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            try {
                notificationManager.notify(event.timerId, notification)
            } catch (e: Exception) {
                showErrorToast(e)
            }

            updateTimerState(event.timerId, TimerState.Finished)
            Handler(Looper.getMainLooper()).postDelayed({
                hideNotification(event.timerId)
            }, config.timerMaxReminderSecs * 1000L)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: TimerEvent.Pause) {
        timerHelper.getTimer(event.timerId) { timer ->
            updateTimerState(event.timerId, TimerState.Paused(event.duration, (timer.state as TimerState.Running).tick))
            countDownTimers[event.timerId]?.cancel()
        }
    }

    private fun updateTimerState(timerId: Int, state: TimerState) {
        timerHelper.getTimer(timerId) { timer ->
            val newTimer = timer.copy(state = state)
            timerHelper.insertOrUpdateTimer(newTimer) {
                EventBus.getDefault().post(TimerEvent.Refresh)
            }
        }
    }
}
