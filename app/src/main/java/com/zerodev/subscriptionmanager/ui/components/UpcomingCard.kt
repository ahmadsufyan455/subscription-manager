package com.zerodev.subscriptionmanager.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.zerodev.subscriptionmanager.data.local.entities.BillingCycle
import com.zerodev.subscriptionmanager.data.local.entities.Subscription
import com.zerodev.subscriptionmanager.data.local.entities.SubscriptionStatus
import com.zerodev.subscriptionmanager.utils.getSubscriptionIcon
import java.util.Locale

@Composable
fun UpcomingCard(
    subscription: Subscription,
    onClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(
                containerColor = when (subscription.status) {
                    SubscriptionStatus.ACTIVE -> MaterialTheme.colorScheme.surface
                    SubscriptionStatus.CANCELLED -> MaterialTheme.colorScheme.error.copy(
                        alpha = 0.2f
                    )

                    SubscriptionStatus.EXPIRED -> MaterialTheme.colorScheme.outline.copy(
                        alpha = 0.3f
                    )
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
                ) {
                    Icon(
                        modifier = Modifier
                            .width(60.dp)
                            .height(60.dp)
                            .padding(8.dp),
                        painter = painterResource(getSubscriptionIcon(subscription.name)),
                        contentDescription = "Subscription Icon",
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        val billingCycle = when (subscription.billingCycle) {
                            BillingCycle.MONTHLY -> "Month"
                            BillingCycle.QUARTERLY -> "Quarter"
                            BillingCycle.YEARLY -> "Year"
                        }
                        Text(
                            text = "$${
                                String.format(
                                    Locale.US,
                                    "%.0f",
                                    subscription.price
                                )
                            }",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "/$billingCycle")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = subscription.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
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
        }
    }
}