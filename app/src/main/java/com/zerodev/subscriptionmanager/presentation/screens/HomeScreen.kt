package com.zerodev.subscriptionmanager.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.material3.TopAppBarDefaults
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
import com.zerodev.subscriptionmanager.ui.components.AppHeaderActions
import com.zerodev.subscriptionmanager.ui.components.AppHeaderNavIcon
import com.zerodev.subscriptionmanager.ui.components.AppHeaderTitle
import com.zerodev.subscriptionmanager.ui.components.SpendingChart
import com.zerodev.subscriptionmanager.ui.components.SubscriptionCard
import com.zerodev.subscriptionmanager.ui.components.UpcomingCard
import com.zerodev.subscriptionmanager.ui.theme.DarkBackground
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    contentPadding: PaddingValues,
    onSeeAllClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                ),
                title = {
                    AppHeaderTitle()
                },
                navigationIcon = {
                    AppHeaderNavIcon()
                },
                actions = {
                    AppHeaderActions(
                        onClick = onNotificationClick,
                        hasUnreadNotifications = uiState.hasUnreadNotifications
                    )
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            LoadingContent(paddingValues)
        } else {
            HomeContent(
                uiState = uiState,
                onDeleted = viewModel::deleteSubscription,
                onUndoDelete = viewModel::addSubscription,
                paddingValues = paddingValues,
                contentPadding = contentPadding,
                onSeeAllClick = onSeeAllClick,
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
    onUndoDelete: (Subscription) -> Unit,
    paddingValues: PaddingValues,
    contentPadding: PaddingValues,
    onSeeAllClick: () -> Unit,
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
                    top = paddingValues.calculateTopPadding() - 32.dp,
                    bottom = paddingValues.calculateBottomPadding(),
                    start = 16.dp,
                    end = 16.dp
                ),
            contentPadding = contentPadding,
        ) {
            item {
                SpendingChart(subscriptions = uiState.subscriptions)
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Upcoming Subscriptions
            val upcomingSubscriptions = uiState.subscriptions
                .filter { (it.getRemainingDays() ?: Int.MAX_VALUE) <= 7 }
                .sortedWith(compareBy({ it.getRemainingDays() ?: Int.MAX_VALUE }, { it.createdAt }))
                .take(1)

            val upcomingSubscription = upcomingSubscriptions.firstOrNull()
            if (upcomingSubscription != null) {
                item {
                    UpcomingCard(
                        subscription = upcomingSubscription,
                        onClick = {
                            onEditSubscription(upcomingSubscription.id)
                        }
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            if (uiState.subscriptions.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Active Subscriptions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "See All",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable(
                                onClick = onSeeAllClick
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // Subscriptions List
            if (uiState.subscriptions.isEmpty()) {
                item {
                    EmptyStateCard()
                }
            } else {
                val sortedSubscriptions = uiState.subscriptions.sortedWith(
                    compareBy<Subscription> { subscription ->
                        when (subscription.status) {
                            SubscriptionStatus.ACTIVE -> 0
                            SubscriptionStatus.CANCELLED -> 1
                            SubscriptionStatus.EXPIRED -> 2
                        }
                    }.thenByDescending { it.createdAt }
                ).take(5)

                itemsIndexed(
                    sortedSubscriptions,
                    key = { _, sub -> sub.id }) { index, subscription ->
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
                                        onUndoDelete(deleted)
                                    }
                                }
                            }
                        },
                        onClick = {
                            onEditSubscription(subscription.id)
                        }
                    )
                    if (index < sortedSubscriptions.lastIndex) {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
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