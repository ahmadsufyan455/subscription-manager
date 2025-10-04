package com.zerodev.subscriptionmanager


import android.app.Application
import com.zerodev.subscriptionmanager.core.di.databaseModule
import com.zerodev.subscriptionmanager.core.di.repositoryModule
import com.zerodev.subscriptionmanager.core.di.viewModelModule
import com.zerodev.subscriptionmanager.core.helper.RenewalScheduler
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

        RenewalScheduler.scheduleRenewalCheck(this)
    }
}