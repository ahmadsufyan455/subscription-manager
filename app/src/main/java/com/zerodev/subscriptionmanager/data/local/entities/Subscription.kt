package com.zerodev.subscriptionmanager.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class BillingCycle(val displayName: String, val daysInCycle: Int) {
    WEEKLY("Weekly", 7),
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
        return if (isActive()) billingCycle.getNextBillingDate(startDate) else null
    }

    fun getRemainingDays(): Int? {
        if (!isActive()) return null
        val nextBilling = getNextBillingDate() ?: return null
        val daysLeft = (nextBilling - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)
        return daysLeft.toInt().coerceAtLeast(0)
    }
}
