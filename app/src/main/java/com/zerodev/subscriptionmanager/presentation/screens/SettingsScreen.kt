package com.zerodev.subscriptionmanager.presentation.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import com.zerodev.subscriptionmanager.R
import com.zerodev.subscriptionmanager.core.helper.NotificationScheduler
import com.zerodev.subscriptionmanager.core.utils.Currency
import com.zerodev.subscriptionmanager.core.utils.CurrencyFormatter
import com.zerodev.subscriptionmanager.presentation.viewmodel.HomeViewModel
import com.zerodev.subscriptionmanager.ui.theme.CardBackground
import com.zerodev.subscriptionmanager.ui.theme.DarkBackground
import com.zerodev.subscriptionmanager.ui.theme.Divider
import com.zerodev.subscriptionmanager.ui.theme.Primary
import com.zerodev.subscriptionmanager.ui.theme.TextPrimary
import com.zerodev.subscriptionmanager.ui.theme.TextSecondary
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

// ─── Accent colours matching the design ───────────────────────────────────────
private val CurrencyIconBg   = Color(0xFF1B3A2B)   // dark green
private val CurrencyIconTint = Color(0xFF3DD68C)   // bright green
private val NotifIconBg      = Color(0xFF3A2B0A)   // dark amber
private val NotifIconTint    = Color(0xFFFFBB0D)   // amber
private val ExportIconBg     = Color(0xFF1A1E3A)   // dark indigo
private val ExportIconTint   = Color(0xFF6870E8)   // indigo
private val ImportIconBg     = Color(0xFF2A1A3A)   // dark purple
private val ImportIconTint   = Color(0xFF9B6DFF)   // purple
private val DeleteIconBg     = Color(0xFF3A1A1A)   // dark red
private val DeleteIconTint   = Color(0xFFFF3B30)   // red

@OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class)
@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val viewModel: HomeViewModel = koinViewModel()
    val sharedPrefs = remember {
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    }

    val hazeState = rememberHazeState()

    var notificationsEnabled by remember {
        mutableStateOf(sharedPrefs.getBoolean("notifications_enabled", false))
    }
    var selectedCurrency by remember {
        mutableStateOf(CurrencyFormatter.getSelectedCurrency(context))
    }
    var showCurrencySheet by remember { mutableStateOf(false) }
    var showClearSheet by remember { mutableStateOf(false) }

    val versionName = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    // SAF launcher — opens the system file picker so the user picks the save location
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.exportSubscriptions(uri) { success ->
                Toast.makeText(
                    context,
                    if (success) "Data exported successfully" else "Export failed. Please try again.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // SAF launcher — opens the system file picker so the user picks a JSON to import
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importSubscriptions(uri) { count ->
                Toast.makeText(
                    context,
                    if (count >= 0) "$count subscriptions imported successfully"
                    else "Import failed. Make sure the file is a valid export.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState)
                .background(DarkBackground)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
        Spacer(Modifier.height(56.dp))

        // ── Page Title ──────────────────────────────────────────────────────
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 28.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        // ── PREFERENCES ─────────────────────────────────────────────────────
        SectionHeader("PREFERENCES")
        Spacer(Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardBackground)
        ) {
            // Currency row
            SettingsRow(
                iconBg = CurrencyIconBg,
                iconTint = CurrencyIconTint,
                iconVector = null,
                iconResId = R.drawable.ic_currency_dollar,
                label = "Currency",
                trailingContent = {
                    Text(
                        text = when (selectedCurrency) {
                            Currency.USD -> "USD (\$)"
                            Currency.IDR -> "IDR (Rp)"
                        },
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                },
                onClick = { showCurrencySheet = true }
            )

            HorizontalDivider(color = Divider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))

            // Notifications row
            SettingsRow(
                iconBg = NotifIconBg,
                iconTint = NotifIconTint,
                iconVector = Icons.Default.Notifications,
                label = "Notifications",
                trailingContent = {
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { enabled ->
                            notificationsEnabled = enabled
                            sharedPrefs.edit { putBoolean("notifications_enabled", enabled) }
                            if (enabled) {
                                NotificationScheduler.scheduleNotificationCheck(context)
                            } else {
                                NotificationScheduler.cancelNotificationCheck(context)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Primary,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = Color(0xFF3A3A50)
                        )
                    )
                }
            )
        }

        Spacer(Modifier.height(28.dp))

        // ── DATA & BACKUP ────────────────────────────────────────────────────
        SectionHeader("DATA & BACKUP")
        Spacer(Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardBackground)
        ) {
            SettingsRow(
                iconBg = ExportIconBg,
                iconTint = ExportIconTint,
                iconVector = null,
                iconResId = R.drawable.ic_export,
                label = "Export Data",
                trailingContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                onClick = {
                    exportLauncher.launch(
                        com.zerodev.subscriptionmanager.core.helper.ExportHelper.suggestedFileName()
                    )
                }
            )

            HorizontalDivider(color = Divider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))

            SettingsRow(
                iconBg = ImportIconBg,
                iconTint = ImportIconTint,
                iconVector = null,
                iconResId = R.drawable.ic_import,
                label = "Import Data",
                trailingContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                onClick = {
                    importLauncher.launch(arrayOf("application/json"))
                }
            )

            HorizontalDivider(color = Divider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))

            SettingsRow(
                iconBg = DeleteIconBg,
                iconTint = DeleteIconTint,
                iconVector = Icons.Default.Delete,
                label = "Clear All Data",
                labelColor = DeleteIconTint,
                trailingContent = {},
                onClick = { showClearSheet = true }
            )
        }

        Spacer(Modifier.height(28.dp))

        // ── ABOUT ────────────────────────────────────────────────────────────
        SectionHeader("ABOUT")
        Spacer(Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardBackground)
        ) {
            SettingsRow(
                iconBg = Color(0xFF1E1D2C),
                iconTint = TextSecondary,
                iconVector = Icons.Default.Info,
                label = "Version",
                trailingContent = {
                    Text(
                        text = versionName,
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            )
        }

        Spacer(Modifier.height(32.dp))
    }

    // ── Haze Blur Overlay when Bottom Sheet is active ───────────────────────
    if (showCurrencySheet || showClearSheet) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .hazeEffect(hazeState, style = HazeMaterials.ultraThin())
        )
    }

    // ── Clear All Data Confirmation Sheet ──────────────────────────────────
    if (showClearSheet) {
        ModalBottomSheet(
            onDismissRequest = { showClearSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = com.zerodev.subscriptionmanager.ui.theme.BottomSheetBackground,
            scrimColor = Color.Transparent,
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.25f)) }
        ) {
            ClearDataConfirmationSheet(
                onConfirm = {
                    showClearSheet = false
                    viewModel.clearAllData { success ->
                        Toast.makeText(
                            context,
                            if (success) "All data cleared" else "Failed to clear data.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                onDismiss = { showClearSheet = false }
            )
        }
    }

    // ── Currency Bottom Sheet ────────────────────────────────────────────────
    if (showCurrencySheet) {
        ModalBottomSheet(
            onDismissRequest = { showCurrencySheet = false },
            sheetState = sheetState,
            containerColor = com.zerodev.subscriptionmanager.ui.theme.BottomSheetBackground,
            scrimColor = Color.Transparent,
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.25f)) }
        ) {
            CurrencyBottomSheetContent(
                selectedCurrency = selectedCurrency,
                onCurrencySelected = { currency ->
                    selectedCurrency = currency
                    CurrencyFormatter.setSelectedCurrency(context, currency)
                    scope.launch {
                        sheetState.hide()
                        showCurrencySheet = false
                    }
                }
            )
        }
    }
}
}

