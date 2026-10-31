package com.novaforge.launcher.icon

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AnimationPlaybackController private constructor(private val context: Context) {

    companion object {
        @Volatile
        private var instance: AnimationPlaybackController? = null

        fun getInstance(context: Context): AnimationPlaybackController {
            return instance ?: synchronized(this) {
                instance ?: AnimationPlaybackController(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }

    private val _isPlaybackAllowed = MutableStateFlow(true)
    val isPlaybackAllowed: StateFlow<Boolean> = _isPlaybackAllowed.asStateFlow()

    private val _activeAnimationCount = MutableStateFlow(0)
    val activeAnimationCount: StateFlow<Int> = _activeAnimationCount.asStateFlow()

    private var isScreenOn = true
    private var isLauncherInForeground = true
    private var currentMode = BatteryEngineMode.BALANCED
    private var isGloballyEnabled = true

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    isScreenOn = false
                    updatePlaybackState()
                }
                Intent.ACTION_SCREEN_ON -> {
                    isScreenOn = true
                    updatePlaybackState()
                }
            }
        }
    }

    init {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        try {
            context.registerReceiver(screenReceiver, filter)
        } catch (_: Exception) {}
    }

    fun onLauncherResumed() {
        isLauncherInForeground = true
        updatePlaybackState()
    }

    fun onLauncherPaused() {
        isLauncherInForeground = false
        updatePlaybackState()
    }

    fun setGlobalEnabled(enabled: Boolean) {
        isGloballyEnabled = enabled
        updatePlaybackState()
    }

    fun setEngineMode(mode: BatteryEngineMode) {
        currentMode = mode
        updatePlaybackState()
    }

    private fun updatePlaybackState() {
        // Animations run ONLY when:
        // 1. Globally enabled
        // 2. Device screen is ON
        // 3. NovaForge launcher is in foreground
        val allowed = isGloballyEnabled && isScreenOn && isLauncherInForeground
        _isPlaybackAllowed.value = allowed
    }

    fun canStartAnimation(currentActive: Int): Boolean {
        if (!_isPlaybackAllowed.value) return false
        return currentActive < currentMode.maxSimultaneous
    }

    fun registerActiveAnimation() {
        _activeAnimationCount.value = _activeAnimationCount.value + 1
    }

    fun unregisterActiveAnimation() {
        if (_activeAnimationCount.value > 0) {
            _activeAnimationCount.value = _activeAnimationCount.value - 1
        }
    }
}
