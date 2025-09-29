package com.zerodev.subscriptionmanager.data.repository

import com.zerodev.subscriptionmanager.data.local.dao.SubscriptionDao
import com.zerodev.subscriptionmanager.data.local.entities.Subscription
import kotlinx.coroutines.flow.Flow

interface SubscriptionRepository {
    fun getAllSubscriptions(): Flow<List<Subscription>>
    suspend fun getSubscriptionById(id: Int): Subscription?
    suspend fun insertSubscription(subscription: Subscription)
    suspend fun updateSubscription(subscription: Subscription)
    suspend fun deleteSubscription(subscription: Subscription)
}

class SubscriptionRepositoryImpl(private val subscriptionDao: SubscriptionDao) :
    SubscriptionRepository {
    override fun getAllSubscriptions(): Flow<List<Subscription>> {
        return subscriptionDao.getAllSubscriptions()
    }

    override suspend fun getSubscriptionById(id: Int): Subscription? {
        return subscriptionDao.getSubscriptionById(id)
    }

    override suspend fun insertSubscription(subscription: Subscription) {
        subscriptionDao.insertSubscription(subscription)
    }

    override suspend fun updateSubscription(subscription: Subscription) {
        subscriptionDao.updateSubscription(subscription)
    }

    override suspend fun deleteSubscription(subscription: Subscription) {
        subscriptionDao.deleteSubscription(subscription)
    }
}