package com.zerodev.subscriptionmanager.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zerodev.subscriptionmanager.R
import com.zerodev.subscriptionmanager.data.local.entities.NotificationEntity
import com.zerodev.subscriptionmanager.data.local.entities.NotificationType
import com.zerodev.subscriptionmanager.presentation.viewmodel.NotificationViewModel
import com.zerodev.subscriptionmanager.ui.theme.DarkBackground
import com.zerodev.subscriptionmanager.ui.theme.TextPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox
import org.koin.androidx.compose.koinViewModel
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onBack: () -> Unit,
    viewModel: NotificationViewModel = koinViewModel()
) {
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    var isClearingAll by remember { mutableStateOf(false) }

    // Mark all as read when screen is opened
    LaunchedEffect(Unit) {
        viewModel.markAllAsRead()
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                ),
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF27272A).copy(alpha = 0.6f))
                            .clickable { onBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = "Notifications",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                },
                actions = {
                    if (uiState.notifications.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                if (!isClearingAll) {
                                    scope.launch {
                                        isClearingAll = true
                                        val totalDelay = 300L + (uiState.notifications.size * 40L)
                                        delay(totalDelay.milliseconds)
                                        viewModel.deleteAllNotifications()
                                        isClearingAll = false
                                    }
                                }
                            },
                            enabled = !isClearingAll,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = "Clear All",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (uiState.notifications.isEmpty()) {
            EmptyNotificationsContent(paddingValues = paddingValues)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(
                    items = uiState.notifications,
                    key = { _, item -> item.id }
                ) { index, notification ->
                    AnimatedVisibility(
                        visible = !isClearingAll,
                        enter = fadeIn() + expandVertically(),
                        exit = slideOutHorizontally(
                            targetOffsetX = { it },
                            animationSpec = tween(
                                durationMillis = 300,
                                delayMillis = index * 40
                            )
                        ) + shrinkVertically(
                            animationSpec = tween(
                                durationMillis = 300,
                                delayMillis = index * 40
                            )
                        ) + fadeOut(
                            animationSpec = tween(
                                durationMillis = 300,
                                delayMillis = index * 40
                            )
                        )
                    ) {
                        val deleteAction = SwipeAction(
                            icon = { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White) },
                            background = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                            isUndo = true,
                            onSwipe = { viewModel.deleteNotification(notification.id) },
                        )

                        SwipeableActionsBox(
                            modifier = Modifier.clip(RoundedCornerShape(20.dp)),
                            endActions = listOf(deleteAction),
                            swipeThreshold = 100.dp,
                            backgroundUntilSwipeThreshold = MaterialTheme.colorScheme.error.copy(alpha = 0.3f),
                            content = {
                                NotificationCard(
                                    notification = notification,
                                    onClick = { viewModel.markAsRead(notification.id) }
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    notification: NotificationEntity,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF141416)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = Color(0xFF27272A).copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon container based on notification type
            NotificationTypeIcon(type = notification.type)

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFA1A1AA),
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = NotificationViewModel.getRelativeTime(notification.timestamp),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF71717A),
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
private fun NotificationTypeIcon(type: NotificationType) {
    val (backgroundColor, iconColor) = when (type) {
        NotificationType.PAYMENT_SUCCESSFUL -> Pair(
            Color(0xFF052E16), // Dark green background
            Color(0xFF10B981)  // Vibrant green icon
        )
        NotificationType.UPCOMING_BILL -> Pair(
            Color(0xFF381A00), // Dark amber background
            Color(0xFFF59E0B)  // Vibrant amber/orange icon
        )
        NotificationType.NEW_FEATURE -> Pair(
            Color(0xFF1E1B4B), // Dark indigo background
            Color(0xFF6366F1)  // Vibrant indigo/purple icon
        )
    }

    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        when (type) {
            NotificationType.PAYMENT_SUCCESSFUL -> {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            NotificationType.UPCOMING_BILL -> {
                Icon(
                    painter = painterResource(R.drawable.ic_calendar),
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            NotificationType.NEW_FEATURE -> {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyNotificationsContent(paddingValues: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF27272A).copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_notif),
                    contentDescription = null,
                    tint = Color(0xFFA1A1AA),
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Notifications Yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "You're all caught up! Important payment reminders and updates will appear here.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF71717A),
                textAlign = TextAlign.Center
            )
        }
    }
}
