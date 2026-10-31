package com.novaforge.launcher.contextring

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun ContextRingRenderer(
    ring: ContextRingDefinition,
    centerX: Float,
    centerY: Float,
    selectedIndex: Int,
    breadcrumbs: List<String>,
    onSelectAction: (ContextRingAction) -> Unit,
    onNavigateBack: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val scaleAnim = remember { Animatable(0.5f) }
    val alphaAnim = remember { Animatable(0f) }

    LaunchedEffect(ring.id) {
        scaleAnim.snapTo(0.6f)
        alphaAnim.snapTo(0.2f)
        scaleAnim.animateTo(1.0f, tween(260, easing = FastOutSlowInEasing))
        alphaAnim.animateTo(1.0f, tween(220, easing = FastOutSlowInEasing))
    }

    val radiusPx = with(density) { ring.radiusDp.dp.toPx() }
    val engine = remember { ContextRingEngine() }
    val slots = ring.actions

    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(alphaAnim.value)
    ) {
        // Background radial canvas track with subtle glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerOffset = Offset(centerX, centerY)

            // Outer guideline ring
            drawCircle(
                color = Color(0x332A9D8F),
                radius = radiusPx,
                center = centerOffset,
                style = Stroke(
                    width = 2.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                )
            )

            // Inner boundary ring
            drawCircle(
                color = Color(0x1F2A9D8F),
                radius = radiusPx * 0.45f,
                center = centerOffset,
                style = Stroke(width = 1.5f)
            )

            // Highlight connection line to selected action
            if (selectedIndex in slots.indices) {
                val (slotX, slotY) = engine.getSlotPosition(selectedIndex, slots.size, radiusPx)
                drawLine(
                    color = Color(0xAA2A9D8F),
                    start = centerOffset,
                    end = Offset(centerX + slotX, centerY + slotY),
                    strokeWidth = 3f
                )
            }
        }

        // Action Slots arranged circularly
        val centerDpX = with(density) { centerX.toDp() }
        val centerDpY = with(density) { centerY.toDp() }

        slots.forEachIndexed { index, action ->
            val isSelected = selectedIndex == index
            val (offsetX, offsetY) = engine.getSlotPosition(index, slots.size, radiusPx)
            val offsetDpX = with(density) { offsetX.toDp() }
            val offsetDpY = with(density) { offsetY.toDp() }

            val itemScale = if (isSelected) 1.22f else 1.0f

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (centerX + offsetX - with(density) { (ring.iconSizeDp / 2).dp.toPx() }).roundToInt(),
                            (centerY + offsetY - with(density) { (ring.iconSizeDp / 2).dp.toPx() }).roundToInt()
                        )
                    }
                    .scale(itemScale * scaleAnim.value)
                    .size(ring.iconSizeDp.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) Color(0xFF2A9D8F) else Color(0xEE1E222A)
                    )
                    .border(
                        width = if (isSelected) 2.5.dp else 1.dp,
                        color = if (isSelected) Color.White else Color(0x442A9D8F),
                        shape = CircleShape
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onSelectAction(action)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = resolveActionIcon(action.iconName),
                    contentDescription = action.title,
                    tint = if (isSelected) Color.White else Color(0xFFE2E8F0),
                    modifier = Modifier.size(22.dp)
                )
            }

            // Text label for highlighted action or positioned label
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (centerX + offsetX - with(density) { 60.dp.toPx() }).roundToInt(),
                                (centerY + offsetY + with(density) { (ring.iconSizeDp / 2 + 10).dp.toPx() }).roundToInt()
                            )
                        }
                        .width(120.dp)
                        .background(Color(0xDD0F141C), CircleShape)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = action.title,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }

        // Center Target Hub
        val hubSize = (ring.iconSizeDp * 1.35f).dp
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        (centerX - with(density) { (hubSize / 2).toPx() }).roundToInt(),
                        (centerY - with(density) { (hubSize / 2).toPx() }).roundToInt()
                    )
                }
                .scale(scaleAnim.value)
                .size(hubSize)
                .clip(CircleShape)
                .background(Color(0xF0181D26))
                .border(2.dp, Color(0xFF2A9D8F), CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if (breadcrumbs.isNotEmpty()) {
                        onNavigateBack()
                    } else {
                        onDismiss()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (breadcrumbs.isNotEmpty()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF2A9D8F),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Back",
                        color = Color(0xFF94A3B8),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = ring.title.take(12),
                        color = Color(0xFFE2E8F0),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                    Text(
                        text = "${slots.size} acts",
                        color = Color(0xFF2A9D8F),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}

fun resolveActionIcon(iconName: String): ImageVector {
    return when (iconName.lowercase()) {
        "grid", "apps" -> Icons.Default.Menu
        "terminal", "command" -> Icons.Default.PlayArrow
        "shield", "focus" -> Icons.Default.Check
        "layers", "widgets" -> Icons.Default.List
        "compass", "star" -> Icons.Default.Star
        "settings" -> Icons.Default.Settings
        "globe", "browser" -> Icons.Default.Search
        "search" -> Icons.Default.Search
        "wifi" -> Icons.Default.Refresh
        "sun", "display" -> Icons.Default.Star
        "play" -> Icons.Default.PlayArrow
        "play-circle" -> Icons.Default.PlayArrow
        "skip-forward" -> Icons.Default.ArrowForward
        "skip-back" -> Icons.Default.ArrowBack
        "music" -> Icons.Default.PlayArrow
        "bluetooth" -> Icons.Default.Share
        "hash", "calc" -> Icons.Default.Add
        "edit-3", "notes" -> Icons.Default.Edit
        "arrow-left" -> Icons.Default.ArrowBack
        "folder" -> Icons.Default.AccountBox
        "message-circle" -> Icons.Default.Email
        "battery", "zap" -> Icons.Default.Warning
        else -> Icons.Default.Star
    }
}
