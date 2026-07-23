package com.zerodev.subscriptionmanager.data.local.converters

import androidx.room.TypeConverter
import com.zerodev.subscriptionmanager.data.local.entities.BillingCycle
import com.zerodev.subscriptionmanager.data.local.entities.SubscriptionStatus

class Converters {
    @TypeConverter
    fun fromBillingCycle(billingCycle: BillingCycle): String {
        return billingCycle.name
    }

    @TypeConverter
    fun toBillingCycle(billingCycle: String): BillingCycle {
        return BillingCycle.valueOf(billingCycle)
    }

    @TypeConverter
    fun fromSubscriptionStatus(status: SubscriptionStatus): String {
        return status.name
    }

    @TypeConverter
    fun toSubscriptionStatus(status: String): SubscriptionStatus {
        return SubscriptionStatus.valueOf(status)
    }

    @TypeConverter
    fun fromNotificationType(type: com.zerodev.subscriptionmanager.data.local.entities.NotificationType): String {
        return type.name
    }

    @TypeConverter
    fun toNotificationType(type: String): com.zerodev.subscriptionmanager.data.local.entities.NotificationType {
        return com.zerodev.subscriptionmanager.data.local.entities.NotificationType.valueOf(type)
    }
}