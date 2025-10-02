package com.zerodev.subscriptionmanager.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.zerodev.subscriptionmanager.data.local.entities.BillingCycle
import com.zerodev.subscriptionmanager.data.local.entities.Subscription
import com.zerodev.subscriptionmanager.presentation.viewmodel.HomeViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubscriptionBottomSheet(
    onDismiss: () -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var selectedBillingCycle by remember { mutableStateOf(BillingCycle.MONTHLY) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Text(
                    text = "Add Subscription",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Service Name Field
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    if (nameError != null) nameError = null
                },
                label = { Text("Service Name") },
                placeholder = { Text("e.g., Netflix, Spotify") },
                modifier = Modifier.fillMaxWidth(),
                isError = nameError != null,
                supportingText = nameError?.let { { Text(it) } }
            )

            // Price Field
            OutlinedTextField(
                value = price,
                onValueChange = {
                    price = it
                    if (priceError != null) priceError = null
                },
                label = { Text("Price") },
                placeholder = { Text("0.00") },
                leadingIcon = { Text("$") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = priceError != null,
                supportingText = priceError?.let { { Text(it) } }
            )

            // Billing Cycle Selection
            Text(
                text = "Billing Cycle",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            BillingCycle.entries.forEach { cycle ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selectedBillingCycle == cycle,
                            onClick = { selectedBillingCycle = cycle },
                            role = Role.RadioButton
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedBillingCycle == cycle) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (selectedBillingCycle == cycle) 4.dp else 1.dp
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedBillingCycle == cycle,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = cycle.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Every ${cycle.daysInCycle} days",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Add Button
            Button(
                onClick = {
                    if (validateInput(name, price, { nameError = it }, { priceError = it })) {
                        val subscription = Subscription(
                            name = name.trim(),
                            price = price.toDouble(),
                            billingCycle = selectedBillingCycle,
                            startDate = System.currentTimeMillis()
                        )
                        viewModel.addSubscription(subscription)
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && price.isNotBlank()
            ) {
                Text(
                    text = "Add Subscription",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun validateInput(
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