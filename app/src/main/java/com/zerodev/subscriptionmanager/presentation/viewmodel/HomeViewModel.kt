package com.zerodev.subscriptionmanager.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zerodev.subscriptionmanager.data.local.entities.BillingCycle
import com.zerodev.subscriptionmanager.data.local.entities.Subscription
import com.zerodev.subscriptionmanager.data.local.entities.SubscriptionStatus
import com.zerodev.subscriptionmanager.data.repository.SubscriptionRepository
import com.zerodev.subscriptionmanager.utils.NotificationTracker
import com.zerodev.subscriptionmanager.utils.RenewalHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class HomeUiState(
    val subscriptions: List<Subscription> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val totalMonthlySpending: Double = 0.0,
    val activeSubscriptionsCount: Int = 0
)

class HomeViewModel(
    private val application: Application,
    private val repository: SubscriptionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadSubscriptions()
    }

    private fun loadSubscriptions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // Process renewals first (check if any subscriptions need renewal)
            try {
                RenewalHelper.processRenewals(application, repository)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            repository.getAllSubscriptions()
                .catch { throwable ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = throwable.message ?: "Unknown error occurred"
                    )
                }
                .collect { subscriptions ->
                    _uiState.value = _uiState.value.copy(
                        subscriptions = subscriptions,
                        isLoading = false,
                        error = null,
                        totalMonthlySpending = calculateTotalMonthlySpending(subscriptions),
                        activeSubscriptionsCount = subscriptions.count { it.status == SubscriptionStatus.ACTIVE }
                    )
                }
        }
    }

    private fun calculateTotalMonthlySpending(subscriptions: List<Subscription>): Double {
        return subscriptions
            .filter { it.status == SubscriptionStatus.ACTIVE }
            .sumOf { subscription ->
                when (subscription.billingCycle) {
                    BillingCycle.MONTHLY -> subscription.price
                    BillingCycle.QUARTERLY -> subscription.price / 3
                    BillingCycle.YEARLY -> subscription.price / 12
                }
            }
    }

    fun addSubscription(subscription: Subscription) {
        viewModelScope.launch {
            try {
                repository.insertSubscription(subscription)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to add subscription: ${e.message}"
                )
            }
        }
    }

    fun updateSubscription(subscription: Subscription) {
        viewModelScope.launch {
            try {
                repository.updateSubscription(subscription)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to update subscription: ${e.message}"
                )
            }
        }
    }

    fun cancelSubscription(subscription: Subscription) {
        viewModelScope.launch {
            try {
                val cancelledSubscription = subscription.copy(
                    status = SubscriptionStatus.CANCELLED,
                    cancelledAt = System.currentTimeMillis()
                )
                repository.updateSubscription(cancelledSubscription)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to cancel subscription: ${e.message}"
                )
            }
        }
    }

    fun deleteSubscription(subscription: Subscription) {
        viewModelScope.launch {
            try {
                repository.deleteSubscription(subscription)
                // Clear notification tracking for deleted subscription
                NotificationTracker.clearTrackingForSubscription(application, subscription.id)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to delete subscription: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}