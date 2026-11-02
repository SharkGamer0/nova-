package com.novaforge.launcher.ui

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.novaforge.launcher.ui.applist.AppListScreen
import com.novaforge.launcher.ui.command.CommandScreen
import com.novaforge.launcher.ui.home.HomeScreen
import com.novaforge.launcher.ui.intelligence.AutomationScreen
import com.novaforge.launcher.ui.settings.SettingsScreen
import com.novaforge.launcher.ui.spaces.SpacesScreen
import com.novaforge.launcher.ui.splash.LauncherSplashScreen
import com.novaforge.launcher.ui.widgets.WidgetsScreen

@Composable
fun LauncherRootScreen(
    viewModel: LauncherViewModel,
    appWidgetHost: AppWidgetHost,
    appWidgetManager: AppWidgetManager,
    onRequestAddWidget: () -> Unit,
    onRequestCalendarPermission: () -> Unit,
    onRequestUsagePermission: () -> Unit,
    onDeleteWidgetId: (Int) -> Unit,
    onLaunchApp: (String) -> Unit,
    onOpenAppInfo: (String) -> Unit,
    onRequestUninstall: (String) -> Unit,
    onOpenSystemSetting: (String) -> Unit,
    onOpenSettings: () -> Unit
) {
    viewModel.onLaunchAppFromMacro = onLaunchApp

    val currentScreen by viewModel.currentScreen.collectAsState()
    val apps by viewModel.installedApps.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val commandQuery by viewModel.commandQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val weather by viewModel.weatherState.collectAsState()
    val screenTime by viewModel.screenTime.collectAsState()
    val batteryInfo by viewModel.batteryState.collectAsState()
    val widgets by viewModel.widgets.collectAsState()

    val currentTheme by viewModel.themeMode.collectAsState()
    val currentAccent by viewModel.accentColor.collectAsState()
    val currentIconShape by viewModel.iconShape.collectAsState()
    val currentIconStyle by viewModel.iconStyle.collectAsState()
    val focusModeEnabled by viewModel.focusModeEnabled.collectAsState()
    val focusWhitelist by viewModel.focusWhitelist.collectAsState()

    val workspaces by viewModel.workspaces.collectAsState()
    val activeWorkspaceId by viewModel.activeWorkspaceId.collectAsState()
    val activeWorkspace by viewModel.activeWorkspace.collectAsState()

    val commandMacros by viewModel.commandMacros.collectAsState()
    val appConstellations by viewModel.appConstellations.collectAsState()
    val automationRules by viewModel.automationRules.collectAsState()
    val automationsEnabled by viewModel.automationsEnabled.collectAsState()
    val favoriteCommands by viewModel.favoriteCommands.collectAsState()
    val recentCommands by viewModel.recentCommands.collectAsState()
    val activeSuggestion by viewModel.activeSuggestion.collectAsState()

    val animations by viewModel.animations.collectAsState()
    val animationCollections by viewModel.animationCollections.collectAsState()
    val iconAssignments by viewModel.iconAssignments.collectAsState()
    val engineSettings by viewModel.engineSettings.collectAsState()

    val contextRingConfig by viewModel.contextRingConfig.collectAsState()
    val activeRingSession by viewModel.activeRingSession.collectAsState()

    // Redesign Preferences
    val clockStyle by viewModel.clockStyle.collectAsState()
    val clockSize by viewModel.clockSize.collectAsState()
    val showBattery by viewModel.showBattery.collectAsState()
    val showWeather by viewModel.showWeather.collectAsState()
    val showCalendarEvent by viewModel.showCalendarEvent.collectAsState()
    val showMusicHome by viewModel.showMusicHome.collectAsState()
    val showMusicDrawer by viewModel.showMusicDrawer.collectAsState()
    val musicExpanded by viewModel.musicExpanded.collectAsState()
    val showAlphabetIndex by viewModel.showAlphabetIndex.collectAsState()
    val wallpaperDimOverlay by viewModel.wallpaperDimOverlay.collectAsState()
    val hapticsEnabled by viewModel.hapticsEnabled.collectAsState()
    val homePageCount by viewModel.homePageCount.collectAsState()
    val mediaPlaybackInfo by viewModel.mediaPlaybackInfo.collectAsState()
    val agendaItems by viewModel.agendaItems.collectAsState()

    var showAssignmentDialog by remember { androidx.compose.runtime.mutableStateOf(false) }
    var assignmentDialogTargetApp by remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
    var assignmentDialogTargetAnim by remember { androidx.compose.runtime.mutableStateOf<com.novaforge.launcher.icon.AnimatedIconItem?>(null) }
    var showSplash by remember { androidx.compose.runtime.mutableStateOf(true) }

    // Filter visible apps: Exclude hidden apps from normal drawer
    val visibleApps = apps.filter { app ->
        if (app.isHidden) return@filter false
        if (focusModeEnabled && focusWhitelist.isNotEmpty() && !focusWhitelist.contains(app.packageName)) {
            return@filter false
        }
        if (selectedCategory != "All" && !app.category.equals(selectedCategory, ignoreCase = true)) {
            return@filter false
        }
        if (searchQuery.isNotEmpty()) {
            return@filter app.label.contains(searchQuery, ignoreCase = true)
        }
        true
    }

    val groupedApps = visibleApps
        .groupBy { it.label.firstOrNull()?.uppercaseChar() ?: '#' }
        .toSortedMap()

    // Recent apps
    val recentApps = visibleApps.filter { it.launchCount > 0 }.sortedByDescending { it.launchCount }.take(4)

    // Workspace-adaptive favorites (Workspace Memory)
    val workspaceFavorites = remember(apps, activeWorkspace) {
        val targetCategory = when (activeWorkspace.id) {
            "work" -> "Productivity"
            "gaming" -> "Games"
            "study" -> "Tools"
            "travel" -> "Tools"
            "personal" -> "Social"
            else -> null
        }

        val pinned = apps.filter { it.isFavorite && !it.isHidden }
        val categoryMatches = if (targetCategory != null) {
            apps.filter { it.category.equals(targetCategory, ignoreCase = true) && !it.isHidden }
        } else emptyList()

        (pinned + categoryMatches + apps.filter { !it.isHidden && it.launchCount > 0 }.sortedByDescending { it.launchCount })
            .distinctBy { it.packageName }
            .take(6)
    }

    // Predictive Dock apps: Contextual recommendations based on usage frequency and workspace
    val predictiveDockApps = remember(apps, activeWorkspace) {
        apps.filter { !it.isHidden }
            .sortedByDescending { it.launchCount }
            .take(4)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
            when (screen) {
                // 0: App Drawer (Full alphabetical listing)
                0 -> AppListScreen(
                    appsGrouped = groupedApps,
                    recentApps = recentApps,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                    selectedCategory = selectedCategory,
                    onSelectCategory = { viewModel.selectCategory(it) },
                    onLaunchApp = { pkg ->
                        viewModel.recordAppLaunch(pkg)
                        onLaunchApp(pkg)
                    },
                    onToggleFavorite = { pkg -> viewModel.toggleFavorite(pkg) },
                    onHideApp = { pkg -> viewModel.setAppHidden(pkg, true) },
                    onOpenAppInfo = onOpenAppInfo,
                    onRequestUninstall = onRequestUninstall,
                    iconAssignments = iconAssignments,
                    animations = animations,
                    onAssignIcon = { pkg ->
                        assignmentDialogTargetApp = pkg
                        assignmentDialogTargetAnim = null
                        showAssignmentDialog = true
                    },
                    showAlphabetIndex = showAlphabetIndex,
                    showMusicDrawer = showMusicDrawer,
                    mediaPlaybackInfo = mediaPlaybackInfo,
                    onPlayPauseMedia = { viewModel.playPauseMedia() },
                    onSkipNextMedia = { viewModel.skipNextMedia() },
                    onSkipPreviousMedia = { viewModel.skipPreviousMedia() },
                    onTriggerContextRing = { x, y, pkg, _ -> viewModel.triggerContextRing(x, y, pkg, null) }
                )

                // 1: HOME (Minimalist Workspace with Workspace Memory, Predictive Dock, Adaptive Suggestions & Constellations)
                1 -> HomeScreen(
                    activeWorkspace = activeWorkspace,
                    allWorkspaces = workspaces,
                    weather = weather,
                    batteryInfo = batteryInfo,
                    favorites = workspaceFavorites,
                    predictiveDockApps = predictiveDockApps,
                    constellations = appConstellations,
                    allApps = apps,
                    agendaItems = agendaItems,
                    mediaPlaybackInfo = mediaPlaybackInfo,
                    suggestion = activeSuggestion,
                    iconAssignments = iconAssignments,
                    animations = animations,
                    clockStyle = clockStyle,
                    clockSize = clockSize,
                    showBattery = showBattery,
                    showWeather = showWeather,
                    showCalendarEvent = showCalendarEvent,
                    showMusicHome = showMusicHome,
                    musicExpanded = musicExpanded,
                    wallpaperDimOverlay = wallpaperDimOverlay,
                    homePageCount = homePageCount,
                    onAcceptSuggestion = { spaceId ->
                        if (spaceId == "battery") {
                            onOpenSystemSetting("battery")
                            viewModel.dismissSuggestion()
                        } else {
                            viewModel.acceptSuggestion(spaceId)
                        }
                    },
                    onDismissSuggestion = { viewModel.dismissSuggestion() },
                    onLaunchApp = { pkg ->
                        viewModel.recordAppLaunch(pkg)
                        onLaunchApp(pkg)
                    },
                    onOpenCommand = { viewModel.navigateToCommand() },
                    onOpenSpaces = { viewModel.navigateToSpaces() },
                    onSwitchWorkspace = { id -> viewModel.switchWorkspace(id) },
                    onOpenAppDrawer = { viewModel.navigateToAppDrawer() },
                    onOpenWidgets = { viewModel.navigateToWidgets() },
                    onOpenSettings = { viewModel.openSettings() },
                    onPlayPauseMedia = { viewModel.playPauseMedia() },
                    onSkipNextMedia = { viewModel.skipNextMedia() },
                    onSkipPreviousMedia = { viewModel.skipPreviousMedia() },
                    onToggleMusicExpanded = { viewModel.setMusicExpanded(!musicExpanded) },
                    onTriggerContextRing = { x, y, pkg, ws -> viewModel.triggerContextRing(x, y, pkg, ws) }
                )

                // 2: COMMAND (Advanced Command Search, Macros, History & System Shortcuts)
                2 -> CommandScreen(
                    query = commandQuery,
                    onQueryChange = { viewModel.updateCommandQuery(it) },
                    apps = apps.filter { !it.isHidden },
                    commands = viewModel.universalCommands,
                    macros = commandMacros,
                    recentCommands = recentCommands,
                    favoriteCommands = favoriteCommands,
                    onToggleFavoriteCommand = { cmd -> viewModel.toggleFavoriteCommand(cmd) },
                    onExecuteCommand = { cmd ->
                        viewModel.recordCommandUsage(cmd)
                        when (cmd) {
                            "/personal" -> viewModel.switchWorkspace("personal")
                            "/work" -> viewModel.switchWorkspace("work")
                            "/study" -> viewModel.switchWorkspace("study")
                            "/gaming" -> viewModel.switchWorkspace("gaming")
                            "/travel" -> viewModel.switchWorkspace("travel")
                            "/spaces" -> viewModel.navigateToSpaces()
                            "/automations" -> viewModel.openAutomations()
                            "/widgets" -> viewModel.navigateToWidgets()
                            "/apps" -> viewModel.navigateToAppDrawer()
                            "/settings", "/about" -> viewModel.openSettings()
                            "/icons", "/animated-icons" -> viewModel.openAnimatedIconLibrary()
                            "/rings", "/context-rings" -> viewModel.openContextRingDesigner()
                            "/focus" -> viewModel.setFocusMode(!focusModeEnabled)
                            "/wifi" -> onOpenSystemSetting("wifi")
                            "/bluetooth" -> onOpenSystemSetting("bluetooth")
                            "/battery" -> onOpenSystemSetting("battery")
                            "/display" -> onOpenSystemSetting("display")
                            "/wallpaper" -> onOpenSystemSetting("wallpaper")
                            "/lock" -> onOpenSystemSetting("lock")
                            else -> viewModel.navigateToHome()
                        }
                    },
                    onExecuteMacro = { macro -> viewModel.executeMacro(macro) },
                    onLaunchApp = { pkg ->
                        viewModel.recordAppLaunch(pkg)
                        onLaunchApp(pkg)
                    },
                    iconAssignments = iconAssignments,
                    animations = animations,
                    onBack = { viewModel.navigateToHome() }
                )

                // 3: SPACES (Workspace capsules)
                3 -> SpacesScreen(
                    workspaces = workspaces,
                    activeWorkspaceId = activeWorkspaceId,
                    onSelectWorkspace = { id -> viewModel.switchWorkspace(id) },
                    onCreateWorkspace = { name, desc, accent -> viewModel.createWorkspace(name, desc, accent) },
                    onOpenAutomations = { viewModel.openAutomations() },
                    onBack = { viewModel.navigateToHome() }
                )

                // 4: WIDGETS (Native hosted Android widgets)
                4 -> WidgetsScreen(
                    weather = weather,
                    batteryInfo = batteryInfo,
                    screenTime = screenTime,
                    widgets = widgets,
                    appWidgetHost = appWidgetHost,
                    appWidgetManager = appWidgetManager,
                    onRequestAddWidget = onRequestAddWidget,
                    onRequestUsagePermission = onRequestUsagePermission,
                    onResizeWidgetWidth = { id, w -> viewModel.resizeWidgetWidth(id, w) },
                    onResizeWidgetHeight = { id, h -> viewModel.resizeWidgetHeight(id, h) },
                    onMoveWidget = { id, dir -> viewModel.moveWidget(id, dir) },
                    onRemoveWidget = { id -> viewModel.removeWidget(id, onDeleteWidgetId) },
                    onOpenSettings = { viewModel.openSettings() }
                )

                // 5: SETTINGS (Launcher preferences)
                5 -> SettingsScreen(
                    weather = weather,
                    currentTheme = currentTheme,
                    currentAccent = currentAccent,
                    currentIconShape = currentIconShape,
                    currentIconStyle = currentIconStyle,
                    focusModeEnabled = focusModeEnabled,
                    focusWhitelist = focusWhitelist,
                    allApps = apps,
                    screenTime = screenTime,
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
                    onSaveLocation = { lat, lon, city -> viewModel.setWeatherLocation(lat, lon, city) },
                    onSetTheme = { viewModel.setThemeMode(it) },
                    onSetAccent = { viewModel.setAccentColor(it) },
                    onSetIconShape = { viewModel.setIconShape(it) },
                    onSetIconStyle = { viewModel.setIconStyle(it) },
                    onToggleFocusMode = { viewModel.setFocusMode(it) },
                    onUpdateFocusWhitelist = { viewModel.setFocusWhitelist(it) },
                    onUnhideApp = { pkg -> viewModel.setAppHidden(pkg, false) },
                    onRequestUsagePermission = onRequestUsagePermission,
                    onExportBackup = { viewModel.exportBackup() },
                    onRestoreBackup = { json -> viewModel.restoreBackup(json) },
                    onSetClockStyle = { viewModel.setClockStyle(it) },
                    onSetClockSize = { viewModel.setClockSize(it) },
                    onSetShowBattery = { viewModel.setShowBattery(it) },
                    onSetShowWeather = { viewModel.setShowWeather(it) },
                    onSetShowCalendarEvent = { viewModel.setShowCalendarEvent(it) },
                    onSetShowMusicHome = { viewModel.setShowMusicHome(it) },
                    onSetShowMusicDrawer = { viewModel.setShowMusicDrawer(it) },
                    onSetShowAlphabetIndex = { viewModel.setShowAlphabetIndex(it) },
                    onSetWallpaperDimOverlay = { viewModel.setWallpaperDimOverlay(it) },
                    onSetHapticsEnabled = { viewModel.setHapticsEnabled(it) },
                    onSetHomePageCount = { viewModel.setHomePageCount(it) },
                    onOpenAutomations = { viewModel.openAutomations() },
                    onOpenAnimatedIcons = { viewModel.openAnimatedIconLibrary() },
                    onOpenContextRings = { viewModel.openContextRingDesigner() },
                    onBack = { viewModel.navigateToHome() }
                )

                // 6: AUTOMATIONS & INTELLIGENCE (Visual Automation Editor & Macro Studio)
                6 -> AutomationScreen(
                    rules = automationRules,
                    macros = commandMacros,
                    constellations = appConstellations,
                    automationsEnabled = automationsEnabled,
                    onToggleGlobalAutomations = { viewModel.toggleGlobalAutomations(it) },
                    onToggleRule = { id, en -> viewModel.toggleAutomationRule(id, en) },
                    onDeleteRule = { id -> viewModel.deleteAutomationRule(id) },
                    onCreateRule = { rule -> viewModel.createAutomationRule(rule) },
                    onCreateMacro = { macro -> viewModel.createCommandMacro(macro) },
                    onDeleteMacro = { id -> viewModel.deleteCommandMacro(id) },
                    onBack = { viewModel.navigateToHome() }
                )

                // 7: ANIMATED ICON ENGINE (Library, Inspector, Collections & Battery Modes)
                7 -> com.novaforge.launcher.ui.icon.AnimatedIconLibraryScreen(
                    animations = animations,
                    collections = animationCollections,
                    assignments = iconAssignments,
                    engineSettings = engineSettings,
                    onImportUri = { viewModel.importAnimation(it) },
                    onRenameAnimation = { id, name -> viewModel.renameAnimation(id, name) },
                    onDeleteAnimation = { id -> viewModel.deleteAnimation(id) },
                    onUpdateEngineMode = { viewModel.updateEngineMode(it) },
                    onToggleGlobalAnimations = { viewModel.toggleGlobalAnimations(it) },
                    onOpenAssignmentDialog = { targetAnim ->
                        assignmentDialogTargetAnim = targetAnim
                        assignmentDialogTargetApp = null
                        showAssignmentDialog = true
                    },
                    onBack = { viewModel.navigateToHome() }
                )

                // 8: CONTEXT RINGS DESIGNER (Radial Command Editor, Geometry & Context Bindings)
                8 -> com.novaforge.launcher.ui.contextring.ContextRingDesignerScreen(
                    config = contextRingConfig,
                    allApps = apps,
                    workspaces = workspaces,
                    onSaveRing = { viewModel.saveContextRing(it) },
                    onDeleteRing = { viewModel.deleteContextRing(it) },
                    onDuplicateRing = { viewModel.duplicateContextRing(it) },
                    onResetDefaults = { viewModel.resetContextRingsToDefaults() },
                    onAssignApp = { pkg, ringId -> viewModel.assignRingToApp(pkg, ringId) },
                    onAssignWorkspace = { wsId, ringId -> viewModel.assignRingToWorkspace(wsId, ringId) },
                    onUpdateHaptics = { viewModel.updateRingHaptics(it) },
                    onUpdateTrigger = { viewModel.updateRingTrigger(it) },
                    onExportJson = { viewModel.exportContextRingsJson() },
                    onImportJson = { viewModel.importContextRingsJson(it) },
                    onBack = { viewModel.navigateToHome() }
                )
            }
        }

        // App Icon Assignment Modal Dialog / Bottom Sheet
        if (showAssignmentDialog) {
            com.novaforge.launcher.ui.icon.IconAssignmentDialog(
                allApps = apps,
                animations = animations,
                assignments = iconAssignments,
                initialAnimation = assignmentDialogTargetAnim,
                initialAppPackage = assignmentDialogTargetApp,
                onAssignAnimation = { viewModel.assignAnimation(it) },
                onRemoveAssignment = { viewModel.removeAssignment(it) },
                onDismiss = { showAssignmentDialog = false }
            )
        }

        // Full-Screen Active Context Ring Overlay
        activeRingSession?.let { session ->
            com.novaforge.launcher.ui.contextring.ContextRingOverlay(
                session = session,
                config = contextRingConfig,
                onExecuteAction = { action ->
                    viewModel.selectRingAction(action) { act ->
                        viewModel.ringActionExecutor.executeAction(
                            action = act,
                            onLaunchApp = { pkg ->
                                viewModel.recordAppLaunch(pkg)
                                onLaunchApp(pkg)
                            },
                            onSwitchWorkspace = { wsId -> viewModel.switchWorkspace(wsId) },
                            onOpenAppDrawer = { viewModel.navigateToAppDrawer() },
                            onOpenSettings = { viewModel.openSettings() },
                            onOpenWidgets = { viewModel.navigateToWidgets() },
                            onOpenCommandBar = { viewModel.navigateToCommand() },
                            onToggleFocusMode = { viewModel.setFocusMode(!focusModeEnabled) },
                            onOpenConstellations = { viewModel.navigateToHome() },
                            onExecuteMacroById = { macroId ->
                                val macro = commandMacros.find { it.id == macroId || it.commandName == macroId }
                                if (macro != null) viewModel.executeMacro(macro)
                            },
                            onPushNestedRing = { targetId -> viewModel.pushNestedRing(targetId) }
                        )
                    }
                },
                onNavigateBack = { viewModel.popNestedRing() },
                onDismiss = { viewModel.dismissContextRing() },
                onUpdatePointer = { x, y -> viewModel.updateRingPointer(x, y) },
                onReleasePointer = {
                    viewModel.releaseRingPointer { act ->
                        viewModel.ringActionExecutor.executeAction(
                            action = act,
                            onLaunchApp = { pkg ->
                                viewModel.recordAppLaunch(pkg)
                                onLaunchApp(pkg)
                            },
                            onSwitchWorkspace = { wsId -> viewModel.switchWorkspace(wsId) },
                            onOpenAppDrawer = { viewModel.navigateToAppDrawer() },
                            onOpenSettings = { viewModel.openSettings() },
                            onOpenWidgets = { viewModel.navigateToWidgets() },
                            onOpenCommandBar = { viewModel.navigateToCommand() },
                            onToggleFocusMode = { viewModel.setFocusMode(!focusModeEnabled) },
                            onOpenConstellations = { viewModel.navigateToHome() },
                            onExecuteMacroById = { macroId ->
                                val macro = commandMacros.find { it.id == macroId || it.commandName == macroId }
                                if (macro != null) viewModel.executeMacro(macro)
                            },
                            onPushNestedRing = { targetId -> viewModel.pushNestedRing(targetId) }
                        )
                    }
                }
            )
        }

        // Official NovaForge Splash Screen with subtle fade-in and near-black cosmic theme
        if (showSplash) {
            LauncherSplashScreen(
                onDismiss = { showSplash = false }
            )
        }
    }
}
