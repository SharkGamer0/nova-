package com.novaforge.launcher

import android.Manifest
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
        val vm: LauncherViewModel = androidx.lifecycle.ViewModelProvider(this)[LauncherViewModel::class.java]
        if (isGranted) {
            vm.loadRealData()
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
            val returnedId = result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, pendingWidgetId) ?: pendingWidgetId
            pendingWidgetId = returnedId
            val info = appWidgetManager.getAppWidgetInfo(pendingWidgetId)
            if (info != null) {
                val canBind = appWidgetManager.bindAppWidgetIdIfAllowed(pendingWidgetId, info.provider)
                if (canBind) {
                    checkAndLaunchWidgetConfiguration(pendingWidgetId)
                } else {
                    val bindIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, pendingWidgetId)
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, info.provider)
                    }
                    bindWidgetLauncher.launch(bindIntent)
                }
            } else {
                appWidgetHost.deleteAppWidgetId(pendingWidgetId)
                pendingWidgetId = -1
            }
        } else if (pendingWidgetId != -1) {
            appWidgetHost.deleteAppWidgetId(pendingWidgetId)
            pendingWidgetId = -1
        }
    }

    // Result launcher for widget binding permission
    private val bindWidgetLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && pendingWidgetId != -1) {
            checkAndLaunchWidgetConfiguration(pendingWidgetId)
        } else if (pendingWidgetId != -1) {
            appWidgetHost.deleteAppWidgetId(pendingWidgetId)
            pendingWidgetId = -1
        }
    }

    // Result launcher for widget configuration
    private val configureWidgetLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val vm: LauncherViewModel = androidx.lifecycle.ViewModelProvider(this)[LauncherViewModel::class.java]
        if (result.resultCode == RESULT_OK && pendingWidgetId != -1) {
            val info = appWidgetManager.getAppWidgetInfo(pendingWidgetId)
            if (info != null) {
                vm.addWidget(
                    com.novaforge.launcher.data.model.LauncherWidget(
                        appWidgetId = pendingWidgetId,
                        providerPackage = info.provider.packageName,
                        providerClass = info.provider.className,
                        cellWidth = 2,
                        cellHeight = 2
                    )
                )
            }
            pendingWidgetId = -1
        } else if (pendingWidgetId != -1) {
            appWidgetHost.deleteAppWidgetId(pendingWidgetId)
            pendingWidgetId = -1
        }
    }

    private fun checkAndLaunchWidgetConfiguration(appWidgetId: Int) {
        val info = appWidgetManager.getAppWidgetInfo(appWidgetId)
        if (info == null) {
            appWidgetHost.deleteAppWidgetId(appWidgetId)
            pendingWidgetId = -1
            return
        }
        val vm: LauncherViewModel = androidx.lifecycle.ViewModelProvider(this)[LauncherViewModel::class.java]
        if (info.configure != null) {
            val configIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
                component = info.configure
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            configureWidgetLauncher.launch(configIntent)
        } else {
            vm.addWidget(
                com.novaforge.launcher.data.model.LauncherWidget(
                    appWidgetId = appWidgetId,
                    providerPackage = info.provider.packageName,
                    providerClass = info.provider.className,
                    cellWidth = 2,
                    cellHeight = 2
                )
            )
            pendingWidgetId = -1
        }
    }

    private fun bindSelectedProvider(provider: AppWidgetProviderInfo) {
        val id = appWidgetHost.allocateAppWidgetId()
        pendingWidgetId = id
        val canBind = try {
            appWidgetManager.bindAppWidgetIdIfAllowed(id, provider.provider)
        } catch (_: Exception) {
            false
        }
        if (canBind) {
            checkAndLaunchWidgetConfiguration(id)
        } else {
            val bindIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, provider.provider)
            }
            bindWidgetLauncher.launch(bindIntent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Enable native Android live wallpaper rendering behind the launcher
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
        window.decorView.setBackgroundColor(android.graphics.Color.TRANSPARENT)

        appWidgetManager = AppWidgetManager.getInstance(this)
        appWidgetHost = NovaForgeAppWidgetHost(this, APPWIDGET_HOST_ID)
        widgetBindingManager = WidgetBindingManager(this, appWidgetManager, appWidgetHost)
        mediaControlHelper = MediaControlHelper(this)
        screenTimeRepo = ScreenTimeRepository(this)

        setContent {
            val viewModel: LauncherViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsState()
            val accentColor by viewModel.accentColor.collectAsState()
            val userMsg by viewModel.userMessage.collectAsState()

            LaunchedEffect(userMsg) {
                userMsg?.let { msg ->
                    Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                    viewModel.clearUserMessage()
                }
            }

            NovaForgeTheme(themeMode = themeMode, accentColor = accentColor) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
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
                        onOpenSettings = { viewModel.openSettings() },
                        onSelectWidgetProvider = { provider -> bindSelectedProvider(provider) }
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
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.fromParts("package", packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    private fun requestUninstall(packageName: String) {
        val intent = Intent(Intent.ACTION_DELETE).apply {
            data = android.net.Uri.fromParts("package", packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    private fun handleSystemSettingCommand(setting: String) {
        when (setting.lowercase()) {
            "wifi" -> startActivity(Intent(Settings.ACTION_WIFI_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
            "bluetooth" -> startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
            "battery" -> startActivity(Intent(Intent.ACTION_POWER_USAGE_SUMMARY).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
            "display" -> startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
            "wallpaper" -> startActivity(Intent(Intent.ACTION_SET_WALLPAPER).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
            else -> startActivity(Intent(Settings.ACTION_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
        }
    }
}
