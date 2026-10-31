package com.novaforge.launcher.ui.settings

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.res.painterResource
import com.novaforge.launcher.R
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novaforge.launcher.data.RealWeatherInfo
import com.novaforge.launcher.data.ScreenTimeSummary
import com.novaforge.launcher.data.model.AppItem

enum class SettingsSubscreen {
    MAIN,
    HOME_CUSTOMIZATION,
    APPEARANCE,
    ICON_DESIGNER,
    GESTURES,
    FOCUS_MODE,
    HIDDEN_APPS,
    SCREEN_TIME,
    BACKUP_RESTORE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    weather: RealWeatherInfo,
    currentTheme: String,
    currentAccent: String,
    currentIconShape: String,
    currentIconStyle: String,
    focusModeEnabled: Boolean,
    focusWhitelist: Set<String>,
    allApps: List<AppItem>,
    screenTime: ScreenTimeSummary,
    clockStyle: String = "digital",
    clockSize: String = "large",
    showBattery: Boolean = true,
    showWeather: Boolean = true,
    showCalendarEvent: Boolean = true,
    showMusicHome: Boolean = true,
    showMusicDrawer: Boolean = false,
    showAlphabetIndex: Boolean = true,
    wallpaperDimOverlay: Float = 0.20f,
    hapticsEnabled: Boolean = true,
    homePageCount: Int = 2,
    onSaveLocation: (Double, Double, String) -> Unit,
    onSetTheme: (String) -> Unit,
    onSetAccent: (String) -> Unit,
    onSetIconShape: (String) -> Unit,
    onSetIconStyle: (String) -> Unit,
    onToggleFocusMode: (Boolean) -> Unit,
    onUpdateFocusWhitelist: (Set<String>) -> Unit,
    onUnhideApp: (String) -> Unit,
    onRequestUsagePermission: () -> Unit,
    onExportBackup: () -> String,
    onRestoreBackup: (String) -> Boolean,
    onSetClockStyle: (String) -> Unit = {},
    onSetClockSize: (String) -> Unit = {},
    onSetShowBattery: (Boolean) -> Unit = {},
    onSetShowWeather: (Boolean) -> Unit = {},
    onSetShowCalendarEvent: (Boolean) -> Unit = {},
    onSetShowMusicHome: (Boolean) -> Unit = {},
    onSetShowMusicDrawer: (Boolean) -> Unit = {},
    onSetShowAlphabetIndex: (Boolean) -> Unit = {},
    onSetWallpaperDimOverlay: (Float) -> Unit = {},
    onSetHapticsEnabled: (Boolean) -> Unit = {},
    onSetHomePageCount: (Int) -> Unit = {},
    onOpenAutomations: () -> Unit = {},
    onOpenAnimatedIcons: () -> Unit = {},
    onOpenContextRings: () -> Unit = {},
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var currentSubscreen by remember { mutableStateOf(SettingsSubscreen.MAIN) }
    var showLocationDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (currentSubscreen) {
                            SettingsSubscreen.MAIN -> "Launcher Settings"
                            SettingsSubscreen.HOME_CUSTOMIZATION -> "Home Screen & Glance"
                            SettingsSubscreen.APPEARANCE -> "Appearance & Theme"
                            SettingsSubscreen.ICON_DESIGNER -> "Icon Designer"
                            SettingsSubscreen.GESTURES -> "Gesture Navigation"
                            SettingsSubscreen.FOCUS_MODE -> "Focus Mode"
                            SettingsSubscreen.HIDDEN_APPS -> "Hidden Applications"
                            SettingsSubscreen.SCREEN_TIME -> "Screen Time Dashboard"
                            SettingsSubscreen.BACKUP_RESTORE -> "Backup & Restore"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentSubscreen == SettingsSubscreen.MAIN) {
                            onBack()
                        } else {
                            currentSubscreen = SettingsSubscreen.MAIN
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (currentSubscreen) {
                SettingsSubscreen.MAIN -> {
                    MainSettingsList(
                        weather = weather,
                        onOpenLocation = { showLocationDialog = true },
                        onNavigate = { currentSubscreen = it },
                        onOpenWallpaperPicker = {
                            try {
                                val intent = Intent(Intent.ACTION_SET_WALLPAPER)
                                context.startActivity(Intent.createChooser(intent, "Select Wallpaper"))
                            } catch (_: Exception) {
                                Toast.makeText(context, "Wallpaper picker not available on this device", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onOpenAutomations = onOpenAutomations,
                        onOpenAnimatedIcons = onOpenAnimatedIcons
                    )
                }
                SettingsSubscreen.HOME_CUSTOMIZATION -> {
                    HomeCustomizationSubscreen(
                        clockStyle = clockStyle,
                        clockSize = clockSize,
                        showBattery = showBattery,
                        showWeather = showWeather,
                        showCalendarEvent = showCalendarEvent,
                        showMusicHome = showMusicHome,
                        showMusicDrawer = showMusicDrawer,
                        showAlphabetIndex = showAlphabetIndex,
                        wallpaperDimOverlay = wallpaperDimOverlay,
                        hapticsEnabled = hapticsEnabled,
                        homePageCount = homePageCount,
                        onSetClockStyle = onSetClockStyle,
                        onSetClockSize = onSetClockSize,
                        onSetShowBattery = onSetShowBattery,
                        onSetShowWeather = onSetShowWeather,
                        onSetShowCalendarEvent = onSetShowCalendarEvent,
                        onSetShowMusicHome = onSetShowMusicHome,
                        onSetShowMusicDrawer = onSetShowMusicDrawer,
                        onSetShowAlphabetIndex = onSetShowAlphabetIndex,
                        onSetWallpaperDimOverlay = onSetWallpaperDimOverlay,
                        onSetHapticsEnabled = onSetHapticsEnabled,
                        onSetHomePageCount = onSetHomePageCount
                    )
                }
                SettingsSubscreen.APPEARANCE -> {
                    AppearanceSettingsSubscreen(
                        currentTheme = currentTheme,
                        currentAccent = currentAccent,
                        onSetTheme = onSetTheme,
                        onSetAccent = onSetAccent
                    )
                }
                SettingsSubscreen.ICON_DESIGNER -> {
                    IconDesignerSubscreen(
                        currentShape = currentIconShape,
                        currentStyle = currentIconStyle,
                        onSetShape = onSetIconShape,
                        onSetStyle = onSetStyle
                    )
                }
                SettingsSubscreen.GESTURES -> {
                    GesturesSubscreen()
                }
                SettingsSubscreen.FOCUS_MODE -> {
                    FocusModeSubscreen(
                        focusModeEnabled = focusModeEnabled,
                        focusWhitelist = focusWhitelist,
                        allApps = allApps,
                        onToggleFocus = onToggleFocusMode,
                        onUpdateWhitelist = onUpdateFocusWhitelist
                    )
                }
                SettingsSubscreen.HIDDEN_APPS -> {
                    HiddenAppsSubscreen(
                        hiddenApps = allApps.filter { it.isHidden },
                        onUnhideApp = onUnhideApp
                    )
                }
                SettingsSubscreen.SCREEN_TIME -> {
                    ScreenTimeSubscreen(
                        screenTime = screenTime,
                        onRequestPermission = onRequestUsagePermission
                    )
                }
                SettingsSubscreen.BACKUP_RESTORE -> {
                    BackupRestoreSubscreen(
                        onExport = onExportBackup,
                        onRestore = onRestoreBackup
                    )
                }
            }
        }
    }

    if (showLocationDialog) {
        LocationCoordinatesDialog(
            currentCity = weather.city,
            onDismiss = { showLocationDialog = false },
            onConfirm = { lat, lon, city ->
                onSaveLocation(lat, lon, city)
                showLocationDialog = false
            }
        )
    }
}

@Composable
private fun MainSettingsList(
    weather: RealWeatherInfo,
    onOpenLocation: () -> Unit,
    onNavigate: (SettingsSubscreen) -> Unit,
    onOpenWallpaperPicker: () -> Unit,
    onOpenAutomations: () -> Unit,
    onOpenAnimatedIcons: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        item {
            // Official NovaForge Cosmic Brand Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.novaforge_logo),
                        contentDescription = "NovaForge Launcher Logo",
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "NovaForge Launcher",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "v1.2.0 Cosmic Orbit Edition",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Minimalist Space • Local-First • 100% Free",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            // Weather Location Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { onOpenLocation() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Weather Location", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("${weather.city} (${weather.temperature})", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Text("Change", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                }
            }
        }

        val menuItems = listOf(
            Triple("Home Screen & Glance", "Clock style, media player, wallpaper dimming & info toggles", SettingsSubscreen.HOME_CUSTOMIZATION),
            Triple("Appearance & Theme", "Off-white, charcoal dark, monochrome & accents", SettingsSubscreen.APPEARANCE),
            Triple("Context Rings", "Radial command menu, custom actions & gesture bindings", null),
            Triple("Animated Icon Engine", "Custom GIF, WebP & Lottie animations with battery saver", null),
            Triple("Icon Designer", "Squircle, circle, pebble shapes & outline tints", SettingsSubscreen.ICON_DESIGNER),
            Triple("Wallpaper", "Set system or lock screen wallpaper", null),
            Triple("Intelligence & Automations", "Command macros, IF/THEN rules & constellations", null),
            Triple("Gestures", "Swipe and double-tap actions", SettingsSubscreen.GESTURES),
            Triple("Focus Mode", "Reduce distractions with whitelisted apps", SettingsSubscreen.FOCUS_MODE),
            Triple("Hidden Apps", "PIN protected applications vault", SettingsSubscreen.HIDDEN_APPS),
            Triple("Screen Time Dashboard", "Real app usage metrics via UsageStatsManager", SettingsSubscreen.SCREEN_TIME),
            Triple("Backup & Restore", "Export and import your launcher layout", SettingsSubscreen.BACKUP_RESTORE)
        )

        items(menuItems) { (title, subtitle, subscreen) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (subscreen != null) {
                            onNavigate(subscreen)
                        } else if (title.contains("Context Rings")) {
                            onOpenContextRings()
                        } else if (title.contains("Animated Icon")) {
                            onOpenAnimatedIcons()
                        } else if (title.contains("Intelligence")) {
                            onOpenAutomations()
                        } else {
                            onOpenWallpaperPicker()
                        }
                    }
                    .padding(vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "NovaForge Launcher Native",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Clean Minimalist Cosmic Architecture • 100% Free",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun AppearanceSettingsSubscreen(
    currentTheme: String,
    currentAccent: String,
    onSetTheme: (String) -> Unit,
    onSetAccent: (String) -> Unit
) {
    val themes = listOf(
        "cosmic-dark" to "Cosmic Dark (Official near-black & midnight blue)",
        "charcoal-dark" to "Charcoal Dark (Deep matte night theme)",
        "monochrome" to "Monochrome (High contrast minimalist)",
        "warm-light" to "Warm Light (Default Grace aesthetic)"
    )

    val accents = listOf(
        "#38BDF8" to "Cosmic Cyan",
        "#6366F1" to "Stellar Indigo",
        "#2A9D8F" to "Muted Teal",
        "#E76F51" to "Burnt Amber",
        "#4A5568" to "Slate Grey"
    )

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text("Theme Mode", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))

        themes.forEach { (mode, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSetTheme(mode) }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = currentTheme == mode, onClick = { onSetTheme(mode) })
                Spacer(modifier = Modifier.width(10.dp))
                Text(label, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Accent Color", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            accents.forEach { (hex, name) ->
                val isSelected = currentAccent.equals(hex, ignoreCase = true)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onSetAccent(hex) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(hex)))
                            .then(
                                if (isSelected) Modifier.clip(CircleShape) else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(name, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun IconDesignerSubscreen(
    currentShape: String,
    currentStyle: String,
    onSetShape: (String) -> Unit,
    onSetStyle: (String) -> Unit
) {
    val shapes = listOf("squircle", "circle", "rounded-square", "pebble")
    val styles = listOf("default", "monochrome", "outline")

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text("Icon Shape", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            shapes.forEach { shape ->
                val isSelected = currentShape == shape
                Button(
                    onClick = { onSetShape(shape) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        shape.replaceFirstChar { it.uppercase() },
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Icon Tint Style", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            styles.forEach { style ->
                val isSelected = currentStyle == style
                Button(
                    onClick = { onSetStyle(style) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        style.replaceFirstChar { it.uppercase() },
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun GesturesSubscreen() {
    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text("Configured Gestures", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(12.dp))

        val gestureList = listOf(
            "Swipe Up" to "Opens App Drawer",
            "Swipe Down" to "Opens Widgets & Glance",
            "Double Tap" to "Lock Screen (Requires Device Admin / Accessibility Service on Android)"
        )

        gestureList.forEach { (gesture, action) ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(gesture, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(action, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun FocusModeSubscreen(
    focusModeEnabled: Boolean,
    focusWhitelist: Set<String>,
    allApps: List<AppItem>,
    onToggleFocus: (Boolean) -> Unit,
    onUpdateWhitelist: (Set<String>) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Enable Focus Mode", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text("Hides non-whitelisted apps during sessions", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = focusModeEnabled, onCheckedChange = onToggleFocus)
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text("Allowed Apps During Focus", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(allApps) { app ->
                val isWhitelisted = focusWhitelist.contains(app.packageName)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val newSet = if (isWhitelisted) focusWhitelist - app.packageName else focusWhitelist + app.packageName
                            onUpdateWhitelist(newSet)
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isWhitelisted,
                        onCheckedChange = { checked ->
                            val newSet = if (checked) focusWhitelist + app.packageName else focusWhitelist - app.packageName
                            onUpdateWhitelist(newSet)
                        }
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(app.label, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun HiddenAppsSubscreen(
    hiddenApps: List<AppItem>,
    onUnhideApp: (String) -> Unit
) {
    var isAuthenticated by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    val correctPin = "1234"

    if (!isAuthenticated) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text("Hidden Apps Vault", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Text("Enter PIN to access hidden apps (Default: 1234)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = enteredPin,
                onValueChange = { enteredPin = it },
                label = { Text("4-digit PIN") },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (enteredPin == correctPin) {
                        isAuthenticated = true
                    }
                },
                enabled = enteredPin.length == 4
            ) {
                Text("Unlock")
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            Text("Hidden Applications (${hiddenApps.size})", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))

            if (hiddenApps.isEmpty()) {
                Text("No applications are hidden.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(hiddenApps) { app ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(app.label, fontSize = 14.sp)
                            TextButton(onClick = { onUnhideApp(app.packageName) }) {
                                Text("Unhide")
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}

@Composable
private fun ScreenTimeSubscreen(
    screenTime: ScreenTimeSummary,
    onRequestPermission: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("Today's Total Screen Time", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(screenTime.totalTimeFormatted, fontSize = 36.sp, fontWeight = FontWeight.Light)
                if (screenTime.isDemoData) {
                    Text("Showing demonstration values. Tap below to grant real UsageStats permission.", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (!screenTime.hasPermission) {
            Button(
                onClick = onRequestPermission,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Grant Usage Access Permission")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text("Top App Usage", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(10.dp))

        screenTime.topAppUsage.forEach { (appName, usageTime) ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(appName, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text(usageTime, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        }
    }
}

@Composable
private fun BackupRestoreSubscreen(
    onExport: () -> String,
    onRestore: (String) -> Boolean
) {
    val context = LocalContext.current
    var backupJson by remember { mutableStateOf("") }
    var restoreInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text("Backup Configuration", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                backupJson = onExport()
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("NovaForge Backup", backupJson))
                Toast.makeText(context, "Backup copied to clipboard", Toast.LENGTH_SHORT).show()
            }
        ) {
            Text("Export & Copy Backup JSON")
        }

        if (backupJson.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = backupJson,
                    fontSize = 11.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Restore Configuration", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = restoreInput,
            onValueChange = { restoreInput = it },
            label = { Text("Paste Backup JSON here") },
            modifier = Modifier.fillMaxWidth().height(120.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                val success = onRestore(restoreInput)
                if (success) {
                    Toast.makeText(context, "Settings restored successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to restore: Invalid JSON", Toast.LENGTH_SHORT).show()
                }
            },
            enabled = restoreInput.isNotEmpty()
        ) {
            Text("Restore From JSON")
        }
    }
}

@Composable
private fun LocationCoordinatesDialog(
    currentCity: String,
    onDismiss: () -> Unit,
    onConfirm: (Double, Double, String) -> Unit
) {
    var cityInput by remember { mutableStateOf(currentCity) }
    var latInput by remember { mutableStateOf("28.6139") }
    var lonInput by remember { mutableStateOf("77.2090") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Weather Coordinates") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Enter location for live Open-Meteo weather forecast:", fontSize = 12.sp)
                OutlinedTextField(
                    value = cityInput,
                    onValueChange = { cityInput = it },
                    label = { Text("City Name") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = latInput,
                    onValueChange = { latInput = it },
                    label = { Text("Latitude (e.g. 37.7749)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = lonInput,
                    onValueChange = { lonInput = it },
                    label = { Text("Longitude (e.g. -122.4194)") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val lat = latInput.toDoubleOrNull() ?: 28.6139
                    val lon = lonInput.toDoubleOrNull() ?: 77.2090
                    onConfirm(lat, lon, cityInput.ifEmpty { "Custom Location" })
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun HomeCustomizationSubscreen(
    clockStyle: String,
    clockSize: String,
    showBattery: Boolean,
    showWeather: Boolean,
    showCalendarEvent: Boolean,
    showMusicHome: Boolean,
    showMusicDrawer: Boolean,
    showAlphabetIndex: Boolean,
    wallpaperDimOverlay: Float,
    hapticsEnabled: Boolean,
    homePageCount: Int,
    onSetClockStyle: (String) -> Unit,
    onSetClockSize: (String) -> Unit,
    onSetShowBattery: (Boolean) -> Unit,
    onSetShowWeather: (Boolean) -> Unit,
    onSetShowCalendarEvent: (Boolean) -> Unit,
    onSetShowMusicHome: (Boolean) -> Unit,
    onSetShowMusicDrawer: (Boolean) -> Unit,
    onSetShowAlphabetIndex: (Boolean) -> Unit,
    onSetWallpaperDimOverlay: (Float) -> Unit,
    onSetHapticsEnabled: (Boolean) -> Unit,
    onSetHomePageCount: (Int) -> Unit
) {
    val clockStyles = listOf(
        "digital" to "Digital (Clean sans-serif tall digits)",
        "minimal" to "Minimal (Thin airy display)",
        "bold" to "Impact (Heavy display numerals)",
        "serif" to "Editorial (Refined classic serif)",
        "monospace" to "Monospace (Tech command terminal)",
        "cosmic" to "Cosmic Orbit (Cyan celestial accent)"
    )

    val clockSizes = listOf("compact" to "Compact", "standard" to "Standard", "large" to "Large")
    val pageCounts = listOf(1, 2, 3, 4)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Clock Typography Style", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            clockStyles.forEach { (style, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSetClockStyle(style) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = clockStyle == style, onClick = { onSetClockStyle(style) })
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(label, fontSize = 13.sp)
                }
            }
        }

        item {
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Clock Size", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                clockSizes.forEach { (size, label) ->
                    val isSelected = clockSize == size
                    Button(
                        onClick = { onSetClockSize(size) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(label, fontSize = 12.sp, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }

        item {
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Wallpaper Readability Dimming (${(wallpaperDimOverlay * 100).toInt()}%)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(
                "Subtle translucent darkening overlay to guarantee text legibility over any wallpaper without hiding it.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Slider(
                value = wallpaperDimOverlay,
                onValueChange = { onSetWallpaperDimOverlay(it) },
                valueRange = 0f..0.70f,
                steps = 14
            )
        }

        item {
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Glance & Information Elements", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            ToggleOptionRow(
                title = "Battery Percentage & Status",
                subtitle = "Display live battery charge in header row",
                checked = showBattery,
                onCheckedChange = onSetShowBattery
            )

            ToggleOptionRow(
                title = "Live Weather Glance",
                subtitle = "Display temperature and conditions under clock",
                checked = showWeather,
                onCheckedChange = onSetShowWeather
            )

            ToggleOptionRow(
                title = "Calendar Events & Schedule",
                subtitle = "Show upcoming calendar events under clock",
                checked = showCalendarEvent,
                onCheckedChange = onSetShowCalendarEvent
            )

            ToggleOptionRow(
                title = "Compact Music Widget on Home",
                subtitle = "Show MediaSession music player widget on Home Screen",
                checked = showMusicHome,
                onCheckedChange = onSetShowMusicHome
            )

            ToggleOptionRow(
                title = "Compact Music Widget in App Drawer",
                subtitle = "Show MediaSession player at top of App Drawer",
                checked = showMusicDrawer,
                onCheckedChange = onSetShowMusicDrawer
            )

            ToggleOptionRow(
                title = "A–Z Fast Index Rail",
                subtitle = "Continuous drag scrub vertical alphabet index on App Drawer edge",
                checked = showAlphabetIndex,
                onCheckedChange = onSetShowAlphabetIndex
            )

            ToggleOptionRow(
                title = "Haptic Vibration Feedback",
                subtitle = "Haptics on Context Ring selections and gestures",
                checked = hapticsEnabled,
                onCheckedChange = onSetHapticsEnabled
            )
        }

        item {
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Home Screen Pages Count", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                pageCounts.forEach { count ->
                    val isSelected = homePageCount == count
                    Button(
                        onClick = { onSetHomePageCount(count) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("$count ${if (count == 1) "Page" else "Pages"}", fontSize = 12.sp, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun ToggleOptionRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

