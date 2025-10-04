package com.zerodev.subscriptionmanager.core.helper

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.zerodev.subscriptionmanager.core.workers.SubscriptionRenewalWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

object RenewalScheduler {

    private const val RENEWAL_WORK_NAME = "subscription_renewal_check"

    /**
     * Schedule daily renewal check
     * Runs once per day to process subscription renewals
     */
    fun scheduleRenewalCheck(context: Context) {
        val constraints = Constraints.Builder()
            .build()

        val renewalWork = PeriodicWorkRequestBuilder<SubscriptionRenewalWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                RENEWAL_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP, // Don't reschedule if already scheduled
                renewalWork
            )
    }

    /**
     * Cancel the renewal check
     */
    fun cancelRenewalCheck(context: Context) {
        WorkManager.getInstance(context)
            .cancelUniqueWork(RENEWAL_WORK_NAME)
    }

    /**
     * Calculate initial delay to run at 9 AM tomorrow (or today if before 9 AM)
     */
    private fun calculateInitialDelay(): Long {
        val currentTime = Calendar.getInstance()
        val targetTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // If 9 AM today has passed, schedule for 9 AM tomorrow
            if (before(currentTime)) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        return targetTime.timeInMillis - currentTime.timeInMillis
    }
}
