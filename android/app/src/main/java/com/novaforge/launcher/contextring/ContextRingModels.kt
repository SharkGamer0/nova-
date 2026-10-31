package com.novaforge.launcher.contextring

enum class RingActionType {
    LAUNCH_APP,
    APP_SHORTCUT,
    SWITCH_WORKSPACE,
    OPEN_APP_DRAWER,
    OPEN_SETTINGS,
    OPEN_WIDGETS,
    OPEN_COMMAND_BAR,
    TOGGLE_FOCUS_MODE,
    OPEN_CONSTELLATIONS,
    SYSTEM_SETTING,
    EXECUTE_MACRO,
    MEDIA_CONTROL,
    NESTED_RING
}

data class ContextRingAction(
    val id: String,
    val title: String,
    val iconName: String,
    val actionType: RingActionType,
    val payload: String = "",
    val secondaryLabel: String = "",
    val accentHex: String = "#2A9D8F",
    val requiresConfirmation: Boolean = false
)

data class ContextRingDefinition(
    val id: String,
    val title: String,
    val description: String = "",
    val slotCount: Int = 6,
    val radiusDp: Int = 140,
    val iconSizeDp: Int = 44,
    val actions: List<ContextRingAction> = emptyList(),
    val isSystemPreset: Boolean = false,
    val parentRingId: String? = null
)

data class ContextRingConfig(
    val defaultGlobalRingId: String = "global_default",
    val rings: List<ContextRingDefinition> = emptyList(),
    val appAssignments: Map<String, String> = emptyMap(),
    val workspaceAssignments: Map<String, String> = emptyMap(),
    val folderAssignments: Map<String, String> = emptyMap(),
    val hapticIntensity: String = "medium",
    val gestureTrigger: String = "long_press",
    val showLocalSuggestions: Boolean = true
)

data class ActiveRingSession(
    val currentRing: ContextRingDefinition,
    val originX: Float,
    val originY: Float,
    val clampedCenterX: Float = originX,
    val clampedCenterY: Float = originY,
    val targetPackage: String? = null,
    val targetWorkspaceId: String? = null,
    val targetFolderId: String? = null,
    val breadcrumbRingIds: List<String> = emptyList(),
    val selectedIndex: Int = -1,
    val highlightedAction: ContextRingAction? = null,
    val currentAngleDeg: Float = 0f,
    val currentDistancePx: Float = 0f,
    val isEngaged: Boolean = false
)
