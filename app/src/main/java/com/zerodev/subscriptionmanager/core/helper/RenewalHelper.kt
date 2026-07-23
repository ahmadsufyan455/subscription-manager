package com.zerodev.subscriptionmanager.core.helper

import android.content.Context
import com.zerodev.subscriptionmanager.data.local.entities.NotificationEntity
import com.zerodev.subscriptionmanager.data.local.entities.NotificationType
import com.zerodev.subscriptionmanager.data.local.entities.Subscription
import com.zerodev.subscriptionmanager.data.local.entities.SubscriptionStatus
import com.zerodev.subscriptionmanager.data.repository.NotificationRepository
import com.zerodev.subscriptionmanager.data.repository.SubscriptionRepository

object RenewalHelper {

    /**
     * Process renewals for all subscriptions
     * - ACTIVE subscriptions: Renew if billing period has passed
     * - CANCELLED subscriptions: Mark as EXPIRED if final billing period has passed
     * - EXPIRED subscriptions: No action
     */
    suspend fun processRenewals(
        context: Context,
        repository: SubscriptionRepository,
        notificationRepository: NotificationRepository? = null
    ) {
        try {
            // Get all subscriptions as snapshot (not Flow)
            val subscriptions = repository.getAllSubscriptionsSnapshot()
            processSubscriptionList(context, subscriptions, repository, notificationRepository)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun processSubscriptionList(
        context: Context,
        subscriptions: List<Subscription>,
        repository: SubscriptionRepository,
        notificationRepository: NotificationRepository?
    ) {
        val currentTime = System.currentTimeMillis()

        subscriptions.forEach { subscription ->
            when (subscription.status) {
                SubscriptionStatus.ACTIVE -> {
                    // Check if needs renewal
                    if (subscription.needsRenewal()) {
                        renewSubscription(context, subscription, repository, notificationRepository)
                    }
                }

                SubscriptionStatus.CANCELLED -> {
                    // Check if final billing period has ended
                    val nextBilling = subscription.getNextBillingDate()
                    if (nextBilling != null && currentTime > nextBilling) {
                        expireSubscription(subscription, repository)
                    }
                }

                SubscriptionStatus.EXPIRED -> {
                    // No action needed
                }
            }
        }
    }

    /**
     * Renew an ACTIVE subscription by updating its start date
     */
    private suspend fun renewSubscription(
        context: Context,
        subscription: Subscription,
        repository: SubscriptionRepository,
        notificationRepository: NotificationRepository?
    ) {
        val newStartDate = subscription.getCurrentBillingPeriodStart()

        if (newStartDate != subscription.startDate) {
            val renewedSubscription = subscription.copy(
                startDate = newStartDate
            )
            repository.updateSubscription(renewedSubscription)

            // Save notification to database
            val formattedPrice = String.format("%.2f", subscription.price)
            notificationRepository?.insertNotification(
                NotificationEntity(
                    title = "Payment Successful",
                    message = "Your payment of \$$formattedPrice for ${subscription.name} was successful.",
                    type = NotificationType.PAYMENT_SUCCESSFUL,
                    subscriptionId = subscription.id
                )
            )

            // Clear notification tracking so notifications can be sent again for new billing cycle
            NotificationTracker.clearTrackingForSubscription(context, subscription.id)
        }
    }

    /**
     * Expire a CANCELLED subscription
     */
    private suspend fun expireSubscription(
        subscription: Subscription,
        repository: SubscriptionRepository
    ) {
        val expiredSubscription = subscription.copy(
            status = SubscriptionStatus.EXPIRED
        )
        repository.updateSubscription(expiredSubscription)
    }
}
