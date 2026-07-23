package com.zerodev.subscriptionmanager.core.utils

fun validateFormInput(
    name: String,
    price: String,
    currency: Currency,
    setNameError: (String?) -> Unit,
    setPriceError: (String?) -> Unit
): Boolean {
    var isValid = true

    // Validate name
    when {
        name.isBlank() -> {
            setNameError("Service name is required")
            isValid = false
        }

        name.length < 2 -> {
            setNameError("Service name must be at least 2 characters")
            isValid = false
        }

        name.length > 50 -> {
            setNameError("Service name must be less than 50 characters")
            isValid = false
        }

        else -> setNameError(null)
    }

    // Validate price
    when {
        price.isBlank() -> {
            setPriceError("Price is required")
            isValid = false
        }

        else -> {
            val parsedPrice = CurrencyFormatter.parse(price, currency)
            val priceInUsd = CurrencyFormatter.convertToUsd(parsedPrice, currency)
            when {
                parsedPrice <= 0 -> {
                    setPriceError("Price must be greater than 0")
                    isValid = false
                }

                priceInUsd > 10000 -> {
                    val limitFormatted = CurrencyFormatter.format(10000.0, currency)
                    setPriceError("Price must be less than $limitFormatted")
                    isValid = false
                }

                else -> setPriceError(null)
            }
        }
    }

    return isValid
}