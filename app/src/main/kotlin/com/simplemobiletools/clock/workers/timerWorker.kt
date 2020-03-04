package com.simplemobiletools.clock.workers

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.work.*
import com.simplemobiletools.clock.extensions.preferences
import com.simplemobiletools.clock.extensions.requiredActivity
import java.util.*
import java.util.concurrent.TimeUnit

private const val TIMER_REQUEST_ID = "TIMER_REQUEST_ID"
private const val TIMER_WORKER_KEY = "TIMER_WORKER_KEY"

private fun Fragment.saveTimerRequestId(uuid: UUID) =
    preferences.edit().putString(TIMER_REQUEST_ID, uuid.toString()).apply()

val Fragment.timerRequestId: UUID get() =
    UUID.fromString(preferences.getString(TIMER_REQUEST_ID, UUID.randomUUID().toString()))

fun Fragment.cancelTimerWorker() =
    WorkManager.getInstance(requiredActivity).apply {
        timerRequestId.let(::cancelWorkById)
    }

fun Fragment.enqueueTimerWorker(delay: Long) =
    WorkManager.getInstance(requiredActivity).enqueueUniqueWork(TIMER_WORKER_KEY, ExistingWorkPolicy.REPLACE, timerRequest(delay))

private fun Fragment.timerRequest(delay: Long) =
    OneTimeWorkRequestBuilder<TimerWorker>().setInitialDelay(delay, TimeUnit.MILLISECONDS).build().also {
        saveTimerRequestId(it.id)
    }

class TimerWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result =
        try {
            Result.success()
        } catch (exception: Exception) {
            Result.failure()
        }
}

