package com.mohamedrejeb.waypoint.sample.demos.theming

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.waypoint.core.TooltipPlacement
import com.mohamedrejeb.waypoint.core.WaypointState
import com.mohamedrejeb.waypoint.core.rememberWaypointState
import com.mohamedrejeb.waypoint.core.waypointTarget
import com.mohamedrejeb.waypoint.material3.WaypointMaterial3Colors
import com.mohamedrejeb.waypoint.material3.WaypointMaterial3Dimensions
import com.mohamedrejeb.waypoint.material3.WaypointMaterial3Host
import com.mohamedrejeb.waypoint.material3.WaypointMaterial3Theme
import com.mohamedrejeb.waypoint.sample.components.DemoScaffold
import com.mohamedrejeb.waypoint.sample.components.FlatCard
import com.mohamedrejeb.waypoint.sample.components.ResetOnLeave
import com.mohamedrejeb.waypoint.sample.components.ScreenPadding
import com.mohamedrejeb.waypoint.sample.components.SectionLabel

private enum class ThemingTarget { Avatar, Search, NewProject }

private data class TooltipPreset(
    val name: String,
    val colors: (@Composable () -> WaypointMaterial3Colors)? = null,
    val dimensions: WaypointMaterial3Dimensions? = null,
)

// Null colors or dimensions fall back to the library defaults.
private val Presets = listOf(
    TooltipPreset(name = "Default"),
    TooltipPreset(
        name = "Midnight",
        colors = {
            WaypointMaterial3Theme.colors(
                tooltipBackground = Color(0xFF1B1B2F),
                title = Color.White,
                description = Color.White.copy(alpha = 0.7f),
                progress = Color.White.copy(alpha = 0.7f),
                primaryButton = Color(0xFFCFBCFF),
                secondaryButton = Color(0xFFCFBCFF),
                skipButton = Color.White.copy(alpha = 0.7f),
            )
        },
    ),
    TooltipPreset(
        name = "Sunrise",
        colors = {
            WaypointMaterial3Theme.colors(
                tooltipBackground = Color(0xFFFFF3E0),
                title = Color(0xFF3E2723),
                description = Color(0xFF6D4C41),
                progress = Color(0xFF6D4C41),
                primaryButton = Color(0xFFE65100),
                secondaryButton = Color(0xFFE65100),
                skipButton = Color(0xFF6D4C41),
            )
        },
    ),
    TooltipPreset(
        name = "Sharp",
        dimensions = WaypointMaterial3Theme.dimensions(
            tooltipShape = RoundedCornerShape(4.dp),
            tooltipElevation = 0.dp,
        ),
    ),
)

/**
 * Preset tooltip themes previewed on a live tour. WaypointMaterial3Theme
 * wraps the host, so the tooltip picks up whichever preset is selected.
 */
@Composable
fun ThemingDemo(onBack: () -> Unit) {
    var selectedPreset by remember { mutableStateOf(0) }
    val preset = Presets[selectedPreset]

    val state = rememberWaypointState {
        step(ThemingTarget.Avatar) {
            title = "Your profile"
            description = "This tooltip is styled by the selected preset."
            placement = TooltipPlacement.Bottom
        }
        step(ThemingTarget.Search) {
            title = "Search"
            description = "Background, text, and button colors come from the theme."
            placement = TooltipPlacement.Bottom
        }
        step(ThemingTarget.NewProject) {
            title = "New project"
            description = "Pick another preset and run the tour again."
        }
    }

    ResetOnLeave {
        state.stop()
        selectedPreset = 0
    }

    DemoScaffold(
        title = "Tooltip theming",
        description = "Preset tooltip themes previewed on a live tour. Pick one, then start the tour.",
        onBack = onBack,
        onStartTour = { state.start() },
        startTourVisible = !state.isActive,
    ) { padding ->
        WaypointMaterial3Theme(
            colors = preset.colors?.invoke(),
            dimensions = preset.dimensions,
        ) {
            WaypointMaterial3Host(state = state) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(padding)
                        .padding(horizontal = ScreenPadding),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SectionLabel("Preset")
                    PresetRow(
                        selected = selectedPreset,
                        onSelect = { selectedPreset = it },
                    )
                    SectionLabel("Preview")
                    PreviewCard(state = state)
                    Spacer(Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
private fun PresetRow(selected: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Presets.forEachIndexed { index, preset ->
            PresetChip(
                name = preset.name,
                selected = index == selected,
                onSelect = { onSelect(index) },
            )
        }
    }
}

@Composable
private fun PresetChip(
    name: String,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    Surface(
        onClick = onSelect,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        },
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun PreviewCard(state: WaypointState<ThemingTarget>) {
    FlatCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(36.dp)
                    .waypointTarget(state, ThemingTarget.Avatar),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "M",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
            Text(
                text = "Acme workspace",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
            )
            IconButton(
                onClick = {},
                modifier = Modifier.waypointTarget(state, ThemingTarget.Search),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Search",
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {},
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .waypointTarget(state, ThemingTarget.NewProject),
        ) {
            Text("New project")
        }
    }
}
