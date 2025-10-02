package com.zerodev.subscriptionmanager.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

val CustomColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = TextPrimary,
    background = DarkBackground,
    surface = CardBackground,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    secondary = TextSecondary,
    onSecondary = TextPrimary,
    error = Error,
    onError = TextPrimary
)

@Composable
fun SubscriptionManagerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CustomColorScheme,
        typography = Typography,
        content = content
    )
}