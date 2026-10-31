package com.novaforge.launcher.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.novaforge.launcher.NovaForgeApp
import com.novaforge.launcher.data.*
import com.novaforge.launcher.data.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.UUID

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as NovaForgeApp
    private val appRepository = app.appRepository
    private val preferences = app.preferences

    private val weatherRepo = WeatherRepository()
    private val calendarRepo = CalendarRepository(application)
    private val screenTimeRepo = ScreenTimeRepository(application)
    private val intelligenceEngine = IntelligenceEngine()
    private val animationRepository = app.animationRepository
    val contextRingRepository = app.contextRingRepository
    val ringEngine = com.novaforge.launcher.contextring.ContextRingEngine()
    val ringActionExecutor = com.novaforge.launcher.contextring.ContextRingActionExecutor(application)

    val installedApps: StateFlow<List<AppItem>> = appRepository.installedApps

    // Context Rings State
    val contextRingConfig: StateFlow<com.novaforge.launcher.contextring.ContextRingConfig> = contextRingRepository.config
    private val _activeRingSession = MutableStateFlow<com.novaforge.launcher.contextring.ActiveRingSession?>(null)
    val activeRingSession: StateFlow<com.novaforge.launcher.contextring.ActiveRingSession?> = _activeRingSession.asStateFlow()

    // Animated Icon Engine State
    val animations: StateFlow<List<com.novaforge.launcher.icon.AnimatedIconItem>> = animationRepository.animations
    val animationCollections: StateFlow<List<com.novaforge.launcher.icon.AnimationCollection>> = animationRepository.collections
    val iconAssignments: StateFlow<Map<String, com.novaforge.launcher.icon.IconAssignment>> = animationRepository.assignments
    val engineSettings: StateFlow<com.novaforge.launcher.icon.AnimatedIconEngineSettings> = animationRepository.engineSettings
    val assignmentManager: com.novaforge.launcher.icon.AnimationAssignmentManager = animationRepository.assignmentManager

    val themeMode: StateFlow<String> = preferences.themeMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "warm-light"
    )

    val accentColor: StateFlow<String> = preferences.accentColor.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "#2A9D8F"
    )

    val iconShape: StateFlow<String> = preferences.iconShape.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "squircle"
    )

    val iconStyle: StateFlow<String> = preferences.iconStyle.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "default"
    )

    val focusModeEnabled: StateFlow<Boolean> = preferences.focusModeEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val focusWhitelist: StateFlow<Set<String>> = preferences.focusWhitelist.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptySet()
    )

    // Navigation Screens:
    // 0: AppDrawer, 1: HOME, 2: COMMAND, 3: SPACES, 4: WIDGETS, 5: SETTINGS, 6: AUTOMATIONS, 7: ANIMATED_ICONS, 8: CONTEXT_RING_DESIGNER
    private val _currentScreen = MutableStateFlow(1)
    val currentScreen: StateFlow<Int> = _currentScreen.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _commandQuery = MutableStateFlow("")
    val commandQuery: StateFlow<String> = _commandQuery.asStateFlow()

    // Workspaces
    val workspaces: StateFlow<List<WorkspaceSpace>> = preferences.workspaces.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = preferences.defaultWorkspaces()
    )

    val activeWorkspaceId: StateFlow<String> = preferences.activeWorkspaceId.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "personal"
    )

    val activeWorkspace: StateFlow<WorkspaceSpace> = combine(workspaces, activeWorkspaceId) { list, id ->
        list.find { it.id == id } ?: list.firstOrNull() ?: WorkspaceSpace("personal", "Personal", "Default", "user")
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WorkspaceSpace("personal", "Personal", "Default", "user")
    )

    // Intelligence Engine: Macros, Constellations, Automation Rules
    val commandMacros: StateFlow<List<CommandMacro>> = preferences.commandMacros.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = preferences.defaultMacros()
    )

    val appConstellations: StateFlow<List<AppConstellation>> = preferences.appConstellations.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = preferences.defaultConstellations()
    )

    val automationRules: StateFlow<List<AutomationRule>> = preferences.automationRules.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = preferences.defaultAutomationRules()
    )

    val automationsEnabled: StateFlow<Boolean> = preferences.automationsEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val favoriteCommands: StateFlow<Set<String>> = preferences.favoriteCommands.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = setOf("/personal", "/work", "/start-study")
    )

    val recentCommands: StateFlow<List<String>> = preferences.recentCommands.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Adaptive Workspace Suggestion
    private val _activeSuggestion = MutableStateFlow<WorkspaceSuggestion?>(null)
    val activeSuggestion: StateFlow<WorkspaceSuggestion?> = _activeSuggestion.asStateFlow()

    // Real Data States
    private val _weatherState = MutableStateFlow(RealWeatherInfo())
    val weatherState: StateFlow<RealWeatherInfo> = _weatherState.asStateFlow()

    private val _agendaItems = MutableStateFlow<List<AgendaItem>>(emptyList())
    val agendaItems: StateFlow<List<AgendaItem>> = _agendaItems.asStateFlow()

    private val _screenTime = MutableStateFlow(ScreenTimeSummary())
    val screenTime: StateFlow<ScreenTimeSummary> = _screenTime.asStateFlow()

    private val _batteryState = MutableStateFlow(BatteryInfo())
    val batteryState: StateFlow<BatteryInfo> = _batteryState.asStateFlow()

    // Hosted 3rd-party Widgets
    private val _widgets = MutableStateFlow<List<LauncherWidget>>(emptyList())
    val widgets: StateFlow<List<LauncherWidget>> = _widgets.asStateFlow()

    // App launch callback for macro execution
    var onLaunchAppFromMacro: ((String) -> Unit)? = null

    // Universal Commands Catalog
    val universalCommands = listOf(
        CommandAction("/personal", "Personal Workspace", "Switch to daily personal workspace", "Workspace"),
        CommandAction("/work", "Work Workspace", "Switch to work & productivity workspace", "Workspace"),
        CommandAction("/study", "Study Workspace", "Switch to deep focus & study workspace", "Workspace"),
        CommandAction("/gaming", "Gaming Workspace", "Switch to games & performance workspace", "Workspace"),
        CommandAction("/travel", "Travel Workspace", "Switch to navigation & transit workspace", "Workspace"),
        CommandAction("/spaces", "Manage Workspaces", "Open workspace capsules dashboard", "Workspace"),
        CommandAction("/automations", "Visual Automations", "Edit IF/THEN rules and command macros", "Launcher"),
        CommandAction("/widgets", "Android Widgets", "View and edit hosted app widgets", "Launcher"),
        CommandAction("/apps", "Application Drawer", "Open full alphabetical app listing", "Launcher"),
        CommandAction("/icons", "Animated Icon Library", "Manage GIF, WebP & Lottie animated icons", "Launcher"),
        CommandAction("/animated-icons", "Animated Icon Engine", "Configure battery engine and app assignments", "Launcher"),
        CommandAction("/rings", "Context Rings Designer", "Configure radial command system & bindings", "Launcher"),
        CommandAction("/settings", "Launcher Settings", "Themes, icons, gestures, backup & restore", "Launcher"),
        CommandAction("/wifi", "Wi-Fi Settings", "Open Android network & internet settings", "System"),
        CommandAction("/bluetooth", "Bluetooth Settings", "Open Android connected devices settings", "System"),
        CommandAction("/battery", "Battery Saver", "Open battery usage & power settings", "System"),
        CommandAction("/display", "Display Settings", "Brightness, dark mode & font scaling", "System"),
        CommandAction("/wallpaper", "Wallpaper Picker", "Change system or lock screen wallpaper", "System"),
        CommandAction("/focus", "Toggle Focus Mode", "Enable or disable distraction-free session", "Launcher"),
        CommandAction("/lock", "Lock Screen", "Trigger screen lock security flow", "System")
    )

    init {
        appRepository.reloadInstalledApps()
        observePersistedWidgets()
        observeWeatherLocation()
        loadRealData()
        updateBatteryState()
        evaluateAdaptiveSuggestions()
    }

    private fun observePersistedWidgets() {
        viewModelScope.launch {
            preferences.savedWidgets.collect { list ->
                _widgets.value = list.sortedBy { it.orderIndex }
            }
        }
    }

    private fun observeWeatherLocation() {
        viewModelScope.launch {
            preferences.weatherLocation.collect { (lat, lon, city) ->
                _weatherState.value = weatherRepo.fetchWeather(lat, lon, city)
            }
        }
    }

    fun updateBatteryState() {
        try {
            val bm = app.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            val capacity = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 100
            val intent = app.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
            _batteryState.value = BatteryInfo(
                percentage = if (capacity in 1..100) capacity else 100,
                isCharging = isCharging,
                statusText = if (isCharging) "Charging" else "Discharging"
            )
            evaluateAdaptiveSuggestions()
        } catch (_: Exception) {
            _batteryState.value = BatteryInfo(percentage = 100, isCharging = false, statusText = "Normal")
        }
    }

    fun loadRealData() {
        updateBatteryState()
        viewModelScope.launch {
            val events = calendarRepo.getUpcomingEvents()
            if (events.isNotEmpty()) {
                _agendaItems.value = events
            } else {
                _agendaItems.value = listOf(
                    AgendaItem("demo-1", "Design Review", "10:00 AM", isDemoData = true),
                    AgendaItem("demo-2", "Gym Workout", "5:30 PM", isDemoData = true),
                    AgendaItem("demo-3", "Read Documentation", "9:00 PM", isDemoData = true)
                )
            }
            evaluateAdaptiveSuggestions()
        }
        viewModelScope.launch {
            _screenTime.value = screenTimeRepo.getDailyUsageStats()
        }
    }

    fun evaluateAdaptiveSuggestions() {
        _activeSuggestion.value = intelligenceEngine.evaluateSuggestions(
            rules = automationRules.value,
            automationsEnabled = automationsEnabled.value,
            activeWorkspaceId = activeWorkspaceId.value,
            batteryInfo = batteryState.value,
            agendaItems = agendaItems.value
        )
    }

    fun acceptSuggestion(spaceId: String) {
        if (spaceId == "battery") {
            // Open battery settings
            _activeSuggestion.value = null
        } else {
            switchWorkspace(spaceId)
            _activeSuggestion.value = null
        }
    }

    fun dismissSuggestion() {
        _activeSuggestion.value = null
    }

    // Workspace Actions & Workspace Memory
    fun switchWorkspace(workspaceId: String) {
        viewModelScope.launch {
            preferences.setActiveWorkspace(workspaceId)
            // Restore accent color and workspace memory if configured
            val space = workspaces.value.find { it.id == workspaceId }
            if (space != null) {
                preferences.setAccentColor(space.accentHex)
            }
            evaluateAdaptiveSuggestions()
            navigateToHome()
        }
    }

    fun createWorkspace(name: String, desc: String, accent: String) {
        viewModelScope.launch {
            val newSpace = WorkspaceSpace(
                id = "custom_" + UUID.randomUUID().toString().take(8),
                name = name,
                description = desc,
                iconName = "star",
                accentHex = accent,
                isCustom = true
            )
            val updated = workspaces.value + newSpace
            preferences.saveWorkspaces(updated)
            preferences.setActiveWorkspace(newSpace.id)
            preferences.setAccentColor(accent)
            navigateToHome()
        }
    }

    // Command Macro Execution Pipeline
    fun executeMacro(macro: CommandMacro) {
        viewModelScope.launch {
            preferences.recordCommandUsage(macro.commandName)

            for (action in macro.actions) {
                when (action.type) {
                    "SWITCH_WORKSPACE" -> {
                        preferences.setActiveWorkspace(action.param)
                        val space = workspaces.value.find { it.id == action.param }
                        if (space != null) preferences.setAccentColor(space.accentHex)
                    }
                    "ENABLE_FOCUS" -> preferences.setFocusMode(true)
                    "DISABLE_FOCUS" -> preferences.setFocusMode(false)
                    "SET_THEME" -> preferences.setThemeMode(action.param)
                    "LAUNCH_APP" -> {
                        appRepository.recordAppLaunch(action.param)
                        onLaunchAppFromMacro?.invoke(action.param)
                    }
                }
            }
            evaluateAdaptiveSuggestions()
            navigateToHome()
        }
    }

    fun toggleFavoriteCommand(cmd: String) {
        viewModelScope.launch { preferences.toggleFavoriteCommand(cmd) }
    }

    fun recordCommandUsage(cmd: String) {
        viewModelScope.launch { preferences.recordCommandUsage(cmd) }
    }

    // Automation Rules Management
    fun toggleGlobalAutomations(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setAutomationsEnabled(enabled)
            if (!enabled) _activeSuggestion.value = null else evaluateAdaptiveSuggestions()
        }
    }

    fun toggleAutomationRule(ruleId: String, enabled: Boolean) {
        viewModelScope.launch {
            val updated = automationRules.value.map {
                if (it.id == ruleId) it.copy(isEnabled = enabled) else it
            }
            preferences.saveAutomationRules(updated)
            evaluateAdaptiveSuggestions()
        }
    }

    fun deleteAutomationRule(ruleId: String) {
        viewModelScope.launch {
            val updated = automationRules.value.filter { it.id != ruleId }
            preferences.saveAutomationRules(updated)
            evaluateAdaptiveSuggestions()
        }
    }

    fun createAutomationRule(rule: AutomationRule) {
        viewModelScope.launch {
            val updated = automationRules.value + rule
            preferences.saveAutomationRules(updated)
            evaluateAdaptiveSuggestions()
        }
    }

    // Command Macros Management
    fun createCommandMacro(macro: CommandMacro) {
        viewModelScope.launch {
            val updated = commandMacros.value + macro
            preferences.saveCommandMacros(updated)
        }
    }

    fun deleteCommandMacro(macroId: String) {
        viewModelScope.launch {
            val updated = commandMacros.value.filter { it.id != macroId }
            preferences.saveCommandMacros(updated)
        }
    }

    // Navigation triggers
    fun isAtHomeScreen(): Boolean = _currentScreen.value == 1

    fun navigateToHome() { _currentScreen.value = 1 }
    fun navigateToAppDrawer() { _currentScreen.value = 0 }
    fun navigateToCommand() {
        _commandQuery.value = ""
        _currentScreen.value = 2
    }
    fun navigateToSpaces() { _currentScreen.value = 3 }
    fun navigateToWidgets() { _currentScreen.value = 4 }
    fun openSettings() { _currentScreen.value = 5 }
    fun openAutomations() { _currentScreen.value = 6 }

    fun updateSearchQuery(query: String) { _searchQuery.value = query }
    fun updateCommandQuery(query: String) { _commandQuery.value = query }
    fun selectCategory(category: String) { _selectedCategory.value = category }

    // Settings actions
    fun setWeatherLocation(lat: Double, lon: Double, city: String) {
        viewModelScope.launch {
            preferences.saveWeatherLocation(lat, lon, city)
            _weatherState.value = weatherRepo.fetchWeather(lat, lon, city)
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch { preferences.setThemeMode(mode) }
    }

    fun setAccentColor(hex: String) {
        viewModelScope.launch { preferences.setAccentColor(hex) }
    }

    fun setIconShape(shape: String) {
        viewModelScope.launch { preferences.setIconShape(shape) }
    }

    fun setIconStyle(style: String) {
        viewModelScope.launch { preferences.setIconStyle(style) }
    }

    fun setFocusMode(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setFocusMode(enabled)
            evaluateAdaptiveSuggestions()
        }
    }

    fun setFocusWhitelist(whitelist: Set<String>) {
        viewModelScope.launch { preferences.setFocusWhitelist(whitelist) }
    }

    fun toggleFavorite(packageName: String) {
        viewModelScope.launch { appRepository.toggleFavorite(packageName) }
    }

    fun setAppHidden(packageName: String, hidden: Boolean) {
        viewModelScope.launch { appRepository.setAppHidden(packageName, hidden) }
    }

    fun recordAppLaunch(packageName: String) {
        viewModelScope.launch { appRepository.recordAppLaunch(packageName) }
    }

    fun exportBackup(): String = runBlocking {
        preferences.exportBackupJson()
    }

    fun restoreBackup(json: String): Boolean = runBlocking {
        preferences.restoreBackupJson(json)
    }

    // Widgets management
    fun addWidget(widget: LauncherWidget) {
        val updated = _widgets.value + widget.copy(orderIndex = _widgets.value.size)
        _widgets.value = updated
        viewModelScope.launch { preferences.persistWidgets(updated) }
    }

    fun resizeWidgetWidth(appWidgetId: Int, newWidth: Int) {
        val updated = _widgets.value.map {
            if (it.appWidgetId == appWidgetId) it.copy(cellWidth = newWidth) else it
        }
        _widgets.value = updated
        viewModelScope.launch { preferences.persistWidgets(updated) }
    }

    fun resizeWidgetHeight(appWidgetId: Int, newHeight: Int) {
        val updated = _widgets.value.map {
            if (it.appWidgetId == appWidgetId) it.copy(cellHeight = newHeight) else it
        }
        _widgets.value = updated
        viewModelScope.launch { preferences.persistWidgets(updated) }
    }

    fun moveWidget(appWidgetId: Int, direction: Int) {
        val currentList = _widgets.value.toMutableList()
        val index = currentList.indexOfFirst { it.appWidgetId == appWidgetId }
        val targetIndex = index + direction
        if (index != -1 && targetIndex in currentList.indices) {
            val item = currentList.removeAt(index)
            currentList.add(targetIndex, item)
            val reindexed = currentList.mapIndexed { idx, w -> w.copy(orderIndex = idx) }
            _widgets.value = reindexed
            viewModelScope.launch { preferences.persistWidgets(reindexed) }
        }
    }

    fun removeWidget(appWidgetId: Int, onIdCleaned: (Int) -> Unit) {
        val updated = _widgets.value.filter { it.appWidgetId != appWidgetId }
        _widgets.value = updated
        onIdCleaned(appWidgetId)
        viewModelScope.launch { preferences.persistWidgets(updated) }
    }

    // Animated Icon Engine Actions
    fun openAnimatedIconLibrary() {
        _currentScreen.value = 7
    }

    fun importAnimation(uri: android.net.Uri, name: String? = null) {
        viewModelScope.launch {
            animationRepository.importAnimationFromUri(uri, name)
        }
    }

    fun renameAnimation(id: String, newName: String) {
        viewModelScope.launch {
            animationRepository.renameAnimation(id, newName)
        }
    }

    fun deleteAnimation(id: String) {
        viewModelScope.launch {
            animationRepository.deleteAnimation(id)
        }
    }

    fun assignAnimation(assignment: com.novaforge.launcher.icon.IconAssignment) {
        viewModelScope.launch {
            animationRepository.assignAnimationToApp(assignment)
        }
    }

    fun removeAssignment(packageName: String) {
        viewModelScope.launch {
            animationRepository.removeAssignmentForApp(packageName)
        }
    }

    fun updateEngineMode(mode: com.novaforge.launcher.icon.BatteryEngineMode) {
        viewModelScope.launch {
            val updated = engineSettings.value.copy(engineMode = mode)
            animationRepository.updateEngineSettings(updated)
            com.novaforge.launcher.icon.AnimationPlaybackController.getInstance(app).setEngineMode(mode)
        }
    }

    fun toggleGlobalAnimations(enabled: Boolean) {
        viewModelScope.launch {
            val updated = engineSettings.value.copy(globalAnimationsEnabled = enabled)
            animationRepository.updateEngineSettings(updated)
            com.novaforge.launcher.icon.AnimationPlaybackController.getInstance(app).setGlobalEnabled(enabled)
        }
    }

    // Context Ring Navigation & Designer Actions
    fun openContextRingDesigner() {
        _currentScreen.value = 8
    }

    // Context Ring Runtime Session Actions
    fun triggerContextRing(
        originX: Float,
        originY: Float,
        targetPackage: String? = null,
        targetWorkspaceId: String? = null,
        targetFolderId: String? = null
    ) {
        val cfg = contextRingConfig.value
        val ring = ringEngine.resolveRing(
            config = cfg,
            targetPackage = targetPackage,
            targetWorkspaceId = targetWorkspaceId ?: activeWorkspaceId.value,
            targetFolderId = targetFolderId
        )

        _activeRingSession.value = com.novaforge.launcher.contextring.ActiveRingSession(
            currentRing = ring,
            originX = originX,
            originY = originY,
            clampedCenterX = originX,
            clampedCenterY = originY,
            targetPackage = targetPackage,
            targetWorkspaceId = targetWorkspaceId,
            targetFolderId = targetFolderId,
            breadcrumbRingIds = emptyList(),
            selectedIndex = -1,
            highlightedAction = null
        )
    }

    fun updateRingPointer(x: Float, y: Float) {
        val session = _activeRingSession.value ?: return
        val (selIdx, act, dist) = ringEngine.evaluatePointer(
            pointerX = x,
            pointerY = y,
            centerX = session.clampedCenterX,
            centerY = session.clampedCenterY,
            ring = session.currentRing,
            density = app.resources.displayMetrics.density
        )
        _activeRingSession.value = session.copy(
            selectedIndex = selIdx,
            highlightedAction = act,
            currentDistancePx = dist,
            isEngaged = true
        )
    }

    fun releaseRingPointer(onExecute: (com.novaforge.launcher.contextring.ContextRingAction) -> Unit) {
        val session = _activeRingSession.value ?: return
        val act = session.highlightedAction
        if (act != null) {
            onExecute(act)
        } else {
            dismissContextRing()
        }
    }

    fun selectRingAction(
        action: com.novaforge.launcher.contextring.ContextRingAction,
        onExecute: (com.novaforge.launcher.contextring.ContextRingAction) -> Unit
    ) {
        if (action.actionType == com.novaforge.launcher.contextring.RingActionType.NESTED_RING) {
            pushNestedRing(action.payload)
        } else {
            dismissContextRing()
            onExecute(action)
        }
    }

    fun pushNestedRing(targetRingId: String) {
        val session = _activeRingSession.value ?: return
        val cfg = contextRingConfig.value
        if (!ringEngine.canNavigateToNestedRing(targetRingId, session.breadcrumbRingIds, cfg)) return

        val target = cfg.rings.find { it.id == targetRingId } ?: return
        val breadcrumbs = session.breadcrumbRingIds + session.currentRing.id
        _activeRingSession.value = session.copy(
            currentRing = target,
            breadcrumbRingIds = breadcrumbs,
            selectedIndex = -1,
            highlightedAction = null
        )
    }

    fun popNestedRing() {
        val session = _activeRingSession.value ?: return
        if (session.breadcrumbRingIds.isEmpty()) {
            dismissContextRing()
            return
        }
        val prevRingId = session.breadcrumbRingIds.last()
        val prevBreadcrumbs = session.breadcrumbRingIds.dropLast(1)
        val cfg = contextRingConfig.value
        val prevRing = cfg.rings.find { it.id == prevRingId } ?: cfg.rings.firstOrNull() ?: return
        _activeRingSession.value = session.copy(
            currentRing = prevRing,
            breadcrumbRingIds = prevBreadcrumbs,
            selectedIndex = -1,
            highlightedAction = null
        )
    }

    fun dismissContextRing() {
        _activeRingSession.value = null
    }

    // Context Ring Configuration & Designer Methods
    fun saveContextRing(ring: com.novaforge.launcher.contextring.ContextRingDefinition) {
        contextRingRepository.saveRing(ring)
    }

    fun deleteContextRing(ringId: String) {
        contextRingRepository.deleteRing(ringId)
    }

    fun duplicateContextRing(ringId: String) {
        contextRingRepository.duplicateRing(ringId)
    }

    fun resetContextRingsToDefaults() {
        contextRingRepository.resetToDefaults()
    }

    fun assignRingToApp(packageName: String, ringId: String) {
        contextRingRepository.assignToApp(packageName, ringId)
    }

    fun assignRingToWorkspace(workspaceId: String, ringId: String) {
        contextRingRepository.assignToWorkspace(workspaceId, ringId)
    }

    fun assignRingToFolder(folderId: String, ringId: String) {
        contextRingRepository.assignToFolder(folderId, ringId)
    }

    fun updateRingHaptics(intensity: String) {
        contextRingRepository.updateHapticIntensity(intensity)
    }

    fun updateRingTrigger(gesture: String) {
        contextRingRepository.updateGestureTrigger(gesture)
    }

    fun exportContextRingsJson(): String {
        return contextRingRepository.exportToJson()
    }

    fun importContextRingsJson(jsonString: String): Boolean {
        return contextRingRepository.importFromJson(jsonString)
    }
}
