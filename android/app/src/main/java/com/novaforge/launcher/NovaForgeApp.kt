package com.novaforge.launcher

import android.app.Application
import android.os.Build
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.novaforge.launcher.data.AppIconManager
import com.novaforge.launcher.data.AppRepository
import com.novaforge.launcher.data.LauncherPreferences

class NovaForgeApp : Application(), ImageLoaderFactory {

    lateinit var appRepository: AppRepository
        private set

    lateinit var preferences: LauncherPreferences
        private set

    lateinit var animationRepository: com.novaforge.launcher.icon.AnimationRepository
        private set

    lateinit var contextRingRepository: com.novaforge.launcher.contextring.ContextRingRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        preferences = LauncherPreferences(this)
        appRepository = AppRepository(this, preferences)
        animationRepository = com.novaforge.launcher.icon.AnimationRepository(this, preferences)
        contextRingRepository = com.novaforge.launcher.contextring.ContextRingRepository(this, preferences)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        AppIconManager.clearCache()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= TRIM_MEMORY_MODERATE) {
            AppIconManager.clearCache()
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.20)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("coil_image_cache"))
                    .maxSizeBytes(40 * 1024 * 1024L) // 40 MB
                    .build()
            }
            .respectCacheHeaders(false)
            .build()
    }

    companion object {
        lateinit var instance: NovaForgeApp
            private set
    }
}
