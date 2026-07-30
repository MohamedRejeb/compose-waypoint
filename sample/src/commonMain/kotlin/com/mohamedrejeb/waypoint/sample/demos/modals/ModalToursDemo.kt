package com.mohamedrejeb.waypoint.sample.demos.modals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.mohamedrejeb.waypoint.core.HighlightStyle
import com.mohamedrejeb.waypoint.core.SpotlightShape
import com.mohamedrejeb.waypoint.core.TooltipPlacement
import com.mohamedrejeb.waypoint.core.WaypointState
import com.mohamedrejeb.waypoint.core.rememberWaypointState
import com.mohamedrejeb.waypoint.core.waypointTarget
import com.mohamedrejeb.waypoint.material3.WaypointMaterial3Host
import com.mohamedrejeb.waypoint.material3.WaypointMaterial3OverlayHost
import com.mohamedrejeb.waypoint.sample.components.DemoScaffold
import com.mohamedrejeb.waypoint.sample.components.FlatCard
import com.mohamedrejeb.waypoint.sample.components.MockRow
import com.mohamedrejeb.waypoint.sample.components.ResetOnLeave
import com.mohamedrejeb.waypoint.sample.components.ScreenPadding
import com.mohamedrejeb.waypoint.sample.components.SectionLabel

private enum class DialogTarget { OpenButton, Notifications, DarkMode }

private enum class SheetTarget { OpenButton, Share, Rename }

private enum class ScrollTarget { NearTop, FarDown }

/**
 * Tours that cross into dialogs and sheets via beforeShow, plus automatic
 * scrolling to targets that start off screen.
 */
@Composable
fun ModalToursDemo(onBack: () -> Unit) {
    DemoScaffold(
        title = "Dialogs & sheets",
        description = "Tours that reach into dialogs and bottom sheets, plus automatic scrolling to off-screen targets.",
        onBack = onBack,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DialogSection()
            SheetSection()
            AutoScrollSection()
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun DialogSection() {
    var showDialog by remember { mutableStateOf(false) }

    val state = rememberWaypointState<DialogTarget> {
        step(DialogTarget.OpenButton) {
            title = "Open settings"
            description = "The tour starts on the button that opens the dialog."
            placement = TooltipPlacement.Bottom
            onEnter { showDialog = false }
        }
        step(DialogTarget.Notifications) {
            title = "Notifications"
            description = "beforeShow opened the dialog so this row could be highlighted."
            placement = TooltipPlacement.Bottom
            highlightStyle = HighlightStyle.Spotlight(shape = SpotlightShape.RoundedRect(12.dp))
            beforeShow { showDialog = true }
        }
        step(DialogTarget.DarkMode) {
            title = "Dark mode"
            description = "The overlay keeps tracking targets across dialog steps."
            placement = TooltipPlacement.Bottom
            highlightStyle = HighlightStyle.Spotlight(shape = SpotlightShape.RoundedRect(12.dp))
            beforeShow { showDialog = true }
        }
    }

    ResetOnLeave {
        state.stop()
        showDialog = false
    }

    WaypointMaterial3Host(
        state = state,
        onTourComplete = { showDialog = false },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionLabel("Dialog")
            FlatCard {
                Text(
                    text = "Steps 2 and 3 live inside a dialog that opens on demand.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = { showDialog = true },
                        modifier = Modifier.waypointTarget(state, DialogTarget.OpenButton),
                    ) {
                        Text("Open settings")
                    }
                    Spacer(Modifier.weight(1f))
                    if (!state.isActive) {
                        TextButton(onClick = { state.start() }) {
                            Text("Start tour")
                        }
                    }
                }
            }
        }

        if (showDialog) {
            SettingsDialog(
                state = state,
                onDismiss = { showDialog = false },
            )
        }
    }
}

@Composable
private fun SettingsDialog(
    state: WaypointState<DialogTarget>,
    onDismiss: () -> Unit,
) {
    var notifications by remember { mutableStateOf(true) }
    var darkMode by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        // The overlay host lives inside the rounded Surface so the scrim is
        // clipped to the dialog shape instead of poking past its corners.
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth(),
        ) {
            WaypointMaterial3OverlayHost(state = state) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.height(12.dp))
                    SwitchRow(
                        icon = Icons.Rounded.Notifications,
                        label = "Notifications",
                        checked = notifications,
                        onCheckedChange = { notifications = it },
                        modifier = Modifier.waypointTarget(state, DialogTarget.Notifications),
                    )
                    Spacer(Modifier.height(8.dp))
                    SwitchRow(
                        icon = Icons.Rounded.DarkMode,
                        label = "Dark mode",
                        checked = darkMode,
                        onCheckedChange = { darkMode = it },
                        modifier = Modifier.waypointTarget(state, DialogTarget.DarkMode),
                    )
                    Spacer(Modifier.height(8.dp))
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

