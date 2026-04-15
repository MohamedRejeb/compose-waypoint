package com.mohamedrejeb.waypoint.sample.demos.modals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VerticalSplit
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.mohamedrejeb.waypoint.core.HighlightStyle
import com.mohamedrejeb.waypoint.core.SpotlightShape
import com.mohamedrejeb.waypoint.core.TooltipPlacement
import com.mohamedrejeb.waypoint.core.rememberWaypointState
import com.mohamedrejeb.waypoint.core.waypointTarget
import com.mohamedrejeb.waypoint.material3.WaypointMaterial3Host
import com.mohamedrejeb.waypoint.sample.components.DemoScaffold

// -- Dialog tour targets --

private enum class DialogTarget {
    Notifications,
    Theme,
}

// -- Bottom sheet tour targets --

private enum class SheetTarget {
    ShareItem,
    EditItem,
}

// -- Scrollable tour targets --

private enum class ScrollTarget {
    Top,
    Middle,
    Bottom,
}

@Composable
fun ModalToursDemo(onBack: () -> Unit) {
    DemoScaffold(
        title = "Modal Tours",
        description = "Tours inside dialogs, bottom sheets, and scrollable containers",
        onBack = onBack,
        onStartTour = {},
        fabVisible = false,
    ) { padding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            DialogTourSection()
            BottomSheetTourSection()
            ScrollableTourSection()
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// Section 1: Dialog Tour
// ---------------------------------------------------------------------------

@Composable
private fun DialogTourSection() {
    var showDialog by remember { mutableStateOf(false) }

    SectionCard(
        icon = Icons.Default.ChatBubble,
        title = "Dialog Tour",
        description = "A 2-step tour inside a Dialog targeting a notifications switch and a theme toggle.",
        buttonLabel = "Open Dialog",
        onClick = { showDialog = true },
    )

    if (showDialog) {
        DialogTourContent(onDismiss = { showDialog = false })
    }
}

@Composable
private fun DialogTourContent(onDismiss: () -> Unit) {
    val state = rememberWaypointState {
        step(DialogTarget.Notifications) {
            title = "Notifications"
            description = "Toggle push notifications on or off"
            placement = TooltipPlacement.Bottom
            highlightStyle = HighlightStyle.Spotlight(
                shape = SpotlightShape.RoundedRect(8.dp),
            )
        }
        step(DialogTarget.Theme) {
            title = "Theme"
            description = "Switch between light and dark mode"
            placement = TooltipPlacement.Top
            highlightStyle = HighlightStyle.Spotlight(
                shape = SpotlightShape.RoundedRect(8.dp),
            )
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        WaypointMaterial3Host(state = state) {
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Notifications row
                    var notificationsOn by remember { mutableStateOf(true) }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .waypointTarget(state, DialogTarget.Notifications),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Notifications",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f),
                        )
                        Switch(
                            checked = notificationsOn,
                            onCheckedChange = { notificationsOn = it },
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Theme row
                    var darkMode by remember { mutableStateOf(false) }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .waypointTarget(state, DialogTarget.Theme),
                    ) {
                        Icon(
                            imageVector = Icons.Default.DarkMode,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Dark Mode",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f),
                        )
                        Switch(
                            checked = darkMode,
                            onCheckedChange = { darkMode = it },
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Language row (not a tour target, just decoration)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Language",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = "English",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.align(Alignment.End),
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Close")
                        }
                        Button(onClick = { state.start() }) {
                            Text("Start Tour")
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Section 2: Bottom Sheet Tour
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomSheetTourSection() {
    var showSheet by remember { mutableStateOf(false) }

    SectionCard(
        icon = Icons.Default.VerticalSplit,
        title = "Bottom Sheet Tour",
        description = "A 2-step tour inside a ModalBottomSheet targeting share and edit actions.",
        buttonLabel = "Open Sheet",
        onClick = { showSheet = true },
    )

    if (showSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        val tourState = rememberWaypointState {
            step(SheetTarget.ShareItem) {
                title = "Share"
                description = "Share this item with friends or colleagues"
                placement = TooltipPlacement.Top
                highlightStyle = HighlightStyle.Spotlight(
                    shape = SpotlightShape.RoundedRect(8.dp),
                )
            }
            step(SheetTarget.EditItem) {
                title = "Edit"
                description = "Modify item details or content"
                placement = TooltipPlacement.Top
                highlightStyle = HighlightStyle.Spotlight(
                    shape = SpotlightShape.RoundedRect(8.dp),
                )
            }
        }

        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
        ) {
            WaypointMaterial3Host(state = tourState) {
                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                    Text(
                        text = "Actions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )

                    HorizontalDivider()

                    SheetActionItem(
                        icon = Icons.Default.Share,
                        label = "Share",
                        modifier = Modifier.waypointTarget(tourState, SheetTarget.ShareItem),
                    )
                    SheetActionItem(
                        icon = Icons.Default.Edit,
                        label = "Edit",
                        modifier = Modifier.waypointTarget(tourState, SheetTarget.EditItem),
                    )
                    SheetActionItem(
                        icon = Icons.Default.Star,
                        label = "Favorite",
                    )
                    SheetActionItem(
                        icon = Icons.Default.Email,
                        label = "Send via Email",
                    )
                    SheetActionItem(
                        icon = Icons.Default.Delete,
                        label = "Delete",
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { tourState.start() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    ) {
                        Text("Start Tour")
                    }
                }
            }
        }
    }
}

@Composable
private fun SheetActionItem(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
) {
    ListItem(
        headlineContent = { Text(label) },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        modifier = modifier,
    )
}

// ---------------------------------------------------------------------------
// Section 3: Scrollable Content Tour
// ---------------------------------------------------------------------------

@Composable
private fun ScrollableTourSection() {
    val scrollTourState = rememberWaypointState {
        step(ScrollTarget.Top) {
            title = "Top Section"
            description = "This target is visible at the top of the scrollable area"
            placement = TooltipPlacement.Bottom
            highlightStyle = HighlightStyle.Spotlight(
                shape = SpotlightShape.RoundedRect(12.dp),
            )
        }
        step(ScrollTarget.Middle) {
            title = "Middle Section"
            description = "This target sits in the middle and may be partially visible"
            placement = TooltipPlacement.Bottom
            highlightStyle = HighlightStyle.Spotlight(
                shape = SpotlightShape.RoundedRect(12.dp),
            )
        }
        step(ScrollTarget.Bottom) {
            title = "Bottom Section"
            description = "This target is far below the fold and requires scrolling"
            placement = TooltipPlacement.Top
            highlightStyle = HighlightStyle.Spotlight(
                shape = SpotlightShape.RoundedRect(12.dp),
            )
        }
    }

    SectionCard(
        icon = Icons.AutoMirrored.Filled.ViewList,
        title = "Scrollable Content Tour",
        description = "A 3-step tour that auto-scrolls to targets placed at different scroll positions.",
        buttonLabel = "Start Tour",
        onClick = { scrollTourState.start() },
    )

    WaypointMaterial3Host(state = scrollTourState) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            ScrollableTargetCard(
                title = "Target 1 - Top",
                description = "This card is at the top of the scrollable area and should be visible immediately.",
                modifier = Modifier.waypointTarget(scrollTourState, ScrollTarget.Top),
            )

            // Spacers to push middle and bottom targets further down
            Spacer(modifier = Modifier.height(200.dp))

            ScrollableTargetCard(
                title = "Target 2 - Middle",
                description = "This card is in the middle. You may need to scroll a bit to see it.",
                modifier = Modifier.waypointTarget(scrollTourState, ScrollTarget.Middle),
            )

            Spacer(modifier = Modifier.height(300.dp))

            ScrollableTargetCard(
                title = "Target 3 - Bottom",
                description = "This card is far below the fold. The tour auto-scrolls here.",
                modifier = Modifier.waypointTarget(scrollTourState, ScrollTarget.Bottom),
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ScrollableTargetCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Shared section card
// ---------------------------------------------------------------------------

@Composable
private fun SectionCard(
    icon: ImageVector,
    title: String,
    description: String,
    buttonLabel: String,
    onClick: () -> Unit,
) {
    OutlinedCard(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp),
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onClick,
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(buttonLabel)
            }
        }
    }
}
