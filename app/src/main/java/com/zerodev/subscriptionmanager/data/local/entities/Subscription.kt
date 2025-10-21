package com.zerodev.subscriptionmanager.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class BillingCycle(val displayName: String, val daysInCycle: Int) {
    MONTHLY("Monthly", 30),
    QUARTERLY("Quarterly", 90),
    YEARLY("Yearly", 365);

    fun getNextBillingDate(startDate: Long): Long {
        return startDate + (daysInCycle * 24 * 60 * 60 * 1000L)
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
                var nextDate = billingCycle.getNextBillingDate(startDate)
                val currentTime = System.currentTimeMillis()

                // Keep advancing until we find a future date
                while (nextDate <= currentTime) {
                    nextDate = billingCycle.getNextBillingDate(nextDate)
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
                billingCycle.getNextBillingDate(startDate)
            }

            SubscriptionStatus.EXPIRED -> null
        }
    }

    fun getCurrentBillingPeriodStart(): Long {
        var periodStart = startDate
        val currentTime = System.currentTimeMillis()

        // Advance to current period
        while (billingCycle.getNextBillingDate(periodStart) <= currentTime) {
            periodStart = billingCycle.getNextBillingDate(periodStart)
        }

        return periodStart
    }

    fun getRemainingDays(): Int? {
        if (!isActive()) return null
        val nextBilling = getNextBillingDate() ?: return null
        val daysLeft = (nextBilling - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)
        return daysLeft.toInt().coerceAtLeast(0)
    }

    fun needsRenewal(): Boolean {
        if (status != SubscriptionStatus.ACTIVE) return false
        val currentPeriodStart = getCurrentBillingPeriodStart()
        return currentPeriodStart != startDate
    }
}
