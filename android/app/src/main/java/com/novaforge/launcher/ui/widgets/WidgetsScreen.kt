package com.novaforge.launcher.ui.widgets

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novaforge.launcher.data.AppIconManager
import com.novaforge.launcher.data.RealWeatherInfo
import com.novaforge.launcher.data.ScreenTimeSummary
import com.novaforge.launcher.data.model.BatteryInfo
import com.novaforge.launcher.data.model.LauncherWidget
import com.novaforge.launcher.widget.ResizableWidgetContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetsScreen(
    weather: RealWeatherInfo,
    batteryInfo: BatteryInfo,
    screenTime: ScreenTimeSummary,
    widgets: List<LauncherWidget>,
    appWidgetHost: AppWidgetHost,
    appWidgetManager: AppWidgetManager,
    onRequestAddWidget: () -> Unit,
    onRequestUsagePermission: () -> Unit,
    onResizeWidgetWidth: (Int, Int) -> Unit,
    onResizeWidgetHeight: (Int, Int) -> Unit,
    onMoveWidget: (Int, Int) -> Unit,
    onRemoveWidget: (Int) -> Unit,
    onSelectProvider: (AppWidgetProviderInfo) -> Unit = {},
    onOpenSettings: () -> Unit = {}
) {
    var isEditMode by remember { mutableStateOf(false) }
    var showProviderPickerSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val installedProviders = remember {
        try {
            appWidgetManager.installedProviders.sortedBy {
                it.loadLabel(context.packageManager).lowercase()
            }
        } catch (_: Exception) {
            emptyList<AppWidgetProviderInfo>()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        item(key = "header") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Widgets & Glance",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { isEditMode = !isEditMode },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Layout",
                            tint = if (isEditMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = {
                            if (installedProviders.isNotEmpty()) {
                                showProviderPickerSheet = true
                            } else {
                                onRequestAddWidget()
                            }
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Widget", tint = Color.White)
                    }
                }
            }
        }

        // Real Weather Card (Open-Meteo) — Refined dark cosmic styling
        item(key = "weather_card") {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clickable { onOpenSettings() }
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = weather.city,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        if (weather.isDemoData) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "OFFLINE DEMO",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = weather.temperature,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Light
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${weather.description} • ${weather.tempRange}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Real Screen Time & Real Battery Gauge Row
        item(key = "metrics_row") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Battery Gauge Card
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("${batteryInfo.percentage}%", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text(
                            batteryInfo.statusText,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Screen Time Card (UsageStatsManager)
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.weight(1.2f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (screenTime.hasPermission) screenTime.totalTimeFormatted else "Access Needed",
                                fontSize = if (screenTime.hasPermission) 20.sp else 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (screenTime.hasPermission) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary
                            )
                        }
                        Text("Screen Time Today", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (!screenTime.hasPermission) {
                            Spacer(modifier = Modifier.height(6.dp))
                            FilledTonalButton(
                                onClick = onRequestUsagePermission,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("Grant Permission", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            }
                        } else if (screenTime.topAppUsage.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = screenTime.topAppUsage.take(2).joinToString(" • ") { "${it.first} (${it.second})" },
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // Edit Mode Explanation Banner
        if (isEditMode) {
            item(key = "edit_banner") {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Edit mode: Tap size buttons to resize width & height, arrow keys to reorder, or ✕ to remove.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Native Hosted Widgets
        if (widgets.isEmpty()) {
            item(key = "empty_widgets") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No Widgets Added",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap + to add Android AppWidgets",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(widgets, key = { it.appWidgetId }) { widget ->
                ResizableWidgetContainer(
                    widget = widget,
                    appWidgetHost = appWidgetHost,
                    appWidgetManager = appWidgetManager,
                    isEditMode = isEditMode,
                    onResizeWidth = { newWidth -> onResizeWidgetWidth(widget.appWidgetId, newWidth) },
                    onResizeHeight = { newHeight -> onResizeWidgetHeight(widget.appWidgetId, newHeight) },
                    onMoveUp = { onMoveWidget(widget.appWidgetId, -1) },
                    onMoveDown = { onMoveWidget(widget.appWidgetId, 1) },
                    onRemoveWidget = { onRemoveWidget(widget.appWidgetId) }
                )
            }
        }
    }

    // Modal Sheet for choosing from Installed Widget Providers or System Picker
    if (showProviderPickerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showProviderPickerSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add Android Widget",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = {
                        showProviderPickerSheet = false
                        onRequestAddWidget()
                    }) {
                        Text("System Picker", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(installedProviders, key = { "${it.provider.packageName}/${it.provider.className}" }) { provider ->
                        val label = provider.loadLabel(context.packageManager)
                        val pkgName = provider.provider.packageName
                        val appIcon = remember(pkgName) {
                            AppIconManager.getAppIconBitmap(context, pkgName)
                        }
                        val spanW = ((provider.minWidth + 30) / 70).coerceIn(1, 4)
                        val spanH = ((provider.minHeight + 30) / 70).coerceIn(1, 4)

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.Transparent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showProviderPickerSheet = false
                                    onSelectProvider(provider)
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (appIcon != null) {
                                    Image(
                                        bitmap = appIcon.asImageBitmap(),
                                        contentDescription = label,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                    )
                                } else {
                                    Surface(
                                        modifier = Modifier.size(36.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Default.Widgets,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = label,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = pkgName,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                                ) {
                                    Text(
                                        text = "${spanW}×${spanH}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
