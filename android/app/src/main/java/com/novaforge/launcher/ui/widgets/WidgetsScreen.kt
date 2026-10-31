package com.novaforge.launcher.ui.widgets

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novaforge.launcher.data.RealWeatherInfo
import com.novaforge.launcher.data.ScreenTimeSummary
import com.novaforge.launcher.data.model.BatteryInfo
import com.novaforge.launcher.data.model.LauncherWidget
import com.novaforge.launcher.widget.ResizableWidgetContainer

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
    onOpenSettings: () -> Unit = {}
) {
    var isEditMode by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        item {
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
                        onClick = onRequestAddWidget,
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

        // Real Weather Card (Open-Meteo)
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clickable { onOpenSettings() }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(weather.city, color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                        if (weather.isDemoData) {
                            Text("DEMO", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(weather.temperature, color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.Light)
                    Text("${weather.description} • ${weather.tempRange}", color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
                }
            }
        }

        // Real Screen Time & Real Battery Gauge Row
        item {
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
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(screenTime.totalTimeFormatted, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            if (screenTime.isDemoData) {
                                Text(
                                    "DEMO",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Text("Screen Time Today", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (!screenTime.hasPermission) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Grant Access",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable { onRequestUsagePermission() }
                            )
                        }
                    }
                }
            }
        }

        // Edit Mode Explanation Banner
        if (isEditMode) {
            item {
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
            item {
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
                            text = "Tap the + button above to pick Android app widgets",
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
}
