package com.novaforge.launcher.ui.contextring

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
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
import com.novaforge.launcher.contextring.*
import com.novaforge.launcher.data.model.AppItem
import com.novaforge.launcher.data.model.WorkspaceSpace
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContextRingDesignerScreen(
    config: ContextRingConfig,
    allApps: List<AppItem>,
    workspaces: List<WorkspaceSpace>,
    onSaveRing: (ContextRingDefinition) -> Unit,
    onDeleteRing: (String) -> Unit,
    onDuplicateRing: (String) -> Unit,
    onResetDefaults: () -> Unit,
    onAssignApp: (String, String) -> Unit,
    onAssignWorkspace: (String, String) -> Unit,
    onUpdateHaptics: (String) -> Unit,
    onUpdateTrigger: (String) -> Unit,
    onExportJson: () -> String,
    onImportJson: (String) -> Boolean,
    onBack: () -> Unit
) {
    var selectedRingId by remember(config.rings) {
        mutableStateOf(config.rings.firstOrNull()?.id ?: "global_default")
    }

    val activeRing = remember(selectedRingId, config.rings) {
        config.rings.find { it.id == selectedRingId } ?: config.rings.firstOrNull() ?: ContextRingDefinition(
            id = "custom_" + UUID.randomUUID().toString().take(6),
            title = "New Ring",
            slotCount = 6,
            radiusDp = 140
        )
    }

    var editTitle by remember(activeRing.id) { mutableStateOf(activeRing.title) }
    var editDescription by remember(activeRing.id) { mutableStateOf(activeRing.description) }
    var editRadius by remember(activeRing.id) { mutableFloatStateOf(activeRing.radiusDp.toFloat()) }
    var editIconSize by remember(activeRing.id) { mutableFloatStateOf(activeRing.iconSizeDp.toFloat()) }
    var editSlotCount by remember(activeRing.id) { mutableIntStateOf(activeRing.slotCount) }
    var currentActions by remember(activeRing.id) { mutableStateOf(activeRing.actions) }

    var showActionEditorDialog by remember { mutableStateOf(false) }
    var editingActionIndex by remember { mutableIntStateOf(-1) }
    var showAppAssignmentDialog by remember { mutableStateOf(false) }
    var showExportImportDialog by remember { mutableStateOf(false) }
    var exportImportText by remember { mutableStateOf("") }
    var exportImportMode by remember { mutableStateOf("export") } // "export" or "import"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Context Ring Designer", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Radial Command System", fontSize = 12.sp, color = Color(0xFF2A9D8F))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val newRing = ContextRingDefinition(
                            id = "ring_" + UUID.randomUUID().toString().take(8),
                            title = "Custom Ring",
                            description = "User created context ring",
                            slotCount = 6,
                            radiusDp = 140,
                            iconSizeDp = 44,
                            actions = listOf(
                                ContextRingAction("act_1", "App Drawer", "grid", RingActionType.OPEN_APP_DRAWER),
                                ContextRingAction("act_2", "Command Bar", "terminal", RingActionType.OPEN_COMMAND_BAR),
                                ContextRingAction("act_3", "Focus Mode", "shield", RingActionType.TOGGLE_FOCUS_MODE),
                                ContextRingAction("act_4", "Settings", "settings", RingActionType.OPEN_SETTINGS)
                            )
                        )
                        onSaveRing(newRing)
                        selectedRingId = newRing.id
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Create New Ring")
                    }
                    IconButton(onClick = {
                        exportImportText = onExportJson()
                        exportImportMode = "export"
                        showExportImportDialog = true
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Export / Import")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF14171E),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF0F1218)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Ring Selector Carousel
            item {
                Text(
                    text = "AVAILABLE RINGS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(top = 8.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    items(config.rings) { ring ->
                        val isSelected = ring.id == selectedRingId
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color(0xFF2A9D8F) else Color(0xFF1C222C))
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Color.White else Color(0x332A9D8F),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedRingId = ring.id }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = ring.title,
                                color = if (isSelected) Color.White else Color(0xFFE2E8F0),
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Live Interactive Preview Box
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF181D26)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        ContextRingRenderer(
                            ring = activeRing.copy(
                                title = editTitle,
                                radiusDp = editRadius.toInt(),
                                iconSizeDp = editIconSize.toInt(),
                                slotCount = editSlotCount,
                                actions = currentActions
                            ),
                            centerX = 180f,
                            centerY = 140f,
                            selectedIndex = 0,
                            breadcrumbs = emptyList(),
                            onSelectAction = {},
                            onNavigateBack = {},
                            onDismiss = {}
                        )

                        Text(
                            text = "Interactive Ring Geometry Preview",
                            color = Color(0x66FFFFFF),
                            fontSize = 11.sp,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 8.dp)
                        )
                    }
                }
            }

            // Ring Properties Editor
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF181D26)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text("Ring Properties", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)

                        OutlinedTextField(
                            value = editTitle,
                            onValueChange = { editTitle = it },
                            label = { Text("Ring Name") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2A9D8F),
                                unfocusedBorderColor = Color(0x442A9D8F),
                                focusedLabelColor = Color(0xFF2A9D8F),
                                unfocusedLabelColor = Color(0xFF94A3B8),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        OutlinedTextField(
                            value = editDescription,
                            onValueChange = { editDescription = it },
                            label = { Text("Description / Purpose") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2A9D8F),
                                unfocusedBorderColor = Color(0x442A9D8F),
                                focusedLabelColor = Color(0xFF2A9D8F),
                                unfocusedLabelColor = Color(0xFF94A3B8),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        // Slot Count Selector (4, 6, 8, 10)
                        Column {
                            Text("Slot Positions: $editSlotCount", fontSize = 13.sp, color = Color(0xFFCBD5E1))
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(4, 6, 8, 10).forEach { count ->
                                    val isCountSelected = editSlotCount == count
                                    Button(
                                        onClick = { editSlotCount = count },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isCountSelected) Color(0xFF2A9D8F) else Color(0xFF252B36)
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("$count", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Radius Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Ring Radius", fontSize = 13.sp, color = Color(0xFFCBD5E1))
                                Text("${editRadius.toInt()} dp", fontSize = 13.sp, color = Color(0xFF2A9D8F))
                            }
                            Slider(
                                value = editRadius,
                                onValueChange = { editRadius = it },
                                valueRange = 100f..180f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF2A9D8F),
                                    activeTrackColor = Color(0xFF2A9D8F)
                                )
                            )
                        }

                        // Icon Size Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Icon Size", fontSize = 13.sp, color = Color(0xFFCBD5E1))
                                Text("${editIconSize.toInt()} dp", fontSize = 13.sp, color = Color(0xFF2A9D8F))
                            }
                            Slider(
                                value = editIconSize,
                                onValueChange = { editIconSize = it },
                                valueRange = 36f..52f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF2A9D8F),
                                    activeTrackColor = Color(0xFF2A9D8F)
                                )
                            )
                        }
                    }
                }
            }

            // Actions in this Ring
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RADIAL ACTIONS (${currentActions.size}/$editSlotCount)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8)
                    )
                    TextButton(onClick = {
                        editingActionIndex = -1
                        showActionEditorDialog = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF2A9D8F))
                        Spacer(Modifier.width(4.dp))
                        Text("Add Action", color = Color(0xFF2A9D8F))
                    }
                }
            }

            itemsIndexed(currentActions) { index, action ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF181D26)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2A9D8F)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = resolveActionIcon(action.iconName),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(action.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text("${action.actionType.name} ${if (action.payload.isNotBlank()) "• " + action.payload.take(18) else ""}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Move Up
                            if (index > 0) {
                                IconButton(onClick = {
                                    val list = currentActions.toMutableList()
                                    val item = list.removeAt(index)
                                    list.add(index - 1, item)
                                    currentActions = list
                                }) {
                                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move Up", tint = Color(0xFFCBD5E1))
                                }
                            }
                            // Move Down
                            if (index < currentActions.size - 1) {
                                IconButton(onClick = {
                                    val list = currentActions.toMutableList()
                                    val item = list.removeAt(index)
                                    list.add(index + 1, item)
                                    currentActions = list
                                }) {
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move Down", tint = Color(0xFFCBD5E1))
                                }
                            }
                            // Delete
                            IconButton(onClick = {
                                currentActions = currentActions.filterIndexed { i, _ -> i != index }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color(0xFFE76F51))
                            }
                        }
                    }
                }
            }

            // Save / Duplicate / Delete Ring Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val updated = activeRing.copy(
                                title = editTitle,
                                description = editDescription,
                                slotCount = editSlotCount,
                                radiusDp = editRadius.toInt(),
                                iconSizeDp = editIconSize.toInt(),
                                actions = currentActions
                            )
                            onSaveRing(updated)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A9D8F)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Save Ring")
                    }

                    OutlinedButton(
                        onClick = { onDuplicateRing(activeRing.id) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Duplicate")
                    }

                    if (!activeRing.isSystemPreset && activeRing.id != "global_default") {
                        IconButton(
                            onClick = { onDeleteRing(activeRing.id) },
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x33E76F51))
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Ring", tint = Color(0xFFE76F51))
                        }
                    }
                }
            }

            // App & Workspace Context Assignments
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF181D26)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Context Bindings", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(
                            "Bind this ring to activate on specific applications or workspaces instead of the global default.",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )

                        Button(
                            onClick = { showAppAssignmentDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF252B36)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Manage App & Workspace Assignments")
                        }
                    }
                }
            }

            // Haptics & Trigger Settings
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF181D26)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text("Interaction & Feedback", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)

                        // Haptic feedback intensity
                        Column {
                            Text("Haptic Feedback Intensity", fontSize = 13.sp, color = Color(0xFFCBD5E1))
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("off", "light", "medium", "strong").forEach { intensity ->
                                    val isSelected = config.hapticIntensity.equals(intensity, ignoreCase = true)
                                    Button(
                                        onClick = { onUpdateHaptics(intensity) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSelected) Color(0xFF2A9D8F) else Color(0xFF252B36)
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(intensity.replaceFirstChar { it.uppercase() }, fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        // Trigger Gesture
                        Column {
                            Text("Primary Ring Activation Gesture", fontSize = 13.sp, color = Color(0xFFCBD5E1))
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("long_press" to "Long Press", "double_tap" to "Double Tap", "swipe_up" to "Swipe Up").forEach { (key, label) ->
                                    val isSelected = config.gestureTrigger.equals(key, ignoreCase = true)
                                    Button(
                                        onClick = { onUpdateTrigger(key) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSelected) Color(0xFF2A9D8F) else Color(0xFF252B36)
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(label, fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        // Reset to defaults
                        OutlinedButton(
                            onClick = onResetDefaults,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Reset All Rings to System Defaults", color = Color(0xFFE76F51))
                        }
                    }
                }
            }
        }

        // Add/Edit Action Dialog
        if (showActionEditorDialog) {
            var actionTitle by remember { mutableStateOf("") }
            var actionType by remember { mutableStateOf(RingActionType.LAUNCH_APP) }
            var actionPayload by remember { mutableStateOf("") }
            var actionIcon by remember { mutableStateOf("star") }
            var actionConfirm by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { showActionEditorDialog = false },
                title = { Text("Add Radial Action") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = actionTitle,
                            onValueChange = { actionTitle = it },
                            label = { Text("Action Title") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Action Type Dropdown or Selection
                        Text("Category: ${actionType.name}", fontSize = 12.sp, color = Color(0xFF2A9D8F))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(listOf(
                                RingActionType.LAUNCH_APP,
                                RingActionType.OPEN_COMMAND_BAR,
                                RingActionType.OPEN_APP_DRAWER,
                                RingActionType.TOGGLE_FOCUS_MODE,
                                RingActionType.OPEN_SETTINGS,
                                RingActionType.SYSTEM_SETTING,
                                RingActionType.EXECUTE_MACRO,
                                RingActionType.MEDIA_CONTROL,
                                RingActionType.NESTED_RING
                            )) { type ->
                                FilterChip(
                                    selected = actionType == type,
                                    onClick = { actionType = type },
                                    label = { Text(type.name.take(12), fontSize = 10.sp) }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = actionPayload,
                            onValueChange = { actionPayload = it },
                            label = { Text("Payload / Package / Setting Key") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = actionIcon,
                            onValueChange = { actionIcon = it },
                            label = { Text("Icon Name (grid, terminal, shield, settings, wifi, sun, play)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = actionConfirm, onCheckedChange = { actionConfirm = it })
                            Text("Require Confirmation Prompt", fontSize = 12.sp)
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val newAction = ContextRingAction(
                            id = "act_" + UUID.randomUUID().toString().take(8),
                            title = if (actionTitle.isNotBlank()) actionTitle else "Action",
                            iconName = if (actionIcon.isNotBlank()) actionIcon else "star",
                            actionType = actionType,
                            payload = actionPayload,
                            requiresConfirmation = actionConfirm
                        )
                        currentActions = currentActions + newAction
                        showActionEditorDialog = false
                    }) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showActionEditorDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // App & Workspace Assignment Dialog
        if (showAppAssignmentDialog) {
            AlertDialog(
                onDismissRequest = { showAppAssignmentDialog = false },
                title = { Text("Context Ring Bindings") },
                text = {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().height(320.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text("WORKSPACES", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF2A9D8F))
                        }
                        items(workspaces) { space ->
                            val currentRingForSpace = config.workspaceAssignments[space.id] ?: "global_default"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(space.name, fontSize = 13.sp)
                                Button(
                                    onClick = { onAssignWorkspace(space.id, activeRing.id) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (currentRingForSpace == activeRing.id) Color(0xFF2A9D8F) else Color(0xFF252B36)
                                    )
                                ) {
                                    Text(if (currentRingForSpace == activeRing.id) "Bound" else "Bind", fontSize = 11.sp)
                                }
                            }
                        }

                        item {
                            Text("INSTALLED APPS", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF2A9D8F), modifier = Modifier.padding(top = 10.dp))
                        }
                        items(allApps.take(15)) { appItem ->
                            val currentRingForApp = config.appAssignments[appItem.packageName]
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(appItem.label.take(16), fontSize = 13.sp)
                                Button(
                                    onClick = {
                                        if (currentRingForApp == activeRing.id) {
                                            onAssignApp(appItem.packageName, "none")
                                        } else {
                                            onAssignApp(appItem.packageName, activeRing.id)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (currentRingForApp == activeRing.id) Color(0xFF2A9D8F) else Color(0xFF252B36)
                                    )
                                ) {
                                    Text(if (currentRingForApp == activeRing.id) "Bound" else "Bind", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showAppAssignmentDialog = false }) {
                        Text("Done")
                    }
                }
            )
        }

        // Export / Import JSON Dialog
        if (showExportImportDialog) {
            AlertDialog(
                onDismissRequest = { showExportImportDialog = false },
                title = { Text(if (exportImportMode == "export") "Export Context Rings JSON" else "Import Context Rings JSON") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    exportImportMode = "export"
                                    exportImportText = onExportJson()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (exportImportMode == "export") Color(0xFF2A9D8F) else Color(0xFF252B36)
                                )
                            ) {
                                Text("Export")
                            }
                            Button(
                                onClick = {
                                    exportImportMode = "import"
                                    exportImportText = ""
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (exportImportMode == "import") Color(0xFF2A9D8F) else Color(0xFF252B36)
                                )
                            ) {
                                Text("Import")
                            }
                        }
                        OutlinedTextField(
                            value = exportImportText,
                            onValueChange = { exportImportText = it },
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            label = { Text("JSON Payload") }
                        )
                    }
                },
                confirmButton = {
                    if (exportImportMode == "import") {
                        Button(onClick = {
                            val success = onImportJson(exportImportText)
                            if (success) showExportImportDialog = false
                        }) {
                            Text("Apply Import")
                        }
                    } else {
                        Button(onClick = { showExportImportDialog = false }) {
                            Text("Close")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExportImportDialog = false }) {
                        Text("Dismiss")
                    }
                }
            )
        }
    }
}
