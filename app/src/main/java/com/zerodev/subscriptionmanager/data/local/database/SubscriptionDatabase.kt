package com.zerodev.subscriptionmanager.data.local.database

import androidx.room.Database
import com.zerodev.subscriptionmanager.data.local.dao.SubscriptionDao
import com.zerodev.subscriptionmanager.data.local.entities.Subscription

@Database(entities = [Subscription::class], version = 1)
abstract class SubscriptionDatabase {
    abstract fun subscriptionDao(): SubscriptionDao
}