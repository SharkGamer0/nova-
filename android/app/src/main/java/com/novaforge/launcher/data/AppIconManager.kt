package com.novaforge.launcher.data

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Process
import android.util.LruCache
import androidx.core.content.ContextCompat

/**
 * High-performance, production-grade native Android application icon engine.
 *
 * Responsibilities:
 * 1. Resolves original launcher activity icons via LauncherApps and PackageManager.
 * 2. Accurately renders Android Adaptive Icons (with foreground/background layers) and legacy drawables.
 * 3. Enforces an in-memory LRU icon cache to eliminate redundant decoding and prevent UI thread jank.
 * 4. Supplies an official neutral Android fallback icon (sym_def_app_icon) if an icon cannot be retrieved.
 * 5. Strictly guarantees ZERO alphabet letter cards or text placeholders anywhere in the launcher.
 */
object AppIconManager {

    // LRU Cache for up to 256 bitmaps (~8-16 MB depending on density)
    private val iconCache = LruCache<String, Bitmap>(256)

    // Key for neutral fallback icon
    private const val FALLBACK_KEY = "__android_default_sym_icon__"

    /**
     * Retrieves the real original launcher application icon as a non-recycled Bitmap.
     * Guaranteed non-null: returns the real app icon or the neutral Android system fallback.
     */
    fun getAppIconBitmap(context: Context, packageName: String, iconSizePx: Int = 144): Bitmap {
        val cacheKey = "$packageName:$iconSizePx"
        val cached = iconCache.get(cacheKey)
        if (cached != null && !cached.isRecycled) {
            return cached
        }

        val resolved = resolveRealAppIcon(context, packageName, iconSizePx)
        val finalBitmap = resolved ?: getNeutralFallbackIcon(context, iconSizePx)
        iconCache.put(cacheKey, finalBitmap)
        return finalBitmap
    }

    /**
     * Resolves the original launcher activity icon using multi-tiered Android APIs:
     * Tier 1: LauncherApps activity icon for the current user handle (exact density-matched icon).
     * Tier 2: PackageManager.getActivityIcon using the component from getLaunchIntentForPackage.
     * Tier 3: PackageManager.queryIntentActivities matching ACTION_MAIN + CATEGORY_LAUNCHER.
     * Tier 4: PackageManager.getApplicationIcon fallback.
     */
    private fun resolveRealAppIcon(context: Context, packageName: String, iconSizePx: Int): Bitmap? {
        // Tier 1: LauncherApps (Android 5.0+, API 21+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps
                if (launcherApps != null) {
                    val myUser = Process.myUserHandle()
                    val activities = launcherApps.getActivityList(packageName, myUser)
                    val launcherActivity = activities.firstOrNull()
                    if (launcherActivity != null) {
                        val densityDpi = context.resources.displayMetrics.densityDpi
                        val drawable = launcherActivity.getIcon(densityDpi)
                        val bmp = drawableToBitmap(drawable, iconSizePx)
                        if (bmp != null) return bmp
                    }
                }
            } catch (_: SecurityException) {
                // LauncherApps permission restricted, fall through to PM
            } catch (_: Exception) {
                // Fall through to PM
            }
        }

        val pm = context.packageManager

        // Tier 2: Resolve specific launcher ActivityComponent from launch intent
        try {
            val launchIntent = pm.getLaunchIntentForPackage(packageName)
            if (launchIntent?.component != null) {
                val activityDrawable = pm.getActivityIcon(launchIntent.component!!)
                val bmp = drawableToBitmap(activityDrawable, iconSizePx)
                if (bmp != null) return bmp
            }
        } catch (_: PackageManager.NameNotFoundException) {
            // Fall through
        } catch (_: Exception) {
            // Fall through
        }

        // Tier 3: Query Intent Activities with MAIN + LAUNCHER
        try {
            val mainIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                setPackage(packageName)
            }
            val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
            val ri = resolveInfos.firstOrNull()
            if (ri != null) {
                val iconDrawable = ri.loadIcon(pm)
                val bmp = drawableToBitmap(iconDrawable, iconSizePx)
                if (bmp != null) return bmp
            }
        } catch (_: Exception) {
            // Fall through
        }

        // Tier 4: Fallback to application-level icon
        try {
            val appDrawable = pm.getApplicationIcon(packageName)
            return drawableToBitmap(appDrawable, iconSizePx)
        } catch (_: PackageManager.NameNotFoundException) {
            return null
        } catch (_: Exception) {
            return null
        }
    }

    /**
     * Converts any Android Drawable (AdaptiveIconDrawable, BitmapDrawable, VectorDrawable)
     * into a high-fidelity ARGB_8888 Bitmap matching targetSize.
     */
    fun drawableToBitmap(drawable: Drawable, targetSize: Int): Bitmap? {
        try {
            // Handle BitmapDrawable directly if dimensions match
            if (drawable is BitmapDrawable && drawable.bitmap != null && !drawable.bitmap.isRecycled) {
                val orig = drawable.bitmap
                if (orig.width == targetSize && orig.height == targetSize) {
                    return orig
                }
                return Bitmap.createScaledBitmap(orig, targetSize, targetSize, true)
            }

            // Create target bitmap with ARGB_8888
            val bitmap = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // AdaptiveIconDrawable handling: render full bounds
            drawable.setBounds(0, 0, targetSize, targetSize)
            drawable.draw(canvas)
            return bitmap
        } catch (_: OutOfMemoryError) {
            iconCache.evictAll()
            return null
        } catch (_: Exception) {
            return null
        }
    }

    /**
     * Retrieves the official neutral Android system app icon (sym_def_app_icon).
     * Used when an app is missing, uninstalled, or restricted. Never returns an alphabet glyph.
     */
    fun getNeutralFallbackIcon(context: Context, iconSizePx: Int = 144): Bitmap {
        val cached = iconCache.get(FALLBACK_KEY)
        if (cached != null && !cached.isRecycled) return cached

        val pm = context.packageManager
        val defaultDrawable: Drawable = try {
            pm.defaultActivityIcon
        } catch (_: Exception) {
            ContextCompat.getDrawable(context, android.R.drawable.sym_def_app_icon)
        } ?: ColorDrawable(android.graphics.Color.DKGRAY)

        val bitmap = drawableToBitmap(defaultDrawable, iconSizePx)
            ?: Bitmap.createBitmap(iconSizePx, iconSizePx, Bitmap.Config.ARGB_8888).apply {
                val canvas = Canvas(this)
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#1E293B")
                    style = android.graphics.Paint.Style.FILL
                }
                canvas.drawRect(0f, 0f, iconSizePx.toFloat(), iconSizePx.toFloat(), paint)
            }

        iconCache.put(FALLBACK_KEY, bitmap)
        return bitmap
    }

    fun clearCache() {
        iconCache.evictAll()
    }
}
