package com.zerodev.subscriptionmanager.core.utils

import android.content.Context
import androidx.core.content.edit
import java.text.NumberFormat
import java.util.Locale

enum class Currency(val code: String, val symbol: String, val locale: Locale, val rateToUsd: Double) {
    USD("USD", "$", Locale.US, 1.0),
    IDR("IDR", "Rp", Locale.forLanguageTag("id-ID"), 17500.0);
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
        prefs.edit {
            putString(KEY_CURRENCY, currency.code)
        }
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

    @Suppress("unused")
    fun formatCompact(amountInUsd: Double, currency: Currency): String {
        val converted = convertFromUsd(amountInUsd, currency)
        return when (currency) {
            Currency.IDR -> when {
                converted >= 1_000_000 -> {
                    "${currency.symbol}${String.format(currency.locale, "%.1f", converted / 1_000_000)}M"
                }
                converted >= 1_000 -> {
                    "${currency.symbol}${String.format(currency.locale, "%.0f", converted / 1_000)}K"
                }
                else -> format(amountInUsd, currency)
            }
            else -> format(amountInUsd, currency)
        }
    }
    fun formatInput(input: String, currency: Currency): String {
        val symbols = java.text.DecimalFormatSymbols.getInstance(currency.locale)
        val groupingSeparator = symbols.groupingSeparator
        val decimalSeparator = symbols.decimalSeparator

        // Remove all grouping separators from the input string
        val cleanGrouping = input.replace(groupingSeparator.toString(), "")

        // Normalize the decimal separator to dot (.) for parsing
        val normalized = cleanGrouping.replace(decimalSeparator.toString(), ".")

        val cleanInput = normalized.filter { it.isDigit() || it == '.' }
        if (cleanInput.isEmpty()) return ""

        val firstDotIndex = cleanInput.indexOf('.')
        val intPart: String
        val decPart: String?

        if (firstDotIndex != -1) {
            intPart = cleanInput.substring(0, firstDotIndex).filter { it.isDigit() }
            decPart = cleanInput.substring(firstDotIndex + 1).filter { it.isDigit() }
        } else {
            intPart = cleanInput.filter { it.isDigit() }
            decPart = null
        }

        if (intPart.isEmpty()) {
            return if (decPart != null && currency != Currency.IDR) "0.$decPart" else ""
        }

        val parsedInt = intPart.toLongOrNull() ?: return ""
        val numberFormat = NumberFormat.getNumberInstance(currency.locale)
        val formattedInt = numberFormat.format(parsedInt)

        return if (decPart != null && currency != Currency.IDR) {
            val limitedDec = if (decPart.length > 2) decPart.substring(0, 2) else decPart
            "$formattedInt$decimalSeparator$limitedDec"
        } else {
            formattedInt
        }
    }

    fun parse(formatted: String, currency: Currency): Double {
        if (currency == Currency.IDR) {
            val digits = formatted.filter { it.isDigit() }
            return digits.toDoubleOrNull() ?: 0.0
        }
        val symbols = java.text.DecimalFormatSymbols.getInstance(currency.locale)
        val decimalSeparator = symbols.decimalSeparator
        val groupingSeparator = symbols.groupingSeparator
        var clean = formatted.replace(groupingSeparator.toString(), "")
        clean = clean.replace(decimalSeparator.toString(), ".")
        clean = clean.filter { it.isDigit() || it == '.' }
        return clean.toDoubleOrNull() ?: 0.0
    }
}
