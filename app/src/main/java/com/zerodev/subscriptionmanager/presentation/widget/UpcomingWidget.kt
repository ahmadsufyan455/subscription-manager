@file:Suppress("RestrictedApi")

package com.zerodev.subscriptionmanager.presentation.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
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
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ColumnScope
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.PreferencesGlanceStateDefinition
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
        val KeyCardIndex = intPreferencesKey("upcoming_card_index")
        val KeyLastUpdated = longPreferencesKey("widget_last_updated")

        // Palette matching app theme
        val WidgetBackground = Color(0xFF0C0B14)
        val WidgetCardBackground = Color(0xFF151421)
        val WidgetCardStack1 = Color(0xFF222036)
        val WidgetCardStack2 = Color(0xFF191828)
        val WidgetIconBackground = Color(0x1FFFFFFF)
        val WidgetPrimary = Color(0xFF605DFF)
        val WidgetTextPrimary = Color(0xFFFFFFFF)
        val WidgetTextSecondary = Color(0xFF9A9AB0)
        val WidgetUrgentRed = Color(0xFFFF3B30)
        val WidgetSoonOrange = Color(0xFFFF9900)
    }

    override val stateDefinition = PreferencesGlanceStateDefinition

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository: SubscriptionRepository by inject()

        provideContent {
            val allSubscriptions by repository.getAllSubscriptions().collectAsState(initial = emptyList())

            // Active subscriptions sorted by next billing date
            val activeSubscriptions = allSubscriptions
                .filter { it.status == SubscriptionStatus.ACTIVE && it.getNextBillingDate() != null }
                .sortedBy { it.getNextBillingDate() }

            // Filter for upcoming payments due within 7 days
            val dueSoonSubscriptions = activeSubscriptions
                .filter { (it.getRemainingDays() ?: Int.MAX_VALUE) <= 7 }

            val currency = CurrencyFormatter.getSelectedCurrency(context)
            val size = LocalSize.current
            val prefs = currentState<Preferences>()
            val rawIndex = prefs[KeyCardIndex] ?: 0

            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .appWidgetBackground()
                    .background(WidgetBackground)
                    .cornerRadius(18.dp)
                    .padding(10.dp)
            ) {
                // If width is narrow (compact 2x2 square), show Hero Card.
                // If width is wide (4x1, 4x2, 4x3), show Adaptive Stacked Carousel.
                if (size.width < 190.dp) {
                    CompactUpcomingView(
                        subscription = activeSubscriptions.firstOrNull(),
                        currency = currency
                    )
                } else {
                    MediumStackedUpcomingView(
                        subscriptions = dueSoonSubscriptions,
                        rawIndex = rawIndex,
                        currency = currency
                    )
                }
            }
        }
    }
}

/**
 * Action callback to navigate cards in the stacked carousel
 */
class NavigateCardAction : ActionCallback {
    companion object {
        val DeltaKey = ActionParameters.Key<Int>("delta")
    }

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val delta = parameters[DeltaKey] ?: 1
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
            prefs.toMutablePreferences().apply {
                val current = this[UpcomingWidget.KeyCardIndex] ?: 0
                this[UpcomingWidget.KeyCardIndex] = current + delta
            }
        }
        UpcomingWidget().update(context, glanceId)
    }
}

/**
 * 2x2 Compact Widget View: Hero Card layout
 */
