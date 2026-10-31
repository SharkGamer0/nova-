package com.novaforge.launcher.icon

import android.content.Context
import android.net.Uri
import com.novaforge.launcher.data.LauncherPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class AnimationRepository(
    private val context: Context,
    private val preferences: LauncherPreferences
) {
    private val iconsDir = File(context.filesDir, "animated_icons")
    private val libraryFile = File(context.filesDir, "animated_icons_library.json")
    private val assignmentsFile = File(context.filesDir, "icon_assignments.json")

    private val _animations = MutableStateFlow<List<AnimatedIconItem>>(emptyList())
    val animations: StateFlow<List<AnimatedIconItem>> = _animations.asStateFlow()

    private val _collections = MutableStateFlow<List<AnimationCollection>>(emptyList())
    val collections: StateFlow<List<AnimationCollection>> = _collections.asStateFlow()

    private val _assignments = MutableStateFlow<Map<String, IconAssignment>>(emptyMap())
    val assignments: StateFlow<Map<String, IconAssignment>> = _assignments.asStateFlow()

    private val _engineSettings = MutableStateFlow(AnimatedIconEngineSettings())
    val engineSettings: StateFlow<AnimatedIconEngineSettings> = _engineSettings.asStateFlow()

    val assignmentManager: AnimationAssignmentManager by lazy {
        AnimationAssignmentManager(this)
    }

    init {
        if (!iconsDir.exists()) {
            iconsDir.mkdirs()
        }
        loadLibrary()
        loadAssignments()
    }

    private fun loadLibrary() {
        if (!libraryFile.exists()) {
            // Seed default curated minimalist animations
            val defaults = createDefaultPresets()
            _animations.value = defaults
            saveLibrary()
        } else {
            try {
                val jsonStr = libraryFile.readText()
                val arr = JSONArray(jsonStr)
                val list = mutableListOf<AnimatedIconItem>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        AnimatedIconItem(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            relativeFilePath = obj.getString("relativeFilePath"),
                            format = AnimationFormat.valueOf(obj.getString("format")),
                            collectionId = obj.optString("collectionId", "all"),
                            width = obj.optInt("width", 128),
                            height = obj.optInt("height", 128),
                            fileSizeBytes = obj.optLong("fileSizeBytes", 0L),
                            durationMs = obj.optLong("durationMs", 1000L),
                            isPreset = obj.optBoolean("isPreset", false),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
                _animations.value = if (list.isEmpty()) createDefaultPresets() else list
            } catch (_: Exception) {
                _animations.value = createDefaultPresets()
            }
        }
        updateCollections()
    }

    private fun createDefaultPresets(): List<AnimatedIconItem> {
        return listOf(
            AnimatedIconItem(
                id = "preset-pulse",
                name = "Minimal Pulse",
                relativeFilePath = "presets/pulse.json",
                format = AnimationFormat.LOTTIE,
                collectionId = "minimal",
                width = 128,
                height = 128,
                fileSizeBytes = 4200,
                durationMs = 1200,
                isPreset = true
            ),
            AnimatedIconItem(
                id = "preset-orbit",
                name = "Quantum Orbit",
                relativeFilePath = "presets/orbit.webp",
                format = AnimationFormat.WEBP,
                collectionId = "orbits",
                width = 128,
                height = 128,
                fileSizeBytes = 8400,
                durationMs = 2000,
                isPreset = true
            ),
            AnimatedIconItem(
                id = "preset-wave",
                name = "Fluid Wave",
                relativeFilePath = "presets/wave.gif",
                format = AnimationFormat.GIF,
                collectionId = "minimal",
                width = 128,
                height = 128,
                fileSizeBytes = 12300,
                durationMs = 1500,
                isPreset = true
            ),
            AnimatedIconItem(
                id = "preset-sparks",
                name = "Nova Sparks",
                relativeFilePath = "presets/sparks.json",
                format = AnimationFormat.LOTTIE,
                collectionId = "tech",
                width = 128,
                height = 128,
                fileSizeBytes = 6100,
                durationMs = 1800,
                isPreset = true
            ),
            AnimatedIconItem(
                id = "preset-prism",
                name = "Rotating Prism",
                relativeFilePath = "presets/prism.webp",
                format = AnimationFormat.WEBP,
                collectionId = "minimal",
                width = 128,
                height = 128,
                fileSizeBytes = 7200,
                durationMs = 2400,
                isPreset = true
            )
        )
    }

    private fun saveLibrary() {
        try {
            val arr = JSONArray()
            _animations.value.forEach { item ->
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("name", item.name)
                    put("relativeFilePath", item.relativeFilePath)
                    put("format", item.format.name)
                    put("collectionId", item.collectionId)
                    put("width", item.width)
                    put("height", item.height)
                    put("fileSizeBytes", item.fileSizeBytes)
                    put("durationMs", item.durationMs)
                    put("isPreset", item.isPreset)
                    put("createdAt", item.createdAt)
                }
                arr.put(obj)
            }
            libraryFile.writeText(arr.toString(2))
        } catch (_: Exception) {}
        updateCollections()
    }

    private fun updateCollections() {
        val countMap = _animations.value.groupingBy { it.collectionId }.eachCount()
        val fixed = listOf(
            AnimationCollection("all", "All Animations", _animations.value.size),
            AnimationCollection("minimal", "Minimal", countMap["minimal"] ?: 0),
            AnimationCollection("orbits", "Orbits & Rings", countMap["orbits"] ?: 0),
            AnimationCollection("tech", "Tech & Dynamic", countMap["tech"] ?: 0),
            AnimationCollection("custom", "User Imports", countMap["custom"] ?: 0)
        )
        _collections.value = fixed
    }

    private fun loadAssignments() {
        if (!assignmentsFile.exists()) {
            _assignments.value = emptyMap()
            return
        }
        try {
            val jsonStr = assignmentsFile.readText()
            val arr = JSONArray(jsonStr)
            val map = mutableMapOf<String, IconAssignment>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val pkg = obj.getString("packageName")
                map[pkg] = IconAssignment(
                    packageName = pkg,
                    animationId = obj.getString("animationId"),
                    playbackSpeed = obj.optDouble("playbackSpeed", 1.0).toFloat(),
                    loopMode = obj.optString("loopMode", "LOOP"),
                    animateOnAppear = obj.optBoolean("animateOnAppear", true),
                    animateOnLaunch = obj.optBoolean("animateOnLaunch", false),
                    staticFirstFrameFallback = obj.optBoolean("staticFirstFrameFallback", true),
                    isEnabled = obj.optBoolean("isEnabled", true)
                )
            }
            _assignments.value = map
        } catch (_: Exception) {
            _assignments.value = emptyMap()
        }
    }

    private fun saveAssignments() {
        try {
            val arr = JSONArray()
            _assignments.value.values.forEach { a ->
                val obj = JSONObject().apply {
                    put("packageName", a.packageName)
                    put("animationId", a.animationId)
                    put("playbackSpeed", a.playbackSpeed.toDouble())
                    put("loopMode", a.loopMode)
                    put("animateOnAppear", a.animateOnAppear)
                    put("animateOnLaunch", a.animateOnLaunch)
                    put("staticFirstFrameFallback", a.staticFirstFrameFallback)
                    put("isEnabled", a.isEnabled)
                }
                arr.put(obj)
            }
            assignmentsFile.writeText(arr.toString(2))
        } catch (_: Exception) {}
    }

    companion object {
        const val MAX_ANIMATION_FILE_SIZE = 15 * 1024 * 1024L // 15 MB max
    }

    suspend fun importAnimationFromUri(uri: Uri, userGivenName: String? = null): Result<AnimatedIconItem> = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri)?.lowercase() ?: ""
            val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "imported_${System.currentTimeMillis()}"

            val targetFileId = UUID.randomUUID().toString()
            val tempDestFile = File(iconsDir, "temp_$targetFileId")

            // Copy stream into temporary file for inspection
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempDestFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext Result.failure(Exception("Cannot open file stream for $uri"))

            val fileSize = tempDestFile.length()
            if (fileSize == 0L) {
                tempDestFile.delete()
                return@withContext Result.failure(Exception("File is empty (0 bytes)"))
            }
            if (fileSize > MAX_ANIMATION_FILE_SIZE) {
                tempDestFile.delete()
                return@withContext Result.failure(Exception("File size exceeds 15 MB limit: ${(fileSize / 1024 / 1024)} MB"))
            }

            // Read magic bytes (first 16 bytes)
            val header = ByteArray(16)
            tempDestFile.inputStream().use { it.read(header) }

            // Validate header and determine exact format
            val isGif = header.size >= 6 && header[0] == 'G'.code.toByte() && header[1] == 'I'.code.toByte() && header[2] == 'F'.code.toByte()
            val isWebP = header.size >= 12 &&
                    header[0] == 'R'.code.toByte() && header[1] == 'I'.code.toByte() && header[2] == 'F'.code.toByte() && header[3] == 'F'.code.toByte() &&
                    header[8] == 'W'.code.toByte() && header[9] == 'E'.code.toByte() && header[10] == 'B'.code.toByte() && header[11] == 'P'.code.toByte()
            val isPng = header.size >= 4 && (header[0].toInt() and 0xFF) == 0x89 && header[1] == 'P'.code.toByte() && header[2] == 'N'.code.toByte() && header[3] == 'G'.code.toByte()

            var isLottie = false
            var lottieWidth = 128
            var lottieHeight = 128
            var lottieDuration = 1500L

            if (!isGif && !isWebP && !isPng) {
                // Check if valid JSON (Lottie)
                try {
                    val textContent = tempDestFile.readText()
                    val json = JSONObject(textContent)
                    if (json.has("v") || json.has("layers") || json.has("assets")) {
                        isLottie = true
                        lottieWidth = json.optInt("w", 128)
                        lottieHeight = json.optInt("h", 128)
                        val fr = json.optDouble("fr", 30.0)
                        val ip = json.optDouble("ip", 0.0)
                        val op = json.optDouble("op", 60.0)
                        if (fr > 0) {
                            lottieDuration = (((op - ip) / fr) * 1000).toLong().coerceIn(200L, 30000L)
                        }
                    }
                } catch (_: Exception) {}
            }

            val format = when {
                isGif -> AnimationFormat.GIF
                isWebP -> AnimationFormat.WEBP
                isLottie -> AnimationFormat.LOTTIE
                isPng -> AnimationFormat.STATIC_PNG
                mimeType.contains("gif") || fileName.endsWith(".gif", ignoreCase = true) -> AnimationFormat.GIF
                mimeType.contains("webp") || fileName.endsWith(".webp", ignoreCase = true) -> AnimationFormat.WEBP
                mimeType.contains("json") || fileName.endsWith(".json", ignoreCase = true) -> AnimationFormat.LOTTIE
                fileName.endsWith(".png", ignoreCase = true) -> AnimationFormat.STATIC_PNG
                else -> {
                    tempDestFile.delete()
                    return@withContext Result.failure(Exception("Unsupported or corrupt animation file format. Expected GIF, WebP, Lottie JSON, or PNG."))
                }
            }

            // Probe image dimensions for bitmap types
            var width = lottieWidth
            var height = lottieHeight
            var duration = lottieDuration

            if (format != AnimationFormat.LOTTIE) {
                val opts = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
                android.graphics.BitmapFactory.decodeFile(tempDestFile.absolutePath, opts)
                if (opts.outWidth > 0 && opts.outHeight > 0) {
                    width = opts.outWidth
                    height = opts.outHeight
                }
                duration = if (format == AnimationFormat.GIF) 1400L else 2000L
            }

            val cleanName = userGivenName?.takeIf { it.isNotBlank() }
                ?: fileName.substringBeforeLast('.').replace('_', ' ').replace('-', ' ').capitalize()

            // Duplicate detection: check if existing animation in library matches name and size
            val existing = _animations.value.find { it.name.equals(cleanName, ignoreCase = true) && it.fileSizeBytes == fileSize }
            if (existing != null) {
                tempDestFile.delete()
                return@withContext Result.success(existing)
            }

            val targetFileName = "$targetFileId.${format.extension}"
            val finalDestFile = File(iconsDir, targetFileName)
            tempDestFile.renameTo(finalDestFile)

            val item = AnimatedIconItem(
                id = targetFileId,
                name = cleanName,
                relativeFilePath = targetFileName,
                format = format,
                collectionId = "custom",
                width = width,
                height = height,
                fileSizeBytes = fileSize,
                durationMs = duration,
                isPreset = false,
                createdAt = System.currentTimeMillis()
            )

            _animations.value = _animations.value + item
            saveLibrary()
            Result.success(item)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun renameAnimation(id: String, newName: String) = withContext(Dispatchers.IO) {
        _animations.value = _animations.value.map {
            if (it.id == id) it.copy(name = newName.trim()) else it
        }
        saveLibrary()
    }

    suspend fun deleteAnimation(id: String) = withContext(Dispatchers.IO) {
        val target = _animations.value.find { it.id == id }
        if (target != null && !target.isPreset) {
            val file = File(iconsDir, target.relativeFilePath)
            if (file.exists()) file.delete()
        }
        _animations.value = _animations.value.filterNot { it.id == id }
        saveLibrary()

        // Clean up any assignments that used this animation
        val updatedAssignments = _assignments.value.filterValues { it.animationId != id }
        _assignments.value = updatedAssignments
        saveAssignments()
    }

    suspend fun assignAnimationToApp(assignment: IconAssignment) = withContext(Dispatchers.IO) {
        val current = _assignments.value.toMutableMap()
        current[assignment.packageName] = assignment
        _assignments.value = current
        saveAssignments()
    }

    suspend fun removeAssignmentForApp(packageName: String) = withContext(Dispatchers.IO) {
        val current = _assignments.value.toMutableMap()
        current.remove(packageName)
        _assignments.value = current
        saveAssignments()
    }

    suspend fun updateEngineSettings(settings: AnimatedIconEngineSettings) = withContext(Dispatchers.IO) {
        _engineSettings.value = settings
    }

    fun getAnimationFile(item: AnimatedIconItem): File {
        return File(iconsDir, item.relativeFilePath)
    }
}
