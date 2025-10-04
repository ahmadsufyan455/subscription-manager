package com.zerodev.subscriptionmanager.core.utils

import com.zerodev.subscriptionmanager.R

fun getSubscriptionIcon(subscriptionName: String): Int {
    return when {
        subscriptionName.contains("chatgpt", ignoreCase = true) -> R.drawable.chatgpt
        subscriptionName.contains("claude", ignoreCase = true) -> R.drawable.claude
        subscriptionName.contains("jetbrains", ignoreCase = true) -> R.drawable.jetbrains
        subscriptionName.contains("music", ignoreCase = true) -> R.drawable.music
        subscriptionName.contains("netflix", ignoreCase = true) -> R.drawable.netflix
        subscriptionName.contains("perplexity", ignoreCase = true) -> R.drawable.perplexity
        subscriptionName.contains("shopee", ignoreCase = true) -> R.drawable.shopee
        subscriptionName.contains("spotify", ignoreCase = true) -> R.drawable.spotify
        subscriptionName.contains("youtube", ignoreCase = true) -> R.drawable.youtube
        else -> R.drawable.claude // default icon
    }
}
