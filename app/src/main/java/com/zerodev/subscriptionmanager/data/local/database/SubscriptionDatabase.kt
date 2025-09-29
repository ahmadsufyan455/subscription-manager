package com.zerodev.subscriptionmanager.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.zerodev.subscriptionmanager.data.local.converters.Converters
import com.zerodev.subscriptionmanager.data.local.dao.SubscriptionDao
import com.zerodev.subscriptionmanager.data.local.entities.Subscription

@Database(entities = [Subscription::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class SubscriptionDatabase : RoomDatabase() {
    abstract fun subscriptionDao(): SubscriptionDao
}