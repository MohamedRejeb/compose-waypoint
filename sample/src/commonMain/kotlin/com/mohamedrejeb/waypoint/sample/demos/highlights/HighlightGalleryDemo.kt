package com.mohamedrejeb.waypoint.sample.demos.highlights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BlurOn
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.Waves
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.waypoint.core.HighlightStyle
import com.mohamedrejeb.waypoint.core.SpotlightEffect
import com.mohamedrejeb.waypoint.core.SpotlightShape
import com.mohamedrejeb.waypoint.core.WaypointState
import com.mohamedrejeb.waypoint.core.rememberWaypointState
import com.mohamedrejeb.waypoint.core.waypointTarget
import com.mohamedrejeb.waypoint.material3.WaypointMaterial3Host
import com.mohamedrejeb.waypoint.sample.components.DemoScaffold
import com.mohamedrejeb.waypoint.sample.components.FlatCard
import com.mohamedrejeb.waypoint.sample.components.IconBadge
import com.mohamedrejeb.waypoint.sample.components.ResetOnLeave
import com.mohamedrejeb.waypoint.sample.components.ScreenPadding
import com.mohamedrejeb.waypoint.sample.components.SectionLabel

// The multi-target entry uses its own keys, kept apart from the entry indices.
private const val MultiTargetEntry = 9
private const val ChipA = 100
private const val ChipB = 101
private const val ChipC = 102

private data class GalleryEntry(
    val label: String,
    val note: String,
    val icon: ImageVector,
    val style: HighlightStyle,
)

/**
 * One card per highlight style, each previewing the highlight on itself.
 * A single tour holds every step; showIf makes only the tapped entry visible,
 * so each Try button runs a one-step tour.
 */
@Composable
fun HighlightGalleryDemo(onBack: () -> Unit) {
    var activeEntry by remember { mutableStateOf(-1) }

    val scheme = MaterialTheme.colorScheme
    val entries = remember {
        listOf(
            GalleryEntry(
                label = "Spotlight - rounded",
                note = "Dimmed overlay with a rounded cutout.",
                icon = Icons.Rounded.Star,
                style = HighlightStyle.Spotlight(shape = SpotlightShape.RoundedRect(16.dp)),
            ),
            GalleryEntry(
                label = "Spotlight - circle",
                note = "Circular cutout around the target.",
                icon = Icons.Rounded.Circle,
                style = HighlightStyle.Spotlight(shape = SpotlightShape.Circle),
            ),
            GalleryEntry(
                label = "Spotlight - pill",
                note = "Capsule cutout that hugs the target.",
                icon = Icons.Rounded.FavoriteBorder,
                style = HighlightStyle.Spotlight(shape = SpotlightShape.Pill),
            ),
            GalleryEntry(
                label = "Glow effect",
                note = "Colored halo around the cutout edge.",
                icon = Icons.Rounded.LightMode,
                style = HighlightStyle.Spotlight(
                    effect = SpotlightEffect.Glow(color = scheme.primary, radius = 28.dp),
                ),
            ),
            GalleryEntry(
                label = "Soft edge",
                note = "The cutout fades into the scrim.",
                icon = Icons.Rounded.BlurOn,
                style = HighlightStyle.Spotlight(
                    effect = SpotlightEffect.SoftEdge(fadeWidth = 20.dp),
                ),
            ),
            GalleryEntry(
                label = "Pulse",
                note = "Breathing shape, no dimmed overlay.",
                icon = Icons.Rounded.AutoAwesome,
                style = HighlightStyle.Pulse(color = scheme.primary),
            ),
            GalleryEntry(
                label = "Border",
                note = "Static outline, no dimmed overlay.",
                icon = Icons.Rounded.RadioButtonUnchecked,
                style = HighlightStyle.Border(color = scheme.tertiary, borderWidth = 3.dp),
            ),
            GalleryEntry(
                label = "Ripple",
                note = "Expanding rings from the center.",
                icon = Icons.Rounded.Waves,
                style = HighlightStyle.Ripple(color = scheme.secondary),
            ),
            GalleryEntry(
                label = "None (tooltip only)",
                note = "No highlight, just the tooltip.",
                icon = Icons.Rounded.VisibilityOff,
                style = HighlightStyle.None,
            ),
        )
    }

    val state = rememberWaypointState<Int> {
        entries.forEachIndexed { index, entry ->
            step(index) {
                title = entry.label
                description = entry.note
                highlightStyle = entry.style
                showIf { activeEntry == index }
            }
        }
        step(ChipA) {
            title = "Multi-target"
            description = "One step can highlight several elements at once."
            additionalTargets = listOf(ChipB, ChipC)
            showIf { activeEntry == MultiTargetEntry }
        }
    }

    ResetOnLeave {
        state.stop()
        activeEntry = -1
    }

    DemoScaffold(
        title = "Highlight gallery",
        description = "Every highlight style in the library. Tap Try on a card to preview it on that card.",
        onBack = onBack,
        onStartTour = null,
    ) { padding ->
        WaypointMaterial3Host(state = state) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
                    .padding(horizontal = ScreenPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SectionLabel("Styles")
                entries.forEachIndexed { index, entry ->
                    GalleryCard(
                        entry = entry,
                        targetModifier = Modifier.waypointTarget(state, index),
                        onTry = {
                            state.stop()
                            activeEntry = index
                            state.start()
                        },
                    )
                }
                SectionLabel("Multi-target")
                MultiTargetCard(
                    state = state,
                    onTry = {
                        state.stop()
                        activeEntry = MultiTargetEntry
                        state.start()
                    },
                )
                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun GalleryCard(
    entry: GalleryEntry,
    targetModifier: Modifier,
    onTry: () -> Unit,
) {
    FlatCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(
                icon = entry.icon,
                size = 44.dp,
                modifier = targetModifier,
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
            ) {
                Text(
                    text = entry.label,
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = entry.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TextButton(onClick = onTry) {
                Text("Try")
            }
        }
    }
}

@Composable
private fun MultiTargetCard(
    state: WaypointState<Int>,
    onTry: () -> Unit,
) {
    FlatCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Multi-target",
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = "Chip A leads, B and C are extra targets.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TextButton(onClick = onTry) {
                Text("Try")
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MultiTargetChip(label = "A", modifier = Modifier.waypointTarget(state, ChipA))
            MultiTargetChip(label = "B", modifier = Modifier.waypointTarget(state, ChipB))
            MultiTargetChip(label = "C", modifier = Modifier.waypointTarget(state, ChipC))
        }
    }
}

@Composable
private fun MultiTargetChip(label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.size(40.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}
