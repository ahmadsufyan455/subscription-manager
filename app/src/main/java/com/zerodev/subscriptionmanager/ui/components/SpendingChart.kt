package com.zerodev.subscriptionmanager.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.LineCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.Insets
import com.patrykandpatrick.vico.compose.common.LayeredComponent
import com.patrykandpatrick.vico.compose.common.component.ShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.lineModel
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.zerodev.subscriptionmanager.core.utils.Currency
import com.zerodev.subscriptionmanager.core.utils.CurrencyFormatter
import com.zerodev.subscriptionmanager.data.local.entities.Subscription
import com.zerodev.subscriptionmanager.data.local.entities.BillingCycle
import com.zerodev.subscriptionmanager.data.local.entities.SubscriptionStatus
import com.zerodev.subscriptionmanager.ui.theme.Primary
import com.zerodev.subscriptionmanager.ui.theme.TextPrimary
import com.zerodev.subscriptionmanager.ui.theme.TextSecondary
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

@Composable
fun SpendingChart(
    subscriptions: List<Subscription>,
    modifier: Modifier = Modifier
) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val context = LocalContext.current
    val currency = remember(subscriptions) { CurrencyFormatter.getSelectedCurrency(context) }
    
    val currentMonth = remember(subscriptions) { YearMonth.now() }
    val months = remember(subscriptions, currentMonth) {
        (5 downTo 0).map { currentMonth.minusMonths(it.toLong()) }
    }
    
    val monthlyData = remember(subscriptions, months) {
        calculateMonthlySpending(subscriptions, months)
    }
    
    val totalMonthlySpend = remember(monthlyData) {
        monthlyData.lastOrNull() ?: 0.0
    }

    LaunchedEffect(monthlyData) {
        val seriesData = if (monthlyData.isEmpty()) listOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0) else monthlyData
        modelProducer.runTransaction {
            lineModel {
                series(seriesData)
            }
        }
    }

    // Configure the custom tooltip marker matching the design
    val markerValueFormatter = remember(currency) {
        DefaultCartesianMarker.ValueFormatter { _, targets ->
            val firstPointY = (targets.firstOrNull() as? LineCartesianLayerMarkerTarget)?.points?.firstOrNull()?.entry?.y ?: 0.0
            val converted = CurrencyFormatter.convertFromUsd(firstPointY, currency)
            val formatter = NumberFormat.getNumberInstance(currency.locale).apply {
                minimumFractionDigits = if (currency == Currency.IDR) 0 else 2
                maximumFractionDigits = if (currency == Currency.IDR) 0 else 2
            }
            val formattedValue = formatter.format(converted)
            "Spend : ${currency.symbol}$formattedValue"
        }
    }

    val markerLabel = rememberTextComponent(
        style = TextStyle(
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        ),
        padding = Insets(horizontal = 12.dp, vertical = 8.dp),
        background = rememberShapeComponent(
            fill = Fill(Color(0xFF1E1E1E)),
            shape = RoundedCornerShape(8.dp)
        )
    )

    val marker = rememberDefaultCartesianMarker(
        label = markerLabel,
        valueFormatter = markerValueFormatter,
        labelPosition = DefaultCartesianMarker.LabelPosition.AroundPoint,
        indicator = { color ->
            LayeredComponent(
                back = ShapeComponent(fill = Fill(Color.White), shape = CircleShape),
                front = ShapeComponent(fill = Fill(color), shape = CircleShape),
                padding = Insets(all = 2.dp)
            )
        },
        indicatorSize = 10.dp,
        guideline = rememberLineComponent(
            fill = Fill(Color.White.copy(alpha = 0.3f)),
            thickness = 1.dp
        )
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Total Monthly Spend",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        
        val converted = CurrencyFormatter.convertFromUsd(totalMonthlySpend, currency)
        val formatter = remember(currency) {
            NumberFormat.getNumberInstance(currency.locale).apply {
                minimumFractionDigits = if (currency == Currency.IDR) 0 else 2
                maximumFractionDigits = if (currency == Currency.IDR) 0 else 2
            }
        }
        val formattedValue = formatter.format(converted)

        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = currency.symbol,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                modifier = Modifier.alignByBaseline()
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = formattedValue,
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 38.sp),
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.alignByBaseline()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(
                    lineProvider = LineCartesianLayer.LineProvider.series(
                        LineCartesianLayer.rememberLine(
                            fill = LineCartesianLayer.LineFill.single(Fill(Primary)),
                            areaFill = LineCartesianLayer.AreaFill.single(
                                Fill(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Primary.copy(alpha = 0.4f),
                                            Color.Transparent
                                        )
                                    )
                                )
                            ),
                            interpolator = LineCartesianLayer.Interpolator.cubic(curvature = 0.4f),
                            pointProvider = null
                        )
                    )
                ),
                marker = marker
            ),
            modelProducer = modelProducer,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        )
    }
}

private fun calculateMonthlySpending(
    subscriptions: List<Subscription>,
    months: List<YearMonth>
): List<Double> {
    val zoneId = ZoneId.systemDefault()
    return months.map { yearMonth ->
        val startOfMonth = yearMonth.atDay(1)
        val endOfMonth = yearMonth.atEndOfMonth()

        subscriptions.sumOf { sub ->
            val startLocalDate = Instant.ofEpochMilli(sub.startDate)
                .atZone(zoneId)
                .toLocalDate()

            // If the subscription starts after the end of the month, it couldn't have been billed yet.
            if (startLocalDate.isAfter(endOfMonth)) {
                return@sumOf 0.0
            }

            val cycleDays = when (sub.billingCycle) {
                BillingCycle.WEEKLY -> 7
                BillingCycle.MONTHLY -> 30
                BillingCycle.YEARLY -> 365
                BillingCycle.CUSTOM -> sub.customCycleDays ?: 30
            }
            val incrementDays = if (cycleDays <= 0) 30 else cycleDays

            // Calculate billing occurrences in this specific month
            var billingDate = startLocalDate
            var occurrences = 0

            // If the subscription was cancelled, it stopped billing after the next billing date following cancellation.
            val cancelLimitDate = sub.cancelledAt?.let {
                Instant.ofEpochMilli(it).atZone(zoneId).toLocalDate()
            }

            val stopDate = cancelLimitDate

            while (!billingDate.isAfter(endOfMonth)) {
                // Check if billing date is within this month
                if (!billingDate.isBefore(startOfMonth)) {
                    // Check if it was cancelled before this billing date
                    val isAfterStop = stopDate != null && billingDate.isAfter(stopDate)
                    if (!isAfterStop) {
                        occurrences++
                    }
                }
                billingDate = billingDate.plusDays(incrementDays.toLong())
            }

            occurrences * sub.price
        }
    }
}