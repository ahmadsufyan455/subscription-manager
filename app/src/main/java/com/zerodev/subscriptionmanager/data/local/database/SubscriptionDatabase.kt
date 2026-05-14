package com.zerodev.subscriptionmanager.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.zerodev.subscriptionmanager.data.local.converters.Converters
import com.zerodev.subscriptionmanager.data.local.dao.SubscriptionDao
import com.zerodev.subscriptionmanager.data.local.entities.Subscription

@Database(entities = [Subscription::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class SubscriptionDatabase : RoomDatabase() {
    abstract fun subscriptionDao(): SubscriptionDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE subscriptions ADD COLUMN custom_cycle_days INTEGER DEFAULT NULL")
            }
        }
    }
}