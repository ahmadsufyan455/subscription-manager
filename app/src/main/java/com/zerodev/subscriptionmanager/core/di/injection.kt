package com.zerodev.subscriptionmanager.core.di

import androidx.room.Room
import com.zerodev.subscriptionmanager.data.local.database.SubscriptionDatabase
import com.zerodev.subscriptionmanager.data.repository.NotificationRepository
import com.zerodev.subscriptionmanager.data.repository.NotificationRepositoryImpl
import com.zerodev.subscriptionmanager.data.repository.SubscriptionRepository
import com.zerodev.subscriptionmanager.data.repository.SubscriptionRepositoryImpl
import com.zerodev.subscriptionmanager.presentation.viewmodel.HomeViewModel
import com.zerodev.subscriptionmanager.presentation.viewmodel.NotificationViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val databaseModule = module {
    factory { get<SubscriptionDatabase>().subscriptionDao() }
    factory { get<SubscriptionDatabase>().notificationDao() }
    single {
        Room.databaseBuilder(
            androidContext(),
            SubscriptionDatabase::class.java,
            "subscription_manager.db",
        ).addMigrations(
            SubscriptionDatabase.MIGRATION_1_2,
            SubscriptionDatabase.MIGRATION_2_3
        ).build()
    }
}

val repositoryModule = module {
    single<SubscriptionRepository> { SubscriptionRepositoryImpl(get()) }
    single<NotificationRepository> { NotificationRepositoryImpl(get()) }
}

val viewModelModule = module {
    viewModel { HomeViewModel(get(), get(), get()) }
    viewModel { NotificationViewModel(get()) }
}