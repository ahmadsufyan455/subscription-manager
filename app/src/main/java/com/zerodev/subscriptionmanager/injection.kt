package com.zerodev.subscriptionmanager

import androidx.room.Room
import com.zerodev.subscriptionmanager.data.local.database.SubscriptionDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    factory { get<SubscriptionDatabase>().subscriptionDao() }
    single {
        Room.databaseBuilder(
            androidContext(),
            SubscriptionDatabase::class.java,
            "subscription_database.db",
        ).build()
    }
}