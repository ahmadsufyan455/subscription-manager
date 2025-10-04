package com.zerodev.subscriptionmanager.core.helper

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.zerodev.subscriptionmanager.core.workers.NotificationWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    private const val NOTIFICATION_WORK_NAME = "subscription_notification_check"

    /**
     * Schedule daily notification check
     * Runs once per day to check for upcoming payments
     */
    fun scheduleNotificationCheck(context: Context) {
        val constraints = Constraints.Builder()
            .build()

        val notificationWork = PeriodicWorkRequestBuilder<NotificationWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                NOTIFICATION_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                notificationWork
            )
    }

    /**
     * Cancel the notification check
     */
    fun cancelNotificationCheck(context: Context) {
        WorkManager.getInstance(context)
            .cancelUniqueWork(NOTIFICATION_WORK_NAME)
    }

    /**
     * Calculate initial delay to run at 6 AM tomorrow (or today if before 6 AM)
     */
    private fun calculateInitialDelay(): Long {
        val currentTime = Calendar.getInstance()
        val targetTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 6)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // If 6 AM today has passed, schedule for 6 AM tomorrow
            if (before(currentTime)) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        return targetTime.timeInMillis - currentTime.timeInMillis
    }
}
