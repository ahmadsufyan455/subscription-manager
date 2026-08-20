package com.zerodev.subscriptionmanager

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.zerodev.subscriptionmanager.core.helper.NotificationHelper
import com.zerodev.subscriptionmanager.core.helper.NotificationScheduler
import com.zerodev.subscriptionmanager.core.helper.RenewalScheduler
import com.zerodev.subscriptionmanager.presentation.navigation.MainScreen
import com.zerodev.subscriptionmanager.presentation.widget.UpcomingWidget
import com.zerodev.subscriptionmanager.presentation.widget.UpcomingWidgetHelper
import com.zerodev.subscriptionmanager.ui.theme.SubscriptionManagerTheme
import kotlinx.coroutines.flow.MutableStateFlow

sealed interface WidgetNavAction {
    data class OpenSubscription(val subscriptionId: Int) : WidgetNavAction
    data object AddSubscription : WidgetNavAction
}

class MainActivity : ComponentActivity() {

    private val _widgetNavAction = MutableStateFlow<WidgetNavAction?>(null)

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

        handleWidgetIntent(intent)

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

        // Update widget on app startup
        UpcomingWidgetHelper.updateWidget(this)

        setContent {
            val navAction by _widgetNavAction.collectAsState()
            SubscriptionManagerTheme {
                SubscriptionManager(
                    widgetNavAction = navAction,
                    onClearWidgetNavAction = { _widgetNavAction.value = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleWidgetIntent(intent)
    }

    override fun onStop() {
        super.onStop()
        // Ensure widget is synchronized with any changes made during the session
        UpcomingWidgetHelper.updateWidget(this)
    }

    private fun handleWidgetIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.getStringExtra("widget_action") ?: intent.action
        when (action) {
            UpcomingWidget.ACTION_OPEN_SUBSCRIPTION -> {
                val id = intent.getIntExtra(UpcomingWidget.EXTRA_SUBSCRIPTION_ID, -1)
                if (id != -1) {
                    _widgetNavAction.value = WidgetNavAction.OpenSubscription(id)
                }
            }
            UpcomingWidget.ACTION_ADD_SUBSCRIPTION -> {
                _widgetNavAction.value = WidgetNavAction.AddSubscription
            }
        }
    }
}

@Composable
fun SubscriptionManager(
    widgetNavAction: WidgetNavAction? = null,
    onClearWidgetNavAction: () -> Unit = {}
) {
    // Set status bar to dark with light icons
    val view = LocalView.current
    LaunchedEffect(Unit) {
        val window = (view.context as? android.app.Activity)?.window
        window?.let {
            WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = false
        }
    }

    MainScreen(
        widgetNavAction = widgetNavAction,
        onClearWidgetNavAction = onClearWidgetNavAction
    )
}

@Preview(showBackground = true)
@Composable
fun SubscriptionManagerAppPreview() {
    SubscriptionManagerTheme {
        SubscriptionManager()
    }
}