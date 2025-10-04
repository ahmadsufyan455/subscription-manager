package com.zerodev.subscriptionmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import com.zerodev.subscriptionmanager.presentation.navigation.MainScreen
import com.zerodev.subscriptionmanager.ui.theme.SubscriptionManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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