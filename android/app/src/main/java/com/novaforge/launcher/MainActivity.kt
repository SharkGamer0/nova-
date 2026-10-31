package com.novaforge.launcher

import android.Manifest
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.novaforge.launcher.data.ScreenTimeRepository
import com.novaforge.launcher.service.MediaControlHelper
import com.novaforge.launcher.ui.LauncherRootScreen
import com.novaforge.launcher.ui.LauncherViewModel
import com.novaforge.launcher.ui.theme.NovaForgeTheme
import com.novaforge.launcher.widget.NovaForgeAppWidgetHost
import com.novaforge.launcher.widget.WidgetBindingManager

class MainActivity : ComponentActivity() {

    companion object {
        const val APPWIDGET_HOST_ID = 2048
    }

    private lateinit var appWidgetManager: AppWidgetManager
    private lateinit var appWidgetHost: AppWidgetHost
    private lateinit var widgetBindingManager: WidgetBindingManager
    private lateinit var mediaControlHelper: MediaControlHelper
    private lateinit var screenTimeRepo: ScreenTimeRepository

    private var pendingWidgetId: Int = -1

    // Calendar runtime permission launcher
    private val calendarPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        val viewModel: LauncherViewModel = androidx.lifecycle.ViewModelProvider(this)[LauncherViewModel::class.java]
        if (isGranted) {
            viewModel.loadRealData()
            Toast.makeText(this, "Calendar events synced", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Calendar permission denied. Showing demo agenda.", Toast.LENGTH_SHORT).show()
        }
    }

    // Result launcher for widget picking
    private val pickWidgetLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && pendingWidgetId != -1) {
            val viewModel: LauncherViewModel = androidx.lifecycle.ViewModelProvider(this)[LauncherViewModel::class.java]
            widgetBindingManager.handleWidgetSelectionResult(
                appWidgetId = pendingWidgetId,
                data = result.data,
                onWidgetReady = { widget -> viewModel.addWidget(widget) },
                onConfigureRequired = { configIntent, _ ->
                    configureWidgetLauncher.launch(configIntent)
                }
            )
        } else if (pendingWidgetId != -1) {
            appWidgetHost.deleteAppWidgetId(pendingWidgetId)
            pendingWidgetId = -1
        }
    }

    // Result launcher for widget configuration
    private val configureWidgetLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val viewModel: LauncherViewModel = androidx.lifecycle.ViewModelProvider(this)[LauncherViewModel::class.java]
        if (result.resultCode == RESULT_OK && pendingWidgetId != -1) {
            val info = appWidgetManager.getAppWidgetInfo(pendingWidgetId)
            if (info != null) {
                viewModel.addWidget(
                    com.novaforge.launcher.data.model.LauncherWidget(
                        appWidgetId = pendingWidgetId,
                        providerPackage = info.provider.packageName,
                        providerClass = info.provider.className,
                        cellWidth = 2,
                        cellHeight = 2
                    )
                )
            }
        } else if (pendingWidgetId != -1) {
            appWidgetHost.deleteAppWidgetId(pendingWidgetId)
            pendingWidgetId = -1
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        appWidgetManager = AppWidgetManager.getInstance(this)
        appWidgetHost = NovaForgeAppWidgetHost(this, APPWIDGET_HOST_ID)
        widgetBindingManager = WidgetBindingManager(this, appWidgetManager, appWidgetHost)
        mediaControlHelper = MediaControlHelper(this)
        screenTimeRepo = ScreenTimeRepository(this)

        setContent {
            val viewModel: LauncherViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsState()
            val accentColor by viewModel.accentColor.collectAsState()

            NovaForgeTheme(themeMode = themeMode, accentColor = accentColor) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LauncherRootScreen(
                        viewModel = viewModel,
                        appWidgetHost = appWidgetHost,
                        appWidgetManager = appWidgetManager,
                        onRequestAddWidget = {
                            val id = appWidgetHost.allocateAppWidgetId()
                            pendingWidgetId = id
                            val pickIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK).apply {
                                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                            }
                            pickWidgetLauncher.launch(pickIntent)
                        },
                        onRequestCalendarPermission = {
                            calendarPermissionLauncher.launch(Manifest.permission.READ_CALENDAR)
                        },
                        onRequestUsagePermission = {
                            startActivity(screenTimeRepo.getUsagePermissionIntent())
                        },
                        onDeleteWidgetId = { id ->
                            appWidgetHost.deleteAppWidgetId(id)
                        },
                        onLaunchApp = { packageName -> launchApp(packageName) },
                        onOpenAppInfo = { packageName -> openAppInfo(packageName) },
                        onRequestUninstall = { packageName -> requestUninstall(packageName) },
                        onOpenSystemSetting = { setting -> handleSystemSettingCommand(setting) },
                        onOpenSettings = { viewModel.openSettings() }
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        appWidgetHost.startListening()
        mediaControlHelper.registerNotificationListener()
    }

    override fun onResume() {
        super.onResume()
        val viewModel: LauncherViewModel = androidx.lifecycle.ViewModelProvider(this)[LauncherViewModel::class.java]
        viewModel.loadRealData()
        com.novaforge.launcher.icon.AnimationPlaybackController.getInstance(this).onLauncherResumed()
    }

    override fun onPause() {
        super.onPause()
        com.novaforge.launcher.icon.AnimationPlaybackController.getInstance(this).onLauncherPaused()
    }

    override fun onStop() {
        super.onStop()
        appWidgetHost.stopListening()
        com.novaforge.launcher.icon.AnimationPlaybackController.getInstance(this).onLauncherPaused()
    }

    private fun launchApp(packageName: String) {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(launchIntent)
        } else {
            Toast.makeText(this, "Unable to launch $packageName", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openAppInfo(packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(this, "Unable to open app info", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestUninstall(packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = Uri.fromParts("package", packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(this, "Unable to request uninstall", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleSystemSettingCommand(setting: String) {
        try {
            when (setting) {
                "wifi" -> startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                "bluetooth" -> startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                "battery" -> startActivity(Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS))
                "display" -> startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS))
                "wallpaper" -> startActivity(Intent.createChooser(Intent(Intent.ACTION_SET_WALLPAPER), "Wallpaper"))
                "lock" -> Toast.makeText(this, "Lock action triggered (Standard Android lock screen)", Toast.LENGTH_SHORT).show()
                else -> startActivity(Intent(Settings.ACTION_SETTINGS))
            }
        } catch (_: Exception) {
            Toast.makeText(this, "Unable to open system setting for $setting", Toast.LENGTH_SHORT).show()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Enforce HOME launcher guarantee: back button never closes NovaForge, always returns to Home workspace
        val viewModel: LauncherViewModel = androidx.lifecycle.ViewModelProvider(this)[LauncherViewModel::class.java]
        if (!viewModel.isAtHomeScreen()) {
            viewModel.navigateToHome()
        }
    }
}
