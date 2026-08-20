@file:Suppress("RestrictedApi")

package com.zerodev.subscriptionmanager.presentation.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.zerodev.subscriptionmanager.MainActivity
import com.zerodev.subscriptionmanager.R
import com.zerodev.subscriptionmanager.core.utils.Currency
import com.zerodev.subscriptionmanager.core.utils.CurrencyFormatter
import com.zerodev.subscriptionmanager.core.utils.getSubscriptionIcon
import com.zerodev.subscriptionmanager.data.local.entities.BillingCycle
import com.zerodev.subscriptionmanager.data.local.entities.RenewalUrgency
import com.zerodev.subscriptionmanager.data.local.entities.Subscription
import com.zerodev.subscriptionmanager.data.local.entities.SubscriptionStatus
import com.zerodev.subscriptionmanager.data.repository.SubscriptionRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

class UpcomingWidget : GlanceAppWidget(), KoinComponent {

    companion object {
        const val ACTION_OPEN_SUBSCRIPTION = "com.zerodev.subscriptionmanager.ACTION_OPEN_SUBSCRIPTION"
        const val ACTION_ADD_SUBSCRIPTION = "com.zerodev.subscriptionmanager.ACTION_ADD_SUBSCRIPTION"
        const val EXTRA_SUBSCRIPTION_ID = "extra_subscription_id"

        val KeyAction = ActionParameters.Key<String>("widget_action")
        val KeySubscriptionId = ActionParameters.Key<Int>(EXTRA_SUBSCRIPTION_ID)

        private val SMALL_BOX = DpSize(100.dp, 100.dp)
        private val MEDIUM_BOX = DpSize(220.dp, 100.dp)
        private val LARGE_BOX = DpSize(220.dp, 200.dp)

        // Palette matching app theme
        val WidgetCardBackground = Color(0xFF151421)
        val WidgetIconBackground = Color(0x1FFFFFFF)
        val WidgetPrimary = Color(0xFF605DFF)
        val WidgetTextPrimary = Color(0xFFFFFFFF)
        val WidgetTextSecondary = Color(0xFF9A9AB0)
        val WidgetUrgentRed = Color(0xFFFF3B30)
        val WidgetSoonOrange = Color(0xFFFF9900)
    }

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(SMALL_BOX, MEDIUM_BOX, LARGE_BOX)
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository: SubscriptionRepository by inject()
        val allSubscriptions = try {
            repository.getAllSubscriptionsSnapshot()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }

        val activeSubscriptions = allSubscriptions
            .filter { it.status == SubscriptionStatus.ACTIVE && it.getNextBillingDate() != null }
            .sortedBy { it.getNextBillingDate() }

        val currency = CurrencyFormatter.getSelectedCurrency(context)

        provideContent {
            val size = LocalSize.current
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .appWidgetBackground()
                    .background(WidgetCardBackground)
                    .cornerRadius(20.dp)
                    .padding(12.dp)
            ) {
                when {
                    size.width < 210.dp -> {
                        CompactUpcomingView(
                            subscription = activeSubscriptions.firstOrNull(),
                            currency = currency
                        )
                    }
                    size.height < 180.dp -> {
                        MediumUpcomingView(
                            subscriptions = activeSubscriptions,
                            currency = currency
                        )
                    }
                    else -> {
                        ExpandedUpcomingView(
                            subscriptions = activeSubscriptions,
                            currency = currency
                        )
                    }
                }
            }
        }
    }
}

/**
 * 2x2 Compact Widget View: Hero Card layout with balanced visual hierarchy
 */
