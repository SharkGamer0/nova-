package com.novaforge.launcher.ui.home

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novaforge.launcher.data.RealWeatherInfo
import com.novaforge.launcher.data.model.*
import com.novaforge.launcher.service.MediaPlaybackInfo
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    activeWorkspace: WorkspaceSpace,
    allWorkspaces: List<WorkspaceSpace> = emptyList(),
    weather: RealWeatherInfo,
    batteryInfo: BatteryInfo,
    favorites: List<AppItem>,
    predictiveDockApps: List<AppItem>,
    constellations: List<AppConstellation>,
    allApps: List<AppItem>,
    agendaItems: List<AgendaItem> = emptyList(),
    mediaPlaybackInfo: MediaPlaybackInfo = MediaPlaybackInfo(),
    suggestion: WorkspaceSuggestion?,
    iconAssignments: Map<String, com.novaforge.launcher.icon.IconAssignment> = emptyMap(),
    animations: List<com.novaforge.launcher.icon.AnimatedIconItem> = emptyList(),
    // Customization Preferences
    clockStyle: String = "digital",
    clockSize: String = "large",
    showBattery: Boolean = true,
    showWeather: Boolean = true,
    showCalendarEvent: Boolean = true,
    showMusicHome: Boolean = true,
    musicExpanded: Boolean = false,
    wallpaperDimOverlay: Float = 0.20f,
    homePageCount: Int = 2,
    onAcceptSuggestion: (String) -> Unit,
    onDismissSuggestion: () -> Unit,
    onLaunchApp: (String) -> Unit,
    onOpenCommand: () -> Unit,
    onOpenSpaces: () -> Unit,
    onSwitchWorkspace: (String) -> Unit = {},
    onOpenAppDrawer: () -> Unit,
    onOpenWidgets: () -> Unit,
    onOpenSettings: () -> Unit,
    onPlayPauseMedia: () -> Unit = {},
    onSkipNextMedia: () -> Unit = {},
    onSkipPreviousMedia: () -> Unit = {},
    onToggleMusicExpanded: () -> Unit = {},
    onTriggerContextRing: ((Float, Float, String?, String?) -> Unit)? = null
) {
    val workspaceAccent = try {
        Color(android.graphics.Color.parseColor(activeWorkspace.accentHex))
    } catch (_: Exception) {
        MaterialTheme.colorScheme.primary
    }

    val pagerState = rememberPagerState(pageCount = { homePageCount.coerceIn(1, 4) })

    Box(
        modifier = Modifier
            .fillMaxSize()
            // Optional user-controlled wallpaper dimming overlay (0% by default for natural wallpaper visibility)
            .background(
                if (wallpaperDimOverlay > 0.005f) {
                    Color.Black.copy(alpha = wallpaperDimOverlay.coerceIn(0f, 0.7f))
                } else {
                    Color.Transparent
                }
            )
            .statusBarsPadding()
            // Native gesture activation: Long press anywhere on empty home canvas opens global/workspace Context Ring
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
            Spacer(modifier = Modifier.height(10.dp))

            // Top Status & Spaces Navigation Capsule Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Spaces Capsule Bar (Graceful horizontal scroll of active space pills)
                if (allWorkspaces.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        items(allWorkspaces, key = { it.id }) { ws ->
                            val isCurrent = ws.id == activeWorkspace.id
                            val chipAccent = try {
                                Color(android.graphics.Color.parseColor(ws.accentHex))
                            } catch (_: Exception) {
                                MaterialTheme.colorScheme.primary
                            }

                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = if (isCurrent) {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f)
                                } else {
                                    Color.Transparent
                                },
                                modifier = Modifier.clickable {
                                    if (isCurrent) onOpenSpaces() else onSwitchWorkspace(ws.id)
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(if (isCurrent) chipAccent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = ws.name,
                                        fontSize = 12.sp,
                                        fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isCurrent) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Fallback single workspace capsule
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
                        }
                    }
                }

                // Top Right Utility Icons: Battery (if enabled) + Widgets + Settings
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (showBattery) {
                        Icon(
                            imageVector = if (batteryInfo.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                            contentDescription = "Battery",
                            tint = if (batteryInfo.isCharging) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${batteryInfo.percentage}%",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    IconButton(
                        onClick = onOpenWidgets,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Widgets,
                            contentDescription = "Widgets",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(17.dp)
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
                            modifier = Modifier.size(17.dp)
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
                            .padding(top = 8.dp)
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

            // Multi-Page Horizontal Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                when (page) {
                    0 -> {
                        HomePagePrimary(
                            weather = weather,
                            agendaItems = agendaItems,
                            mediaPlaybackInfo = mediaPlaybackInfo,
                            workspaceAccent = workspaceAccent,
                            activeWorkspace = activeWorkspace,
                            favorites = favorites,
                            constellations = constellations,
                            allApps = allApps,
                            iconAssignments = iconAssignments,
                            animations = animations,
                            clockStyle = clockStyle,
                            clockSize = clockSize,
                            showWeather = showWeather,
                            showCalendarEvent = showCalendarEvent,
                            showMusicHome = showMusicHome,
                            musicExpanded = musicExpanded,
                            onLaunchApp = onLaunchApp,
                            onOpenWidgets = onOpenWidgets,
                            onPlayPauseMedia = onPlayPauseMedia,
                            onSkipNextMedia = onSkipNextMedia,
                            onSkipPreviousMedia = onSkipPreviousMedia,
                            onToggleMusicExpanded = onToggleMusicExpanded,
                            onTriggerContextRing = onTriggerContextRing
                        )
                    }
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
                    else -> {
                        HomePageWidgetsGrid(
                            onOpenWidgets = onOpenWidgets,
                            onTriggerContextRing = onTriggerContextRing,
                            workspaceId = activeWorkspace.id
                        )
                    }
                }
            }

            // Subtle Page Indicator Dots
            if (pagerState.pageCount > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(pagerState.pageCount) { index ->
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
            }

            // Predictive Dock Section (Contextual fast row)
            if (predictiveDockApps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    predictiveDockApps.take(4).forEach { app ->
                        val assignment = iconAssignments[app.packageName]
                        val animItem = if (assignment != null) animations.find { it.id == assignment.animationId } else null
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                .combinedClickable(
                                    onClick = { onLaunchApp(app.packageName) },
                                    onLongClick = {
                                        onTriggerContextRing?.invoke(200f, 400f, app.packageName, activeWorkspace.id)
                                    }
                                )
                                .padding(horizontal = 7.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            com.novaforge.launcher.icon.AnimatedAppIcon(
                                packageName = app.packageName,
                                label = app.label,
                                fallbackColor = Color(app.colorHex),
                                size = 24.dp,
                                shape = RoundedCornerShape(7.dp),
                                assignment = assignment,
                                animationItem = animItem
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = app.label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Compact Minimalist Command Bar Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
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
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Type /command or search...",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                        )
                    }
                }

                IconButton(
                    onClick = onOpenAppDrawer,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
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
 * Isolated Clock and Date composable to prevent full-screen recompositions every second.
 */
@Composable
fun IsolatedClockDisplay(
    clockStyle: String,
    clockSize: String,
    weather: RealWeatherInfo,
    agendaItems: List<AgendaItem>,
    showWeather: Boolean,
    showCalendarEvent: Boolean,
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

    val fontSize = when (clockSize) {
        "compact" -> 44.sp
        "standard" -> 56.sp
        else -> 68.sp
    }

    val (fontFamily, fontWeight, fontStyle, letterSpacing) = when (clockStyle) {
        "minimal" -> Quad(FontFamily.SansSerif, FontWeight.Thin, FontStyle.Normal, 1.sp)
        "bold" -> Quad(FontFamily.SansSerif, FontWeight.Black, FontStyle.Normal, (-2).sp)
        "serif" -> Quad(FontFamily.Serif, FontWeight.Light, FontStyle.Italic, 0.sp)
        "monospace" -> Quad(FontFamily.Monospace, FontWeight.Normal, FontStyle.Normal, 0.sp)
        "cosmic" -> Quad(FontFamily.SansSerif, FontWeight.ExtraLight, FontStyle.Normal, 2.sp)
        else -> Quad(FontFamily.SansSerif, FontWeight.ExtraLight, FontStyle.Normal, (-1).sp)
    }

    val nextEvent = remember(agendaItems) { agendaItems.firstOrNull() }

    Column(modifier = Modifier.padding(top = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = currentTime.ifEmpty { "10:24" },
                fontSize = fontSize,
                fontFamily = fontFamily,
                fontWeight = fontWeight,
                fontStyle = fontStyle,
                letterSpacing = letterSpacing,
                color = if (clockStyle == "cosmic") Color(0xFF38BDF8) else MaterialTheme.colorScheme.onBackground
            )

            if (clockStyle == "cosmic") {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8).copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Text(
            text = currentDate.ifEmpty { "Loading..." },
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (showWeather) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onOpenWidgets() }
                ) {
                    Icon(
                        Icons.Default.WbSunny,
                        contentDescription = "Weather",
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${weather.temperature} ${weather.description}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }

            if (showCalendarEvent && nextEvent != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onOpenWidgets() }
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = "Event",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${nextEvent.title} (${nextEvent.time})",
                        fontSize = 12.sp,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

/**
 * Beautiful compact Media Player widget using native MediaSession APIs
 */
@Composable
fun CompactMusicWidget(
    info: MediaPlaybackInfo,
    isExpanded: Boolean,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onToggleExpanded: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { onToggleExpanded() }
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Album artwork or vinyl cover
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF1E1B4B), Color(0xFF0F172A), Color(0xFF0284C7))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (info.albumArt != null) {
                            Image(
                                bitmap = info.albumArt.asImageBitmap(),
                                contentDescription = "Album Artwork",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = info.title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = info.artist,
                                fontSize = 11.sp,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (info.appLabel != null) {
                                Text(
                                    text = " • ${info.appLabel}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // Playback Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onSkipPrevious,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            Icons.Default.SkipPrevious,
                            contentDescription = "Previous",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onPlayPause,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            imageVector = if (info.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (info.isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onSkipNext,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            Icons.Default.SkipNext,
                            contentDescription = "Next",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Expanded detail controls
            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (info.hasActiveSession) "Active MediaSession" else "Local Ambient Audio",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Tap to collapse",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomePagePrimary(
    weather: RealWeatherInfo,
    agendaItems: List<AgendaItem>,
    mediaPlaybackInfo: MediaPlaybackInfo,
    workspaceAccent: Color,
    activeWorkspace: WorkspaceSpace,
    favorites: List<AppItem>,
    constellations: List<AppConstellation>,
    allApps: List<AppItem>,
    iconAssignments: Map<String, com.novaforge.launcher.icon.IconAssignment>,
    animations: List<com.novaforge.launcher.icon.AnimatedIconItem>,
    clockStyle: String,
    clockSize: String,
    showWeather: Boolean,
    showCalendarEvent: Boolean,
    showMusicHome: Boolean,
    musicExpanded: Boolean,
    onLaunchApp: (String) -> Unit,
    onOpenWidgets: () -> Unit,
    onPlayPauseMedia: () -> Unit,
    onSkipNextMedia: () -> Unit,
    onSkipPreviousMedia: () -> Unit,
    onToggleMusicExpanded: () -> Unit,
    onTriggerContextRing: ((Float, Float, String?, String?) -> Unit)?
) {
    var expandedConstellationId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Isolated Large Minimalist Clock
        IsolatedClockDisplay(
            clockStyle = clockStyle,
            clockSize = clockSize,
            weather = weather,
            agendaItems = agendaItems,
            showWeather = showWeather,
            showCalendarEvent = showCalendarEvent,
            onOpenWidgets = onOpenWidgets
        )

        // Compact Music Widget (if enabled)
        if (showMusicHome) {
            Spacer(modifier = Modifier.height(12.dp))
            CompactMusicWidget(
                info = mediaPlaybackInfo,
                isExpanded = musicExpanded,
                onPlayPause = onPlayPauseMedia,
                onSkipNext = onSkipNextMedia,
                onSkipPrevious = onSkipPreviousMedia,
                onToggleExpanded = onToggleMusicExpanded
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // App Constellations Orbit Row (Clean celestial pills)
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
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isExpanded) 0.65f else 0.4f),
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
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Workspace Essentials / Favorites Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${activeWorkspace.name.uppercase()} ESSENTIALS",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = workspaceAccent,
                letterSpacing = 1.sp
            )

            Text(
                text = "${favorites.size} apps",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            if (favorites.isEmpty()) {
                item {
                    Text(
                        text = "No pinned favorites in this space. Long press any app to assign Context Rings or pin to space.",
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
                            fontSize = 14.sp,
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
                fontSize = 17.sp,
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
                            size = 44.dp,
                            shape = RoundedCornerShape(13.dp),
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

@Composable
fun HomePageWidgetsGrid(
    onOpenWidgets: () -> Unit,
    onTriggerContextRing: ((Float, Float, String?, String?) -> Unit)?,
    workspaceId: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Widgets,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Widgets & Quick Tools",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Tap to manage or add native Android app widgets to this space",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))
            Button(onClick = onOpenWidgets) {
                Text("Open Widget Manager")
            }
        }
    }
}
