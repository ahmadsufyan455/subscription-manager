package com.zerodev.subscriptionmanager


import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.logger.AndroidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class SubscriptionManagerApp : Application() {
    override fun onCreate() {
        super.onCreate()

        val appModules = listOf(
            databaseModule,
            repositoryModule,
            viewModelModule
        )

        startKoin {
            AndroidLogger(Level.NONE)
            androidContext(this@SubscriptionManagerApp)
            modules(appModules)
        }
    }
}