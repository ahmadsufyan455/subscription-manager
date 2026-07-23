package com.zerodev.subscriptionmanager.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zerodev.subscriptionmanager.R
import com.zerodev.subscriptionmanager.core.utils.CurrencyFormatter
import com.zerodev.subscriptionmanager.core.utils.validateFormInput
import com.zerodev.subscriptionmanager.core.utils.getSubscriptionIcon
import com.zerodev.subscriptionmanager.data.local.entities.BillingCycle
import com.zerodev.subscriptionmanager.data.local.entities.Subscription
import com.zerodev.subscriptionmanager.presentation.viewmodel.HomeViewModel
import com.zerodev.subscriptionmanager.ui.components.CustomInputField
import com.zerodev.subscriptionmanager.ui.components.SegmentedControl
import com.zerodev.subscriptionmanager.ui.components.PopularServiceItem
import com.zerodev.subscriptionmanager.ui.components.DetailSubscriptionContent
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubscriptionBottomSheet(
    onDismiss: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
    isEditMode: Boolean = false,
    subscriptionId: Int? = null,
) {
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val currency = remember { CurrencyFormatter.getSelectedCurrency(context) }
    val uiState by viewModel.uiState.collectAsState()

    val existingSubscription = remember(subscriptionId, uiState.subscriptions) {
        subscriptionId?.let { id ->
            uiState.subscriptions.find { it.id == id }
        }
    }

    var name by remember(existingSubscription) { mutableStateOf(existingSubscription?.name ?: "") }
    var price by remember(existingSubscription, currency) {
        mutableStateOf(
            existingSubscription?.let {
                val converted = CurrencyFormatter.convertFromUsd(it.price, currency)
                val rawStr = if (converted % 1.0 == 0.0) converted.toLong().toString() else converted.toString()
                CurrencyFormatter.formatInput(rawStr, currency)
            } ?: ""
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

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = startDate)
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) {
                showDatePicker = true
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        startDate = it
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    headlineContentColor = MaterialTheme.colorScheme.onSurface,
                    weekdayContentColor = MaterialTheme.colorScheme.onSurface,
                    subheadContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    yearContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    currentYearContentColor = MaterialTheme.colorScheme.primary,
                    selectedYearContentColor = MaterialTheme.colorScheme.onPrimary,
                    selectedYearContainerColor = MaterialTheme.colorScheme.primary,
                    dayContentColor = MaterialTheme.colorScheme.onSurface,
                    selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                    selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                    todayContentColor = MaterialTheme.colorScheme.primary,
                    todayDateBorderColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }

    if (isEditMode && existingSubscription != null) {
        DetailSubscriptionContent(
            existingSubscription = existingSubscription,
            currency = currency,
            onCancelClick = { showCancelConfirmation = true }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Title
            Text(
                text = if (isEditMode) "Edit Subscription" else "New Subscription",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            // Large Cost Section
            PriceSection(
                price = price,
                onPriceChange = { input ->
                    price = CurrencyFormatter.formatInput(input, currency)
                    if (priceError != null) priceError = null
                },
                currencySymbol = currency.symbol,
                priceError = priceError,
                selectedBillingCycle = selectedBillingCycle
            )

            // Service Name Input
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "SERVICE NAME",
                    style = MaterialTheme.typography.labelMedium,
                    color = com.zerodev.subscriptionmanager.ui.theme.TextSecondary,
                    fontWeight = FontWeight.Bold
                )
                val brandIcon = remember(name) { getSubscriptionIcon(name) }
                CustomInputField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (nameError != null) nameError = null
                    },
                    placeholder = "e.g. Netflix, Spotify",
                    leadingIcon = {
                        if (brandIcon != R.drawable.subtrack) {
                            Icon(
                                painter = painterResource(id = brandIcon),
                                contentDescription = "Brand Logo",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Label,
                                contentDescription = "Service Name",
                                tint = com.zerodev.subscriptionmanager.ui.theme.TextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    isError = nameError != null,
                    errorMessage = nameError
                )
            }

            // Popular Services Row
            PopularServicesSection(
                onServiceSelect = { selectedName ->
                    name = selectedName
                    if (nameError != null) nameError = null
                }
            )

            // Billing Cycle Section
            BillingCycleSection(
                selectedBillingCycle = selectedBillingCycle,
                onBillingCycleSelect = { selectedBillingCycle = it },
                customDays = customDays,
                onCustomDaysChange = { input ->
                    customDays = input.filter { it.isDigit() }
                    val days = customDays.toIntOrNull()
                    customDaysError = when {
                        customDays.isBlank() -> null
                        days == null || days < 1 -> "Must be >= 1"
                        days > 365 -> "Must be <= 365"
                        else -> null
                    }
                },
                customDaysError = customDaysError,
                focusManager = focusManager
            )

            // First Payment / Start Date Picker
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "FIRST PAYMENT",
                    style = MaterialTheme.typography.labelMedium,
                    color = com.zerodev.subscriptionmanager.ui.theme.TextSecondary,
                    fontWeight = FontWeight.Bold
                )
                val dateFormat = "dd/MM/yyyy"
                CustomInputField(
                    value = SimpleDateFormat(dateFormat, Locale.getDefault()).format(Date(startDate)),
                    onValueChange = {},
                    placeholder = "dd/mm/yyyy",
                    trailingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_calendar),
                            contentDescription = "Select date",
                            tint = com.zerodev.subscriptionmanager.ui.theme.TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    readOnly = true,
                    interactionSource = interactionSource,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Save Button
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                onClick = {
                    if (validateFormInput(name, price, currency, { nameError = it }, { priceError = it })) {
                        val cycleDays =
                            if (selectedBillingCycle == BillingCycle.CUSTOM) customDays.toIntOrNull() else null
                        val parsedPrice = CurrencyFormatter.parse(price, currency)
                        val priceInUsd = CurrencyFormatter.convertToUsd(parsedPrice, currency)
                        if (isEditMode && existingSubscription != null) {
                            val updatedSubscription = existingSubscription.copy(
                                name = name.trim(),
                                price = priceInUsd,
                                billingCycle = selectedBillingCycle,
                                customCycleDays = cycleDays,
                                startDate = startDate
                            )
                            viewModel.updateSubscription(updatedSubscription)
                        } else {
                            val subscription = Subscription(
                                name = name.trim(),
                                price = priceInUsd,
                                billingCycle = selectedBillingCycle,
                                customCycleDays = cycleDays,
                                startDate = startDate
                            )
                            viewModel.addSubscription(subscription)
                        }
                        onDismiss()
                    }
                },
                enabled = isButtonEnable,
                colors = ButtonDefaults.buttonColors(
                    containerColor = com.zerodev.subscriptionmanager.ui.theme.Primary,
                    disabledContainerColor = com.zerodev.subscriptionmanager.ui.theme.Primary.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEditMode) "Update Subscription" else "Save Subscription",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.White,
                        ),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Cancel / Delete Subscription button (Edit Mode only)
            if (isEditMode && existingSubscription != null) {
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
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Cancel Subscription",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Cancel Confirmation Dialog
    if (showCancelConfirmation && existingSubscription != null) {
        AlertDialog(
            containerColor = MaterialTheme.colorScheme.surface,
            onDismissRequest = { showCancelConfirmation = false },
            title = {
                Text(
                    text = "Cancel Subscription",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Are you sure you want to cancel ${existingSubscription.name}? This will stop future renewals.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.cancelSubscription(existingSubscription)
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

@Composable
private fun PriceSection(
    price: String,
    onPriceChange: (String) -> Unit,
    currencySymbol: String,
    priceError: String?,
    selectedBillingCycle: BillingCycle
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val costLabel = when (selectedBillingCycle) {
            BillingCycle.WEEKLY -> "Weekly Cost"
            BillingCycle.MONTHLY -> "Monthly Cost"
            BillingCycle.YEARLY -> "Yearly Cost"
            BillingCycle.CUSTOM -> "Custom Cycle Cost"
        }
        Text(
            text = costLabel,
            style = MaterialTheme.typography.bodyMedium,
            color = com.zerodev.subscriptionmanager.ui.theme.TextSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = currencySymbol,
                style = TextStyle(
                    color = com.zerodev.subscriptionmanager.ui.theme.TextSecondary,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(end = 4.dp)
            )
            BasicTextField(
                value = price,
                onValueChange = onPriceChange,
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                cursorBrush = SolidColor(com.zerodev.subscriptionmanager.ui.theme.Primary),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.Center) {
                        if (price.isEmpty()) {
                            Text(
                                text = "0.00",
                                style = TextStyle(
                                    color = Color.White.copy(alpha = 0.3f),
                                    fontSize = 44.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
        if (priceError != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = priceError,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun PopularServicesSection(
    onServiceSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "POPULAR SERVICES",
            style = MaterialTheme.typography.labelMedium,
            color = com.zerodev.subscriptionmanager.ui.theme.TextSecondary,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PopularServiceItem(
                name = "Netflix",
                iconRes = R.drawable.netflix,
                onClick = { onServiceSelect("Netflix") }
            )
            PopularServiceItem(
                name = "Spotify",
                iconRes = R.drawable.spotify,
                onClick = { onServiceSelect("Spotify") }
            )
            PopularServiceItem(
                name = "YouTube",
                iconRes = R.drawable.youtube,
                onClick = { onServiceSelect("YouTube") }
            )
            PopularServiceItem(
                name = "ChatGPT",
                iconRes = R.drawable.chatgpt,
                onClick = { onServiceSelect("ChatGPT") }
            )
        }
    }
}

@Composable
private fun BillingCycleSection(
    selectedBillingCycle: BillingCycle,
    onBillingCycleSelect: (BillingCycle) -> Unit,
    customDays: String,
    onCustomDaysChange: (String) -> Unit,
    customDaysError: String?,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "BILLING CYCLE",
                style = MaterialTheme.typography.labelMedium,
                color = com.zerodev.subscriptionmanager.ui.theme.TextSecondary,
                fontWeight = FontWeight.Bold
            )
            SegmentedControl(
                selectedItem = selectedBillingCycle,
                onItemSelect = onBillingCycleSelect
            )
        }

        if (selectedBillingCycle == BillingCycle.CUSTOM) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Every",
                        color = com.zerodev.subscriptionmanager.ui.theme.TextSecondary,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    OutlinedTextField(
                        value = customDays,
                        onValueChange = onCustomDaysChange,
                        placeholder = {
                            Text(
                                text = "1",
                                color = com.zerodev.subscriptionmanager.ui.theme.TextSecondary,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        },
                        textStyle = TextStyle(
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        isError = customDaysError != null,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = com.zerodev.subscriptionmanager.ui.theme.DarkBackground,
                            unfocusedContainerColor = com.zerodev.subscriptionmanager.ui.theme.DarkBackground,
                            focusedBorderColor = if (customDaysError != null) MaterialTheme.colorScheme.error else com.zerodev.subscriptionmanager.ui.theme.Divider,
                            unfocusedBorderColor = if (customDaysError != null) MaterialTheme.colorScheme.error else com.zerodev.subscriptionmanager.ui.theme.Divider,
                            errorContainerColor = com.zerodev.subscriptionmanager.ui.theme.DarkBackground,
                            errorBorderColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.width(80.dp)
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(com.zerodev.subscriptionmanager.ui.theme.DarkBackground)
                            .clickable { /* no-op container matches spec mockup layout */ }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "Days",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                if (customDaysError != null) {
                    Text(
                        text = customDaysError,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}