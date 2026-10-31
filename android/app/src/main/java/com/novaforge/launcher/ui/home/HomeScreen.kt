package com.novaforge.launcher.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.novaforge.launcher.data.model.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    activeWorkspace: WorkspaceSpace,
    weather: RealWeatherInfo,
    batteryInfo: BatteryInfo,
    favorites: List<AppItem>,
    predictiveDockApps: List<AppItem>,
    constellations: List<AppConstellation>,
    allApps: List<AppItem>,
    suggestion: WorkspaceSuggestion?,
    iconAssignments: Map<String, com.novaforge.launcher.icon.IconAssignment> = emptyMap(),
    animations: List<com.novaforge.launcher.icon.AnimatedIconItem> = emptyList(),
    onAcceptSuggestion: (String) -> Unit,
    onDismissSuggestion: () -> Unit,
    onLaunchApp: (String) -> Unit,
    onOpenCommand: () -> Unit,
    onOpenSpaces: () -> Unit,
    onOpenAppDrawer: () -> Unit,
    onOpenWidgets: () -> Unit,
    onOpenSettings: () -> Unit,
    onTriggerContextRing: ((Float, Float, String?, String?) -> Unit)? = null
) {
    // Dynamic real-time clock and date ticking every second
    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }
    var expandedConstellationId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault())
        while (true) {
            val now = Date()
            currentTime = timeFormat.format(now)
            currentDate = dateFormat.format(now)
            delay(1000)
        }
    }

    val workspaceAccent = try {
        Color(android.graphics.Color.parseColor(activeWorkspace.accentHex))
    } catch (_: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // Top Status Bar: Active Workspace Capsule & Battery
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Workspace Capsule Pill (Tapping opens SPACES)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.clickable { onOpenSpaces() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(workspaceAccent)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${activeWorkspace.name} Space",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = "Switch Space",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Inline Battery & Quick Actions (Widgets, Settings)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (batteryInfo.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                        contentDescription = "Battery",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${batteryInfo.percentage}%",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = { onTriggerContextRing?.invoke(200f, 220f, null, activeWorkspace.id) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Context Ring",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onOpenWidgets,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Widgets,
                            contentDescription = "Widgets",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Non-intrusive Adaptive Workspace Suggestion Banner
            AnimatedVisibility(visible = suggestion != null) {
                if (suggestion != null) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Suggestion: Switch to ${suggestion.spaceName}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = suggestion.reason,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextButton(
                                    onClick = { onAcceptSuggestion(suggestion.spaceId) },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Switch", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                IconButton(
                                    onClick = onDismissSuggestion,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Dismiss", modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Minimalist Clock & Date (Grace Launcher Aesthetic)
            Text(
                text = currentTime.ifEmpty { "..." },
                fontSize = 62.sp,
                fontWeight = FontWeight.ExtraLight,
                letterSpacing = (-1).sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = currentDate.ifEmpty { "..." },
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Subtle 1-line glance
            Text(
                text = "${weather.temperature} ${weather.description} • ${weather.city}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                modifier = Modifier.clickable { onOpenWidgets() }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // App Constellations Orbit Row
            if (constellations.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(constellations, key = { it.id }) { const ->
                        val isExpanded = expandedConstellationId == const.id
                        val constAccent = try {
                            Color(android.graphics.Color.parseColor(const.accentHex))
                        } catch (_: Exception) {
                            workspaceAccent
                        }

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier.clickable {
                                expandedConstellationId = if (isExpanded) null else const.id
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(constAccent)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = const.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Expanded Constellation Apps Tray
                AnimatedVisibility(visible = expandedConstellationId != null) {
                    val activeConst = constellations.find { it.id == expandedConstellationId }
                    if (activeConst != null) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "${activeConst.name.uppercase()} ORBIT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    activeConst.packageNames.forEach { pkg ->
                                        val appItem = allApps.find { it.packageName == pkg }
                                        val label = appItem?.label ?: pkg.substringAfterLast('.').replaceFirstChar { it.uppercase() }
                                        val assignment = iconAssignments[pkg]
                                        val animItem = if (assignment != null) animations.find { it.id == assignment.animationId } else null
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.clickable { onLaunchApp(pkg) }
                                        ) {
                                            com.novaforge.launcher.icon.AnimatedAppIcon(
                                                packageName = pkg,
                                                label = label,
                                                fallbackColor = Color(appItem?.colorHex ?: 0xFF2A9D8F.toInt()),
                                                size = 36.dp,
                                                shape = RoundedCornerShape(10.dp),
                                                assignment = assignment,
                                                animationItem = animItem
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = label,
                                                fontSize = 10.sp,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Minimalist Workspace Essentials Section
            Text(
                text = "${activeWorkspace.name.uppercase()} ESSENTIALS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = workspaceAccent,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                if (favorites.isEmpty()) {
                    item {
                        Text(
                            text = "No favorites in this space. Tap the command bar or drawer to launch applications.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                } else {
                    items(favorites, key = { it.packageName }) { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onLaunchApp(app.packageName) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val assignment = iconAssignments[app.packageName]
                            val animItem = if (assignment != null) animations.find { it.id == assignment.animationId } else null
                            com.novaforge.launcher.icon.AnimatedAppIcon(
                                packageName = app.packageName,
                                label = app.label,
                                fallbackColor = Color(app.colorHex),
                                size = 34.dp,
                                shape = RoundedCornerShape(11.dp),
                                assignment = assignment,
                                animationItem = animItem
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            Text(
                                text = app.label,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }

            // Predictive Dock Section
            if (predictiveDockApps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "PREDICTIVE DOCK",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    predictiveDockApps.take(4).forEach { app ->
                        val assignment = iconAssignments[app.packageName]
                        val animItem = if (assignment != null) animations.find { it.id == assignment.animationId } else null
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                .clickable { onLaunchApp(app.packageName) }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            com.novaforge.launcher.icon.AnimatedAppIcon(
                                packageName = app.packageName,
                                label = app.label,
                                fallbackColor = Color(app.colorHex),
                                size = 24.dp,
                                shape = RoundedCornerShape(8.dp),
                                assignment = assignment,
                                animationItem = animItem
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = app.label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Bottom Navigation & Universal Command Bar Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Universal Command Bar Trigger
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .clickable { onOpenCommand() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Terminal,
                            contentDescription = "Command",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Type /command or search...",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }

                // Quick App Drawer Button
                IconButton(
                    onClick = onOpenAppDrawer,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                ) {
                    Icon(
                        Icons.Default.Apps,
                        contentDescription = "App Drawer",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
