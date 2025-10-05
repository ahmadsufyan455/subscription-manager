package com.zerodev.subscriptionmanager.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.zerodev.subscriptionmanager.core.utils.validateFormInput
import com.zerodev.subscriptionmanager.data.local.entities.BillingCycle
import com.zerodev.subscriptionmanager.data.local.entities.Subscription
import com.zerodev.subscriptionmanager.presentation.viewmodel.HomeViewModel
import com.zerodev.subscriptionmanager.ui.components.DatePickerField
import com.zerodev.subscriptionmanager.ui.components.GlobalTextField
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubscriptionBottomSheet(
    onDismiss: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
    isEditMode: Boolean = false,
    subscriptionId: Int? = null,
) {
    val focusManager = LocalFocusManager.current
    val uiState by viewModel.uiState.collectAsState()

    // Find subscription if in edit mode
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
    var startDate by remember(existingSubscription) {
        mutableLongStateOf(
            existingSubscription?.startDate ?: System.currentTimeMillis()
        )
    }

    var nameError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }

    val isButtonEnable = name.isNotBlank() && price.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Service Name Field
        GlobalTextField(
            value = name,
            onValueChange = {
                name = it
                if (nameError != null) nameError = null
            },
            label = "Service Name",
            placeholder = "e.g., Netflix, Spotify",
            modifier = Modifier.fillMaxWidth(),
            isError = nameError != null,
            errorMessage = nameError
        )

        // Price Field
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

        // Start Date Picker
        DatePickerField(
            selectedDate = startDate,
            onDateSelected = { startDate = it },
            label = "Start Date",
            modifier = Modifier.fillMaxWidth()
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
                    MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    width = 2.dp,
                    color = if (selectedBillingCycle == cycle) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    }
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (selectedBillingCycle == cycle) 4.dp else 1.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = if (selectedBillingCycle == cycle) MaterialTheme.colorScheme.primary.copy(
                                alpha = 0.1f
                            ) else MaterialTheme.colorScheme.surface
                        )
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
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Every ${cycle.daysInCycle} days",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Add/Update Button
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            onClick = {
                if (validateFormInput(name, price, { nameError = it }, { priceError = it })) {
                    if (isEditMode && existingSubscription != null) {
                        val updatedSubscription = existingSubscription.copy(
                            name = name.trim(),
                            price = price.toDouble(),
                            billingCycle = selectedBillingCycle,
                            startDate = startDate
                        )
                        viewModel.updateSubscription(updatedSubscription)
                    } else {
                        val subscription = Subscription(
                            name = name.trim(),
                            price = price.toDouble(),
                            billingCycle = selectedBillingCycle,
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

        if (isEditMode && existingSubscription != null)
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                onClick = {
                    val updatedSubscription = existingSubscription.copy(
                        name = name.trim(),
                        price = price.toDouble(),
                        billingCycle = selectedBillingCycle,
                        startDate = startDate
                    )
                    viewModel.cancelSubscription(updatedSubscription)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "Cancel Subscription",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }

        Spacer(modifier = Modifier.height(16.dp))
    }
}