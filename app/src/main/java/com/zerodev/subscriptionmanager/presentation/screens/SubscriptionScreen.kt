package com.zerodev.subscriptionmanager.presentation.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import com.zerodev.subscriptionmanager.data.local.entities.Subscription
import com.zerodev.subscriptionmanager.data.local.entities.SubscriptionStatus
import com.zerodev.subscriptionmanager.presentation.viewmodel.HomeViewModel
import com.zerodev.subscriptionmanager.ui.components.NoSubscriptionsEmptyState
import com.zerodev.subscriptionmanager.ui.components.SearchNoResultsEmptyState
import com.zerodev.subscriptionmanager.ui.components.SubscriptionScrollableList
import com.zerodev.subscriptionmanager.ui.components.SubscriptionSearchHeader
import com.zerodev.subscriptionmanager.ui.components.SubscriptionStandardHeader
import com.zerodev.subscriptionmanager.ui.theme.DarkBackground
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

import androidx.compose.foundation.layout.Box
import androidx.compose.ui.graphics.Color
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class)
@Composable
fun SubscriptionScreen(
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel()
) {
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var deletedSubscription by remember { mutableStateOf<Subscription?>(null) }

    var showEditSheet by remember { mutableStateOf(false) }
    var editSubscriptionId by remember { mutableStateOf<Int?>(null) }
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    val hazeState = rememberHazeState()

    val filteredSubscriptions = remember(uiState.subscriptions, searchQuery) {
        val sorted = uiState.subscriptions.sortedWith(
            compareBy<Subscription> { subscription ->
                when (subscription.status) {
                    SubscriptionStatus.ACTIVE -> 0
                    SubscriptionStatus.CANCELLED -> 1
                    SubscriptionStatus.EXPIRED -> 2
                }
            }.thenByDescending { it.createdAt }
        )
        if (searchQuery.isBlank()) {
            sorted
        } else {
            sorted.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            focusRequester.requestFocus()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = DarkBackground,
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = hazeState)
                    .padding(paddingValues)
            ) {
                // Header Section
                Crossfade(
                    targetState = isSearchActive,
                    animationSpec = tween(250),
                    label = "SearchHeaderCrossfade"
                ) { active ->
                    if (active) {
                        SubscriptionSearchHeader(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            onCancelSearch = {
                                isSearchActive = false
                                searchQuery = ""
                            },
                            focusRequester = focusRequester
                        )
                    } else {
                        SubscriptionStandardHeader(
                            onBack = onBack,
                            onSearchClick = { isSearchActive = true }
                        )
                    }
                }

                // Subscriptions Content List or Empty State
                if (uiState.subscriptions.isEmpty()) {
                    NoSubscriptionsEmptyState(modifier = Modifier.weight(1f))
                } else if (filteredSubscriptions.isEmpty()) {
                    SearchNoResultsEmptyState(
                        searchQuery = searchQuery,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    SubscriptionScrollableList(
                        subscriptions = filteredSubscriptions,
                        contentPadding = contentPadding,
                        onDelete = { sub ->
                            deletedSubscription = sub
                            viewModel.deleteSubscription(sub)
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
                        onCardClick = { subscriptionId ->
                            editSubscriptionId = subscriptionId
                            showEditSheet = true
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (showEditSheet) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeEffect(hazeState, style = HazeMaterials.ultraThin())
            )
            ModalBottomSheet(
                onDismissRequest = {
                    showEditSheet = false
                    editSubscriptionId = null
                },
                sheetState = bottomSheetState,
                containerColor = com.zerodev.subscriptionmanager.ui.theme.BottomSheetBackground,
                scrimColor = Color.Transparent,
                dragHandle = { androidx.compose.material3.BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.25f)) }
            ) {
                AddSubscriptionBottomSheet(
                    onDismiss = {
                        scope.launch {
                            bottomSheetState.hide()
                        }.invokeOnCompletion {
                            if (!bottomSheetState.isVisible) {
                                showEditSheet = false
                                editSubscriptionId = null
                            }
                        }
                    },
                    isEditMode = editSubscriptionId != null,
                    subscriptionId = editSubscriptionId,
                )
            }
        }
    }
}