package com.zerodev.subscriptionmanager.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.zerodev.subscriptionmanager.core.utils.getSubscriptionIcon
import com.zerodev.subscriptionmanager.core.utils.validateFormInput
import com.zerodev.subscriptionmanager.data.local.entities.BillingCycle
import com.zerodev.subscriptionmanager.data.local.entities.Subscription
import com.zerodev.subscriptionmanager.presentation.viewmodel.HomeViewModel
import com.zerodev.subscriptionmanager.ui.components.DatePickerField
import com.zerodev.subscriptionmanager.ui.components.GlobalTextField
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddSubscriptionBottomSheet(
    onDismiss: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
    isEditMode: Boolean = false,
    subscriptionId: Int? = null,
) {
    val focusManager = LocalFocusManager.current
    val uiState by viewModel.uiState.collectAsState()

    val existingSubscription = remember(subscriptionId, uiState.subscriptions) {
        subscriptionId?.let { id ->
            uiState.subscriptions.find { it.id == id }
        }
    }

    var name by remember(existingSubscription) { mutableStateOf(existingSubscription?.name ?: "") }
    var price by remember(existingSubscription) {
        mutableStateOf(
            existingSubscription?.price?.toString() ?: ""
        )
    }
    var selectedBillingCycle by remember(existingSubscription) {
        mutableStateOf(
            existingSubscription?.billingCycle ?: BillingCycle.MONTHLY
        )
    }
    var customDays by remember(existingSubscription) {
        mutableStateOf(
            existingSubscription?.customCycleDays?.toString() ?: ""
        )
    }
    var startDate by remember(existingSubscription) {
        mutableLongStateOf(
            existingSubscription?.startDate ?: System.currentTimeMillis()
        )
    }

    var nameError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }
    var customDaysError by remember { mutableStateOf<String?>(null) }
    var showCancelConfirmation by remember { mutableStateOf(false) }

    val isButtonEnable = name.isNotBlank() && price.isNotBlank() &&
        (selectedBillingCycle != BillingCycle.CUSTOM ||
            (customDays.toIntOrNull()?.let { it in 1..365 } == true))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = if (isEditMode) "Edit Subscription" else "Add Subscription",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (isEditMode) "Update your subscription details" else "Track a new subscription",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Details",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )

        GlobalTextField(
            value = name,
            onValueChange = {
                name = it
                if (nameError != null) nameError = null
            },
            label = "Service Name",
            placeholder = "e.g., Netflix, Spotify",
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    painter = painterResource(id = getSubscriptionIcon(name)),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color.Unspecified
                )
            },
            isError = nameError != null,
            errorMessage = nameError
        )

        GlobalTextField(
            value = price,
            onValueChange = {
                price = it
                if (priceError != null) priceError = null
            },
            label = "Price",
            placeholder = "0.00",
            leadingIcon = { Text("$") },
            modifier = Modifier.fillMaxWidth(),
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            isError = priceError != null,
            errorMessage = priceError
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 4.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )

        Text(
            text = "Schedule",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )

        DatePickerField(
            selectedDate = startDate,
            onDateSelected = { startDate = it },
            label = "Start Date",
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Billing Cycle",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BillingCycle.entries.forEach { cycle ->
                FilterChip(
                    selected = selectedBillingCycle == cycle,
                    onClick = { selectedBillingCycle = cycle },
                    label = {
                        Text(
                            text = cycle.displayName,
                            fontWeight = if (selectedBillingCycle == cycle) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        width = if (selectedBillingCycle == cycle) 2.dp else 1.dp,
                        color = if (selectedBillingCycle == cycle) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        }
                    ),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        selectedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        if (selectedBillingCycle == BillingCycle.CUSTOM) {
            GlobalTextField(
                value = customDays,
                onValueChange = { input ->
                    customDays = input.filter { it.isDigit() }
                    val days = customDays.toIntOrNull()
                    customDaysError = when {
                        customDays.isBlank() -> null
                        days == null || days < 1 -> "Must be at least 1 day"
                        days > 365 -> "Must be 365 days or less"
                        else -> null
                    }
                },
                label = "Number of Days",
                placeholder = "e.g., 3, 7, 14",
                modifier = Modifier.fillMaxWidth(),
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                isError = customDaysError != null,
                errorMessage = customDaysError
            )

            val days = customDays.toIntOrNull()
            if (days != null && days in 1..365) {
                Text(
                    text = "Renews every $days day${if (days > 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 4.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            onClick = {
                if (validateFormInput(name, price, { nameError = it }, { priceError = it })) {
                    val cycleDays = if (selectedBillingCycle == BillingCycle.CUSTOM) customDays.toIntOrNull() else null
                    if (isEditMode && existingSubscription != null) {
                        val updatedSubscription = existingSubscription.copy(
                            name = name.trim(),
                            price = price.toDouble(),
                            billingCycle = selectedBillingCycle,
                            customCycleDays = cycleDays,
                            startDate = startDate
                        )
                        viewModel.updateSubscription(updatedSubscription)
                    } else {
                        val subscription = Subscription(
                            name = name.trim(),
                            price = price.toDouble(),
                            billingCycle = selectedBillingCycle,
                            customCycleDays = cycleDays,
                            startDate = startDate
                        )
                        viewModel.addSubscription(subscription)
                    }
                    onDismiss()
                }
            },
            enabled = isButtonEnable
        ) {
            val buttonLabelColor = if (isButtonEnable) 1f else 0.5f
            Text(
                text = if (isEditMode) "Update Subscription" else "Add Subscription",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = buttonLabelColor),
                ),
                fontWeight = FontWeight.Bold
            )
        }

        if (isEditMode && existingSubscription != null) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                onClick = { showCancelConfirmation = true },
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.error
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(
                    text = "Cancel Subscription",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    if (showCancelConfirmation && existingSubscription != null) {
        AlertDialog(
            onDismissRequest = { showCancelConfirmation = false },
            title = {
                Text(
                    text = "Cancel Subscription",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Are you sure you want to cancel ${existingSubscription.name}? This will stop future renewals.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val updatedSubscription = existingSubscription.copy(
                            name = name.trim(),
                            price = price.toDouble(),
                            billingCycle = selectedBillingCycle,
                            startDate = startDate
                        )
                        viewModel.cancelSubscription(updatedSubscription)
                        showCancelConfirmation = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Cancel Subscription", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelConfirmation = false }) {
                    Text("Keep Active")
                }
            }
        )
    }
}