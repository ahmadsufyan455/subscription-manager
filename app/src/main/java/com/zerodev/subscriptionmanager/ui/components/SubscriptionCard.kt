package com.zerodev.subscriptionmanager.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zerodev.subscriptionmanager.R
import com.zerodev.subscriptionmanager.core.utils.CurrencyFormatter
import com.zerodev.subscriptionmanager.core.utils.getSubscriptionIcon
import com.zerodev.subscriptionmanager.data.local.entities.BillingCycle
import com.zerodev.subscriptionmanager.data.local.entities.RenewalUrgency
import com.zerodev.subscriptionmanager.data.local.entities.Subscription
import com.zerodev.subscriptionmanager.data.local.entities.SubscriptionStatus
import com.zerodev.subscriptionmanager.ui.theme.CardBackground
import com.zerodev.subscriptionmanager.ui.theme.Primary
import com.zerodev.subscriptionmanager.ui.theme.TextPrimary
import com.zerodev.subscriptionmanager.ui.theme.TextSecondary
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox
import kotlin.math.absoluteValue

@Composable
fun SubscriptionCard(
    subscription: Subscription,
    onDelete: (Subscription) -> Unit,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val currency = remember(subscription) { CurrencyFormatter.getSelectedCurrency(context) }

    val deleteSubscription = SwipeAction(
        icon = { Icon(Icons.Default.Delete, contentDescription = "Delete") },
        background = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
        isUndo = true,
        onSwipe = { onDelete(subscription) },
    )

    SwipeableActionsBox(
        endActions = listOf(deleteSubscription),
        swipeThreshold = 100.dp,
        backgroundUntilSwipeThreshold = MaterialTheme.colorScheme.primary.copy(
            alpha = 0.3f
        ),
        content = {
            Box(modifier = Modifier.fillMaxWidth()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = subscription.isActive()) { onClick() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = CardBackground
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (subscription.isActive()) Primary.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.1f)
                    ),
                ) {
                    val contentAlpha = if (subscription.isActive()) 1f else 0.5f
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .alpha(contentAlpha),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isBrandLogoAvailable = remember(subscription.name) {
                            getSubscriptionIcon(subscription.name) != R.drawable.subtrack
                        }

                        if (isBrandLogoAvailable) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .background(
                                        color = Color(0xFF1E1E1E),
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(getSubscriptionIcon(subscription.name)),
                                    contentDescription = "Subscription Logo",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        } else {
                            val colors = listOf(
                                Color(0xFFE50914), // Red
                                Color(0xFF1DB954), // Green
                                Color(0xFF1F85DE), // Blue
                                Color(0xFFFF9900), // Orange
                                Color(0xFF7C3AED), // Violet
                                Color(0xFFEC4899), // Pink
                                Color(0xFF00DF89), // Mint
                                Color(0xFFF59E0B)  // Yellow/Amber
                            )
                            val randomColor = remember(subscription.name) {
                                val index = (subscription.name.hashCode().absoluteValue) % colors.size
                                colors[index]
                            }
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .background(
                                        color = randomColor,
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                val firstLetter = subscription.name.firstOrNull()?.uppercase() ?: "S"
                                Text(
                                    text = firstLetter,
                                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = subscription.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )

                            if (!subscription.notes.isNullOrBlank()) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Notes,
                                    contentDescription = "Has note",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = CurrencyFormatter.format(subscription.price, currency),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                textDecoration = if (subscription.isActive()) TextDecoration.None else TextDecoration.LineThrough
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            if (subscription.isActive()) {
                                val remainingLabel = subscription.getRemainingDaysLabel()
                                val urgency = subscription.getRenewalUrgency()

                                if (remainingLabel != null && (urgency == RenewalUrgency.URGENT || urgency == RenewalUrgency.SOON)) {
                                    val (badgeBgColor, badgeTextColor) = when (urgency) {
                                        RenewalUrgency.URGENT -> Pair(
                                            Color(0xFFFF9500).copy(alpha = 0.18f),
                                            Color(0xFFFF9F0A)
                                        )
                                        RenewalUrgency.SOON -> Pair(
                                            Primary.copy(alpha = 0.15f),
                                            Primary
                                        )
                                        else -> Pair(Color.Transparent, TextSecondary)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(badgeBgColor)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = remainingLabel.uppercase(),
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = badgeTextColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else {
                                    val labelText = remainingLabel?.uppercase() ?: when (subscription.billingCycle) {
                                        BillingCycle.MONTHLY -> "MONTHLY"
                                        BillingCycle.WEEKLY -> "WEEKLY"
                                        BillingCycle.YEARLY -> "YEARLY"
                                        BillingCycle.CUSTOM -> "${subscription.customCycleDays ?: 0} DAYS"
                                    }
                                    Text(
                                        text = labelText,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                val (badgeBgColor, badgeTextColor, badgeText) = when (subscription.status) {
                                    SubscriptionStatus.CANCELLED -> Triple(
                                        Color(0xFFFF3B30).copy(alpha = 0.15f),
                                        Color(0xFFFF453A),
                                        "CANCELLED"
                                    )
                                    SubscriptionStatus.EXPIRED -> Triple(
                                        Color.Gray.copy(alpha = 0.15f),
                                        Color.LightGray,
                                        "EXPIRED"
                                    )
                                    else -> Triple(Color.Transparent, Color.Transparent, "")
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(badgeBgColor)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = badgeText,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = badgeTextColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Details",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    )
}