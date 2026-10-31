package com.novaforge.launcher.ui.spaces

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novaforge.launcher.data.model.WorkspaceSpace

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpacesScreen(
    workspaces: List<WorkspaceSpace>,
    activeWorkspaceId: String,
    onSelectWorkspace: (String) -> Unit,
    onCreateWorkspace: (String, String, String) -> Unit,
    onOpenAutomations: () -> Unit = {},
    onBack: () -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Workspaces & Spaces",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onOpenAutomations) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "Intelligence & Automations", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "New Space", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Select an active workspace capsule. Your Home screen, predictive dock, and shortcuts adapt automatically to your context.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            items(workspaces, key = { it.id }) { space ->
                val isActive = space.id == activeWorkspaceId
                val accentColor = try {
                    Color(android.graphics.Color.parseColor(space.accentHex))
                } catch (_: Exception) {
                    MaterialTheme.colorScheme.primary
                }

                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isActive) {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .then(
                            if (isActive) {
                                Modifier.border(1.5.dp, accentColor, RoundedCornerShape(22.dp))
                            } else Modifier
                        )
                        .clickable { onSelectWorkspace(space.id) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Space Icon Box
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(accentColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (space.iconName) {
                                    "briefcase", "work" -> Icons.Default.Work
                                    "book", "study" -> Icons.Default.MenuBook
                                    "gamepad", "gaming" -> Icons.Default.SportsEsports
                                    "compass", "travel" -> Icons.Default.Explore
                                    else -> Icons.Default.Person
                                },
                                contentDescription = space.name,
                                tint = accentColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = space.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                if (isActive) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = accentColor.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "ACTIVE",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = accentColor,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = space.description,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (isActive) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Active",
                                tint = accentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(
                                text = "SWITCH",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedButton(
                    onClick = { showCreateDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Create Custom Workspace Capsule")
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showCreateDialog) {
        var spaceName by remember { mutableStateOf("") }
        var spaceDesc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("New Workspace Capsule") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Design a tailored workspace for your routine:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = spaceName,
                        onValueChange = { spaceName = it },
                        label = { Text("Workspace Name (e.g. Fitness)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = spaceDesc,
                        onValueChange = { spaceDesc = it },
                        label = { Text("Description") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (spaceName.isNotBlank()) {
                            onCreateWorkspace(spaceName.trim(), spaceDesc.trim(), "#2A9D8F")
                            showCreateDialog = false
                        }
                    },
                    enabled = spaceName.isNotBlank()
                ) {
                    Text("Create Space")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
