package com.novaforge.launcher

import android.app.Application
import com.novaforge.launcher.data.AppRepository
import com.novaforge.launcher.data.LauncherPreferences

class NovaForgeApp : Application() {

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

    companion object {
        lateinit var instance: NovaForgeApp
            private set
    }
}
