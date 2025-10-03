package com.zerodev.subscriptionmanager.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import com.zerodev.subscriptionmanager.R

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: @Composable () -> Painter
) {
    object Home : BottomNavItem(
        route = "home",
        title = "Home",
        icon = { painterResource(id = R.drawable.home) }
    )

    object AddSubscription : BottomNavItem(
        route = "add_subscription",
        title = "Add",
        icon = { rememberVectorPainter(Icons.Default.Add) }
    )

    object Settings : BottomNavItem(
        route = "settings",
        title = "Settings",
        icon = { painterResource(id = R.drawable.settings) }
    )
}

val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.AddSubscription,
    BottomNavItem.Settings
)