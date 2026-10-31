package com.novaforge.launcher.ui.applist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.novaforge.launcher.data.model.AppItem
import kotlinx.coroutines.launch
import java.util.SortedMap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListScreen(
    appsGrouped: SortedMap<Char, List<AppItem>>,
    recentApps: List<AppItem> = emptyList(),
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    onLaunchApp: (String) -> Unit,
    onToggleFavorite: (String) -> Unit = {},
    onHideApp: (String) -> Unit = {},
    onOpenAppInfo: (String) -> Unit = {},
    onRequestUninstall: (String) -> Unit = {},
    iconAssignments: Map<String, com.novaforge.launcher.icon.IconAssignment> = emptyMap(),
    animations: List<com.novaforge.launcher.icon.AnimatedIconItem> = emptyList(),
    onAssignIcon: (String) -> Unit = {}
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var selectedAppForAction by remember { mutableStateOf<AppItem?>(null) }

    val categories = listOf("All", "Social", "Productivity", "Tools", "Media", "Games")

    // Flattened alphabet index to scroll positions
    val alphabetKeys = remember(appsGrouped) { appsGrouped.keys.toList() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                placeholder = {
                    Text(
                        "Search applications...",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(26.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Category Filter Rail
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectCategory(category) },
                        label = {
                            Text(
                                text = category,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            // Recently Used Apps Shelf (when not actively searching)
            AnimatedVisibility(visible = searchQuery.isEmpty() && selectedCategory == "All" && recentApps.isNotEmpty()) {
                Column(modifier = Modifier.padding(top = 10.dp, bottom = 6.dp)) {
                    Text(
                        text = "RECENT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        recentApps.take(4).forEach { app ->
                            val assignment = iconAssignments[app.packageName]
                            val animItem = if (assignment != null) animations.find { it.id == assignment.animationId } else null
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(64.dp)
                                    .clickable { onLaunchApp(app.packageName) }
                            ) {
                                com.novaforge.launcher.icon.AnimatedAppIcon(
                                    packageName = app.packageName,
                                    label = app.label,
                                    fallbackColor = Color(app.colorHex),
                                    size = 44.dp,
                                    shape = RoundedCornerShape(14.dp),
                                    assignment = assignment,
                                    animationItem = animItem
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = app.label,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Main Alphabetical App List with A-Z rail
            Row(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f)
                ) {
                    if (appsGrouped.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No applications found",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    appsGrouped.forEach { (initial, appList) ->
                        item(key = "header_$initial") {
                            Text(
                                text = initial.toString(),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 14.dp, bottom = 4.dp)
                            )
                        }

                        items(appList, key = { it.packageName }) { app ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                            val assignment = iconAssignments[app.packageName]
                            val animItem = if (assignment != null) animations.find { it.id == assignment.animationId } else null
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = { onLaunchApp(app.packageName) },
                                        onLongClick = { selectedAppForAction = app }
                                    )
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                com.novaforge.launcher.icon.AnimatedAppIcon(
                                    packageName = app.packageName,
                                    label = app.label,
                                    fallbackColor = Color(app.colorHex),
                                    size = 38.dp,
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
                                        text = app.category,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }

                                if (app.isFavorite) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = "Favorite",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Fast scroll A–Z rail on right
                if (alphabetKeys.size > 2) {
                    Column(
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .align(Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        alphabetKeys.forEach { ch ->
                            Text(
                                text = ch.toString(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .clickable {
                                        coroutineScope.launch {
                                            // Find index of header
                                            var targetIndex = 0
                                            for (k in alphabetKeys) {
                                                if (k == ch) break
                                                targetIndex += 1 + (appsGrouped[k]?.size ?: 0)
                                            }
                                            listState.animateScrollToItem(targetIndex)
                                        }
                                    }
                                    .padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Long-Press App Action Modal / Bottom Sheet
        if (selectedAppForAction != null) {
            val app = selectedAppForAction!!
            ModalBottomSheet(
                onDismissRequest = { selectedAppForAction = null },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 32.dp)
                ) {
                    // Header with app icon and name
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 20.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(app.colorHex)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = app.label.take(1),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = app.label,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = app.packageName,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Action: Favorite Toggle
                    AppActionItem(
                        icon = if (app.isFavorite) Icons.Default.StarOutline else Icons.Default.Star,
                        title = if (app.isFavorite) "Remove from Favorites" else "Add to Favorites",
                        subtitle = "Pin to Home screen quick access list",
                        onClick = {
                            onToggleFavorite(app.packageName)
                            selectedAppForAction = null
                        }
                    )

                    // Action: Assign Animated Icon
                    AppActionItem(
                        icon = Icons.Default.Animation,
                        title = "Assign Animated Icon",
                        subtitle = "Select GIF, WebP or Lottie animation",
                        onClick = {
                            val pkg = app.packageName
                            selectedAppForAction = null
                            onAssignIcon(pkg)
                        }
                    )

                    // Action: Hide App
                    AppActionItem(
                        icon = Icons.Default.VisibilityOff,
                        title = "Hide Application",
                        subtitle = "Move to PIN-protected hidden apps vault",
                        onClick = {
                            onHideApp(app.packageName)
                            selectedAppForAction = null
                        }
                    )

                    // Action: App Info
                    AppActionItem(
                        icon = Icons.Default.Info,
                        title = "App Info",
                        subtitle = "Open system storage, permissions & settings",
                        onClick = {
                            onOpenAppInfo(app.packageName)
                            selectedAppForAction = null
                        }
                    )

                    // Action: Uninstall App
                    AppActionItem(
                        icon = Icons.Default.Delete,
                        title = "Uninstall",
                        subtitle = "Remove app from device",
                        isDestructive = true,
                        onClick = {
                            onRequestUninstall(app.packageName)
                            selectedAppForAction = null
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AppActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
