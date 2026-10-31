package com.novaforge.launcher.data

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.LruCache

/**
 * High-performance native Android application icon cache.
 * Resolves real application icons (Adaptive and Legacy Drawables) into Bitmaps
 * without blocking the main UI thread during list rendering.
 */
object AppIconManager {

    // 256 app icons cache (~8-16 MB depending on screen density)
    private val iconCache = LruCache<String, Bitmap>(256)

    fun getAppIconBitmap(context: Context, packageName: String, iconSizePx: Int = 144): Bitmap? {
        val cached = iconCache.get(packageName)
        if (cached != null && !cached.isRecycled) {
            return cached
        }

        return try {
            val pm = context.packageManager
            val drawable = pm.getApplicationIcon(packageName)
            val bitmap = drawableToBitmap(drawable, iconSizePx)
            if (bitmap != null) {
                iconCache.put(packageName, bitmap)
            }
            bitmap
        } catch (_: PackageManager.NameNotFoundException) {
            null
        } catch (_: Exception) {
            null
        }
    }

    fun clearCache() {
        iconCache.evictAll()
    }

    private fun drawableToBitmap(drawable: Drawable, targetSize: Int): Bitmap? {
        try {
            if (drawable is BitmapDrawable && drawable.bitmap != null && !drawable.bitmap.isRecycled) {
                val orig = drawable.bitmap
                if (orig.width == targetSize && orig.height == targetSize) {
                    return orig
                }
                return Bitmap.createScaledBitmap(orig, targetSize, targetSize, true)
            }

            val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else targetSize
            val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else targetSize

            val bitmap = Bitmap.createBitmap(
                targetSize,
                targetSize,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        } catch (_: OutOfMemoryError) {
            iconCache.evictAll()
            return null
        } catch (_: Exception) {
            return null
        }
    }
}
