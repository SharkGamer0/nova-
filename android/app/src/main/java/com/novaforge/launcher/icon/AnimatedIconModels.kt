package com.novaforge.launcher.icon

enum class AnimationFormat(val displayName: String, val extension: String) {
    GIF("GIF", "gif"),
    WEBP("Animated WebP", "webp"),
    LOTTIE("Lottie JSON", "json"),
    STATIC_PNG("PNG", "png"),
    STATIC_WEBP("WebP", "webp")
}

data class AnimatedIconItem(
    val id: String,
    val name: String,
    val relativeFilePath: String, // e.g. "icons/orbit_ring.webp" or "icons/lottie_sparks.json"
    val format: AnimationFormat,
    val collectionId: String = "all",
    val width: Int = 128,
    val height: Int = 128,
    val fileSizeBytes: Long = 0,
    val durationMs: Long = 1000,
    val isPreset: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class AnimationCollection(
    val id: String,
    val name: String,
    val iconCount: Int = 0
)

data class IconAssignment(
    val packageName: String,
    val animationId: String,
    val playbackSpeed: Float = 1.0f, // 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f
    val loopMode: String = "LOOP", // "LOOP" or "ONCE"
    val animateOnAppear: Boolean = true,
    val animateOnLaunch: Boolean = false,
    val staticFirstFrameFallback: Boolean = true,
    val isEnabled: Boolean = true
)

enum class BatteryEngineMode(val displayName: String, val maxSimultaneous: Int, val fpsTarget: Int) {
    BATTERY_SAVER("Battery Saver", 2, 15),
    BALANCED("Balanced", 6, 30),
    PERFORMANCE("Performance", 16, 60)
}

data class AnimatedIconEngineSettings(
    val engineMode: BatteryEngineMode = BatteryEngineMode.BALANCED,
    val globalAnimationsEnabled: Boolean = true,
    val pauseWhenScreenOff: Boolean = true,
    val pauseWhenLauncherHidden: Boolean = true,
    val pauseDuringLowPowerMode: Boolean = true
)
