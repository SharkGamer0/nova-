package com.novaforge.launcher.widget

import android.app.Activity
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.WidthFull
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.novaforge.launcher.data.model.LauncherWidget

class NovaForgeAppWidgetHost(
    context: Context,
    hostId: Int
) : AppWidgetHost(context, hostId) {

    override fun onCreateView(
        context: Context,
        appWidgetId: Int,
        appWidget: AppWidgetProviderInfo?
    ): AppWidgetHostView {
        return super.onCreateView(context, appWidgetId, appWidget).apply {
            setPadding(0, 0, 0, 0)
        }
    }
}

class WidgetBindingManager(
    private val activity: Activity,
    private val appWidgetManager: AppWidgetManager,
    private val appWidgetHost: AppWidgetHost
) {
    fun handleWidgetSelectionResult(
        appWidgetId: Int,
        data: Intent?,
        onWidgetReady: (LauncherWidget) -> Unit,
        onConfigureRequired: (Intent, Int) -> Unit
    ) {
        val returnedId = data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId) ?: appWidgetId
        val widgetInfo = appWidgetManager.getAppWidgetInfo(returnedId) ?: return

        if (widgetInfo.configure != null) {
            val configIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
                component = widgetInfo.configure
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, returnedId)
            }
            onConfigureRequired(configIntent, returnedId)
        } else {
            onWidgetReady(
                LauncherWidget(
                    appWidgetId = returnedId,
                    providerPackage = widgetInfo.provider.packageName,
                    providerClass = widgetInfo.provider.className,
                    cellWidth = 2,
                    cellHeight = 2
                )
            )
        }
    }
}

@Composable
fun ResizableWidgetContainer(
    widget: LauncherWidget,
    appWidgetHost: AppWidgetHost,
    appWidgetManager: AppWidgetManager,
    isEditMode: Boolean,
    onResizeWidth: (Int) -> Unit,
    onResizeHeight: (Int) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemoveWidget: () -> Unit,
    modifier: Modifier = Modifier
) {
    val widgetInfo: AppWidgetProviderInfo? = remember(widget.appWidgetId) {
        appWidgetManager.getAppWidgetInfo(widget.appWidgetId)
    }

    val heightDp = when (widget.cellHeight) {
        1 -> 90.dp
        2 -> 160.dp
        3 -> 240.dp
        4 -> 320.dp
        else -> 160.dp
    }

    val widthFraction = when (widget.cellWidth) {
        1 -> 0.5f
        else -> 1.0f
    }

    Box(
        modifier = modifier
            .fillMaxWidth(widthFraction)
            .height(heightDp)
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(20.dp))
            .then(
                if (isEditMode) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp))
                } else Modifier
            )
    ) {
        if (widgetInfo != null) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    appWidgetHost.createView(context, widget.appWidgetId, widgetInfo).apply {
                        setAppWidget(widget.appWidgetId, widgetInfo)
                    }
                }
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Widget Unavailable",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(onClick = onRemoveWidget) {
                        Text("Remove", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        // Edit Mode overlay with width/height resizing, up/down positioning, and removal
        if (isEditMode) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), RoundedCornerShape(12.dp)),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Width resize toggle (Half vs Full)
                IconButton(
                    onClick = {
                        val nextWidth = if (widget.cellWidth == 1) 2 else 1
                        onResizeWidth(nextWidth)
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.WidthFull,
                        contentDescription = "Toggle Width",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Height cycle (1 -> 2 -> 3 -> 4 -> 1)
                IconButton(
                    onClick = {
                        val nextHeight = if (widget.cellHeight >= 4) 1 else widget.cellHeight + 1
                        onResizeHeight(nextHeight)
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Height,
                        contentDescription = "Cycle Height",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Move up in stack
                IconButton(
                    onClick = onMoveUp,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowUp,
                        contentDescription = "Move Up",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Move down in stack
                IconButton(
                    onClick = onMoveDown,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = "Move Down",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Delete widget
                IconButton(
                    onClick = onRemoveWidget,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove Widget",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
