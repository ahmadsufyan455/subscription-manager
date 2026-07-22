package com.zerodev.subscriptionmanager.core.utils

import com.zerodev.subscriptionmanager.R

fun getSubscriptionIcon(subscriptionName: String): Int {
    return when {
        // AI & Dev Tools
        subscriptionName.containsAny("chatgpt", "gpt", "openai") -> R.drawable.chatgpt
        subscriptionName.containsAny("claude", "anthropic") -> R.drawable.claude
        subscriptionName.containsAny("perplexity") -> R.drawable.perplexity
        subscriptionName.containsAny("jetbrains", "intellij", "pycharm", "webstorm", "clion", "rider", "phpstorm") -> R.drawable.jetbrains
        
        // Entertainment & Streaming
        subscriptionName.containsAny("netflix") -> R.drawable.netflix
        subscriptionName.containsAny("youtube", "yt") -> R.drawable.youtube
        subscriptionName.containsAny("disney") -> R.drawable.disney
        subscriptionName.containsAny("hbo", "max") -> R.drawable.hbo
        subscriptionName.containsAny("hulu") -> R.drawable.hulu
        subscriptionName.containsAny("twitch") -> R.drawable.twitch
        
        // Music & Audio
        subscriptionName.containsAny("spotify") -> R.drawable.spotify
        subscriptionName.containsAny("music") -> R.drawable.music
        subscriptionName.containsAny("soundcloud") -> R.drawable.soundcloud
        subscriptionName.containsAny("epidemic") -> R.drawable.epidemic
        
        // Design & Creative
        subscriptionName.containsAny("photoshop", "ps") -> R.drawable.photoshop
        subscriptionName.containsAny("lightroom", "lr") -> R.drawable.lightroom
        subscriptionName.containsAny("adobe", "creative cloud") -> R.drawable.adobe
        subscriptionName.containsAny("canva") -> R.drawable.canva
        subscriptionName.containsAny("figma") -> R.drawable.figma
        
        // Productivity & Work
        subscriptionName.containsAny("notion") -> R.drawable.notion
        subscriptionName.containsAny("slack") -> R.drawable.slack
        subscriptionName.containsAny("zoom") -> R.drawable.zoom
        subscriptionName.containsAny("microsoft", "office", "m365", "outlook", "excel", "powerpoint", "word") -> R.drawable.microsoft
        subscriptionName.containsAny("google", "gsuite", "workspace", "drive", "gmail", "g1", "google one") -> R.drawable.google
        
        // Gaming
        subscriptionName.containsAny("xbox", "game pass", "gamepass") -> R.drawable.xbox
        subscriptionName.containsAny("playstation", "ps plus", "psn", "psplus") -> R.drawable.playstation
        subscriptionName.containsAny("discord", "nitro") -> R.drawable.discord
        subscriptionName.containsAny("steam") -> R.drawable.steam
        
        // E-commerce & Lifestyle
        subscriptionName.containsAny("amazon", "prime") -> R.drawable.amazon
        subscriptionName.containsAny("shopee") -> R.drawable.shopee
        subscriptionName.containsAny("apple", "icloud", "arcade", "apple one") -> R.drawable.apple
        subscriptionName.containsAny("strava") -> R.drawable.strava
        subscriptionName.containsAny("headspace", "calm") -> R.drawable.headspace
        subscriptionName.containsAny("duolingo") -> R.drawable.duolingo
        subscriptionName.containsAny("medium") -> R.drawable.medium
        
        else -> R.drawable.subtrack // default icon
    }
}

private fun String.containsAny(vararg keywords: String): Boolean {
    return keywords.any { this.contains(it, ignoreCase = true) }
}
