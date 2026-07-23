package com.zerodev.subscriptionmanager.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class NotificationType {
    UPCOMING_BILL,
    PAYMENT_SUCCESSFUL,
    NEW_FEATURE
}

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val message: String,
    val type: NotificationType,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val subscriptionId: Int? = null
)
