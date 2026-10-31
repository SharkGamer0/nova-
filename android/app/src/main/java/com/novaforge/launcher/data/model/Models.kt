package com.novaforge.launcher.data.model

data class AppItem(
    val packageName: String,
    val className: String = "",
    val label: String,
    val colorHex: Int = 0xFF2A9D8F.toInt(),
    val category: String = "Tools",
    val isFavorite: Boolean = false,
    val isHidden: Boolean = false,
    val launchCount: Int = 0,
    val lastUsedTime: Long = System.currentTimeMillis()
)

data class AgendaItem(
    val id: String,
    val title: String,
    val time: String,
    val completed: Boolean = false,
    val isDemoData: Boolean = false
)

data class LauncherWidget(
    val appWidgetId: Int,
    val providerPackage: String,
    val providerClass: String,
    val cellWidth: Int = 2,
    val cellHeight: Int = 2,
    val orderIndex: Int = 0
)

data class BatteryInfo(
    val percentage: Int = 100,
    val isCharging: Boolean = false,
    val statusText: String = "Normal"
)

data class WorkspaceSpace(
    val id: String,
    val name: String,
    val description: String,
    val iconName: String,
    val accentHex: String = "#2A9D8F",
    val appPackageNames: List<String> = emptyList(),
    val isCustom: Boolean = false
)

data class CommandAction(
    val command: String,
    val title: String,
    val subtitle: String,
    val category: String, // "System", "Workspace", "App", "Launcher", "Macro"
    val iconName: String = "command",
    val macroId: String? = null
)

data class MacroAction(
    val type: String, // "SWITCH_WORKSPACE", "ENABLE_FOCUS", "DISABLE_FOCUS", "LAUNCH_APP", "SET_THEME", "OPEN_SETTINGS"
    val param: String,
    val description: String
)

data class CommandMacro(
    val id: String,
    val commandName: String, // e.g. "/start-study"
    val title: String,
    val description: String,
    val actions: List<MacroAction>,
    val isCustom: Boolean = false
)

data class AppConstellation(
    val id: String,
    val name: String,
    val description: String,
    val accentHex: String = "#2A9D8F",
    val packageNames: List<String> = emptyList()
)

data class WorkspaceMemory(
    val workspaceId: String,
    val favoritePackages: List<String> = emptyList(),
    val dockPackages: List<String> = emptyList(),
    val accentColor: String = "#2A9D8F",
    val layoutMode: String = "list"
)

data class AutomationRule(
    val id: String,
    val title: String,
    val triggerType: String, // "TIME_OF_DAY", "FOCUS_MODE", "BATTERY_LEVEL"
    val triggerValue: String, // e.g. "08:00", "true", "20"
    val actionType: String, // "SUGGEST_WORKSPACE", "SWITCH_WORKSPACE", "SUGGEST_BATTERY_SAVER"
    val actionValue: String, // e.g. "study", "work"
    val isEnabled: Boolean = true
)

data class WorkspaceSuggestion(
    val spaceId: String,
    val spaceName: String,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)
