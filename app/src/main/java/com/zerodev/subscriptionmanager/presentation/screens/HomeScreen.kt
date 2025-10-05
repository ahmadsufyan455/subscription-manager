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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zerodev.subscriptionmanager.data.local.entities.Subscription
import com.zerodev.subscriptionmanager.data.local.entities.SubscriptionStatus
import com.zerodev.subscriptionmanager.presentation.viewmodel.HomeUiState
import com.zerodev.subscriptionmanager.presentation.viewmodel.HomeViewModel
import com.zerodev.subscriptionmanager.ui.components.SubscriptionCard
import com.zerodev.subscriptionmanager.ui.components.UpcomingCard
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    contentPadding: PaddingValues,
    viewModel: HomeViewModel = koinViewModel()
) {
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showAddSubscriptionSheet by remember { mutableStateOf(false) }
    var editSubscriptionId by remember { mutableStateOf<Int?>(null) }

    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    if (showAddSubscriptionSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showAddSubscriptionSheet = false
                editSubscriptionId = null
            },
            sheetState = bottomSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            AddSubscriptionBottomSheet(
                onDismiss = {
                    scope.launch {
                        bottomSheetState.hide()
                    }.invokeOnCompletion {
                        if (!bottomSheetState.isVisible) {
                            showAddSubscriptionSheet = false
                            editSubscriptionId = null
                        }
                    }
                },
                isEditMode = editSubscriptionId != null,
                subscriptionId = editSubscriptionId,
            )
        }
    }

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
                        text = "Subscription Manager",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            LoadingContent(paddingValues)
        } else {
            HomeContent(
                uiState = uiState,
                onDeleted = viewModel::deleteSubscription,
                paddingValues = paddingValues,
                contentPadding = contentPadding,
                onEditSubscription = { subscriptionId ->
                    editSubscriptionId = subscriptionId
                    showAddSubscriptionSheet = true
                }
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
    onDeleted: (Subscription) -> Unit,
    paddingValues: PaddingValues,
    contentPadding: PaddingValues,
    viewModel: HomeViewModel = koinViewModel(),
    onEditSubscription: (Int) -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var deletedSubscription by remember { mutableStateOf<Subscription?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = paddingValues.calculateTopPadding() - 24.dp,
                    bottom = paddingValues.calculateBottomPadding(),
                    start = 16.dp,
                    end = 16.dp
                ),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Summary Cards
            item {
                SummarySection(
                    totalMonthlySpending = uiState.totalMonthlySpending,
                    activeSubscriptionsCount = uiState.activeSubscriptionsCount
                )
            }

            // Upcoming Subscriptions Grid
            val upcomingSubscriptions = uiState.subscriptions
                .filter { (it.getRemainingDays() ?: Int.MAX_VALUE) <= 7 }
                .sortedWith(compareBy({ it.getRemainingDays() ?: Int.MAX_VALUE }, { it.createdAt }))
                .take(2)

            if (upcomingSubscriptions.isNotEmpty()) {
                item {
                    Text(
                        text = "Upcoming Payments",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    val upcomingSpace = if (upcomingSubscriptions.size == 1) 150 else 75
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.height((upcomingSubscriptions.size * upcomingSpace).dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(upcomingSubscriptions) { subscription ->
                            UpcomingCard(
                                subscription = subscription,
                                onClick = {
                                    onEditSubscription(subscription.id)
                                }
                            )
                        }
                    }
                }
            }

            if (uiState.subscriptions.isNotEmpty()) {
                item {
                    Text(
                        text = "My Subscriptions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Subscriptions List
            if (uiState.subscriptions.isEmpty()) {
                item {
                    EmptyStateCard()
                }
            } else {
                val sortedSubscriptions = uiState.subscriptions.sortedWith(
                    compareBy(
                        { subscription ->
                            when (subscription.status) {
                                SubscriptionStatus.ACTIVE -> 0
                                SubscriptionStatus.CANCELLED -> 1
                                SubscriptionStatus.EXPIRED -> 2
                            }
                        },
                        { it.createdAt }
                    )
                )

                items(sortedSubscriptions) { subscription ->
                    SubscriptionCard(
                        subscription = subscription,
                        onDelete = { sub ->
                            deletedSubscription = sub
                            onDeleted(sub)
                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "${sub.name} deleted",
                                    actionLabel = "Undo",
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    deletedSubscription?.let { deleted ->
                                        viewModel.addSubscription(deleted)
                                    }
                                }
                            }
                        },
                        onClick = {
                            onEditSubscription(subscription.id)
                        }
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = contentPadding.calculateBottomPadding())
        )
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
            value = "$${String.format(Locale.US, "%.0f", totalMonthlySpending)}",
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
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun EmptyStateCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
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
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tap the + button to add your first subscription",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}