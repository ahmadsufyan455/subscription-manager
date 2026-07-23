package com.zerodev.subscriptionmanager.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zerodev.subscriptionmanager.data.local.entities.NotificationEntity
import com.zerodev.subscriptionmanager.data.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

data class NotificationUiState(
    val notifications: List<NotificationEntity> = emptyList(),
    val isLoading: Boolean = false,
    val unreadCount: Int = 0
)

class NotificationViewModel(
    private val repository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState(isLoading = true))
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            repository.getAllNotifications()
                .catch {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                .collect { list ->
                    _uiState.value = _uiState.value.copy(
                        notifications = list,
                        isLoading = false,
                        unreadCount = list.count { !it.isRead }
                    )
                }
        }
    }

    fun markAsRead(id: Int) {
        viewModelScope.launch {
            repository.markAsRead(id)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            repository.markAllAsRead()
        }
    }

    fun deleteNotification(id: Int) {
        viewModelScope.launch {
            repository.deleteNotification(id)
        }
    }

    fun deleteAllNotifications() {
        viewModelScope.launch {
            repository.deleteAllNotifications()
        }
    }

    companion object {
        fun getRelativeTime(timestamp: Long): String {
            val diff = System.currentTimeMillis() - timestamp
            if (diff < 0) return "JUST NOW"

            val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
            val hours = TimeUnit.MILLISECONDS.toHours(diff)
            val days = TimeUnit.MILLISECONDS.toDays(diff)

            return when {
                minutes < 1 -> "JUST NOW"
                minutes < 60 -> if (minutes == 1L) "1 MIN AGO" else "$minutes MINS AGO"
                hours < 24 -> if (hours == 1L) "1 HOUR AGO" else "$hours HOURS AGO"
                days < 30 -> if (days == 1L) "1 DAY AGO" else "$days DAYS AGO"
                else -> {
                    val months = days / 30
                    if (months <= 1L) "1 MONTH AGO" else "$months MONTHS AGO"
                }
            }
        }
    }
}