// ─── Section Header ───────────────────────────────────────────────────────────
@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextSecondary,
        letterSpacing = 1.sp
    )
}

// ─── Generic Settings Row ─────────────────────────────────────────────────────
@Composable
private fun SettingsRow(
    iconBg: Color,
    iconTint: Color,
    iconVector: ImageVector? = null,
    iconResId: Int? = null,
    label: String,
    labelColor: Color = TextPrimary,
    trailingContent: @Composable () -> Unit,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon badge
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            when {
                iconResId != null -> Icon(
                    painter = painterResource(id = iconResId),
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
                iconVector != null -> Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(Modifier.width(14.dp))

        Text(
            text = label,
            color = labelColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        trailingContent()
    }
}

// ─── Currency Bottom Sheet Content ───────────────────────────────────────────
@Composable
private fun CurrencyBottomSheetContent(
    selectedCurrency: Currency,
    onCurrencySelected: (Currency) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 40.dp)
    ) {
        Text(
            text = "Select Currency",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        Currency.entries.forEachIndexed { index, currency ->
            val isSelected = selectedCurrency == currency

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) Color(0xFF1F1E33) else Color.Transparent)
                    .clickable { onCurrencySelected(currency) }
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Flag / icon badge
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (currency == Currency.USD) CurrencyIconBg else Color(0xFF1A2F3A)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currency.symbol,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (currency == Currency.USD) CurrencyIconTint else Color(0xFF3DD6C0)
                    )
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when (currency) {
                            Currency.USD -> "US Dollar"
                            Currency.IDR -> "Indonesian Rupiah"
                        },
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${currency.code} • ${currency.symbol}",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (index < Currency.entries.size - 1) {
                HorizontalDivider(
                    color = Divider,
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// ─── Clear All Data Confirmation Sheet ────────────────────────────────────────
@Composable
private fun ClearDataConfirmationSheet(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))

        // ── Warning icon: two concentric circles ─────────────────────────────
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFF3A1010)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF5A1A1A)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = DeleteIconTint,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── Title ─────────────────────────────────────────────────────────────
        Text(
            text = "Clear All Data?",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))

        // ── Description ───────────────────────────────────────────────────────
        Text(
            text = "This action cannot be undone. All your subscriptions, history, and settings will be permanently deleted from this device.",
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 20.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(Modifier.height(32.dp))

        // ── Yes, Delete Everything button ─────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(DeleteIconTint)
                .clickable { onConfirm() }
                .padding(vertical = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Yes, Delete Everything",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(Modifier.height(4.dp))

        // ── Cancel button ─────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Transparent)
                .clickable { onDismiss() }
                .padding(vertical = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Cancel",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }
    }
}