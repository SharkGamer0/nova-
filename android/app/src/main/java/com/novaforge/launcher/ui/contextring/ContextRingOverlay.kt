package com.novaforge.launcher.ui.contextring

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novaforge.launcher.contextring.*

@Composable
fun ContextRingOverlay(
    session: ActiveRingSession,
    config: ContextRingConfig,
    onExecuteAction: (ContextRingAction) -> Unit,
    onNavigateBack: () -> Unit,
    onDismiss: () -> Unit,
    onUpdatePointer: (Float, Float) -> Unit,
    onReleasePointer: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val configScreen = LocalConfiguration.current
    val engine = remember { ContextRingEngine() }

    val screenWidthPx = with(density) { configScreen.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configScreen.screenHeightDp.dp.toPx() }
    val radiusPx = with(density) { session.currentRing.radiusDp.dp.toPx() }

    // Clamp center so ring never gets cut off at screen edges
    val (clampedX, clampedY) = remember(session.originX, session.originY, session.currentRing) {
        engine.calculateClampedCenter(
            rawX = session.originX,
            rawY = session.originY,
            screenWidthPx = screenWidthPx,
            screenHeightPx = screenHeightPx,
            radiusPx = radiusPx,
            density = density.density
        )
    }

    var lastHapticIndex by remember { androidx.compose.runtime.mutableIntStateOf(-1) }
    var pendingConfirmationAction by remember { androidx.compose.runtime.mutableStateOf<ContextRingAction?>(null) }

    // Dismiss or back on physical back key
    BackHandler {
        if (session.breadcrumbRingIds.isNotEmpty()) {
            onNavigateBack()
        } else {
            onDismiss()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x77000000))
            .pointerInput(session.currentRing.id) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val (selIdx, act, dist) = engine.evaluatePointer(
                            pointerX = offset.x,
                            pointerY = offset.y,
                            centerX = clampedX,
                            centerY = clampedY,
                            ring = session.currentRing,
                            density = density.density
                        )
                        if (selIdx != -1 && selIdx != lastHapticIndex && config.hapticIntensity != "off") {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            lastHapticIndex = selIdx
                        }
                        onUpdatePointer(offset.x, offset.y)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val (selIdx, act, dist) = engine.evaluatePointer(
                            pointerX = change.position.x,
                            pointerY = change.position.y,
                            centerX = clampedX,
                            centerY = clampedY,
                            ring = session.currentRing,
                            density = density.density
                        )
                        if (selIdx != -1 && selIdx != lastHapticIndex && config.hapticIntensity != "off") {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            lastHapticIndex = selIdx
                        }
                        onUpdatePointer(change.position.x, change.position.y)
                    },
                    onDragEnd = {
                        val (_, selectedAction, dist) = engine.evaluatePointer(
                            pointerX = session.clampedCenterX,
                            pointerY = session.clampedCenterY,
                            centerX = clampedX,
                            centerY = clampedY,
                            ring = session.currentRing,
                            density = density.density
                        )
                        if (selectedAction != null) {
                            if (selectedAction.requiresConfirmation) {
                                pendingConfirmationAction = selectedAction
                            } else {
                                onExecuteAction(selectedAction)
                            }
                        } else {
                            onDismiss()
                        }
                    },
                    onDragCancel = {
                        onDismiss()
                    }
                )
            }
    ) {
        ContextRingRenderer(
            ring = session.currentRing,
            centerX = clampedX,
            centerY = clampedY,
            selectedIndex = session.selectedIndex,
            breadcrumbs = session.breadcrumbRingIds,
            onSelectAction = { action ->
                if (action.requiresConfirmation) {
                    pendingConfirmationAction = action
                } else {
                    onExecuteAction(action)
                }
            },
            onNavigateBack = onNavigateBack,
            onDismiss = onDismiss
        )

        // Explicit visible close button in top right
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(16.dp)
                .background(Color(0x55000000), CircleShape)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Close Context Ring",
                tint = Color.White
            )
        }

        // Confirmation Dialog for high-impact multi-step macros
        pendingConfirmationAction?.let { action ->
            AlertDialog(
                onDismissRequest = { pendingConfirmationAction = null },
                title = { Text(text = "Execute ${action.title}?") },
                text = {
                    Text(
                        text = "This action will trigger multi-step macro [${action.payload}].\nAre you sure you want to proceed?",
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A9D8F)),
                        onClick = {
                            val act = action
                            pendingConfirmationAction = null
                            onExecuteAction(act)
                        }
                    ) {
                        Text("Execute")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { pendingConfirmationAction = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
