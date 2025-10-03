package com.zerodev.subscriptionmanager.utils

fun validateFormInput(
    name: String,
    price: String,
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
            try {
                val priceValue = price.toDouble()
                when {
                    priceValue <= 0 -> {
                        setPriceError("Price must be greater than 0")
                        isValid = false
                    }

                    priceValue > 10000 -> {
                        setPriceError("Price must be less than $10,000")
                        isValid = false
                    }

                    else -> setPriceError(null)
                }
            } catch (_: NumberFormatException) {
                setPriceError("Please enter a valid price")
                isValid = false
            }
        }
    }

    return isValid
}