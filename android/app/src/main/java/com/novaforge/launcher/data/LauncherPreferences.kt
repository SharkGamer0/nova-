package com.novaforge.launcher.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.novaforge.launcher.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore by preferencesDataStore(name = "novaforge_settings")

class LauncherPreferences(private val context: Context) {

    companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ACCENT_COLOR = stringPreferencesKey("accent_color")
        val ICON_SHAPE = stringPreferencesKey("icon_shape")
        val ICON_STYLE = stringPreferencesKey("icon_style")
        val FOCUS_MODE = booleanPreferencesKey("focus_mode_enabled")
        val FOCUS_WHITELIST = stringSetPreferencesKey("focus_whitelist")
        val HIDDEN_PIN = stringPreferencesKey("hidden_apps_pin")
        val FAVORITE_PACKAGES = stringSetPreferencesKey("favorite_packages")
        val HIDDEN_PACKAGES = stringSetPreferencesKey("hidden_packages")
        val LAUNCH_COUNTS = stringPreferencesKey("launch_counts_json")
        val SAVED_WIDGETS = stringPreferencesKey("saved_widgets_json")
        val WEATHER_LAT = doublePreferencesKey("weather_lat")
        val WEATHER_LON = doublePreferencesKey("weather_lon")
        val WEATHER_CITY = stringPreferencesKey("weather_city_name")
        val GESTURE_DOUBLE_TAP = stringPreferencesKey("gesture_double_tap")
        val GESTURE_SWIPE_UP = stringPreferencesKey("gesture_swipe_up")
        val GESTURE_SWIPE_DOWN = stringPreferencesKey("gesture_swipe_down")
        val ACTIVE_WORKSPACE_ID = stringPreferencesKey("active_workspace_id")
        val USER_WORKSPACES = stringPreferencesKey("user_workspaces_json")

        // Intelligence Engine Keys
        val COMMAND_MACROS = stringPreferencesKey("command_macros_json")
        val APP_CONSTELLATIONS = stringPreferencesKey("app_constellations_json")
        val WORKSPACE_MEMORIES = stringPreferencesKey("workspace_memories_json")
        val AUTOMATION_RULES = stringPreferencesKey("automation_rules_json")
        val AUTOMATIONS_ENABLED = booleanPreferencesKey("automations_enabled")
        val RECENT_COMMANDS = stringPreferencesKey("recent_commands_json")
        val FAVORITE_COMMANDS = stringSetPreferencesKey("favorite_commands")
        val CONTEXT_RINGS = stringPreferencesKey("context_rings_json")

