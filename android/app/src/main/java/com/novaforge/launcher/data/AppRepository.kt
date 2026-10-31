package com.novaforge.launcher.data

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import com.novaforge.launcher.data.model.AppItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Repository to discover, filter, categorize, and track applications using Android PackageManager.
 */
class AppRepository(
    private val context: Context,
    private val preferences: LauncherPreferences
) {
    private val _installedApps = MutableStateFlow<List<AppItem>>(emptyList())
    val installedApps: StateFlow<List<AppItem>> = _installedApps.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    fun reloadInstalledApps() {
        scope.launch {
            val pm = context.packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }

            val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
            val favs = preferences.favoritePackages.first()
            val hidden = preferences.hiddenPackages.first()
            val launchCounts = preferences.launchCounts.first()

            val appList = resolveInfos.mapNotNull { ri ->
                val pkg = ri.activityInfo.packageName
                val label = ri.loadLabel(pm).toString()
                if (pkg == context.packageName) return@mapNotNull null // Exclude launcher itself

                val cat = resolveCategory(ri.activityInfo.applicationInfo, pkg)
                val isFav = favs.contains(pkg)
                val isHid = hidden.contains(pkg)
                val count = launchCounts[pkg] ?: 0

                val color = generateColorForApp(pkg)

                AppItem(
                    packageName = pkg,
                    className = ri.activityInfo.name,
                    label = label,
                    colorHex = color,
                    category = cat,
                    isFavorite = isFav,
                    isHidden = isHid,
                    launchCount = count
                )
            }.sortedBy { it.label }

            _installedApps.value = appList
        }
    }

    suspend fun toggleFavorite(packageName: String) {
        preferences.toggleFavorite(packageName)
        reloadInstalledApps()
    }

    suspend fun setAppHidden(packageName: String, hidden: Boolean) {
        preferences.setAppHidden(packageName, hidden)
        reloadInstalledApps()
    }

    suspend fun recordAppLaunch(packageName: String) {
        preferences.recordAppLaunch(packageName)
        reloadInstalledApps()
    }

    private fun resolveCategory(appInfo: ApplicationInfo, packageName: String): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            when (appInfo.category) {
                ApplicationInfo.CATEGORY_GAME -> return "Games"
                ApplicationInfo.CATEGORY_AUDIO, ApplicationInfo.CATEGORY_VIDEO -> return "Media"
                ApplicationInfo.CATEGORY_SOCIAL -> return "Social"
                ApplicationInfo.CATEGORY_PRODUCTIVITY, ApplicationInfo.CATEGORY_NEWS -> return "Productivity"
            }
        }
        val lowerPkg = packageName.lowercase()
        return when {
            lowerPkg.contains("game") || lowerPkg.contains("play") -> "Games"
            lowerPkg.contains("social") || lowerPkg.contains("whatsapp") || lowerPkg.contains("telegram") ||
                lowerPkg.contains("twitter") || lowerPkg.contains("instagram") || lowerPkg.contains("facebook") ||
                lowerPkg.contains("reddit") || lowerPkg.contains("discord") -> "Social"
            lowerPkg.contains("music") || lowerPkg.contains("spotify") || lowerPkg.contains("youtube") ||
                lowerPkg.contains("media") || lowerPkg.contains("video") || lowerPkg.contains("camera") ||
                lowerPkg.contains("gallery") || lowerPkg.contains("photos") -> "Media"
            lowerPkg.contains("doc") || lowerPkg.contains("office") || lowerPkg.contains("note") ||
                lowerPkg.contains("calendar") || lowerPkg.contains("mail") || lowerPkg.contains("gmail") -> "Productivity"
            else -> "Tools"
        }
    }

    private fun generateColorForApp(packageName: String): Int {
        val palette = intArrayOf(
            0xFF2A9D8F.toInt(), // Muted Teal
            0xFFE76F51.toInt(), // Burnt Amber
            0xFF4A5568.toInt(), // Slate
            0xFF52796F.toInt(), // Sage
            0xFF457B9D.toInt(), // Steel Blue
            0xFF6B705C.toInt(), // Olive
            0xFF8338EC.toInt()  // Purple
        )
        val hash = kotlin.math.abs(packageName.hashCode())
        return palette[hash % palette.size]
    }
}
