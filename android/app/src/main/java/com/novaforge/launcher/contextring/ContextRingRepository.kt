package com.novaforge.launcher.contextring

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.novaforge.launcher.data.LauncherPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class ContextRingRepository(
    private val context: Context,
    private val preferences: LauncherPreferences
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        val CONTEXT_RINGS_KEY = stringPreferencesKey("context_rings_json")
    }

    private val _config = MutableStateFlow(createDefaultConfig())
    val config: StateFlow<ContextRingConfig> = _config.asStateFlow()

    init {
        scope.launch {
            loadPersistedConfig()
        }
    }

    private suspend fun loadPersistedConfig() {
        try {
            val jsonStr = preferences.contextRingsRawJson.first()
            if (!jsonStr.isNullOrBlank()) {
                val parsed = parseConfigFromJson(jsonStr)
                if (parsed.rings.isNotEmpty()) {
                    _config.value = parsed
                    return
                }
            }
        } catch (_: Exception) {
            // Fall back to default
        }
        val def = createDefaultConfig()
        _config.value = def
        persistConfig(def)
    }

    private fun persistConfig(newConfig: ContextRingConfig) {
        _config.value = newConfig
        scope.launch {
            try {
                val json = serializeConfigToJson(newConfig)
                preferences.saveContextRingsRawJson(json)
            } catch (_: Exception) {}
        }
    }

    fun saveRing(ring: ContextRingDefinition) {
        val existing = _config.value.rings.toMutableList()
        val index = existing.indexOfFirst { it.id == ring.id }
        if (index != -1) {
            existing[index] = ring
        } else {
            existing.add(ring)
        }
        persistConfig(_config.value.copy(rings = existing))
    }

    fun deleteRing(ringId: String) {
        // Prevent deleting the default global ring
        if (ringId == "global_default") return
        val filtered = _config.value.rings.filter { it.id != ringId }
        val updatedAppAssignments = _config.value.appAssignments.filterValues { it != ringId }
        val updatedWorkspaceAssignments = _config.value.workspaceAssignments.filterValues { it != ringId }
        val updatedFolderAssignments = _config.value.folderAssignments.filterValues { it != ringId }
        persistConfig(
            _config.value.copy(
                rings = filtered,
                appAssignments = updatedAppAssignments,
                workspaceAssignments = updatedWorkspaceAssignments,
                folderAssignments = updatedFolderAssignments
            )
        )
    }

    fun duplicateRing(ringId: String): ContextRingDefinition? {
        val original = _config.value.rings.find { it.id == ringId } ?: return null
        val newId = "ring_" + UUID.randomUUID().toString().take(8)
        val copy = original.copy(
            id = newId,
            title = "${original.title} (Copy)",
            isSystemPreset = false
        )
        saveRing(copy)
        return copy
    }

    fun resetToDefaults() {
        val def = createDefaultConfig()
        persistConfig(def)
    }

    fun assignToApp(packageName: String, ringId: String) {
        val updated = _config.value.appAssignments.toMutableMap()
        if (ringId.isEmpty() || ringId == "none") {
            updated.remove(packageName)
        } else {
            updated[packageName] = ringId
        }
        persistConfig(_config.value.copy(appAssignments = updated))
    }

    fun assignToWorkspace(workspaceId: String, ringId: String) {
        val updated = _config.value.workspaceAssignments.toMutableMap()
        if (ringId.isEmpty() || ringId == "none") {
            updated.remove(workspaceId)
        } else {
            updated[workspaceId] = ringId
        }
        persistConfig(_config.value.copy(workspaceAssignments = updated))
    }

    fun assignToFolder(folderId: String, ringId: String) {
        val updated = _config.value.folderAssignments.toMutableMap()
        if (ringId.isEmpty() || ringId == "none") {
            updated.remove(folderId)
        } else {
            updated[folderId] = ringId
        }
        persistConfig(_config.value.copy(folderAssignments = updated))
    }

    fun updateHapticIntensity(intensity: String) {
        persistConfig(_config.value.copy(hapticIntensity = intensity))
    }

    fun updateGestureTrigger(gesture: String) {
        persistConfig(_config.value.copy(gestureTrigger = gesture))
    }

    fun toggleLocalSuggestions(enabled: Boolean) {
        persistConfig(_config.value.copy(showLocalSuggestions = enabled))
    }

    fun exportToJson(): String {
        return serializeConfigToJson(_config.value)
    }

    fun importFromJson(jsonString: String): Boolean {
        return try {
            val parsed = parseConfigFromJson(jsonString)
            if (parsed.rings.isNotEmpty()) {
                persistConfig(parsed)
                true
            } else false
        } catch (_: Exception) {
            false
        }
    }

    // Default configuration with pre-crafted rings
    fun createDefaultConfig(): ContextRingConfig {
        val globalDefault = ContextRingDefinition(
            id = "global_default",
            title = "Global Command Ring",
            description = "Launcher quick actions and navigation hub",
            slotCount = 6,
            radiusDp = 140,
            iconSizeDp = 44,
            isSystemPreset = true,
            actions = listOf(
                ContextRingAction("act_app_drawer", "App Drawer", "grid", RingActionType.OPEN_APP_DRAWER, accentHex = "#2A9D8F"),
                ContextRingAction("act_command", "Command Bar", "terminal", RingActionType.OPEN_COMMAND_BAR, accentHex = "#457B9D"),
                ContextRingAction("act_focus", "Focus Mode", "shield", RingActionType.TOGGLE_FOCUS_MODE, accentHex = "#E76F51"),
                ContextRingAction("act_widgets", "Widgets", "layers", RingActionType.OPEN_WIDGETS, accentHex = "#E9C46A"),
                ContextRingAction("act_constellations", "Constellations", "compass", RingActionType.OPEN_CONSTELLATIONS, accentHex = "#9B5DE5"),
                ContextRingAction("act_settings", "Settings", "settings", RingActionType.OPEN_SETTINGS, accentHex = "#6C757D")
            )
        )

        val browserRing = ContextRingDefinition(
            id = "browser_ring",
            title = "Browser Context Ring",
            description = "Web browsing shortcuts and actions",
            slotCount = 6,
            radiusDp = 140,
            iconSizeDp = 44,
            isSystemPreset = true,
            actions = listOf(
                ContextRingAction("act_br_launch", "Open Browser", "globe", RingActionType.LAUNCH_APP, payload = "com.brave.browser", accentHex = "#FB542B"),
                ContextRingAction("act_br_search", "Search Web", "search", RingActionType.OPEN_COMMAND_BAR, accentHex = "#2A9D8F"),
                ContextRingAction("act_br_wifi", "Wi-Fi Config", "wifi", RingActionType.SYSTEM_SETTING, payload = "wifi", accentHex = "#457B9D"),
                ContextRingAction("act_br_focus", "Focus Mode", "shield", RingActionType.TOGGLE_FOCUS_MODE, accentHex = "#E76F51"),
                ContextRingAction("act_br_display", "Display Mode", "sun", RingActionType.SYSTEM_SETTING, payload = "display", accentHex = "#E9C46A"),
                ContextRingAction("act_br_settings", "Launcher Settings", "settings", RingActionType.OPEN_SETTINGS, accentHex = "#6C757D")
            )
        )

        val musicRing = ContextRingDefinition(
            id = "music_ring",
            title = "Music Context Ring",
            description = "Direct playback controls and audio utilities",
            slotCount = 6,
            radiusDp = 140,
            iconSizeDp = 44,
            isSystemPreset = true,
            actions = listOf(
                ContextRingAction("act_mus_play", "Play / Pause", "play", RingActionType.MEDIA_CONTROL, payload = "play_pause", accentHex = "#1DB954"),
                ContextRingAction("act_mus_next", "Next Track", "skip-forward", RingActionType.MEDIA_CONTROL, payload = "next", accentHex = "#1DB954"),
                ContextRingAction("act_mus_prev", "Previous Track", "skip-back", RingActionType.MEDIA_CONTROL, payload = "prev", accentHex = "#1DB954"),
                ContextRingAction("act_mus_app", "Open Spotify", "music", RingActionType.LAUNCH_APP, payload = "com.spotify.music", accentHex = "#1DB954"),
                ContextRingAction("act_mus_bluetooth", "Bluetooth Devices", "bluetooth", RingActionType.SYSTEM_SETTING, payload = "bluetooth", accentHex = "#457B9D"),
                ContextRingAction("act_mus_focus", "Focus Music", "shield", RingActionType.TOGGLE_FOCUS_MODE, accentHex = "#E76F51")
            )
        )

        val studyToolsNested = ContextRingDefinition(
            id = "study_tools_nested",
            title = "Study Tools",
            description = "Sub-ring for calculator and study helpers",
            slotCount = 4,
            radiusDp = 130,
            iconSizeDp = 44,
            isSystemPreset = true,
            parentRingId = "study_ring",
            actions = listOf(
                ContextRingAction("act_st_calc", "Calculator", "hash", RingActionType.LAUNCH_APP, payload = "com.google.android.calculator", accentHex = "#E9C46A"),
                ContextRingAction("act_st_notes", "Notes", "edit-3", RingActionType.LAUNCH_APP, payload = "com.google.android.keep", accentHex = "#F4A261"),
                ContextRingAction("act_st_focus", "Deep Work Mode", "shield", RingActionType.TOGGLE_FOCUS_MODE, accentHex = "#E76F51"),
                ContextRingAction("act_st_back", "Back to Study", "arrow-left", RingActionType.NESTED_RING, payload = "study_ring", accentHex = "#2A9D8F")
            )
        )

        val studyRing = ContextRingDefinition(
            id = "study_ring",
            title = "Study Workspace Ring",
            description = "Dedicated tools for productive sessions",
            slotCount = 6,
            radiusDp = 145,
            iconSizeDp = 44,
            isSystemPreset = true,
            actions = listOf(
                ContextRingAction("act_std_tools", "Study Tools", "folder", RingActionType.NESTED_RING, payload = "study_tools_nested", accentHex = "#2A9D8F"),
                ContextRingAction("act_std_macro", "Start Study Macro", "play-circle", RingActionType.EXECUTE_MACRO, payload = "macro-study", requiresConfirmation = true, accentHex = "#F4A261"),
                ContextRingAction("act_std_constellation", "Study Constellation", "compass", RingActionType.OPEN_CONSTELLATIONS, accentHex = "#9B5DE5"),
                ContextRingAction("act_std_space", "Study Workspace", "layers", RingActionType.SWITCH_WORKSPACE, payload = "study", accentHex = "#457B9D"),
                ContextRingAction("act_std_focus", "Focus Mode", "shield", RingActionType.TOGGLE_FOCUS_MODE, accentHex = "#E76F51"),
                ContextRingAction("act_std_battery", "Battery Check", "battery", RingActionType.SYSTEM_SETTING, payload = "battery", accentHex = "#6C757D")
            )
        )

        val gamingRing = ContextRingDefinition(
            id = "gaming_ring",
            title = "Gaming Workspace Ring",
            description = "Quick gaming shortcuts and display settings",
            slotCount = 6,
            radiusDp = 145,
            iconSizeDp = 44,
            isSystemPreset = true,
            actions = listOf(
                ContextRingAction("act_gm_space", "Gaming Workspace", "play", RingActionType.SWITCH_WORKSPACE, payload = "gaming", accentHex = "#E76F51"),
                ContextRingAction("act_gm_constellation", "Gaming Suite", "compass", RingActionType.OPEN_CONSTELLATIONS, accentHex = "#9B5DE5"),
                ContextRingAction("act_gm_discord", "Discord", "message-circle", RingActionType.LAUNCH_APP, payload = "com.discord", accentHex = "#5865F2"),
                ContextRingAction("act_gm_display", "Display Refresh", "sun", RingActionType.SYSTEM_SETTING, payload = "display", accentHex = "#E9C46A"),
                ContextRingAction("act_gm_wifi", "Game Wi-Fi", "wifi", RingActionType.SYSTEM_SETTING, payload = "wifi", accentHex = "#457B9D"),
                ContextRingAction("act_gm_battery", "Performance Mode", "zap", RingActionType.SYSTEM_SETTING, payload = "battery", accentHex = "#E76F51")
            )
        )

        return ContextRingConfig(
            defaultGlobalRingId = "global_default",
            rings = listOf(globalDefault, browserRing, musicRing, studyRing, studyToolsNested, gamingRing),
            appAssignments = mapOf(
                "com.brave.browser" to "browser_ring",
                "com.android.chrome" to "browser_ring",
                "com.spotify.music" to "music_ring"
            ),
            workspaceAssignments = mapOf(
                "study" to "study_ring",
                "gaming" to "gaming_ring"
            ),
            folderAssignments = emptyMap(),
            hapticIntensity = "medium",
            gestureTrigger = "long_press",
            showLocalSuggestions = true
        )
    }

    private fun serializeConfigToJson(config: ContextRingConfig): String {
        val root = JSONObject()
        root.put("defaultGlobalRingId", config.defaultGlobalRingId)
        root.put("hapticIntensity", config.hapticIntensity)
        root.put("gestureTrigger", config.gestureTrigger)
        root.put("showLocalSuggestions", config.showLocalSuggestions)

        // Rings array
        val ringsArr = JSONArray()
        for (ring in config.rings) {
            val rObj = JSONObject()
            rObj.put("id", ring.id)
            rObj.put("title", ring.title)
            rObj.put("description", ring.description)
            rObj.put("slotCount", ring.slotCount)
            rObj.put("radiusDp", ring.radiusDp)
            rObj.put("iconSizeDp", ring.iconSizeDp)
            rObj.put("isSystemPreset", ring.isSystemPreset)
            if (ring.parentRingId != null) rObj.put("parentRingId", ring.parentRingId)

            val actionsArr = JSONArray()
            for (act in ring.actions) {
                val aObj = JSONObject()
                aObj.put("id", act.id)
                aObj.put("title", act.title)
                aObj.put("iconName", act.iconName)
                aObj.put("actionType", act.actionType.name)
                aObj.put("payload", act.payload)
                aObj.put("secondaryLabel", act.secondaryLabel)
                aObj.put("accentHex", act.accentHex)
                aObj.put("requiresConfirmation", act.requiresConfirmation)
                actionsArr.put(aObj)
            }
            rObj.put("actions", actionsArr)
            ringsArr.put(rObj)
        }
        root.put("rings", ringsArr)

        // App assignments
        val appObj = JSONObject()
        for ((pkg, ringId) in config.appAssignments) {
            appObj.put(pkg, ringId)
        }
        root.put("appAssignments", appObj)

        // Workspace assignments
        val wsObj = JSONObject()
        for ((wsId, ringId) in config.workspaceAssignments) {
            wsObj.put(wsId, ringId)
        }
        root.put("workspaceAssignments", wsObj)

        // Folder assignments
        val fldObj = JSONObject()
        for ((fldId, ringId) in config.folderAssignments) {
            fldObj.put(fldId, ringId)
        }
        root.put("folderAssignments", fldObj)

        return root.toString(2)
    }

    private fun parseConfigFromJson(jsonStr: String): ContextRingConfig {
        val root = JSONObject(jsonStr)
        val defaultRingId = root.optString("defaultGlobalRingId", "global_default")
        val haptic = root.optString("hapticIntensity", "medium")
        val trigger = root.optString("gestureTrigger", "long_press")
        val suggestions = root.optBoolean("showLocalSuggestions", true)

        val ringsList = mutableListOf<ContextRingDefinition>()
        val ringsArr = root.optJSONArray("rings") ?: JSONArray()
        for (i in 0 until ringsArr.length()) {
            val rObj = ringsArr.getJSONObject(i)
            val actionsList = mutableListOf<ContextRingAction>()
            val actsArr = rObj.optJSONArray("actions") ?: JSONArray()
            for (j in 0 until actsArr.length()) {
                val aObj = actsArr.getJSONObject(j)
                val typeStr = aObj.optString("actionType", "LAUNCH_APP")
                val actionType = try {
                    RingActionType.valueOf(typeStr)
                } catch (_: Exception) {
                    RingActionType.LAUNCH_APP
                }
                actionsList.add(
                    ContextRingAction(
                        id = aObj.optString("id", UUID.randomUUID().toString()),
                        title = aObj.optString("title", "Action"),
                        iconName = aObj.optString("iconName", "circle"),
                        actionType = actionType,
                        payload = aObj.optString("payload", ""),
                        secondaryLabel = aObj.optString("secondaryLabel", ""),
                        accentHex = aObj.optString("accentHex", "#2A9D8F"),
                        requiresConfirmation = aObj.optBoolean("requiresConfirmation", false)
                    )
                )
            }
            ringsList.add(
                ContextRingDefinition(
                    id = rObj.optString("id", UUID.randomUUID().toString()),
                    title = rObj.optString("title", "Custom Ring"),
                    description = rObj.optString("description", ""),
                    slotCount = rObj.optInt("slotCount", 6),
                    radiusDp = rObj.optInt("radiusDp", 140),
                    iconSizeDp = rObj.optInt("iconSizeDp", 44),
                    actions = actionsList,
                    isSystemPreset = rObj.optBoolean("isSystemPreset", false),
                    parentRingId = if (rObj.has("parentRingId")) rObj.getString("parentRingId") else null
                )
            )
        }

        val appMap = mutableMapOf<String, String>()
        val appObj = root.optJSONObject("appAssignments")
        if (appObj != null) {
            val keys = appObj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                appMap[k] = appObj.getString(k)
            }
        }

        val wsMap = mutableMapOf<String, String>()
        val wsObj = root.optJSONObject("workspaceAssignments")
        if (wsObj != null) {
            val keys = wsObj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                wsMap[k] = wsObj.getString(k)
            }
        }

        val fldMap = mutableMapOf<String, String>()
        val fldObj = root.optJSONObject("folderAssignments")
        if (fldObj != null) {
            val keys = fldObj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                fldMap[k] = fldObj.getString(k)
            }
        }

        return ContextRingConfig(
            defaultGlobalRingId = defaultRingId,
            rings = ringsList,
            appAssignments = appMap,
            workspaceAssignments = wsMap,
            folderAssignments = fldMap,
            hapticIntensity = haptic,
            gestureTrigger = trigger,
            showLocalSuggestions = suggestions
        )
    }
}
