package com.zerodev.subscriptionmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
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
    MainScreen()
}

@Preview(showBackground = true)
@Composable
fun SubscriptionManagerAppPreview() {
    SubscriptionManagerTheme {
        SubscriptionManagerApp()
    }
}