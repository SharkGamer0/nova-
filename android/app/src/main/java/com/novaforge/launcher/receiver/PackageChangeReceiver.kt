package com.novaforge.launcher.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.novaforge.launcher.NovaForgeApp

class PackageChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val app = context.applicationContext as? NovaForgeApp ?: return
        when (intent?.action) {
            Intent.ACTION_PACKAGE_ADDED,
            Intent.ACTION_PACKAGE_REMOVED,
            Intent.ACTION_PACKAGE_REPLACED -> {
                app.appRepository.reloadInstalledApps()
            }
        }
    }
}
