package com.zerodev.subscriptionmanager.core.utils

import android.content.Context
import java.text.NumberFormat
import java.util.Locale

enum class Currency(val code: String, val symbol: String, val locale: Locale, val rateToUsd: Double) {
    USD("USD", "$", Locale.US, 1.0),
    IDR("IDR", "Rp", Locale("id", "ID"), 16000.0);
}

object CurrencyFormatter {

    private const val PREFS_NAME = "app_settings"
    private const val KEY_CURRENCY = "selected_currency"

    fun getSelectedCurrency(context: Context): Currency {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val code = prefs.getString(KEY_CURRENCY, Currency.USD.code) ?: Currency.USD.code
        return Currency.entries.find { it.code == code } ?: Currency.USD
    }

    fun setSelectedCurrency(context: Context, currency: Currency) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_CURRENCY, currency.code).apply()
    }

    fun convertFromUsd(amount: Double, currency: Currency): Double {
        return amount * currency.rateToUsd
    }

    fun convertToUsd(amount: Double, currency: Currency): Double {
        return amount / currency.rateToUsd
    }

    fun format(amountInUsd: Double, currency: Currency): String {
        val converted = convertFromUsd(amountInUsd, currency)
        val formatter = NumberFormat.getCurrencyInstance(currency.locale).apply {
            maximumFractionDigits = if (currency == Currency.IDR) 0 else 2
            minimumFractionDigits = 0
        }
        return formatter.format(converted)
    }

    fun formatCompact(amountInUsd: Double, currency: Currency): String {
        val converted = convertFromUsd(amountInUsd, currency)
        return when {
            currency == Currency.IDR && converted >= 1_000_000 -> {
                "${currency.symbol}${String.format(currency.locale, "%.1f", converted / 1_000_000)}M"
            }
            currency == Currency.IDR && converted >= 1_000 -> {
                "${currency.symbol}${String.format(currency.locale, "%.0f", converted / 1_000)}K"
            }
            else -> {
                val formatter = NumberFormat.getCurrencyInstance(currency.locale).apply {
                    maximumFractionDigits = if (currency == Currency.IDR) 0 else 2
                    minimumFractionDigits = 0
                }
                formatter.format(converted)
            }
        }
    }
}
