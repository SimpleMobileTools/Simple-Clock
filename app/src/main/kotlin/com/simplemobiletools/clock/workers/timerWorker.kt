package com.simplemobiletools.clock.workers

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.work.*
import com.simplemobiletools.clock.extensions.*
import java.util.*
import java.util.concurrent.TimeUnit

private const val TIMER_REQUEST_ID = "TIMER_REQUEST_ID"
const val TIMER_WORKER_KEY = "TIMER_WORKER_KEY"

private fun Fragment.saveTimerRequestId(uuid: UUID) =
        preferences.edit().putString(TIMER_REQUEST_ID, uuid.toString()).apply()

val Fragment.timerRequestId: UUID?
    get() =
        preferences.getString(TIMER_REQUEST_ID, UUID.randomUUID().toString())?.let { UUID.fromString(it) }

fun Fragment.cancelTimerWorker() =
        WorkManager.getInstance(requiredActivity).cancelAllWorkByTag(TIMER_WORKER_KEY)

fun Fragment.enqueueTimerWorker(delay: Long) =
        WorkManager.getInstance(requiredActivity).enqueueUniqueWork(TIMER_WORKER_KEY, ExistingWorkPolicy.REPLACE, timerRequest(delay))

private fun Fragment.timerRequest(delay: Long): OneTimeWorkRequest =
        OneTimeWorkRequestBuilder<TimerWorker>().setInitialDelay(delay, TimeUnit.MILLISECONDS).addTag(TIMER_WORKER_KEY).build().also {
        saveTimerRequestId(it.id)
}

class TimerWorker(val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result =
            try {
                context.showTimerNotification(false)
                context.config.timerTickStamp = 0L
                context.config.timerStartStamp = 0L
                Result.success()
            } catch (exception: Exception) {
                Result.failure()
            }
}
