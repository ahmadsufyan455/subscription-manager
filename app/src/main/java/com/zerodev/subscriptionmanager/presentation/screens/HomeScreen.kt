package com.zerodev.subscriptionmanager.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zerodev.subscriptionmanager.data.local.entities.Subscription
import com.zerodev.subscriptionmanager.data.local.entities.SubscriptionStatus
import com.zerodev.subscriptionmanager.presentation.viewmodel.HomeUiState
import com.zerodev.subscriptionmanager.presentation.viewmodel.HomeViewModel
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    contentPadding: PaddingValues,
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle error messages
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Subscriptions",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshSubscriptions() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            LoadingContent(paddingValues)
        } else {
            HomeContent(
                uiState = uiState,
                paddingValues = paddingValues,
                onCancelSubscription = viewModel::cancelSubscription,
                onDeleteSubscription = viewModel::deleteSubscription,
                contentPadding = contentPadding
            )
        }
    }
}

@Composable
private fun LoadingContent(paddingValues: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading subscriptions...",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    paddingValues: PaddingValues,
    onCancelSubscription: (Subscription) -> Unit,
    onDeleteSubscription: (Subscription) -> Unit,
    contentPadding: PaddingValues
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Summary Cards
        item {
            SummarySection(
                totalMonthlySpending = uiState.totalMonthlySpending,
                activeSubscriptionsCount = uiState.activeSubscriptionsCount
            )
        }

        // Subscriptions List
        if (uiState.subscriptions.isEmpty()) {
            item {
                EmptyStateCard()
            }
        } else {
            items(uiState.subscriptions) { subscription ->
                SubscriptionCard(
                    subscription = subscription,
                    onCancel = onCancelSubscription,
                    onDelete = onDeleteSubscription
                )
            }
        }
    }
}

@Composable
private fun SummarySection(
    totalMonthlySpending: Double,
    activeSubscriptionsCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard(
            title = "Monthly Spending",
            value = "$${String.format("%.2f", totalMonthlySpending)}",
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            title = "Active Subscriptions",
            value = activeSubscriptionsCount.toString(),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun SubscriptionCard(
    subscription: Subscription,
    onCancel: (Subscription) -> Unit,
    onDelete: (Subscription) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (subscription.status) {
                SubscriptionStatus.ACTIVE -> MaterialTheme.colorScheme.surface
                SubscriptionStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer
                SubscriptionStatus.EXPIRED -> MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subscription.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$${
                            String.format(
                                "%.2f",
                                subscription.price
                            )
                        } ${subscription.billingCycle.displayName.lowercase()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Status Badge
                StatusBadge(status = subscription.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Subscription Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Started: ${formatDate(subscription.startDate)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (subscription.status == SubscriptionStatus.ACTIVE) {
                        subscription.getRemainingDays()?.let { days ->
                            Text(
                                text = "Next billing: $days days",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    if (subscription.cancelledAt != null) {
                        Text(
                            text = "Cancelled: ${formatDate(subscription.cancelledAt)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                // Action Buttons
                if (subscription.status == SubscriptionStatus.ACTIVE) {
                    Row {
                        IconButton(onClick = { onCancel(subscription) }) {
                            Icon(
                                imageVector = Icons.Default.Cancel,
                                contentDescription = "Cancel Subscription",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                        IconButton(onClick = { onDelete(subscription) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Subscription",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                } else {
                    IconButton(onClick = { onDelete(subscription) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Subscription",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: SubscriptionStatus) {
    val (backgroundColor, textColor, text) = when (status) {
        SubscriptionStatus.ACTIVE -> Triple(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.onPrimary,
            "Active"
        )

        SubscriptionStatus.CANCELLED -> Triple(
            MaterialTheme.colorScheme.error,
            MaterialTheme.colorScheme.onError,
            "Cancelled"
        )

        SubscriptionStatus.EXPIRED -> Triple(
            MaterialTheme.colorScheme.outline,
            MaterialTheme.colorScheme.onSurface,
            "Expired"
        )
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun EmptyStateCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📱",
                    style = MaterialTheme.typography.headlineLarge
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Subscriptions Yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tap the + button to add your first subscription",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return formatter.format(Date(timestamp))
}