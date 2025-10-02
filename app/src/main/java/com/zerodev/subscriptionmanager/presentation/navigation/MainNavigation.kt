package com.zerodev.subscriptionmanager.presentation.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.zerodev.subscriptionmanager.presentation.screens.AddSubscriptionBottomSheet
import com.zerodev.subscriptionmanager.presentation.screens.HomeScreen
import com.zerodev.subscriptionmanager.presentation.screens.SettingsScreen
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class)
@Composable
fun MainScreen(
    navController: NavHostController = rememberNavController()
) {
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    var showAddSubscriptionSheet by remember { mutableStateOf(false) }

    // Show the modal sheet only when needed
    if (showAddSubscriptionSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSubscriptionSheet = false },
            sheetState = bottomSheetState
        ) {
            AddSubscriptionBottomSheet(
                onDismiss = { showAddSubscriptionSheet = false }
            )
        }
    }

    val hazeState = rememberHazeState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                onAddSubscriptionClick = { showAddSubscriptionSheet = true },
                modifier = Modifier.hazeEffect(hazeState, style = HazeMaterials.ultraThin())
            )
        }
    ) { contentPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier
                .hazeSource(state = hazeState)
        ) {
            composable(BottomNavItem.Home.route) { HomeScreen(contentPadding = contentPadding) }
            composable(BottomNavItem.Settings.route) { SettingsScreen() }
        }
    }
}

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    onAddSubscriptionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true

            // Animate scale for selected items
            val scale by animateFloatAsState(
                targetValue = if (selected && item != BottomNavItem.AddSubscription) 1.1f else 1f,
                animationSpec = tween(300),
                label = "scale"
            )

            // Animate icon color
            val iconColor by animateColorAsState(
                targetValue = when {
                    selected -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                animationSpec = tween(300),
                label = "iconColor"
            )

            NavigationBarItem(
                icon = {
                    if (item == BottomNavItem.AddSubscription) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(iconColor.copy(alpha = 0.1f))
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = item.title,
                                tint = iconColor
                            )
                        }
                    } else {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = iconColor,
                            modifier = Modifier.scale(scale)
                        )
                    }
                },
                label = {
                    Text(
                        text = item.title,
                        color = iconColor
                    )
                },
                selected = selected,
                onClick = {
                    if (item == BottomNavItem.AddSubscription) {
                        onAddSubscriptionClick()
                    } else {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

@Preview
@Composable
private fun BottomNavBarPrev() {
    BottomNavigationBar(
        navController = rememberNavController(),
        onAddSubscriptionClick = {}
    )
}