@Composable
private fun CompactUpcomingView(
    subscription: Subscription?,
    currency: Currency
) {
    if (subscription == null) {
        EmptyUpcomingView(
            isCompact = true,
            message = "No upcoming bills"
        )
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
            .background(UpcomingWidget.WidgetCardBackground)
            .cornerRadius(16.dp)
            .padding(10.dp)
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
 * 4x2 Medium/Wide Widget View: Ergonomic Bottom-Stepper Stacked Card Carousel
 */
@Composable
private fun MediumStackedUpcomingView(
    subscriptions: List<Subscription>,
    rawIndex: Int,
    currency: Currency
) {
    if (subscriptions.isEmpty()) {
        EmptyUpcomingView(
            isCompact = false,
            message = "No payments due this week 🎉"
        )
        return
    }

    val totalCount = subscriptions.size
    val currentIndex = ((rawIndex % totalCount) + totalCount) % totalCount
    val currentSub = subscriptions[currentIndex]
    val totalThisWeek = subscriptions.sumOf { it.price }

    val openHomeAction = actionStartActivity<MainActivity>()

    Column(
        modifier = GlanceModifier.fillMaxSize()
    ) {
        // 1. Clean Minimal Header (Tapping opens the app)
        Row(
            modifier = GlanceModifier.fillMaxWidth().clickable(openHomeAction),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Column(
                modifier = GlanceModifier.fillMaxWidth()
            ) {
                Text(
                    text = "THIS WEEK",
                    style = TextStyle(
                        color = ColorProvider(UpcomingWidget.WidgetTextPrimary),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "$totalCount due (${CurrencyFormatter.format(totalThisWeek, currency)})",
                    style = TextStyle(
                        color = ColorProvider(UpcomingWidget.WidgetTextSecondary),
                        fontSize = 10.sp
                    )
                )
            }
        }

        Spacer(modifier = GlanceModifier.height(6.dp))

        // 2. Full Adaptive Stacked Card Layout
        Column(
            modifier = GlanceModifier.fillMaxWidth().defaultWeight()
        ) {
            // Main Top Card with Bottom Stepper
            StackedCardItem(
                subscription = currentSub,
                currentIndex = currentIndex,
                totalCount = totalCount,
                currency = currency
            )

            // 3D Layered Under-cards effect when multiple items exist
            if (totalCount > 1) {
                Spacer(modifier = GlanceModifier.height(2.dp))
                // Layer 1 under-card
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .padding(horizontal = 12.dp)
                        .background(UpcomingWidget.WidgetCardStack1)
                        .cornerRadius(4.dp)
                ) {}
                if (totalCount > 2) {
                    Spacer(modifier = GlanceModifier.height(2.dp))
                    // Layer 2 under-card
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .padding(horizontal = 24.dp)
                            .background(UpcomingWidget.WidgetCardStack2)
                            .cornerRadius(3.dp)
                    ) {}
                }
            }
        }
    }
}

/**
 * Single card item rendered inside the Stacked Carousel (with Bottom Ergonomic Stepper Pill)
 */
@Composable
private fun ColumnScope.StackedCardItem(
    subscription: Subscription,
    currentIndex: Int,
    totalCount: Int,
    currency: Currency
) {
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
            .fillMaxWidth()
            .defaultWeight()
            .background(UpcomingWidget.WidgetCardBackground)
            .cornerRadius(16.dp)
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .clickable(openDetailAction),
        verticalAlignment = Alignment.Vertical.Top
    ) {
        // 1. Top Section: Brand Icon Tile + Subscription Name & Subtitle + Urgency Pill
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            // Brand Icon Tile
            Box(
                modifier = GlanceModifier
                    .size(42.dp)
                    .background(UpcomingWidget.WidgetIconBackground)
                    .cornerRadius(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(iconRes),
                    contentDescription = subscription.name,
                    modifier = GlanceModifier.size(28.dp)
                )
            }

            Spacer(modifier = GlanceModifier.width(12.dp))

            // Subscription Title & Subtitle
            Column(
                modifier = GlanceModifier.defaultWeight()
            ) {
                Text(
                    text = subscription.name,
                    maxLines = 1,
                    style = TextStyle(
                        color = ColorProvider(UpcomingWidget.WidgetTextPrimary),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.height(2.dp))
                Text(
                    text = subtitle,
                    maxLines = 1,
                    style = TextStyle(
                        color = ColorProvider(UpcomingWidget.WidgetTextSecondary),
                        fontSize = 12.sp
                    )
                )
            }

            Spacer(modifier = GlanceModifier.width(8.dp))

            // Urgency Pill
            Box(
                modifier = GlanceModifier
                    .background(badgeColor)
                    .cornerRadius(7.dp)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = remainingLabel,
                    style = TextStyle(
                        color = ColorProvider(UpcomingWidget.WidgetTextPrimary),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Spacer(modifier = GlanceModifier.defaultWeight())

        // 2. Bottom Section: Amount Due & Price on Left, Stepper Pill [ ‹ 3/5 › ] on Right
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.Bottom
        ) {
            Column {
                Text(
                    text = "AMOUNT DUE",
                    style = TextStyle(
                        color = ColorProvider(UpcomingWidget.WidgetTextSecondary),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.height(2.dp))
                Text(
                    text = CurrencyFormatter.format(subscription.price, currency),
                    style = TextStyle(
                        color = ColorProvider(UpcomingWidget.WidgetTextPrimary),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = GlanceModifier.defaultWeight())

            if (totalCount > 1) {
                // Ergonomic Bottom Stepper Pill [ ‹  3/5  › ]
                Row(
                    modifier = GlanceModifier
                        .background(UpcomingWidget.WidgetIconBackground)
                        .cornerRadius(16.dp)
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.Vertical.CenterVertically
                ) {
                    // Prev ‹ Button
                    Box(
                        modifier = GlanceModifier
                            .size(30.dp)
                            .cornerRadius(15.dp)
                            .clickable(
                                actionRunCallback<NavigateCardAction>(
                                    actionParametersOf(NavigateCardAction.DeltaKey to -1)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "‹",
                            style = TextStyle(
                                color = ColorProvider(UpcomingWidget.WidgetTextPrimary),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = GlanceModifier.width(4.dp))

                    // Index Indicator e.g. "3/5"
                    Text(
                        text = "${currentIndex + 1}/$totalCount",
                        style = TextStyle(
                            color = ColorProvider(UpcomingWidget.WidgetTextPrimary),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = GlanceModifier.width(4.dp))

                    // Next › Button
                    Box(
                        modifier = GlanceModifier
                            .size(30.dp)
                            .cornerRadius(15.dp)
                            .clickable(
                                actionRunCallback<NavigateCardAction>(
                                    actionParametersOf(NavigateCardAction.DeltaKey to 1)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "›",
                            style = TextStyle(
                                color = ColorProvider(UpcomingWidget.WidgetTextPrimary),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            } else {
                // If single item, show View Details button
                Box(
                    modifier = GlanceModifier
                        .background(UpcomingWidget.WidgetPrimary)
                        .cornerRadius(10.dp)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "View Details →",
                        style = TextStyle(
                            color = ColorProvider(UpcomingWidget.WidgetTextPrimary),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

/**
 * Friendly Empty State view
 */
@Composable
private fun EmptyUpcomingView(
    isCompact: Boolean,
    message: String
) {
    val addAction = actionStartActivity<MainActivity>(
        actionParametersOf(
            UpcomingWidget.KeyAction to UpcomingWidget.ACTION_ADD_SUBSCRIPTION
        )
    )

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(UpcomingWidget.WidgetCardBackground)
            .cornerRadius(16.dp)
            .padding(10.dp)
            .clickable(addAction),
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        Image(
            provider = ImageProvider(R.drawable.subtrack),
            contentDescription = "SubTrack",
            modifier = GlanceModifier.size(if (isCompact) 28.dp else 34.dp)
        )

        Spacer(modifier = GlanceModifier.height(4.dp))

        Text(
            text = message,
            style = TextStyle(
                color = ColorProvider(UpcomingWidget.WidgetTextPrimary),
                fontSize = 11.sp,
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
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}
