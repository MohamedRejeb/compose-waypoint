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
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.VerticalSplit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import com.mohamedrejeb.waypoint.material3.WaypointMaterial3OverlayHost
import com.mohamedrejeb.waypoint.sample.components.DemoScaffold

// -- Dialog tour targets --

private enum class DialogTarget {
    OpenButton,
    Notifications,
    Theme,
}

// -- Bottom sheet tour targets --

private enum class SheetTarget {
    OpenButton,
    ShareItem,
    EditItem,
}

@Composable
fun ModalToursDemo(onBack: () -> Unit) {
    DemoScaffold(
        title = "Modal Tours",
        description = "Cross-layer tours — the overlay renders fullscreen above dialogs and sheets",
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

    val state = rememberWaypointState {
        step(DialogTarget.OpenButton) {
            title = "Open Settings"
            description = "Tap to open the settings dialog"
            placement = TooltipPlacement.Bottom
            onEnter { showDialog = false }
        }
        step(DialogTarget.Notifications) {
            title = "Notifications"
            description = "Toggle push notifications on or off"
            placement = TooltipPlacement.Bottom
            highlightStyle = HighlightStyle.Spotlight(
                shape = SpotlightShape.RoundedRect(8.dp),
            )
            beforeShow { showDialog = true }
        }
        step(DialogTarget.Theme) {
            title = "Theme"
            description = "Switch between light and dark mode"
            placement = TooltipPlacement.Top
            highlightStyle = HighlightStyle.Spotlight(
                shape = SpotlightShape.RoundedRect(8.dp),
            )
            beforeShow { showDialog = true }
        }
    }

    WaypointMaterial3Host(state = state) {
        SectionCard(
            icon = Icons.Rounded.ChatBubble,
            title = "Dialog Tour",
            description = "A cross-layer tour: step 1 highlights the button, steps 2-3 highlight elements inside a Dialog.",
            primaryButtonLabel = "Open Dialog",
            onPrimaryClick = { showDialog = true },
            secondaryButtonLabel = "Start Tour",
            onSecondaryClick = { state.start() },
            primaryButtonModifier = Modifier.waypointTarget(state, DialogTarget.OpenButton),
        )

        if (showDialog) {
            DialogContent(
                state = state,
                onDismiss = { showDialog = false },
            )
        }
    }
}

@Composable
private fun DialogContent(
    state: com.mohamedrejeb.waypoint.core.WaypointState<DialogTarget>,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        WaypointMaterial3OverlayHost(state = state) {
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
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
                            imageVector = Icons.Rounded.Notifications,
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
                            imageVector = Icons.Rounded.DarkMode,
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
                            imageVector = Icons.Rounded.Translate,
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

                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.End),
                    ) {
                        Text("Close")
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

    val state = rememberWaypointState {
        step(SheetTarget.OpenButton) {
            title = "Open Actions"
            description = "Tap to open the actions sheet"
            placement = TooltipPlacement.Bottom
            onEnter { showSheet = false }
        }
        step(SheetTarget.ShareItem) {
            title = "Share"
            description = "Share this item with friends or colleagues"
            placement = TooltipPlacement.Top
            highlightStyle = HighlightStyle.Spotlight(
                shape = SpotlightShape.RoundedRect(8.dp),
            )
            beforeShow { showSheet = true }
        }
        step(SheetTarget.EditItem) {
            title = "Edit"
            description = "Modify item details or content"
            placement = TooltipPlacement.Top
            highlightStyle = HighlightStyle.Spotlight(
                shape = SpotlightShape.RoundedRect(8.dp),
            )
            beforeShow { showSheet = true }
        }
    }

    WaypointMaterial3Host(state = state) {
        SectionCard(
            icon = Icons.Rounded.VerticalSplit,
            title = "Bottom Sheet Tour",
            description = "A cross-layer tour: step 1 highlights the button, steps 2-3 highlight elements inside a ModalBottomSheet.",
            primaryButtonLabel = "Open Sheet",
            onPrimaryClick = { showSheet = true },
            secondaryButtonLabel = "Start Tour",
            onSecondaryClick = { state.start() },
            primaryButtonModifier = Modifier.waypointTarget(state, SheetTarget.OpenButton),
        )

        if (showSheet) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                sheetState = sheetState,
            ) {
                WaypointMaterial3OverlayHost(state = state) {
                    Column(modifier = Modifier.padding(bottom = 16.dp)) {
                        Text(
                            text = "Actions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )

                        HorizontalDivider()

                        SheetActionItem(
                            icon = Icons.Rounded.Share,
                            label = "Share",
                            modifier = Modifier.waypointTarget(state, SheetTarget.ShareItem),
                        )
                        SheetActionItem(
                            icon = Icons.Rounded.Edit,
                            label = "Edit",
                            modifier = Modifier.waypointTarget(state, SheetTarget.EditItem),
                        )
                        SheetActionItem(
                            icon = Icons.Rounded.Star,
                            label = "Favorite",
                        )
                        SheetActionItem(
                            icon = Icons.Rounded.Email,
                            label = "Send via Email",
                        )
                        SheetActionItem(
                            icon = Icons.Rounded.Delete,
                            label = "Delete",
                        )
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
// Shared section card
// ---------------------------------------------------------------------------

@Composable
private fun SectionCard(
    icon: ImageVector,
    title: String,
    description: String,
    primaryButtonLabel: String,
    onPrimaryClick: () -> Unit,
    secondaryButtonLabel: String,
    onSecondaryClick: () -> Unit,
    primaryButtonModifier: Modifier = Modifier,
) {
    OutlinedCard(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
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

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.align(Alignment.End),
            ) {
                OutlinedButton(
                    onClick = onPrimaryClick,
                    modifier = primaryButtonModifier,
                ) {
                    Text(primaryButtonLabel)
                }
                Button(onClick = onSecondaryClick) {
                    Text(secondaryButtonLabel)
                }
            }
        }
    }
}
