package com.novaforge.launcher.ui.icon

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.novaforge.launcher.data.AppIconManager
import com.novaforge.launcher.data.model.AppItem
import com.novaforge.launcher.icon.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconAssignmentDialog(
    allApps: List<AppItem>,
    animations: List<AnimatedIconItem>,
    assignments: Map<String, IconAssignment>,
    initialAnimation: AnimatedIconItem? = null,
    initialAppPackage: String? = null,
    onAssignAnimation: (IconAssignment) -> Unit,
    onRemoveAssignment: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var filterMode by remember { mutableStateOf("All") } // "All", "Assigned", "Unassigned"
    var selectedAppPackage by remember { mutableStateOf(initialAppPackage ?: allApps.firstOrNull()?.packageName ?: "") }
    var selectedAnimationId by remember {
        mutableStateOf(
            initialAnimation?.id ?: assignments[selectedAppPackage]?.animationId ?: animations.firstOrNull()?.id ?: ""
        )
    }
    var playbackSpeed by remember {
        mutableStateOf(assignments[selectedAppPackage]?.playbackSpeed ?: 1.0f)
    }
    var loopMode by remember {
        mutableStateOf(assignments[selectedAppPackage]?.loopMode ?: "LOOP")
    }

    // When app selection changes, sync with existing assignment if any
    LaunchedEffect(selectedAppPackage) {
        val existing = assignments[selectedAppPackage]
        if (existing != null) {
            selectedAnimationId = existing.animationId
            playbackSpeed = existing.playbackSpeed
            loopMode = existing.loopMode
        } else if (initialAnimation != null) {
            selectedAnimationId = initialAnimation.id
        }
    }

    val selectedApp = allApps.find { it.packageName == selectedAppPackage }
    val selectedAnimation = animations.find { it.id == selectedAnimationId }

    val filteredApps = remember(allApps, searchQuery, filterMode, assignments) {
        allApps.filter { app ->
            val matchesQuery = app.label.contains(searchQuery, ignoreCase = true) ||
                    app.packageName.contains(searchQuery, ignoreCase = true)
            val isAssigned = assignments.containsKey(app.packageName)
            val matchesFilter = when (filterMode) {
                "Assigned" -> isAssigned
                "Unassigned" -> !isAssigned
                else -> true
            }
            matchesQuery && matchesFilter
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Assign Animated Icon",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Customize application icons in NovaForge",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Live Preview Card of the Selected App + Selected Animation
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Preview Icon
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(selectedApp?.colorHex ?: 0xFF2A9D8F.toInt())),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedAnimation != null) {
                            RenderProceduralAnimation(
                                format = selectedAnimation.format,
                                animationId = selectedAnimation.id,
                                speed = playbackSpeed,
                                size = 38.dp
                            )
                        } else {
                            val appBmp = remember(selectedAppPackage) {
                                AppIconManager.getAppIconBitmap(context, selectedAppPackage, 144)
                            }
                            Image(
                                bitmap = appBmp.asImageBitmap(),
                                contentDescription = selectedApp?.label,
                                modifier = Modifier.fillMaxSize().padding(6.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedApp?.label ?: "Select Application",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (selectedAnimation != null) "Animation: ${selectedAnimation.name} (${selectedAnimation.format.displayName})" else "Using original default icon",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (assignments.containsKey(selectedAppPackage)) {
                        TextButton(
                            onClick = {
                                onRemoveAssignment(selectedAppPackage)
                            }
                        ) {
                            Text("Reset", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Step 1: Animation Picker (Horizontal Scroll)
            Text(
                text = "1. CHOOSE ANIMATION FROM LIBRARY",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(animations, key = { it.id }) { anim ->
                    val isSelected = anim.id == selectedAnimationId
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                        modifier = Modifier
                            .width(110.dp)
                            .clickable { selectedAnimationId = anim.id }
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                                contentAlignment = Alignment.Center
                            ) {
                                RenderProceduralAnimation(
                                    format = anim.format,
                                    animationId = anim.id,
                                    speed = 1.0f,
                                    size = 24.dp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = anim.name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Speed & Loop Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Speed: ", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    listOf(0.5f, 1.0f, 1.5f, 2.0f).forEach { s ->
                        val isCurrent = playbackSpeed == s
                        Text(
                            text = "${s}x",
                            fontSize = 10.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isCurrent) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { playbackSpeed = s }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Loop: ", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = loopMode,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            .clickable {
                                loopMode = if (loopMode == "LOOP") "ONCE" else "LOOP"
                            }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Step 2: Target App Selection (Search + Filter + List)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "2. SELECT APPLICATION",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("All", "Assigned", "Unassigned").forEach { mode ->
                        val isCurrent = filterMode == mode
                        Text(
                            text = mode,
                            fontSize = 10.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isCurrent) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
                                .clickable { filterMode = mode }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // App Search
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Filter applications...", fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // App list
            LazyColumn(
                modifier = Modifier.heightIn(max = 160.dp)
            ) {
                items(filteredApps, key = { it.packageName }) { app ->
                    val isSelected = app.packageName == selectedAppPackage
                    val hasAssignment = assignments.containsKey(app.packageName)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
                            .clickable { selectedAppPackage = app.packageName }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val appBmp = remember(app.packageName) {
                            AppIconManager.getAppIconBitmap(context, app.packageName, 96)
                        }
                        Image(
                            bitmap = appBmp.asImageBitmap(),
                            contentDescription = app.label,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = app.label,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier.weight(1f)
                        )
                        if (hasAssignment) {
                            Text(
                                text = "Custom",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Apply / Restore Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        if (selectedAppPackage.isNotBlank() && selectedAnimationId.isNotBlank()) {
                            onAssignAnimation(
                                IconAssignment(
                                    packageName = selectedAppPackage,
                                    animationId = selectedAnimationId,
                                    playbackSpeed = playbackSpeed,
                                    loopMode = loopMode,
                                    animateOnAppear = true,
                                    animateOnLaunch = false,
                                    staticFirstFrameFallback = true,
                                    isEnabled = true
                                )
                            )
                        }
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Apply Animation", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Cancel", fontSize = 13.sp)
                }
            }
        }
    }
}
