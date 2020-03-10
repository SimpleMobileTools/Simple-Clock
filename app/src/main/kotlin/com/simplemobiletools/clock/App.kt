package com.simplemobiletools.clock

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.CountDownTimer
import androidx.annotation.RequiresApi
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.facebook.stetho.Stetho
import com.simplemobiletools.clock.extensions.config
import com.simplemobiletools.clock.extensions.getOpenTimerTabIntent
import com.simplemobiletools.clock.extensions.getTimerNotification
import com.simplemobiletools.clock.helpers.TIMER_NOTIF_ID
import com.simplemobiletools.clock.models.TimerState
import com.simplemobiletools.clock.services.TimerStopService
import com.simplemobiletools.clock.services.startTimerService
import com.simplemobiletools.commons.extensions.checkUseEnglish
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class App : Application(), LifecycleObserver {

    private var timer: CountDownTimer? = null
    private var lastTick = 0L

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        EventBus.getDefault().register(this)

        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this)
        }

        checkUseEnglish()
    }

    override fun onTerminate() {
        EventBus.getDefault().unregister(this)
        super.onTerminate()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun onAppBackgrounded() {
        if (config.timerState is TimerState.Running) {
            startTimerService(this)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun onAppForegrounded() {
        EventBus.getDefault().post(TimerStopService)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(state: TimerState.Idle) {
        config.timerState = state
        timer?.cancel()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(state: TimerState.Start) {
        timer = object : CountDownTimer(state.duration, 1000) {
            override fun onTick(tick: Long) {
                lastTick = tick

                val newState = TimerState.Running(state.duration, tick)
                EventBus.getDefault().post(newState)
                config.timerState = newState
            }

            override fun onFinish() {
                EventBus.getDefault().post(TimerState.Finish(state.duration))
                EventBus.getDefault().post(TimerStopService)
            }
        }.start()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: TimerState.Finish) {
        val pendingIntent = getOpenTimerTabIntent()
        val notification = getTimerNotification(pendingIntent, false) //MAYBE IN FUTURE ADD TIME TO NOTIFICATION
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(TIMER_NOTIF_ID, notification)

        EventBus.getDefault().post(TimerState.Finished)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(state: TimerState.Finished) {
        config.timerState = state
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: TimerState.Pause) {
        EventBus.getDefault().post(TimerState.Paused(event.duration, lastTick))
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(state: TimerState.Paused) {
        config.timerState = state
        timer?.cancel()
    }
}
