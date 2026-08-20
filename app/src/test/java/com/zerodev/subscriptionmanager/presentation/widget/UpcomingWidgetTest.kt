package com.zerodev.subscriptionmanager.presentation.widget

import com.zerodev.subscriptionmanager.data.local.entities.BillingCycle
import com.zerodev.subscriptionmanager.data.local.entities.Subscription
import com.zerodev.subscriptionmanager.data.local.entities.SubscriptionStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class UpcomingWidgetTest {

    @Test
    fun testWidgetConstants() {
        assertEquals("com.zerodev.subscriptionmanager.ACTION_OPEN_SUBSCRIPTION", UpcomingWidget.ACTION_OPEN_SUBSCRIPTION)
        assertEquals("com.zerodev.subscriptionmanager.ACTION_ADD_SUBSCRIPTION", UpcomingWidget.ACTION_ADD_SUBSCRIPTION)
        assertEquals("extra_subscription_id", UpcomingWidget.EXTRA_SUBSCRIPTION_ID)
    }

    @Test
    fun testUpcomingSubscriptionsOrdering() {
        val now = System.currentTimeMillis()
        val dayMillis = 24 * 60 * 60 * 1000L

        val sub1 = Subscription(
            id = 1,
            name = "Spotify",
            price = 9.99,
            billingCycle = BillingCycle.MONTHLY,
            startDate = now - (20 * dayMillis), // Due in 10 days
            status = SubscriptionStatus.ACTIVE
        )

        val sub2 = Subscription(
            id = 2,
            name = "Netflix",
            price = 15.99,
            billingCycle = BillingCycle.MONTHLY,
            startDate = now - (28 * dayMillis), // Due in 2 days
            status = SubscriptionStatus.ACTIVE
        )

        val subCancelled = Subscription(
            id = 3,
            name = "Cancelled Sub",
            price = 5.0,
            billingCycle = BillingCycle.MONTHLY,
            startDate = now - (10 * dayMillis),
            status = SubscriptionStatus.CANCELLED
        )

        val allSubscriptions = listOf(sub1, sub2, subCancelled)

        // Filter and sort as UpcomingWidget does
        val activeUpcoming = allSubscriptions
            .filter { it.status == SubscriptionStatus.ACTIVE && it.getNextBillingDate() != null }
            .sortedBy { it.getNextBillingDate() }

        assertEquals(2, activeUpcoming.size)
        // Netflix (due in 2 days) should come before Spotify (due in 10 days)
        assertEquals("Netflix", activeUpcoming[0].name)
        assertEquals("Spotify", activeUpcoming[1].name)
    }
}
