package com.novaforge.launcher.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novaforge.launcher.data.RealWeatherInfo
import com.novaforge.launcher.data.model.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
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
    val workspaceAccent = try {
        Color(android.graphics.Color.parseColor(activeWorkspace.accentHex))
    } catch (_: Exception) {
        MaterialTheme.colorScheme.primary
    }

    // Support multiple home screen pages (2 pages by default per workspace)
    val pageCount = 2
    val pagerState = rememberPagerState(pageCount = { pageCount })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            // Gesture-driven Context Ring: Long press on empty wallpaper area opens workspace ring
            .pointerInput(activeWorkspace.id) {
                detectTapGestures(
                    onLongPress = { offset ->
                        onTriggerContextRing?.invoke(offset.x, offset.y, null, activeWorkspace.id)
                    }
                )
            }
            .padding(horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Top Status Bar: Active Workspace Capsule & Battery
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Workspace Capsule Pill (Tapping opens SPACES)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
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

                // Inline Battery & Quick Actions
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

                    Spacer(modifier = Modifier.width(8.dp))

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

            // Adaptive Workspace Suggestion Banner
            AnimatedVisibility(visible = suggestion != null) {
                if (suggestion != null) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
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

            // Horizontal Pager for Multiple Home Screen Pages
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                when (page) {
                    // Page 0: Primary Glance & Essentials
                    0 -> {
                        HomePagePrimary(
                            weather = weather,
                            workspaceAccent = workspaceAccent,
                            activeWorkspace = activeWorkspace,
                            favorites = favorites,
                            constellations = constellations,
                            allApps = allApps,
                            iconAssignments = iconAssignments,
                            animations = animations,
                            onLaunchApp = onLaunchApp,
                            onOpenWidgets = onOpenWidgets,
                            onTriggerContextRing = onTriggerContextRing
                        )
                    }
                    // Page 1: Extended Workspace Apps & Quick Glance Grid
                    1 -> {
                        HomePageExtended(
                            activeWorkspace = activeWorkspace,
                            allApps = allApps,
                            iconAssignments = iconAssignments,
                            animations = animations,
                            workspaceAccent = workspaceAccent,
                            onLaunchApp = onLaunchApp,
                            onTriggerContextRing = onTriggerContextRing
                        )
                    }
                }
            }

            // Page Indicator Dots
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pageCount) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (isSelected) 6.dp else 4.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                            )
                    )
                }
            }

            // Predictive Dock Section
            if (predictiveDockApps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    predictiveDockApps.take(4).forEach { app ->
                        val assignment = iconAssignments[app.packageName]
                        val animItem = if (assignment != null) animations.find { it.id == assignment.animationId } else null
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                                .combinedClickable(
                                    onClick = { onLaunchApp(app.packageName) },
                                    onLongClick = {
                                        onTriggerContextRing?.invoke(200f, 400f, app.packageName, activeWorkspace.id)
                                    }
                                )
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            com.novaforge.launcher.icon.AnimatedAppIcon(
                                packageName = app.packageName,
                                label = app.label,
                                fallbackColor = Color(app.colorHex),
                                size = 26.dp,
                                shape = RoundedCornerShape(8.dp),
                                assignment = assignment,
                                animationItem = animItem
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = app.label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Universal Command Bar Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }

                IconButton(
                    onClick = onOpenAppDrawer,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
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

/**
 * Isolated Clock and Date composable to prevent full-screen recompositions.
 */
@Composable
fun HomeClockAndGlance(
    weather: RealWeatherInfo,
    onOpenWidgets: () -> Unit
) {
    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }

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

    Column {
        Text(
            text = currentTime.ifEmpty { "12:00" },
            fontSize = 58.sp,
            fontWeight = FontWeight.ExtraLight,
            letterSpacing = (-1).sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = currentDate.ifEmpty { "..." },
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${weather.temperature} ${weather.description} • ${weather.city}",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.clickable { onOpenWidgets() }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomePagePrimary(
    weather: RealWeatherInfo,
    workspaceAccent: Color,
    activeWorkspace: WorkspaceSpace,
    favorites: List<AppItem>,
    constellations: List<AppConstellation>,
    allApps: List<AppItem>,
    iconAssignments: Map<String, com.novaforge.launcher.icon.IconAssignment>,
    animations: List<com.novaforge.launcher.icon.AnimatedIconItem>,
    onLaunchApp: (String) -> Unit,
    onOpenWidgets: () -> Unit,
    onTriggerContextRing: ((Float, Float, String?, String?) -> Unit)?
) {
    var expandedConstellationId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // Clock & Date isolated
        HomeClockAndGlance(weather = weather, onOpenWidgets = onOpenWidgets)

        Spacer(modifier = Modifier.height(18.dp))

        // App Constellations Orbit Row
        if (constellations.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
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

            AnimatedVisibility(visible = expandedConstellationId != null) {
                val activeConst = constellations.find { it.id == expandedConstellationId }
                if (activeConst != null) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
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
                                        modifier = Modifier.combinedClickable(
                                            onClick = { onLaunchApp(pkg) },
                                            onLongClick = { onTriggerContextRing?.invoke(200f, 300f, pkg, activeWorkspace.id) }
                                        )
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
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Workspace Essentials Section
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
                        text = "No pinned favorites in this space. Long press any app to assign Context Rings.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                items(favorites, key = { it.packageName }) { app ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { onLaunchApp(app.packageName) },
                                onLongClick = {
                                    onTriggerContextRing?.invoke(200f, 300f, app.packageName, activeWorkspace.id)
                                }
                            )
                            .padding(vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val assignment = iconAssignments[app.packageName]
                        val animItem = if (assignment != null) animations.find { it.id == assignment.animationId } else null
                        com.novaforge.launcher.icon.AnimatedAppIcon(
                            packageName = app.packageName,
                            label = app.label,
                            fallbackColor = Color(app.colorHex),
                            size = 36.dp,
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
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomePageExtended(
    activeWorkspace: WorkspaceSpace,
    allApps: List<AppItem>,
    iconAssignments: Map<String, com.novaforge.launcher.icon.IconAssignment>,
    animations: List<com.novaforge.launcher.icon.AnimatedIconItem>,
    workspaceAccent: Color,
    onLaunchApp: (String) -> Unit,
    onTriggerContextRing: ((Float, Float, String?, String?) -> Unit)?
) {
    // Filter apps corresponding to this workspace
    val targetCategory = when (activeWorkspace.id) {
        "work" -> "Productivity"
        "gaming" -> "Games"
        "study" -> "Tools"
        "travel" -> "Tools"
        "personal" -> "Social"
        else -> null
    }

    val spaceApps = allApps.filter { app ->
        !app.isHidden && (
            activeWorkspace.appPackageNames.contains(app.packageName) ||
            (targetCategory != null && app.category.equals(targetCategory, ignoreCase = true)) ||
            app.isFavorite
        )
    }.distinctBy { it.packageName }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${activeWorkspace.name} Canvas",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "${spaceApps.size} apps in space",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (spaceApps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Swipe back to glance page or open App Drawer",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(spaceApps, key = { it.packageName }) { app ->
                    val assignment = iconAssignments[app.packageName]
                    val animItem = if (assignment != null) animations.find { it.id == assignment.animationId } else null

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { onLaunchApp(app.packageName) },
                                onLongClick = {
                                    onTriggerContextRing?.invoke(200f, 300f, app.packageName, activeWorkspace.id)
                                }
                            )
                    ) {
                        com.novaforge.launcher.icon.AnimatedAppIcon(
                            packageName = app.packageName,
                            label = app.label,
                            fallbackColor = Color(app.colorHex),
                            size = 46.dp,
                            shape = RoundedCornerShape(14.dp),
                            assignment = assignment,
                            animationItem = animItem
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = app.label,
                            fontSize = 11.sp,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }
    }
}
