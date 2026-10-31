package com.novaforge.launcher.contextring

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.provider.Settings
import android.view.KeyEvent

class ContextRingActionExecutor(private val context: Context) {

    fun executeAction(
        action: ContextRingAction,
        onLaunchApp: (String) -> Unit,
        onSwitchWorkspace: (String) -> Unit,
        onOpenAppDrawer: () -> Unit,
        onOpenSettings: () -> Unit,
        onOpenWidgets: () -> Unit,
        onOpenCommandBar: () -> Unit,
        onToggleFocusMode: () -> Unit,
        onOpenConstellations: () -> Unit,
        onExecuteMacroById: (String) -> Unit,
        onPushNestedRing: (String) -> Unit
    ) {
        when (action.actionType) {
            RingActionType.LAUNCH_APP -> {
                if (action.payload.isNotBlank()) {
                    onLaunchApp(action.payload)
                }
            }
            RingActionType.APP_SHORTCUT -> {
                if (action.payload.isNotBlank()) {
                    onLaunchApp(action.payload)
                }
            }
            RingActionType.SWITCH_WORKSPACE -> {
                if (action.payload.isNotBlank()) {
                    onSwitchWorkspace(action.payload)
                }
            }
            RingActionType.OPEN_APP_DRAWER -> onOpenAppDrawer()
            RingActionType.OPEN_SETTINGS -> onOpenSettings()
            RingActionType.OPEN_WIDGETS -> onOpenWidgets()
            RingActionType.OPEN_COMMAND_BAR -> onOpenCommandBar()
            RingActionType.TOGGLE_FOCUS_MODE -> onToggleFocusMode()
            RingActionType.OPEN_CONSTELLATIONS -> onOpenConstellations()
            RingActionType.SYSTEM_SETTING -> launchSystemSetting(action.payload)
            RingActionType.EXECUTE_MACRO -> {
                if (action.payload.isNotBlank()) {
                    onExecuteMacroById(action.payload)
                }
            }
            RingActionType.MEDIA_CONTROL -> handleMediaControl(action.payload)
            RingActionType.NESTED_RING -> {
                if (action.payload.isNotBlank()) {
                    onPushNestedRing(action.payload)
                }
            }
        }
    }

    private fun launchSystemSetting(type: String) {
        val action = when (type.lowercase()) {
            "wifi" -> Settings.ACTION_WIFI_SETTINGS
            "bluetooth" -> Settings.ACTION_BLUETOOTH_SETTINGS
            "display" -> Settings.ACTION_DISPLAY_SETTINGS
            "battery" -> Intent.ACTION_POWER_USAGE_SUMMARY
            "wallpaper" -> Intent.ACTION_SET_WALLPAPER
            "sound" -> Settings.ACTION_SOUND_SETTINGS
            else -> Settings.ACTION_SETTINGS
        }
        try {
            val intent = Intent(action).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            // Fall back to general settings
            try {
                val fallback = Intent(Settings.ACTION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallback)
            } catch (_: Exception) {}
        }
    }

    private fun handleMediaControl(command: String) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
        val keyCode = when (command.lowercase()) {
            "play_pause", "toggle" -> KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
            "next", "skip" -> KeyEvent.KEYCODE_MEDIA_NEXT
            "prev", "previous" -> KeyEvent.KEYCODE_MEDIA_PREVIOUS
            "stop" -> KeyEvent.KEYCODE_MEDIA_STOP
            else -> KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
        }

        try {
            val downEvent = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
            audioManager.dispatchMediaKeyEvent(downEvent)
            val upEvent = KeyEvent(KeyEvent.ACTION_UP, keyCode)
            audioManager.dispatchMediaKeyEvent(upEvent)
        } catch (_: Exception) {}
    }
}
