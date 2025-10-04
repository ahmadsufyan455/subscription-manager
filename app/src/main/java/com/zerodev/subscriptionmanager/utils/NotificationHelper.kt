package com.zerodev.subscriptionmanager.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.zerodev.subscriptionmanager.R
import com.zerodev.subscriptionmanager.data.local.entities.Subscription
import java.util.Locale

object NotificationHelper {

    private const val CHANNEL_ID = "payment_reminders"
    private const val CHANNEL_NAME = "Payment Reminders"
    private const val CHANNEL_DESCRIPTION = "Notifications for upcoming subscription payments"

    /**
     * Create notification channel (required for Android 8.0+)
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Send payment reminder notification
     */
    fun sendPaymentReminder(
        context: Context,
        subscription: Subscription,
        daysRemaining: Int
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val title = when (daysRemaining) {
            7 -> "Payment Due in 1 Week"
            3 -> "Payment Due in 3 Days"
            1 -> "Payment Due Tomorrow"
            else -> "Upcoming Payment"
        }

        val price = String.format(Locale.US, "%.0f", subscription.price)
        val message =
            "${subscription.name} payment is due in $daysRemaining day${if (daysRemaining > 1) "s" else ""}"

        val priority = when (daysRemaining) {
            1 -> NotificationCompat.PRIORITY_HIGH
            else -> NotificationCompat.PRIORITY_DEFAULT
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(priority)
            .setAutoCancel(true)
            .build()

        // Use subscription ID + days as unique notification ID
        val notificationId = "${subscription.id}_$daysRemaining".hashCode()
        notificationManager.notify(notificationId, notification)
    }
}
