package com.zerodev.subscriptionmanager.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zerodev.subscriptionmanager.data.repository.SubscriptionRepository
import com.zerodev.subscriptionmanager.utils.RenewalHelper
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Background worker that runs daily to process subscription renewals
 * - Renews ACTIVE subscriptions when billing period passes
 * - Expires CANCELLED subscriptions after final billing period
 */
class SubscriptionRenewalWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val repository: SubscriptionRepository by inject()

    override suspend fun doWork(): Result {
        return try {
            // Process all subscription renewals
            RenewalHelper.processRenewals(repository)

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
