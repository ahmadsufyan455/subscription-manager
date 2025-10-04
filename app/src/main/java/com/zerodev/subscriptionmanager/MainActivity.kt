package com.zerodev.subscriptionmanager

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.zerodev.subscriptionmanager.presentation.navigation.MainScreen
import com.zerodev.subscriptionmanager.ui.theme.SubscriptionManagerTheme
import com.zerodev.subscriptionmanager.core.helper.NotificationHelper
import com.zerodev.subscriptionmanager.core.helper.NotificationScheduler
import com.zerodev.subscriptionmanager.core.helper.RenewalScheduler

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, schedule notifications if enabled
            val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
            val notificationsEnabled = prefs.getBoolean("notifications_enabled", false)
            if (notificationsEnabled) {
                NotificationScheduler.scheduleNotificationCheck(this)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        NotificationHelper.createNotificationChannel(this)

        RenewalScheduler.scheduleRenewalCheck(this)

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
                val notificationsEnabled = prefs.getBoolean("notifications_enabled", false)
                if (notificationsEnabled) {
                    NotificationScheduler.scheduleNotificationCheck(this)
                }
            }
        } else {
            // Below Android 13, no runtime permission needed
            val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
            val notificationsEnabled = prefs.getBoolean("notifications_enabled", false)
            if (notificationsEnabled) {
                NotificationScheduler.scheduleNotificationCheck(this)
            }
        }

        setContent {
            SubscriptionManagerTheme {
                SubscriptionManager()
            }
        }
    }
}

@Composable
fun SubscriptionManager() {
    // Set status bar to dark with light icons
    val view = LocalView.current
    LaunchedEffect(Unit) {
        val window = (view.context as? android.app.Activity)?.window
        window?.let {
            WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = false
        }
    }

    MainScreen()
}

@Preview(showBackground = true)
@Composable
fun SubscriptionManagerAppPreview() {
    SubscriptionManagerTheme {
        SubscriptionManagerApp()
    }
}