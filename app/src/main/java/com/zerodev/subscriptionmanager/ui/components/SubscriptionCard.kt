package com.zerodev.subscriptionmanager.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.zerodev.subscriptionmanager.R
import com.zerodev.subscriptionmanager.data.local.entities.BillingCycle
import com.zerodev.subscriptionmanager.data.local.entities.Subscription
import com.zerodev.subscriptionmanager.data.local.entities.SubscriptionStatus
import com.zerodev.subscriptionmanager.utils.formatDate
import java.util.Locale

@Composable
fun SubscriptionCard(subscription: Subscription) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(
                containerColor = when (subscription.status) {
                    SubscriptionStatus.ACTIVE -> MaterialTheme.colorScheme.surface
                    SubscriptionStatus.CANCELLED -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                    SubscriptionStatus.EXPIRED -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                }
            ),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
            ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            modifier = Modifier
                                .width(60.dp)
                                .height(60.dp)
                                .padding(8.dp),
                            painter = painterResource(R.drawable.claude),
                            contentDescription = "Subscription Icon",
                            tint = Color.Unspecified
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = subscription.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            when (subscription.status) {
                                SubscriptionStatus.ACTIVE -> {
                                    subscription.getRemainingDays()?.let { days ->
                                        Text(
                                            text = buildAnnotatedString {
                                                append("Due in ")
                                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                                    append("$days")
                                                }
                                                append(" days")
                                            },
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }

                                SubscriptionStatus.CANCELLED -> {
                                    subscription.cancelledAt?.let {
                                        Text(
                                            text = "Cancelled at ${formatDate(it)}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }

                                SubscriptionStatus.EXPIRED -> {}
                            }
                        }
                    }

                    Column {
                        val billingCycle = when (subscription.billingCycle) {
                            BillingCycle.MONTHLY -> "Month"
                            BillingCycle.QUARTERLY -> "Quartile"
                            BillingCycle.YEARLY -> "Year"
                        }
                        Text(
                            text = "$${String.format(Locale.US, "%.2f", subscription.price)}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "/$billingCycle")
                    }
                }
            }
        }
        StatusBadge(
            status = subscription.status,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .absoluteOffset(y = -(10.dp))
        )
    }
}

@Composable
private fun StatusBadge(status: SubscriptionStatus, modifier: Modifier = Modifier) {
    val (backgroundColor, textColor, text) = when (status) {
        SubscriptionStatus.ACTIVE -> Triple(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.onPrimary,
            "Active"
        )

        SubscriptionStatus.CANCELLED -> Triple(
            MaterialTheme.colorScheme.error,
            MaterialTheme.colorScheme.onError,
            "Cancelled"
        )

        SubscriptionStatus.EXPIRED -> Triple(
            MaterialTheme.colorScheme.outline,
            MaterialTheme.colorScheme.onSurface,
            "Expired"
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}