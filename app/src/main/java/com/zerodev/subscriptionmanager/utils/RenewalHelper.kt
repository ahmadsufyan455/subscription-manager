package com.zerodev.subscriptionmanager.utils

import android.content.Context
import com.zerodev.subscriptionmanager.data.local.entities.Subscription
import com.zerodev.subscriptionmanager.data.local.entities.SubscriptionStatus
import com.zerodev.subscriptionmanager.data.repository.SubscriptionRepository

object RenewalHelper {

    /**
     * Process renewals for all subscriptions
     * - ACTIVE subscriptions: Renew if billing period has passed
     * - CANCELLED subscriptions: Mark as EXPIRED if final billing period has passed
     * - EXPIRED subscriptions: No action
     */
    suspend fun processRenewals(context: Context, repository: SubscriptionRepository) {
        try {
            // Get all subscriptions as snapshot (not Flow)
            val subscriptions = repository.getAllSubscriptionsSnapshot()
            processSubscriptionList(context, subscriptions, repository)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun processSubscriptionList(
        context: Context,
        subscriptions: List<Subscription>,
        repository: SubscriptionRepository
    ) {
        val currentTime = System.currentTimeMillis()

        subscriptions.forEach { subscription ->
            when (subscription.status) {
                SubscriptionStatus.ACTIVE -> {
                    // Check if needs renewal
                    if (subscription.needsRenewal()) {
                        renewSubscription(context, subscription, repository)
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
        repository: SubscriptionRepository
    ) {
        val newStartDate = subscription.getCurrentBillingPeriodStart()

        if (newStartDate != subscription.startDate) {
            val renewedSubscription = subscription.copy(
                startDate = newStartDate
            )
            repository.updateSubscription(renewedSubscription)

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
