package com.zerodev.subscriptionmanager.core.helper

import android.content.Context
import androidx.core.content.edit

object NotificationTracker {

    private const val PREFS_NAME = "notification_tracker"

    /**
     * Check if notification has been sent for this subscription and day count
     */
    fun hasNotificationBeenSent(
        context: Context,
        subscriptionId: Int,
        daysRemaining: Int
    ): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val key = "notif_${subscriptionId}_${daysRemaining}"
        return prefs.getBoolean(key, false)
    }

    /**
     * Mark notification as sent for this subscription and day count
     */
    fun markNotificationSent(
        context: Context,
        subscriptionId: Int,
        daysRemaining: Int
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val key = "notif_${subscriptionId}_${daysRemaining}"
        prefs.edit { putBoolean(key, true) }
    }

    /**
     * Clear tracking for a subscription (when it renews or is deleted)
     */
    fun clearTrackingForSubscription(
        context: Context,
        subscriptionId: Int
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {

            // Clear all tracking for this subscription ID
            listOf(7, 3, 1).forEach { days ->
                val key = "notif_${subscriptionId}_${days}"
                remove(key)
            }

        }
    }
}
