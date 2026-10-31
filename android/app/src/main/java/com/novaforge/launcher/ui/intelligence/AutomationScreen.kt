package com.novaforge.launcher.ui.intelligence

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novaforge.launcher.data.model.AppConstellation
import com.novaforge.launcher.data.model.AutomationRule
import com.novaforge.launcher.data.model.CommandMacro
import com.novaforge.launcher.data.model.MacroAction
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutomationScreen(
    rules: List<AutomationRule>,
    macros: List<CommandMacro>,
    constellations: List<AppConstellation>,
    automationsEnabled: Boolean,
    onToggleGlobalAutomations: (Boolean) -> Unit,
    onToggleRule: (String, Boolean) -> Unit,
    onDeleteRule: (String) -> Unit,
    onCreateRule: (AutomationRule) -> Unit,
    onCreateMacro: (CommandMacro) -> Unit,
    onDeleteMacro: (String) -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Automations, 1: Macros, 2: Constellations
    var showAddRuleDialog by remember { mutableStateOf(false) }
    var showAddMacroDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "NovaForge Intelligence",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            // Tabs: Automations | Macros | Constellations
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = { HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Automations", fontSize = 13.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Command Macros", fontSize = 13.sp) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Constellations", fontSize = 13.sp) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            when (selectedTab) {
                // Tab 0: Visual Automation Rules
                0 -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Workspace Automation", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("Local rules to trigger suggestions & space transitions", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = automationsEnabled, onCheckedChange = onToggleGlobalAutomations)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(rules, key = { it.id }) { rule ->
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(rule.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "IF ${rule.triggerType} = ${rule.triggerValue} → THEN ${rule.actionType} (${rule.actionValue})",
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Switch(
                                        checked = rule.isEnabled && automationsEnabled,
                                        onCheckedChange = { onToggleRule(rule.id, it) },
                                        enabled = automationsEnabled
                                    )

                                    IconButton(onClick = { onDeleteRule(rule.id) }) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedButton(
                                onClick = { showAddRuleDialog = true },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add Automation Rule")
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }

                // Tab 1: Command Macros
                1 -> {
                    Text(
                        "Command Macros chain actions together into one runnable command.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(macros, key = { it.id }) { macro ->
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(macro.commandName, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.primary)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(macro.title, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                        }

                                        if (macro.isCustom) {
                                            IconButton(onClick = { onDeleteMacro(macro.id) }, modifier = Modifier.size(24.dp)) {
                                                Icon(Icons.Default.Close, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(macro.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        macro.actions.forEachIndexed { idx, act ->
                                            Text(
                                                text = "${idx + 1}. ${act.description}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedButton(
                                onClick = { showAddMacroDialog = true },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Create Custom Command Macro")
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }

                // Tab 2: App Constellations
                2 -> {
                    Text(
                        "App Constellations are visual orbits of related applications for focused routines.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(constellations, key = { it.id }) { const ->
                            val accent = try {
                                Color(android.graphics.Color.parseColor(const.accentHex))
                            } catch (_: Exception) {
                                MaterialTheme.colorScheme.primary
                            }

                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(accent)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(const.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(const.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "${const.packageNames.size} apps connected in orbit",
                                        fontSize = 11.sp,
                                        color = accent,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog: Add Automation Rule
    if (showAddRuleDialog) {
        var ruleTitle by remember { mutableStateOf("") }
        var triggerType by remember { mutableStateOf("TIME_OF_DAY") }
        var triggerValue by remember { mutableStateOf("09:00") }
        var actionValue by remember { mutableStateOf("work") }

        AlertDialog(
            onDismissRequest = { showAddRuleDialog = false },
            title = { Text("New Automation Rule") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = ruleTitle,
                        onValueChange = { ruleTitle = it },
                        label = { Text("Rule Title (e.g. Work Time)") },
                        singleLine = true
                    )
                    Text("IF Trigger:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = triggerValue,
                        onValueChange = { triggerValue = it },
                        label = { Text("Time (e.g. 09:00)") },
                        singleLine = true
                    )
                    Text("THEN Suggest Workspace:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = actionValue,
                        onValueChange = { actionValue = it },
                        label = { Text("Workspace ID (work, study, gaming)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (ruleTitle.isNotBlank()) {
                            onCreateRule(
                                AutomationRule(
                                    id = "rule_" + UUID.randomUUID().toString().take(6),
                                    title = ruleTitle.trim(),
                                    triggerType = triggerType,
                                    triggerValue = triggerValue.trim(),
                                    actionType = "SUGGEST_WORKSPACE",
                                    actionValue = actionValue.trim()
                                )
                            )
                            showAddRuleDialog = false
                        }
                    },
                    enabled = ruleTitle.isNotBlank()
                ) {
                    Text("Add Rule")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddRuleDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog: Add Command Macro
    if (showAddMacroDialog) {
        var macroCommand by remember { mutableStateOf("/") }
        var macroTitle by remember { mutableStateOf("") }
        var targetSpace by remember { mutableStateOf("study") }

        AlertDialog(
            onDismissRequest = { showAddMacroDialog = false },
            title = { Text("Create Command Macro") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = macroCommand,
                        onValueChange = {
                            macroCommand = if (it.startsWith("/")) it else "/$it"
                        },
                        label = { Text("Command Trigger (e.g. /focus-now)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = macroTitle,
                        onValueChange = { macroTitle = it },
                        label = { Text("Title") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = targetSpace,
                        onValueChange = { targetSpace = it },
                        label = { Text("Target Workspace (study, work, gaming)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (macroCommand.length > 2 && macroTitle.isNotBlank()) {
                            onCreateMacro(
                                CommandMacro(
                                    id = "macro_" + UUID.randomUUID().toString().take(6),
                                    commandName = macroCommand.trim(),
                                    title = macroTitle.trim(),
                                    description = "Switch to ${targetSpace.trim()} and enable focus mode",
                                    actions = listOf(
                                        MacroAction("SWITCH_WORKSPACE", targetSpace.trim(), "Switch to ${targetSpace.trim()} space"),
                                        MacroAction("ENABLE_FOCUS", "true", "Engage Focus Mode")
                                    ),
                                    isCustom = true
                                )
                            )
                            showAddMacroDialog = false
                        }
                    },
                    enabled = macroCommand.length > 2 && macroTitle.isNotBlank()
                ) {
                    Text("Create Macro")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMacroDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
