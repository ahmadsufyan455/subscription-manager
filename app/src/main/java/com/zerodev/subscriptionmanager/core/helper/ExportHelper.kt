package com.zerodev.subscriptionmanager.core.helper

import android.content.Context
import android.net.Uri
import com.zerodev.subscriptionmanager.data.local.entities.Subscription
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object ExportHelper {

    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
    }

    /**
     * Serialises [subscriptions] to a pretty-printed JSON string and writes it
     * to the [uri] chosen by the user via the SAF file picker.
     *
     * @return `true` on success, `false` on failure.
     */
    fun exportToJson(
        context: Context,
        uri: Uri,
        subscriptions: List<Subscription>
    ): Boolean {
        return try {
            val jsonString = json.encodeToString(subscriptions)
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(jsonString.toByteArray(Charsets.UTF_8))
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /** Suggested file name shown in the SAF picker. */
    fun suggestedFileName(): String {
        val timestamp = java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        return "subscriptions_$timestamp.json"
    }
}
