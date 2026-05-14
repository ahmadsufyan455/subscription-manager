package com.zerodev.subscriptionmanager.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class BillingCycle(val displayName: String, val daysInCycle: Int) {
    WEEKLY("Weekly", 7),
    MONTHLY("Monthly", 30),
    YEARLY("Yearly", 365),
    CUSTOM("Custom", 0);

    fun getNextBillingDate(startDate: Long, customDays: Int? = null): Long {
        val days = if (this == CUSTOM) (customDays ?: 0) else daysInCycle
        return startDate + (days * 24 * 60 * 60 * 1000L)
    }
}

enum class SubscriptionStatus {
    ACTIVE,
    CANCELLED,
    EXPIRED
}

@Entity(tableName = "subscriptions")
data class Subscription(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "price")
    val price: Double,

    @ColumnInfo(name = "billing_cycle")
    val billingCycle: BillingCycle,

    @ColumnInfo(name = "custom_cycle_days")
    val customCycleDays: Int? = null,

    @ColumnInfo(name = "start_date")
    val startDate: Long,

    @ColumnInfo(name = "status")
    val status: SubscriptionStatus = SubscriptionStatus.ACTIVE,

    @ColumnInfo(name = "cancelled_at")
    val cancelledAt: Long? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
) {
    fun isActive(): Boolean = status == SubscriptionStatus.ACTIVE

    fun getNextBillingDate(): Long? {
        return when (status) {
            SubscriptionStatus.ACTIVE -> {
                var nextDate = billingCycle.getNextBillingDate(startDate, customCycleDays)
                val currentTime = System.currentTimeMillis()

                // Keep advancing until we find a future date
                while (nextDate <= currentTime) {
                    nextDate = billingCycle.getNextBillingDate(nextDate, customCycleDays)
                }

                nextDate
            }

            SubscriptionStatus.CANCELLED -> {
                // A cancelled subscription remains active until the end of the
                // current billing period. Returning the immediate next billing
                // date (without advancing past it) ensures that
                // [RenewalHelper] can mark it as expired once that period ends
                // instead of incorrectly pushing the renewal far into the
                // future.
                billingCycle.getNextBillingDate(startDate, customCycleDays)
            }

            SubscriptionStatus.EXPIRED -> null
        }
    }

    fun getCurrentBillingPeriodStart(): Long {
        var periodStart = startDate
        val currentTime = System.currentTimeMillis()

        // Advance to current period
        while (billingCycle.getNextBillingDate(periodStart, customCycleDays) <= currentTime) {
            periodStart = billingCycle.getNextBillingDate(periodStart, customCycleDays)
        }

        return periodStart
    }

    fun getRemainingDays(): Int? {
        if (!isActive()) return null
        val nextBilling = getNextBillingDate() ?: return null
        val millisLeft = nextBilling - System.currentTimeMillis()
        if (millisLeft <= 0) return 0
        val dayMillis = 24 * 60 * 60 * 1000L
        return ((millisLeft + dayMillis - 1) / dayMillis).toInt()
    }

    fun needsRenewal(): Boolean {
        if (status != SubscriptionStatus.ACTIVE) return false
        val currentPeriodStart = getCurrentBillingPeriodStart()
        return currentPeriodStart != startDate
    }
}