@Composable
private fun SwitchRow(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SheetSection() {
    var showSheet by remember { mutableStateOf(false) }

    val state = rememberWaypointState<SheetTarget> {
        step(SheetTarget.OpenButton) {
            title = "Open actions"
            description = "This button opens the bottom sheet."
            placement = TooltipPlacement.Bottom
            onEnter { showSheet = false }
        }
        step(SheetTarget.Share) {
            title = "Share"
            description = "Steps can highlight rows inside an open sheet."
            placement = TooltipPlacement.Top
            highlightStyle = HighlightStyle.Spotlight(shape = SpotlightShape.RoundedRect(12.dp))
            beforeShow { showSheet = true }
        }
        step(SheetTarget.Rename) {
            title = "Rename"
            description = "The overlay renders above the sheet the whole time."
            placement = TooltipPlacement.Top
            highlightStyle = HighlightStyle.Spotlight(shape = SpotlightShape.RoundedRect(12.dp))
            beforeShow { showSheet = true }
        }
    }

    ResetOnLeave {
        state.stop()
        showSheet = false
    }

    WaypointMaterial3Host(
        state = state,
        onTourComplete = { showSheet = false },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionLabel("Bottom sheet")
            FlatCard {
                Text(
                    text = "The same pattern works for sheets, beforeShow opens it before each step.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = { showSheet = true },
                        modifier = Modifier.waypointTarget(state, SheetTarget.OpenButton),
                    ) {
                        Text("Open actions")
                    }
                    Spacer(Modifier.weight(1f))
                    if (!state.isActive) {
                        TextButton(onClick = { state.start() }) {
                            Text("Start tour")
                        }
                    }
                }
            }
        }

        if (showSheet) {
            ActionsSheet(
                state = state,
                onDismiss = { showSheet = false },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionsSheet(
    state: WaypointState<SheetTarget>,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        WaypointMaterial3OverlayHost(state = state) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp),
            ) {
                Text(
                    text = "Actions",
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(12.dp))
                MockRow(
                    icon = Icons.Rounded.Share,
                    title = "Share",
                    subtitle = "Send a copy to teammates",
                    modifier = Modifier.waypointTarget(state, SheetTarget.Share),
                )
                Spacer(Modifier.height(12.dp))
                MockRow(
                    icon = Icons.Rounded.Edit,
                    title = "Rename",
                    subtitle = "Give the file a clearer name",
                    modifier = Modifier.waypointTarget(state, SheetTarget.Rename),
                )
            }
        }
    }
}

@Composable
private fun AutoScrollSection() {
    val state = rememberWaypointState<ScrollTarget> {
        step(ScrollTarget.NearTop) {
            title = "Already visible"
            description = "This item is on screen, so the tour shows it right away."
            placement = TooltipPlacement.Bottom
            highlightStyle = HighlightStyle.Spotlight(shape = SpotlightShape.RoundedRect(12.dp))
        }
        step(ScrollTarget.FarDown) {
            title = "Scrolled into view"
            description = "This item started off screen and Waypoint scrolled to it."
            placement = TooltipPlacement.Top
            highlightStyle = HighlightStyle.Spotlight(shape = SpotlightShape.RoundedRect(12.dp))
        }
    }

    ResetOnLeave { state.stop() }

    WaypointMaterial3Host(state = state) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionLabel("Auto-scroll")
            FlatCard {
                Text(
                    text = "Waypoint scrolls off-screen targets into view automatically.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .height(240.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    repeat(12) { index ->
                        MockRow(
                            icon = Icons.Rounded.Folder,
                            title = "Folder ${index + 1}",
                            subtitle = "${index + 2} items",
                            modifier = when (index) {
                                1 -> Modifier.waypointTarget(state, ScrollTarget.NearTop)
                                9 -> Modifier.waypointTarget(state, ScrollTarget.FarDown)
                                else -> Modifier
                            },
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                if (!state.isActive) {
                    TextButton(
                        onClick = { state.start() },
                        modifier = Modifier.align(Alignment.End),
                    ) {
                        Text("Start tour")
                    }
                }
            }
        }
    }
}