@Composable
private fun CompactUpcomingView(
    subscription: Subscription?,
    currency: Currency
) {
    if (subscription == null) {
        EmptyUpcomingView(isCompact = true)
        return
    }

    val iconRes = getSubscriptionIcon(subscription.name)
    val nextBilling = subscription.getNextBillingDate()
    val remainingLabel = subscription.getRemainingDaysLabel() ?: "Upcoming"
    val urgency = subscription.getRenewalUrgency()

    val badgeColor = when (urgency) {
        RenewalUrgency.URGENT -> UpcomingWidget.WidgetUrgentRed
        RenewalUrgency.SOON -> UpcomingWidget.WidgetSoonOrange
        RenewalUrgency.NORMAL -> UpcomingWidget.WidgetPrimary
    }

    val dateFormatted = if (nextBilling != null) {
        val localDate = Instant.ofEpochMilli(nextBilling)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val monthName = localDate.month.getDisplayName(java.time.format.TextStyle.SHORT, Locale.US)
        "$monthName ${localDate.dayOfMonth}"
    } else {
        ""
    }

    val cycleLabel = when (subscription.billingCycle) {
        BillingCycle.CUSTOM -> "${subscription.customCycleDays ?: 0}d"
        else -> subscription.billingCycle.displayName
    }

    val subtitle = if (dateFormatted.isNotEmpty()) "$cycleLabel · $dateFormatted" else cycleLabel

    val openDetailAction = actionStartActivity<MainActivity>(
        actionParametersOf(
            UpcomingWidget.KeyAction to UpcomingWidget.ACTION_OPEN_SUBSCRIPTION,
            UpcomingWidget.KeySubscriptionId to subscription.id
        )
    )

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .clickable(openDetailAction),
        verticalAlignment = Alignment.Vertical.Top
    ) {
        // 1. Top Row: "UPCOMING" Tag & Urgency Pill
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Text(
                text = "UPCOMING",
                style = TextStyle(
                    color = ColorProvider(UpcomingWidget.WidgetTextSecondary),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = GlanceModifier.defaultWeight())

            Box(
                modifier = GlanceModifier
                    .background(badgeColor)
                    .cornerRadius(8.dp)
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            ) {
                Text(
                    text = remainingLabel,
                    style = TextStyle(
                        color = ColorProvider(UpcomingWidget.WidgetTextPrimary),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Spacer(modifier = GlanceModifier.defaultWeight())

        // 2. Middle Hero Row: Brand Icon Tile + Name & Cycle/Date
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Box(
                modifier = GlanceModifier
                    .size(40.dp)
                    .background(UpcomingWidget.WidgetIconBackground)
                    .cornerRadius(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(iconRes),
                    contentDescription = subscription.name,
                    modifier = GlanceModifier.size(26.dp)
                )
            }

            Spacer(modifier = GlanceModifier.width(10.dp))

            Column(
                modifier = GlanceModifier.defaultWeight()
            ) {
                Text(
                    text = subscription.name,
                    maxLines = 1,
                    style = TextStyle(
                        color = ColorProvider(UpcomingWidget.WidgetTextPrimary),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.height(2.dp))
                Text(
                    text = subtitle,
                    maxLines = 1,
                    style = TextStyle(
                        color = ColorProvider(UpcomingWidget.WidgetTextSecondary),
                        fontSize = 11.sp
                    )
                )
            }
        }

        Spacer(modifier = GlanceModifier.defaultWeight())

        // 3. Bottom Row: Price & View action indicator
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.Bottom
        ) {
            Text(
                text = CurrencyFormatter.format(subscription.price, currency),
                style = TextStyle(
                    color = ColorProvider(UpcomingWidget.WidgetTextPrimary),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = GlanceModifier.defaultWeight())

            Text(
                text = "View →",
                style = TextStyle(
                    color = ColorProvider(UpcomingWidget.WidgetPrimary),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

/**
 * 4x2 Medium Widget View: Header with Upcoming count/total + 2-3 upcoming items
 */
@Composable
private fun MediumUpcomingView(
    subscriptions: List<Subscription>,
    currency: Currency
) {
    if (subscriptions.isEmpty()) {
        EmptyUpcomingView(isCompact = false)
        return
    }

    val totalUpcoming = subscriptions.sumOf { it.price }
    val displayItems = subscriptions.take(2)

    Column(
        modifier = GlanceModifier.fillMaxSize()
    ) {
        WidgetHeader(
            subtitle = "${subscriptions.size} due soon (${CurrencyFormatter.format(totalUpcoming, currency)})"
        )

        Spacer(modifier = GlanceModifier.height(8.dp))

        Column(
            modifier = GlanceModifier.fillMaxWidth().defaultWeight()
        ) {
            displayItems.forEachIndexed { index, sub ->
                if (index > 0) {
                    Spacer(modifier = GlanceModifier.height(6.dp))
                }
                UpcomingSubscriptionRow(
                    subscription = sub,
                    currency = currency
                )
            }
        }
    }
}

/**
 * 4x3+ Expanded Widget View: Header with Upcoming count/total + scrollable list
 */
@Composable
private fun ExpandedUpcomingView(
    subscriptions: List<Subscription>,
    currency: Currency
) {
    if (subscriptions.isEmpty()) {
        EmptyUpcomingView(isCompact = false)
        return
    }

    val totalUpcoming = subscriptions.sumOf { it.price }

    Column(
        modifier = GlanceModifier.fillMaxSize()
    ) {
        WidgetHeader(
            subtitle = "${subscriptions.size} active (${CurrencyFormatter.format(totalUpcoming, currency)} total)"
        )

        Spacer(modifier = GlanceModifier.height(8.dp))

        LazyColumn(
            modifier = GlanceModifier.fillMaxWidth().defaultWeight()
        ) {
            items(subscriptions) { sub ->
                Box(modifier = GlanceModifier.padding(vertical = 3.dp)) {
                    UpcomingSubscriptionRow(
                        subscription = sub,
                        currency = currency
                    )
                }
            }
        }
    }
}

/**
 * Common Header for Medium and Large widgets
 */
@Composable
private fun WidgetHeader(
    subtitle: String,
    title: String = "Upcoming Payments"
) {
    val openHomeAction = actionStartActivity<MainActivity>()

    val addAction = actionStartActivity<MainActivity>(
        actionParametersOf(
            UpcomingWidget.KeyAction to UpcomingWidget.ACTION_ADD_SUBSCRIPTION
        )
    )

    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        Column(
            modifier = GlanceModifier.defaultWeight().clickable(openHomeAction)
        ) {
            Text(
                text = title,
                style = TextStyle(
                    color = ColorProvider(UpcomingWidget.WidgetTextPrimary),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = subtitle,
                style = TextStyle(
                    color = ColorProvider(UpcomingWidget.WidgetTextSecondary),
                    fontSize = 10.sp
                )
            )
        }

        // Quick "+" Add Button
        Box(
            modifier = GlanceModifier
                .size(28.dp)
                .background(UpcomingWidget.WidgetPrimary)
                .cornerRadius(14.dp)
                .clickable(addAction),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+",
                style = TextStyle(
                    color = ColorProvider(UpcomingWidget.WidgetTextPrimary),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

/**
 * Single subscription row used in Medium and Large widget views
 */
@Composable
private fun UpcomingSubscriptionRow(
    subscription: Subscription,
    currency: Currency
) {
    val iconRes = getSubscriptionIcon(subscription.name)
    val nextBilling = subscription.getNextBillingDate()
    val remainingLabel = subscription.getRemainingDaysLabel() ?: "Upcoming"
    val urgency = subscription.getRenewalUrgency()

    val dateFormatted = if (nextBilling != null) {
        val localDate = Instant.ofEpochMilli(nextBilling)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val monthName = localDate.month.getDisplayName(java.time.format.TextStyle.SHORT, Locale.US)
        "$monthName ${localDate.dayOfMonth}"
    } else {
        ""
    }

    val urgencyColor = when (urgency) {
        RenewalUrgency.URGENT -> UpcomingWidget.WidgetUrgentRed
        RenewalUrgency.SOON -> UpcomingWidget.WidgetSoonOrange
        RenewalUrgency.NORMAL -> UpcomingWidget.WidgetTextSecondary
    }

    val openDetailAction = actionStartActivity<MainActivity>(
        actionParametersOf(
            UpcomingWidget.KeyAction to UpcomingWidget.ACTION_OPEN_SUBSCRIPTION,
            UpcomingWidget.KeySubscriptionId to subscription.id
        )
    )

    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(UpcomingWidget.WidgetIconBackground)
            .cornerRadius(12.dp)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .clickable(openDetailAction),
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        Image(
            provider = ImageProvider(iconRes),
            contentDescription = subscription.name,
            modifier = GlanceModifier.size(24.dp)
        )

        Spacer(modifier = GlanceModifier.width(8.dp))

        Column(
            modifier = GlanceModifier.defaultWeight()
        ) {
            Text(
                text = subscription.name,
                maxLines = 1,
                style = TextStyle(
                    color = ColorProvider(UpcomingWidget.WidgetTextPrimary),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = if (dateFormatted.isNotEmpty()) "$remainingLabel · $dateFormatted" else remainingLabel,
                style = TextStyle(
                    color = ColorProvider(urgencyColor),
                    fontSize = 10.sp
                )
            )
        }

        Spacer(modifier = GlanceModifier.width(8.dp))

        Text(
            text = CurrencyFormatter.format(subscription.price, currency),
            style = TextStyle(
                color = ColorProvider(UpcomingWidget.WidgetTextPrimary),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

/**
 * Friendly Empty State view when there are no upcoming subscriptions
 */
@Composable
private fun EmptyUpcomingView(
    isCompact: Boolean
) {
    val addAction = actionStartActivity<MainActivity>(
        actionParametersOf(
            UpcomingWidget.KeyAction to UpcomingWidget.ACTION_ADD_SUBSCRIPTION
        )
    )

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(8.dp)
            .clickable(addAction),
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        Image(
            provider = ImageProvider(R.drawable.subtrack),
            contentDescription = "SubTrack",
            modifier = GlanceModifier.size(if (isCompact) 28.dp else 36.dp)
        )

        Spacer(modifier = GlanceModifier.height(6.dp))

        Text(
            text = if (isCompact) "No upcoming bills" else "No upcoming payments",
            style = TextStyle(
                color = ColorProvider(UpcomingWidget.WidgetTextPrimary),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = GlanceModifier.height(6.dp))

        Box(
            modifier = GlanceModifier
                .background(UpcomingWidget.WidgetPrimary)
                .cornerRadius(10.dp)
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = "+ Add Subscription",
                style = TextStyle(
                    color = ColorProvider(UpcomingWidget.WidgetTextPrimary),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}