        // Minimalist Redesign & Customization Keys
        val CLOCK_STYLE = stringPreferencesKey("clock_style")
        val CLOCK_SIZE = stringPreferencesKey("clock_size")
        val SHOW_BATTERY = booleanPreferencesKey("show_battery")
        val SHOW_WEATHER = booleanPreferencesKey("show_weather")
        val SHOW_CALENDAR_EVENT = booleanPreferencesKey("show_calendar_event")
        val SHOW_MUSIC_HOME = booleanPreferencesKey("show_music_home")
        val SHOW_MUSIC_DRAWER = booleanPreferencesKey("show_music_drawer")
        val MUSIC_EXPANDED = booleanPreferencesKey("music_expanded")
        val SHOW_ALPHABET_INDEX = booleanPreferencesKey("show_alphabet_index")
        val WALLPAPER_DIM_OVERLAY = floatPreferencesKey("wallpaper_dim_overlay")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val HOME_PAGE_COUNT = intPreferencesKey("home_page_count")
    }

    val clockStyle: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[CLOCK_STYLE] ?: "digital"
    }

    val clockSize: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[CLOCK_SIZE] ?: "large"
    }

    val showBattery: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[SHOW_BATTERY] ?: true
    }

    val showWeather: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[SHOW_WEATHER] ?: true
    }

    val showCalendarEvent: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[SHOW_CALENDAR_EVENT] ?: true
    }

    val showMusicHome: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[SHOW_MUSIC_HOME] ?: true
    }

    val showMusicDrawer: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[SHOW_MUSIC_DRAWER] ?: false
    }

    val musicExpanded: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[MUSIC_EXPANDED] ?: false
    }

    val showAlphabetIndex: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[SHOW_ALPHABET_INDEX] ?: true
    }

    val wallpaperDimOverlay: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[WALLPAPER_DIM_OVERLAY] ?: 0.0f
    }

    val hapticsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[HAPTICS_ENABLED] ?: true
    }

    val homePageCount: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[HOME_PAGE_COUNT] ?: 2
    }

    suspend fun setClockStyle(style: String) {
        context.dataStore.edit { it[CLOCK_STYLE] = style }
    }

    suspend fun setClockSize(size: String) {
        context.dataStore.edit { it[CLOCK_SIZE] = size }
    }

    suspend fun setShowBattery(show: Boolean) {
        context.dataStore.edit { it[SHOW_BATTERY] = show }
    }

    suspend fun setShowWeather(show: Boolean) {
        context.dataStore.edit { it[SHOW_WEATHER] = show }
    }

    suspend fun setShowCalendarEvent(show: Boolean) {
        context.dataStore.edit { it[SHOW_CALENDAR_EVENT] = show }
    }

    suspend fun setShowMusicHome(show: Boolean) {
        context.dataStore.edit { it[SHOW_MUSIC_HOME] = show }
    }

    suspend fun setShowMusicDrawer(show: Boolean) {
        context.dataStore.edit { it[SHOW_MUSIC_DRAWER] = show }
    }

    suspend fun setMusicExpanded(expanded: Boolean) {
        context.dataStore.edit { it[MUSIC_EXPANDED] = expanded }
    }

    suspend fun setShowAlphabetIndex(show: Boolean) {
        context.dataStore.edit { it[SHOW_ALPHABET_INDEX] = show }
    }

    suspend fun setWallpaperDimOverlay(dim: Float) {
        context.dataStore.edit { it[WALLPAPER_DIM_OVERLAY] = dim }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[HAPTICS_ENABLED] = enabled }
    }

    suspend fun setHomePageCount(count: Int) {
        context.dataStore.edit { it[HOME_PAGE_COUNT] = count }
    }

    val contextRingsRawJson: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[CONTEXT_RINGS]
    }

    suspend fun saveContextRingsRawJson(json: String) {
        context.dataStore.edit { prefs ->
            prefs[CONTEXT_RINGS] = json
        }
    }

    val themeMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[THEME_MODE] ?: "system"
    }

    val accentColor: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[ACCENT_COLOR] ?: "#38BDF8"
    }

    val iconShape: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[ICON_SHAPE] ?: "squircle"
    }

    val iconStyle: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[ICON_STYLE] ?: "default"
    }

    val focusModeEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[FOCUS_MODE] ?: false
    }

    val focusWhitelist: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[FOCUS_WHITELIST] ?: emptySet()
    }

    val hiddenPin: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[HIDDEN_PIN] ?: "1234"
    }

    val favoritePackages: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[FAVORITE_PACKAGES] ?: emptySet()
    }

    val hiddenPackages: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[HIDDEN_PACKAGES] ?: emptySet()
    }

    val activeWorkspaceId: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[ACTIVE_WORKSPACE_ID] ?: "personal"
    }

    val automationsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[AUTOMATIONS_ENABLED] ?: true
    }

    val favoriteCommands: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[FAVORITE_COMMANDS] ?: setOf("/personal", "/work", "/start-study")
    }

    val recentCommands: Flow<List<String>> = context.dataStore.data.map { prefs ->
        val jsonStr = prefs[RECENT_COMMANDS] ?: "[]"
        val list = mutableListOf<String>()
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) list.add(arr.getString(i))
        } catch (_: Exception) {}
        list
    }

    val workspaces: Flow<List<WorkspaceSpace>> = context.dataStore.data.map { prefs ->
        val jsonStr = prefs[USER_WORKSPACES]
        if (jsonStr.isNullOrEmpty()) {
            defaultWorkspaces()
        } else {
            try {
                val list = mutableListOf<WorkspaceSpace>()
                val arr = JSONArray(jsonStr)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val pkgs = mutableListOf<String>()
                    val pkgsArr = obj.optJSONArray("appPackageNames")
                    if (pkgsArr != null) {
                        for (j in 0 until pkgsArr.length()) pkgs.add(pkgsArr.getString(j))
                    }
                    list.add(
                        WorkspaceSpace(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            description = obj.optString("description", ""),
                            iconName = obj.optString("iconName", "user"),
                            accentHex = obj.optString("accentHex", "#2A9D8F"),
                            appPackageNames = pkgs,
                            isCustom = obj.optBoolean("isCustom", false)
                        )
                    )
                }
                if (list.isEmpty()) defaultWorkspaces() else list
            } catch (_: Exception) {
                defaultWorkspaces()
            }
        }
    }

    val commandMacros: Flow<List<CommandMacro>> = context.dataStore.data.map { prefs ->
        val jsonStr = prefs[COMMAND_MACROS]
        if (jsonStr.isNullOrEmpty()) {
            defaultMacros()
        } else {
            try {
                val list = mutableListOf<CommandMacro>()
                val arr = JSONArray(jsonStr)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val actions = mutableListOf<MacroAction>()
                    val actArr = obj.optJSONArray("actions")
                    if (actArr != null) {
                        for (j in 0 until actArr.length()) {
                            val aObj = actArr.getJSONObject(j)
                            actions.add(
                                MacroAction(
                                    type = aObj.getString("type"),
                                    param = aObj.getString("param"),
                                    description = aObj.optString("description", "")
                                )
                            )
                        }
                    }
                    list.add(
                        CommandMacro(
                            id = obj.getString("id"),
                            commandName = obj.getString("commandName"),
                            title = obj.getString("title"),
                            description = obj.optString("description", ""),
                            actions = actions,
                            isCustom = obj.optBoolean("isCustom", false)
                        )
                    )
                }
                if (list.isEmpty()) defaultMacros() else list
            } catch (_: Exception) {
                defaultMacros()
            }
        }
    }

    val appConstellations: Flow<List<AppConstellation>> = context.dataStore.data.map { prefs ->
        val jsonStr = prefs[APP_CONSTELLATIONS]
        if (jsonStr.isNullOrEmpty()) {
            defaultConstellations()
        } else {
            try {
                val list = mutableListOf<AppConstellation>()
                val arr = JSONArray(jsonStr)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val pkgs = mutableListOf<String>()
                    val pkgsArr = obj.optJSONArray("packageNames")
                    if (pkgsArr != null) {
                        for (j in 0 until pkgsArr.length()) pkgs.add(pkgsArr.getString(j))
                    }
                    list.add(
                        AppConstellation(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            description = obj.optString("description", ""),
                            accentHex = obj.optString("accentHex", "#2A9D8F"),
                            packageNames = pkgs
                        )
                    )
                }
                if (list.isEmpty()) defaultConstellations() else list
            } catch (_: Exception) {
                defaultConstellations()
            }
        }
    }

    val workspaceMemories: Flow<Map<String, WorkspaceMemory>> = context.dataStore.data.map { prefs ->
        val jsonStr = prefs[WORKSPACE_MEMORIES] ?: "{}"
        val map = mutableMapOf<String, WorkspaceMemory>()
        try {
            val root = JSONObject(jsonStr)
            val keys = root.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                val obj = root.getJSONObject(k)
                val favs = mutableListOf<String>()
                val docks = mutableListOf<String>()
                val fArr = obj.optJSONArray("favoritePackages")
                if (fArr != null) {
                    for (i in 0 until fArr.length()) favs.add(fArr.getString(i))
                }
                val dArr = obj.optJSONArray("dockPackages")
                if (dArr != null) {
                    for (i in 0 until dArr.length()) docks.add(dArr.getString(i))
                }
                map[k] = WorkspaceMemory(
                    workspaceId = k,
                    favoritePackages = favs,
                    dockPackages = docks,
                    accentColor = obj.optString("accentColor", "#2A9D8F"),
                    layoutMode = obj.optString("layoutMode", "list")
                )
            }
        } catch (_: Exception) {}
        map
    }

    val automationRules: Flow<List<AutomationRule>> = context.dataStore.data.map { prefs ->
        val jsonStr = prefs[AUTOMATION_RULES]
        if (jsonStr.isNullOrEmpty()) {
            defaultAutomationRules()
        } else {
            try {
                val list = mutableListOf<AutomationRule>()
                val arr = JSONArray(jsonStr)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        AutomationRule(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            triggerType = obj.getString("triggerType"),
                            triggerValue = obj.getString("triggerValue"),
                            actionType = obj.getString("actionType"),
                            actionValue = obj.getString("actionValue"),
                            isEnabled = obj.optBoolean("isEnabled", true)
                        )
                    )
                }
                if (list.isEmpty()) defaultAutomationRules() else list
            } catch (_: Exception) {
                defaultAutomationRules()
            }
        }
    }

    val launchCounts: Flow<Map<String, Int>> = context.dataStore.data.map { prefs ->
        val jsonStr = prefs[LAUNCH_COUNTS] ?: "{}"
        val map = mutableMapOf<String, Int>()
        try {
            val obj = JSONObject(jsonStr)
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                map[k] = obj.getInt(k)
            }
        } catch (_: Exception) {}
        map
    }

    val weatherLocation: Flow<Triple<Double, Double, String>> = context.dataStore.data.map { prefs ->
        val lat = prefs[WEATHER_LAT] ?: 28.6139
        val lon = prefs[WEATHER_LON] ?: 77.2090
        val city = prefs[WEATHER_CITY] ?: "New Delhi"
        Triple(lat, lon, city)
    }

    val savedWidgets: Flow<List<LauncherWidget>> = context.dataStore.data.map { prefs ->
        val jsonStr = prefs[SAVED_WIDGETS] ?: "[]"
        val list = mutableListOf<LauncherWidget>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    LauncherWidget(
                        appWidgetId = obj.getInt("appWidgetId"),
                        providerPackage = obj.getString("providerPackage"),
                        providerClass = obj.getString("providerClass"),
                        cellWidth = obj.optInt("cellWidth", 2),
                        cellHeight = obj.optInt("cellHeight", 2),
                        orderIndex = obj.optInt("orderIndex", i)
                    )
                )
            }
        } catch (_: Exception) {}
        list
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[THEME_MODE] = mode }
    }

    suspend fun setAccentColor(colorHex: String) {
        context.dataStore.edit { it[ACCENT_COLOR] = colorHex }
    }

    suspend fun setIconShape(shape: String) {
        context.dataStore.edit { it[ICON_SHAPE] = shape }
    }

    suspend fun setIconStyle(style: String) {
        context.dataStore.edit { it[ICON_STYLE] = style }
    }

    suspend fun setFocusMode(enabled: Boolean) {
        context.dataStore.edit { it[FOCUS_MODE] = enabled }
    }

    suspend fun setFocusWhitelist(whitelist: Set<String>) {
        context.dataStore.edit { it[FOCUS_WHITELIST] = whitelist }
    }

    suspend fun setHiddenPin(pin: String) {
        context.dataStore.edit { it[HIDDEN_PIN] = pin }
    }

    suspend fun setActiveWorkspace(workspaceId: String) {
        context.dataStore.edit { it[ACTIVE_WORKSPACE_ID] = workspaceId }
    }

    suspend fun setAutomationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[AUTOMATIONS_ENABLED] = enabled }
    }

    suspend fun recordCommandUsage(command: String) {
        context.dataStore.edit { prefs ->
            val jsonStr = prefs[RECENT_COMMANDS] ?: "[]"
            val list = mutableListOf<String>()
            try {
                val arr = JSONArray(jsonStr)
                for (i in 0 until arr.length()) list.add(arr.getString(i))
            } catch (_: Exception) {}
            list.remove(command)
            list.add(0, command)
            val trimmed = list.take(12)
            prefs[RECENT_COMMANDS] = JSONArray(trimmed).toString()
        }
    }

    suspend fun toggleFavoriteCommand(command: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[FAVORITE_COMMANDS] ?: emptySet()
            if (current.contains(command)) {
                prefs[FAVORITE_COMMANDS] = current - command
            } else {
                prefs[FAVORITE_COMMANDS] = current + command
            }
        }
    }

    suspend fun saveWorkspaces(spaces: List<WorkspaceSpace>) {
        val arr = JSONArray()
        for (s in spaces) {
            val obj = JSONObject().apply {
                put("id", s.id)
                put("name", s.name)
                put("description", s.description)
                put("iconName", s.iconName)
                put("accentHex", s.accentHex)
                put("appPackageNames", JSONArray(s.appPackageNames))
                put("isCustom", s.isCustom)
            }
            arr.put(obj)
        }
        context.dataStore.edit { it[USER_WORKSPACES] = arr.toString() }
    }

    suspend fun saveCommandMacros(macros: List<CommandMacro>) {
        val arr = JSONArray()
        for (m in macros) {
            val actArr = JSONArray()
            for (a in m.actions) {
                actArr.put(
                    JSONObject().apply {
                        put("type", a.type)
                        put("param", a.param)
                        put("description", a.description)
                    }
                )
            }
            val obj = JSONObject().apply {
                put("id", m.id)
                put("commandName", m.commandName)
                put("title", m.title)
                put("description", m.description)
                put("actions", actArr)
                put("isCustom", m.isCustom)
            }
            arr.put(obj)
        }
        context.dataStore.edit { it[COMMAND_MACROS] = arr.toString() }
    }

    suspend fun saveAppConstellations(constellations: List<AppConstellation>) {
        val arr = JSONArray()
        for (c in constellations) {
            val obj = JSONObject().apply {
                put("id", c.id)
                put("name", c.name)
                put("description", c.description)
                put("accentHex", c.accentHex)
                put("packageNames", JSONArray(c.packageNames))
            }
            arr.put(obj)
        }
        context.dataStore.edit { it[APP_CONSTELLATIONS] = arr.toString() }
    }

    suspend fun saveWorkspaceMemory(memory: WorkspaceMemory) {
        context.dataStore.edit { prefs ->
            val jsonStr = prefs[WORKSPACE_MEMORIES] ?: "{}"
            val root = try { JSONObject(jsonStr) } catch (_: Exception) { JSONObject() }
            val obj = JSONObject().apply {
                put("workspaceId", memory.workspaceId)
                put("favoritePackages", JSONArray(memory.favoritePackages))
                put("dockPackages", JSONArray(memory.dockPackages))
                put("accentColor", memory.accentColor)
                put("layoutMode", memory.layoutMode)
            }
            root.put(memory.workspaceId, obj)
            prefs[WORKSPACE_MEMORIES] = root.toString()
        }
    }

    suspend fun saveAutomationRules(rules: List<AutomationRule>) {
        val arr = JSONArray()
        for (r in rules) {
            val obj = JSONObject().apply {
                put("id", r.id)
                put("title", r.title)
                put("triggerType", r.triggerType)
                put("triggerValue", r.triggerValue)
                put("actionType", r.actionType)
                put("actionValue", r.actionValue)
                put("isEnabled", r.isEnabled)
            }
            arr.put(obj)
        }
        context.dataStore.edit { it[AUTOMATION_RULES] = arr.toString() }
    }

    suspend fun toggleFavorite(packageName: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[FAVORITE_PACKAGES] ?: emptySet()
            if (current.contains(packageName)) {
                prefs[FAVORITE_PACKAGES] = current - packageName
            } else {
                prefs[FAVORITE_PACKAGES] = current + packageName
            }
        }
    }

    suspend fun setAppHidden(packageName: String, hidden: Boolean) {
        context.dataStore.edit { prefs ->
            val current = prefs[HIDDEN_PACKAGES] ?: emptySet()
            if (hidden) {
                prefs[HIDDEN_PACKAGES] = current + packageName
            } else {
                prefs[HIDDEN_PACKAGES] = current - packageName
            }
        }
    }

    suspend fun recordAppLaunch(packageName: String) {
        context.dataStore.edit { prefs ->
            val jsonStr = prefs[LAUNCH_COUNTS] ?: "{}"
            val obj = try { JSONObject(jsonStr) } catch (_: Exception) { JSONObject() }
            val currentCount = obj.optInt(packageName, 0)
            obj.put(packageName, currentCount + 1)
            prefs[LAUNCH_COUNTS] = obj.toString()
        }
    }

    suspend fun saveWeatherLocation(lat: Double, lon: Double, cityName: String) {
        context.dataStore.edit {
            it[WEATHER_LAT] = lat
            it[WEATHER_LON] = lon
            it[WEATHER_CITY] = cityName
        }
    }

    suspend fun persistWidgets(widgets: List<LauncherWidget>) {
        val array = JSONArray()
        for (w in widgets) {
            val obj = JSONObject().apply {
                put("appWidgetId", w.appWidgetId)
                put("providerPackage", w.providerPackage)
                put("providerClass", w.providerClass)
                put("cellWidth", w.cellWidth)
                put("cellHeight", w.cellHeight)
                put("orderIndex", w.orderIndex)
            }
            array.put(obj)
        }
        context.dataStore.edit { it[SAVED_WIDGETS] = array.toString() }
    }

    fun defaultWorkspaces(): List<WorkspaceSpace> {
        return listOf(
            WorkspaceSpace("personal", "Personal", "Daily life, messaging & leisure", "user", "#2A9D8F"),
            WorkspaceSpace("work", "Work", "Productivity, email, docs & collaboration", "briefcase", "#457B9D"),
            WorkspaceSpace("study", "Study", "Reading, research, notes & focus", "book", "#52796F"),
            WorkspaceSpace("gaming", "Gaming", "Games, performance & media", "gamepad", "#E76F51"),
            WorkspaceSpace("travel", "Travel", "Maps, transit, bookings & photos", "compass", "#E9C46A")
        )
    }

    fun defaultMacros(): List<CommandMacro> {
        return listOf(
            CommandMacro(
                id = "macro-study",
                commandName = "/start-study",
                title = "Start Study Session",
                description = "Switch to Study Space and enable Focus Mode",
                actions = listOf(
                    MacroAction("SWITCH_WORKSPACE", "study", "Switch to Study Space"),
                    MacroAction("ENABLE_FOCUS", "true", "Engage distraction-free Focus Mode")
                )
            ),
            CommandMacro(
                id = "macro-gaming",
                commandName = "/gaming-mode",
                title = "Enter Gaming Mode",
                description = "Switch to Gaming Space and prepare performance dock",
                actions = listOf(
                    MacroAction("SWITCH_WORKSPACE", "gaming", "Switch to Gaming Space")
                )
            ),
            CommandMacro(
                id = "macro-work",
                commandName = "/work-start",
                title = "Start Work Routine",
                description = "Switch to Work Space and restore productivity layout",
                actions = listOf(
                    MacroAction("SWITCH_WORKSPACE", "work", "Switch to Work Space")
                )
            ),
            CommandMacro(
                id = "macro-night",
                commandName = "/night-mode",
                title = "Night Focus",
                description = "Switch to Charcoal Dark theme and enable Focus Mode",
                actions = listOf(
                    MacroAction("SET_THEME", "charcoal-dark", "Switch theme to Charcoal Dark"),
                    MacroAction("ENABLE_FOCUS", "true", "Enable quiet focus mode")
                )
            ),
            CommandMacro(
                id = "macro-travel",
                commandName = "/travel-ready",
                title = "Travel Ready",
                description = "Switch to Travel Space for maps and transit essentials",
                actions = listOf(
                    MacroAction("SWITCH_WORKSPACE", "travel", "Switch to Travel Space")
                )
            )
        )
    }

    fun defaultConstellations(): List<AppConstellation> {
        return listOf(
            AppConstellation(
                id = "const-study",
                name = "Study Orbit",
                description = "Research, notes and calculator",
                accentHex = "#52796F",
                packageNames = listOf("com.android.chrome", "com.google.android.keep", "com.google.android.calculator")
            ),
            AppConstellation(
                id = "const-gaming",
                name = "Gaming Nexus",
                description = "Discord, games and streams",
                accentHex = "#E76F51",
                packageNames = listOf("com.discord", "com.google.android.youtube")
            ),
            AppConstellation(
                id = "const-travel",
                name = "Travel Suite",
                description = "Navigation, bookings and translate",
                accentHex = "#E9C46A",
                packageNames = listOf("com.google.android.apps.maps")
            )
        )
    }

    fun defaultAutomationRules(): List<AutomationRule> {
        return listOf(
            AutomationRule(
                id = "rule-study-morning",
                title = "Morning Study Recommendation",
                triggerType = "TIME_OF_DAY",
                triggerValue = "08:00",
                actionType = "SUGGEST_WORKSPACE",
                actionValue = "study",
                isEnabled = true
            ),
            AutomationRule(
                id = "rule-focus-work",
                title = "Auto-Work on Focus Mode",
                triggerType = "FOCUS_MODE",
                triggerValue = "true",
                actionType = "SWITCH_WORKSPACE",
                actionValue = "work",
                isEnabled = true
            ),
            AutomationRule(
                id = "rule-battery-saver",
                title = "Battery Saver Advice",
                triggerType = "BATTERY_LEVEL",
                triggerValue = "20",
                actionType = "SUGGEST_BATTERY_SAVER",
                actionValue = "battery",
                isEnabled = true
            )
        )
    }

    suspend fun exportBackupJson(): String {
        val prefs = context.dataStore.data.first()
        val root = JSONObject().apply {
            put("theme_mode", prefs[THEME_MODE] ?: "warm-light")
            put("accent_color", prefs[ACCENT_COLOR] ?: "#2A9D8F")
            put("icon_shape", prefs[ICON_SHAPE] ?: "squircle")
            put("icon_style", prefs[ICON_STYLE] ?: "default")
            put("focus_mode_enabled", prefs[FOCUS_MODE] ?: false)
            put("active_workspace_id", prefs[ACTIVE_WORKSPACE_ID] ?: "personal")
            put("saved_widgets_json", prefs[SAVED_WIDGETS] ?: "[]")
            put("command_macros_json", prefs[COMMAND_MACROS] ?: "[]")
            put("app_constellations_json", prefs[APP_CONSTELLATIONS] ?: "[]")
            put("automation_rules_json", prefs[AUTOMATION_RULES] ?: "[]")
            put("context_rings_json", prefs[CONTEXT_RINGS] ?: "")
        }
        return root.toString(2)
    }

    suspend fun restoreBackupJson(jsonString: String): Boolean {
        return try {
            val obj = JSONObject(jsonString)
            context.dataStore.edit { prefs ->
                if (obj.has("theme_mode")) prefs[THEME_MODE] = obj.getString("theme_mode")
                if (obj.has("accent_color")) prefs[ACCENT_COLOR] = obj.getString("accent_color")
                if (obj.has("icon_shape")) prefs[ICON_SHAPE] = obj.getString("icon_shape")
                if (obj.has("active_workspace_id")) prefs[ACTIVE_WORKSPACE_ID] = obj.getString("active_workspace_id")
                if (obj.has("command_macros_json")) prefs[COMMAND_MACROS] = obj.getString("command_macros_json")
                if (obj.has("app_constellations_json")) prefs[APP_CONSTELLATIONS] = obj.getString("app_constellations_json")
                if (obj.has("automation_rules_json")) prefs[AUTOMATION_RULES] = obj.getString("automation_rules_json")
                if (obj.has("context_rings_json")) prefs[CONTEXT_RINGS] = obj.getString("context_rings_json")
            }
            true
        } catch (_: Exception) {
            false
        }
    }
}
