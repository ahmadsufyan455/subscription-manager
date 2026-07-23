package com.zerodev.subscriptionmanager.core.helper

import android.content.Context
import android.net.Uri
import com.zerodev.subscriptionmanager.data.local.entities.Subscription
import kotlinx.serialization.json.Json

object ImportHelper {

    private val json = Json {
        ignoreUnknownKeys = true   // tolerates extra fields in future versions
        isLenient = true
    }

    /**
     * Reads the JSON file at [uri] and deserialises it into a list of
     * [Subscription] objects, with each ID reset to 0 so Room generates
     * fresh primary keys on insert.
     *
     * @return The parsed list on success, or `null` if the file is invalid.
     */
    fun readFromJson(context: Context, uri: Uri): List<Subscription>? {
        return try {
            val jsonString = context.contentResolver
                .openInputStream(uri)
                ?.use { it.bufferedReader().readText() }
                ?: return null

            json.decodeFromString<List<Subscription>>(jsonString)
                .map { it.copy(id = 0) }   // strip IDs → Room assigns new ones
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
