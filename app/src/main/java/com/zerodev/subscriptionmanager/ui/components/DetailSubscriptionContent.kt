package com.zerodev.subscriptionmanager.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zerodev.subscriptionmanager.R
import com.zerodev.subscriptionmanager.core.utils.Currency
import com.zerodev.subscriptionmanager.core.utils.CurrencyFormatter
import com.zerodev.subscriptionmanager.core.utils.getSubscriptionIcon
import com.zerodev.subscriptionmanager.data.local.entities.BillingCycle
import com.zerodev.subscriptionmanager.data.local.entities.Subscription
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun DetailSubscriptionContent(
    existingSubscription: Subscription,
    currency: Currency,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 1. Logo and Name
        val brandIcon = remember(existingSubscription.name) { getSubscriptionIcon(existingSubscription.name) }
        val firstLetter = existingSubscription.name.firstOrNull()?.uppercase() ?: "S"

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        if (brandIcon != R.drawable.subtrack) Color(0xFF1E1E1E)
                        else {
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
                            val index = abs(existingSubscription.name.hashCode()) % colors.size
                            colors[index]
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (brandIcon != R.drawable.subtrack) {
                    Icon(
                        painter = painterResource(id = brandIcon),
                        contentDescription = "Brand Logo",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(48.dp)
                    )
                } else {
                    Text(
                        text = firstLetter,
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Text(
                text = existingSubscription.name,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp),
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // 2. Info Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(com.zerodev.subscriptionmanager.ui.theme.CardBackground)
                .border(
                    width = 1.dp,
                    color = com.zerodev.subscriptionmanager.ui.theme.Divider.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(16.dp)
        ) {
            Column {
                // Row 1: Cost and Billing Cycle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Dollar Icon Container
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(com.zerodev.subscriptionmanager.ui.theme.Primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currency.symbol,
                            style = TextStyle(
                                color = com.zerodev.subscriptionmanager.ui.theme.Primary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Cost Details
                    Column(modifier = Modifier.weight(1f)) {
                        val costLabel = when (existingSubscription.billingCycle) {
                            BillingCycle.WEEKLY -> "Weekly Cost"
                            BillingCycle.MONTHLY -> "Monthly Cost"
                            BillingCycle.YEARLY -> "Yearly Cost"
                            BillingCycle.CUSTOM -> "Custom Cycle Cost"
                        }
                        Text(
                            text = costLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = com.zerodev.subscriptionmanager.ui.theme.TextSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = CurrencyFormatter.format(existingSubscription.price, currency),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Billing Cycle Details
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Billing Cycle",
                            style = MaterialTheme.typography.bodySmall,
                            color = com.zerodev.subscriptionmanager.ui.theme.TextSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val cycleText = when (existingSubscription.billingCycle) {
                            BillingCycle.CUSTOM -> "${existingSubscription.customCycleDays ?: 0} Days"
                            else -> existingSubscription.billingCycle.displayName
                        }
                        Text(
                            text = cycleText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(com.zerodev.subscriptionmanager.ui.theme.Divider.copy(alpha = 0.5f))
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Row 2: Next Payment and Auto-Renew
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Calendar Icon Container
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF9900).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_calendar),
                            contentDescription = "Calendar",
                            tint = Color(0xFFFF9900),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Next Payment Details
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Next Payment",
                            style = MaterialTheme.typography.bodySmall,
                            color = com.zerodev.subscriptionmanager.ui.theme.TextSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val nextBillingDate = existingSubscription.getNextBillingDate()
                        val formattedDate = if (nextBillingDate != null) {
                            SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(nextBillingDate))
                        } else {
                            "N/A"
                        }
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // AUTO-RENEW pill badge (only if active!)
                    if (existingSubscription.isActive()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "AUTO-RENEW",
                                style = TextStyle(
                                    color = com.zerodev.subscriptionmanager.ui.theme.TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }

        // 3. Cancel button
        if (existingSubscription.isActive()) {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                onClick = onCancelClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF3B30).copy(alpha = 0.1f),
                    contentColor = Color(0xFFFF3B30)
                ),
                border = BorderStroke(1.dp, Color(0xFFFF3B30).copy(alpha = 0.2f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = "Warning",
                        tint = Color(0xFFFF3B30),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cancel Subscription",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
