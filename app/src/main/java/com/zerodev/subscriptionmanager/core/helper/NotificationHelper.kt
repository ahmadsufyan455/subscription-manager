package com.zerodev.subscriptionmanager.core.helper

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import com.zerodev.subscriptionmanager.MainActivity
import com.zerodev.subscriptionmanager.R
import com.zerodev.subscriptionmanager.core.utils.getSubscriptionIcon
import com.zerodev.subscriptionmanager.data.local.entities.Subscription

object NotificationHelper {

    private const val CHANNEL_ID = "payment_reminders"
    private const val CHANNEL_NAME = "Payment Reminders"
    private const val CHANNEL_DESCRIPTION = "Notifications for upcoming subscription payments"

    /**
     * Create notification channel (required for Android 8.0+)
     */
    fun createNotificationChannel(context: Context) {
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
            description = CHANNEL_DESCRIPTION
        }

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
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

        val message =
            "${subscription.name} payment is due in $daysRemaining day${if (daysRemaining > 1) "s" else ""}"

        val priority = when (daysRemaining) {
            1 -> NotificationCompat.PRIORITY_HIGH
            else -> NotificationCompat.PRIORITY_DEFAULT
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val brandIconRes = getSubscriptionIcon(subscription.name)
        val largeIconBitmap = getBitmapFromDrawable(context, brandIconRes)

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.subtrack)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setPriority(priority)
            .setAutoCancel(true)

        if (largeIconBitmap != null) {
            notificationBuilder.setLargeIcon(largeIconBitmap)
        }

        // Use subscription ID + days as unique notification ID
        val notificationId = "${subscription.id}_$daysRemaining".hashCode()
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    /**
     * Convert any Drawable (VectorDrawable or BitmapDrawable) into a high-resolution Bitmap for notification largeIcon
     */
    private fun getBitmapFromDrawable(context: Context, resId: Int): Bitmap? {
        val drawable = ContextCompat.getDrawable(context, resId) ?: return null
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap
        }

        val density = context.resources.displayMetrics.density
        val targetSize = (64 * density).toInt().coerceAtLeast(128)
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth.coerceAtLeast(targetSize) else targetSize
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight.coerceAtLeast(targetSize) else targetSize

        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}
