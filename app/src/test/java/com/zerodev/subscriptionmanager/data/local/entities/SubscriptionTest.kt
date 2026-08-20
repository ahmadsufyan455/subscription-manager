package com.zerodev.subscriptionmanager.data.local.entities

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class SubscriptionTest {

    @Test
    fun testRemainingDaysLabelAndUrgency() {
        val now = System.currentTimeMillis()
        val dayMillis = 24 * 60 * 60 * 1000L

        // Subscription due in 5 days
        val subSoon = Subscription(
            name = "Netflix",
            price = 15.99,
            billingCycle = BillingCycle.MONTHLY,
            startDate = now - (25 * dayMillis),
            status = SubscriptionStatus.ACTIVE
        )

        val days = subSoon.getRemainingDays()
        assertEquals(5, days)
        assertEquals("In 5 days", subSoon.getRemainingDaysLabel())
        assertEquals(RenewalUrgency.SOON, subSoon.getRenewalUrgency())
    }

    @Test
    fun testUrgentRenewalStatus() {
        val now = System.currentTimeMillis()
        val dayMillis = 24 * 60 * 60 * 1000L

        // Subscription due today/tomorrow (e.g. started 29 or 30 days ago)
        val subUrgent = Subscription(
            name = "Spotify",
            price = 9.99,
            billingCycle = BillingCycle.MONTHLY,
            startDate = now - (29 * dayMillis),
            status = SubscriptionStatus.ACTIVE
        )

        assertEquals(1, subUrgent.getRemainingDays())
        assertEquals("Renews Tomorrow", subUrgent.getRemainingDaysLabel())
        assertEquals(RenewalUrgency.URGENT, subUrgent.getRenewalUrgency())
    }

    @Test
    fun testSubscriptionWithNotes() {
        val subWithNotes = Subscription(
            name = "Claude Pro",
            price = 20.0,
            billingCycle = BillingCycle.MONTHLY,
            startDate = System.currentTimeMillis(),
            notes = "Work account split with team"
        )

        assertEquals("Work account split with team", subWithNotes.notes)
    }

    @Test
    fun testSerializationWithNotesBackwardCompatibility() {
        val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

        // Legacy JSON without notes field
        val legacyJson = """{"id":1,"name":"Netflix","price":15.99,"billing_cycle":"MONTHLY","start_date":1000000}"""
        val decoded = json.decodeFromString<Subscription>(legacyJson)
        assertEquals("Netflix", decoded.name)
        assertEquals(null, decoded.notes)

        // Modern JSON with notes
        val modernSub = Subscription(
            id = 2,
            name = "Spotify",
            price = 10.99,
            billingCycle = BillingCycle.MONTHLY,
            startDate = 1000000,
            notes = "Family tier"
        )
        val encoded = json.encodeToString(modernSub)
        val reDecoded = json.decodeFromString<Subscription>(encoded)
        assertEquals("Family tier", reDecoded.notes)
    }
}
