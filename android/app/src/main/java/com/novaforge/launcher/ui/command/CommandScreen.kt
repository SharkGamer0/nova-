package com.novaforge.launcher.ui.command

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novaforge.launcher.data.model.AppItem
import com.novaforge.launcher.data.model.CommandAction
import com.novaforge.launcher.data.model.CommandMacro

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommandScreen(
    query: String,
    onQueryChange: (String) -> Unit,
    apps: List<AppItem>,
    commands: List<CommandAction>,
    macros: List<CommandMacro>,
    recentCommands: List<String>,
    favoriteCommands: Set<String>,
    onToggleFavoriteCommand: (String) -> Unit,
    onExecuteCommand: (String) -> Unit,
    onExecuteMacro: (CommandMacro) -> Unit,
    onLaunchApp: (String) -> Unit,
    iconAssignments: Map<String, com.novaforge.launcher.icon.IconAssignment> = emptyMap(),
    animations: List<com.novaforge.launcher.icon.AnimatedIconItem> = emptyList(),
    onBack: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    var selectedFilter by remember { mutableStateOf("All") }
    var macroForPreview by remember { mutableStateOf<CommandMacro?>(null) }

    LaunchedEffect(Unit) {
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    // Filtered Commands
    val filteredCommands = remember(query, commands, selectedFilter) {
        if (selectedFilter == "Apps" || selectedFilter == "Macros") return@remember emptyList()
        val cleanQuery = query.removePrefix("/").trim()
        if (cleanQuery.isEmpty()) {
            commands
        } else {
            commands.filter {
                it.command.contains(cleanQuery, ignoreCase = true) ||
                    it.title.contains(cleanQuery, ignoreCase = true) ||
                    it.subtitle.contains(cleanQuery, ignoreCase = true)
            }
        }
    }

    // Filtered Macros
    val filteredMacros = remember(query, macros, selectedFilter) {
        if (selectedFilter == "Apps" || selectedFilter == "Commands") return@remember emptyList()
        val cleanQuery = query.removePrefix("/").trim()
        if (cleanQuery.isEmpty()) {
            macros
        } else {
            macros.filter {
                it.commandName.contains(cleanQuery, ignoreCase = true) ||
                    it.title.contains(cleanQuery, ignoreCase = true) ||
                    it.description.contains(cleanQuery, ignoreCase = true)
            }
        }
    }

    // Filtered Apps
    val filteredApps = remember(query, apps, selectedFilter) {
        if (selectedFilter == "Commands" || selectedFilter == "Macros") return@remember emptyList()
        if (query.startsWith("/")) return@remember emptyList()
        if (query.isEmpty()) return@remember emptyList()
        apps.filter {
            it.label.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true)
        }.take(8)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(12.dp))

            // Command Input Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .focusRequester(focusRequester),
                    placeholder = {
                        Text(
                            "Type /command or search app...",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Terminal,
                            contentDescription = "Command",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    shape = RoundedCornerShape(26.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    ),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Filter Chips
            val filterOptions = listOf("All", "Macros", "Commands", "Apps")
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filterOptions) { opt ->
                    val isSelected = selectedFilter == opt
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = opt },
                        label = {
                            Text(
                                opt,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Command / Macro / App Results
            LazyColumn(modifier = Modifier.weight(1f)) {
                // Section: Recent & Favorite Commands (when query is empty)
                if (query.isEmpty() && favoriteCommands.isNotEmpty()) {
                    item {
                        Text(
                            text = "STARRED COMMANDS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }

                    items(favoriteCommands.toList()) { cmdStr ->
                        val matchedCmd = commands.find { it.command == cmdStr }
                        val matchedMacro = macros.find { it.commandName == cmdStr }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (matchedMacro != null) {
                                        onExecuteMacro(matchedMacro)
                                    } else {
                                        onExecuteCommand(cmdStr)
                                    }
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Star, contentDescription = "Starred", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(cmdStr, fontSize = 13.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(matchedMacro?.title ?: matchedCmd?.title ?: "", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    }
                }

                // Section: App Matches
                if (filteredApps.isNotEmpty()) {
                    item {
                        Text(
                            text = "APPLICATIONS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(filteredApps, key = { "app_${it.packageName}" }) { app ->
                        val assignment = iconAssignments[app.packageName]
                        val animItem = if (assignment != null) animations.find { it.id == assignment.animationId } else null
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onLaunchApp(app.packageName) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            com.novaforge.launcher.icon.AnimatedAppIcon(
                                packageName = app.packageName,
                                label = app.label,
                                fallbackColor = Color(app.colorHex),
                                size = 36.dp,
                                shape = RoundedCornerShape(12.dp),
                                assignment = assignment,
                                animationItem = animItem
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = app.label,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "${app.category} • Tap to launch",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                Icons.Default.ArrowOutward,
                                contentDescription = "Launch",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    }
                }

                // Section: Command Macros
                if (filteredMacros.isNotEmpty()) {
                    item {
                        Text(
                            text = "COMMAND MACROS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(filteredMacros, key = { "macro_${it.id}" }) { macro ->
                        val isStarred = favoriteCommands.contains(macro.commandName)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { macroForPreview = macro }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Bolt, contentDescription = "Macro", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(macro.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text("MACRO", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                    }
                                }
                                Text(
                                    text = "${macro.commandName} • ${macro.actions.size} actions",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = { onToggleFavoriteCommand(macro.commandName) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isStarred) Icons.Default.Star else Icons.Default.StarOutline,
                                    contentDescription = "Star",
                                    tint = if (isStarred) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    }
                }

                // Section: Universal Commands
                if (filteredCommands.isNotEmpty()) {
                    item {
                        Text(
                            text = "UNIVERSAL COMMANDS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(filteredCommands, key = { "cmd_${it.command}" }) { cmd ->
                        val isStarred = favoriteCommands.contains(cmd.command)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onExecuteCommand(cmd.command) }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (cmd.category) {
                                        "Workspace" -> Icons.Default.Workspaces
                                        "System" -> Icons.Default.SettingsSuggest
                                        "Launcher" -> Icons.Default.Dashboard
                                        else -> Icons.Default.Terminal
                                    },
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = cmd.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = cmd.command,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    text = cmd.subtitle,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = { onToggleFavoriteCommand(cmd.command) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isStarred) Icons.Default.Star else Icons.Default.StarOutline,
                                    contentDescription = "Star",
                                    tint = if (isStarred) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Preview & Run Macro Bottom Sheet
        if (macroForPreview != null) {
            val macro = macroForPreview!!
            ModalBottomSheet(
                onDismissRequest = { macroForPreview = null },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 32.dp)
                ) {
                    Text(macro.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = macro.commandName,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(macro.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("ACTIONS TO EXECUTE:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))

                    macro.actions.forEachIndexed { i, a ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("${i + 1}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(a.description, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            val target = macro
                            macroForPreview = null
                            onExecuteMacro(target)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Execute Macro Actions")
                    }
                }
            }
        }
    }
}
