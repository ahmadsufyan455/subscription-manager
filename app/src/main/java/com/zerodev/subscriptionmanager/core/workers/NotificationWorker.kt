package com.zerodev.subscriptionmanager.core.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zerodev.subscriptionmanager.data.local.entities.NotificationEntity
import com.zerodev.subscriptionmanager.data.local.entities.NotificationType
import com.zerodev.subscriptionmanager.data.local.entities.SubscriptionStatus
import com.zerodev.subscriptionmanager.data.repository.NotificationRepository
import com.zerodev.subscriptionmanager.data.repository.SubscriptionRepository
import com.zerodev.subscriptionmanager.core.helper.NotificationHelper
import com.zerodev.subscriptionmanager.core.helper.NotificationTracker
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Locale

/**
 * Background worker that runs daily to check for upcoming payments
 * Sends notifications at 7, 3, and 1 days before payment
 */
class NotificationWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val repository: SubscriptionRepository by inject()
    private val notificationRepository: NotificationRepository by inject()

    override suspend fun doWork(): Result {
        return try {
            // Check if notifications are enabled in settings
            val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            val notificationsEnabled = prefs.getBoolean("notifications_enabled", false)

            if (!notificationsEnabled) {
                return Result.success()
            }

            // Get all active subscriptions
            val subscriptions = repository.getAllSubscriptionsSnapshot()
                .filter { it.status == SubscriptionStatus.ACTIVE }

            subscriptions.forEach { subscription ->
                val remainingDays = subscription.getRemainingDays() ?: return@forEach

                // Check if we should send notification for this subscription
                when (remainingDays) {
                    7, 3, 1 -> {
                        // Check if notification already sent
                        if (!NotificationTracker.hasNotificationBeenSent(
                                context,
                                subscription.id,
                                remainingDays
                            )
                        ) {
                            // Send notification
                            NotificationHelper.sendPaymentReminder(
                                context,
                                subscription,
                                remainingDays
                            )

                            // Save notification to database
                            val formattedPrice = String.format(Locale.US, "%.2f", subscription.price)
                            val message = "${subscription.name} ($$formattedPrice) will renew in $remainingDays day${if (remainingDays > 1) "s" else ""}. Make sure you have sufficient funds."
                            notificationRepository.insertNotification(
                                NotificationEntity(
                                    title = "Upcoming Bill",
                                    message = message,
                                    type = NotificationType.UPCOMING_BILL,
                                    subscriptionId = subscription.id
                                )
                            )

                            // Mark as sent
                            NotificationTracker.markNotificationSent(
                                context,
                                subscription.id,
                                remainingDays
                            )
                        }
                    }
                }
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
