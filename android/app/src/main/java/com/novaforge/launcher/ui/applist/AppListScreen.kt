package com.novaforge.launcher.ui.applist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novaforge.launcher.data.model.AppItem
import com.novaforge.launcher.service.MediaPlaybackInfo
import com.novaforge.launcher.ui.home.CompactMusicWidget
import kotlinx.coroutines.launch
import java.util.SortedMap

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
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
    onAssignIcon: (String) -> Unit = {},
    // Redesign preferences
    showAlphabetIndex: Boolean = true,
    showMusicDrawer: Boolean = false,
    mediaPlaybackInfo: MediaPlaybackInfo = MediaPlaybackInfo(),
    onPlayPauseMedia: () -> Unit = {},
    onSkipNextMedia: () -> Unit = {},
    onSkipPreviousMedia: () -> Unit = {},
    onTriggerContextRing: ((Float, Float, String?, String?) -> Unit)? = null
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var selectedAppForAction by remember { mutableStateOf<AppItem?>(null) }

    val categories = listOf("All", "Social", "Productivity", "Tools", "Media", "Games")

    // Full vertical index items: Top arrow, Star (favorites/recent), A-Z, #
    val alphabetRail = remember {
        listOf("↑", "★") + ('A'..'Z').map { it.toString() } + listOf("#")
    }

    var activeScrubLetter by remember { mutableStateOf<String?>(null) }
    var railHeightPx by remember { androidx.compose.runtime.mutableFloatStateOf(1f) }

    // Map each section header to its item index in the LazyColumn
    fun calculateTargetIndex(item: String): Int {
        if (item == "↑") return 0
        if (item == "★") return 0

        var currentIndex = 0
        val targetChar = item.firstOrNull() ?: return 0

        for ((initial, list) in appsGrouped) {
            if (initial == targetChar) {
                return currentIndex
            }
            // 1 for the header item + size of the list
            currentIndex += 1 + list.size
        }
        return 0
    }

    fun handleRailTouch(yPos: Float) {
        if (railHeightPx <= 0f) return
        val fraction = (yPos / railHeightPx).coerceIn(0f, 0.999f)
        val index = (fraction * alphabetRail.size).toInt().coerceIn(0, alphabetRail.size - 1)
        val selectedLetter = alphabetRail[index]
        activeScrubLetter = selectedLetter

        val targetLazyIndex = calculateTargetIndex(selectedLetter)
        coroutineScope.launch {
            listState.scrollToItem(targetLazyIndex)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

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
                        modifier = Modifier.size(19.dp)
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
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                ),
                singleLine = true
            )

            // Optional compact media widget at top of drawer
            if (showMusicDrawer && searchQuery.isEmpty()) {
                var isExpanded by remember { mutableStateOf(false) }
                Spacer(modifier = Modifier.height(10.dp))
                CompactMusicWidget(
                    info = mediaPlaybackInfo,
                    isExpanded = isExpanded,
                    onPlayPause = onPlayPauseMedia,
                    onSkipNext = onSkipNextMedia,
                    onSkipPrevious = onSkipPreviousMedia,
                    onToggleExpanded = { isExpanded = !isExpanded }
                )
            }

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
                Column(modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) {
                    Text(
                        text = "RECENT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        recentApps.take(4).forEach { app ->
                            val assignment = iconAssignments[app.packageName]
                            val animItem = if (assignment != null) animations.find { it.id == assignment.animationId } else null
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(64.dp)
                                    .combinedClickable(
                                        onClick = { onLaunchApp(app.packageName) },
                                        onLongClick = {
                                            if (onTriggerContextRing != null) {
                                                onTriggerContextRing(200f, 300f, app.packageName, null)
                                            } else {
                                                selectedAppForAction = app
                                            }
                                        }
                                    )
                            ) {
                                com.novaforge.launcher.icon.AnimatedAppIcon(
                                    packageName = app.packageName,
                                    label = app.label,
                                    fallbackColor = Color(app.colorHex),
                                    size = 42.dp,
                                    shape = RoundedCornerShape(13.dp),
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
                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

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
                            val assignment = iconAssignments[app.packageName]
                            val animItem = if (assignment != null) animations.find { it.id == assignment.animationId } else null
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = { onLaunchApp(app.packageName) },
                                        onLongClick = {
                                            if (onTriggerContextRing != null) {
                                                onTriggerContextRing(200f, 300f, app.packageName, null)
                                            } else {
                                                selectedAppForAction = app
                                            }
                                        }
                                    )
                                    .padding(vertical = 7.dp),
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
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f)
                                        ) {
                                            Text(
                                                text = app.category,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        if (app.launchCount > 0) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "• ${app.launchCount} launches",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
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

                // Vertical A–Z Index Rail on Right (Continuous tap & drag scrub)
                if (showAlphabetIndex && appsGrouped.size > 2) {
                    Column(
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .fillMaxHeight()
                            .onGloballyPositioned { coordinates ->
                                railHeightPx = coordinates.size.height.toFloat()
                            }
                            .pointerInput(alphabetRail) {
                                detectTapGestures(
                                    onPress = { offset ->
                                        handleRailTouch(offset.y)
                                        tryAwaitRelease()
                                        activeScrubLetter = null
                                    }
                                )
                            }
                            .pointerInput(alphabetRail) {
                                detectDragGestures(
                                    onDragStart = { offset -> handleRailTouch(offset.y) },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        handleRailTouch(change.position.y)
                                    },
                                    onDragEnd = { activeScrubLetter = null },
                                    onDragCancel = { activeScrubLetter = null }
                                )
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        alphabetRail.forEach { ch ->
                            Text(
                                text = ch,
                                fontSize = if (ch == "↑" || ch == "★") 10.sp else 9.sp,
                                fontWeight = if (activeScrubLetter == ch) FontWeight.Black else FontWeight.Medium,
                                color = if (activeScrubLetter == ch) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                                },
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Floating Letter Indicator Bubble (appears while user scrubs vertical rail)
        AnimatedVisibility(
            visible = activeScrubLetter != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .shadow(12.dp, CircleShape)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = activeScrubLetter ?: "",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
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

                    // Action: Trigger Context Ring
                    if (onTriggerContextRing != null) {
                        AppActionItem(
                            icon = Icons.Default.RadioButtonChecked,
                            title = "Open Context Ring",
                            subtitle = "Radial menu for app shortcuts and actions",
                            onClick = {
                                val pkg = app.packageName
                                selectedAppForAction = null
                                onTriggerContextRing(200f, 400f, pkg, null)
                            }
                        )
                    }

                    // Action: App Info
                    AppActionItem(
                        icon = Icons.Default.Info,
                        title = "Application Info",
                        subtitle = "System permissions, storage & notifications",
                        onClick = {
                            onOpenAppInfo(app.packageName)
                            selectedAppForAction = null
                        }
                    )

                    // Action: Hide App
                    AppActionItem(
                        icon = Icons.Default.VisibilityOff,
                        title = "Hide Application",
                        subtitle = "Protected behind PIN lock in Settings",
                        onClick = {
                            onHideApp(app.packageName)
                            selectedAppForAction = null
                        }
                    )

                    // Action: Request Uninstall
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
    icon: ImageVector,
    title: String,
    subtitle: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 8.dp),
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
