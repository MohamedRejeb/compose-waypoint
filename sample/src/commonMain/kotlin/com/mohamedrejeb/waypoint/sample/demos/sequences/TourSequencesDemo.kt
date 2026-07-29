package com.mohamedrejeb.waypoint.sample.demos.sequences

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.waypoint.core.HighlightStyle
import com.mohamedrejeb.waypoint.core.SpotlightPadding
import com.mohamedrejeb.waypoint.core.SpotlightShape
import com.mohamedrejeb.waypoint.core.TooltipPlacement
import com.mohamedrejeb.waypoint.core.WaypointPersistence
import com.mohamedrejeb.waypoint.core.WaypointSequenceEffect
import com.mohamedrejeb.waypoint.core.WaypointState
import com.mohamedrejeb.waypoint.core.rememberWaypointSequenceState
import com.mohamedrejeb.waypoint.core.rememberWaypointState
import com.mohamedrejeb.waypoint.core.waypointTarget
import com.mohamedrejeb.waypoint.material3.WaypointMaterial3Host
import com.mohamedrejeb.waypoint.sample.components.DemoScaffold
import com.mohamedrejeb.waypoint.sample.components.ResetOnLeave

private enum class BasicsTarget { Profile, Bookmark }
private enum class CustomizeTarget { Theme, Accent }
private enum class AdvancedTarget { Shortcuts, Insights }

/** In-memory persistence: dismissed state survives within the session. */
private class InMemoryPersistence : WaypointPersistence {
    private val completed = mutableSetOf<String>()
    override fun isCompleted(tourId: String): Boolean = tourId in completed
    override fun markCompleted(tourId: String) { completed.add(tourId) }
    override fun reset(tourId: String) { completed.remove(tourId) }
    override fun resetAll() { completed.clear() }
}

@Composable
fun TourSequencesDemo(onBack: () -> Unit) {
    val persistence = remember { InMemoryPersistence() }

    val tourBasics = rememberWaypointState<BasicsTarget>(
        tourId = "seq-basics",
        persistence = persistence,
    ) {
        step(BasicsTarget.Profile) {
            title = "Your profile"
            description = "Tap here to manage your account."
            placement = TooltipPlacement.Bottom
            highlightStyle = HighlightStyle.Spotlight(shape = SpotlightShape.Circle)
        }
        step(BasicsTarget.Bookmark) {
            title = "Save for later"
            description = "Bookmark items to revisit quickly."
            placement = TooltipPlacement.Bottom
            highlightStyle = HighlightStyle.Spotlight(shape = SpotlightShape.Circle)
        }
    }

    val tourCustomize = rememberWaypointState<CustomizeTarget>(
        tourId = "seq-customize",
        persistence = persistence,
    ) {
        step(CustomizeTarget.Theme) {
            title = "Light or dark"
            description = "Switch the app theme to match your preference."
            placement = TooltipPlacement.Top
            highlightStyle = HighlightStyle.Spotlight(
                shape = SpotlightShape.RoundedRect(12.dp),
                padding = SpotlightPadding(8.dp),
            )
        }
        step(CustomizeTarget.Accent) {
            title = "Pick an accent"
            description = "Choose an accent color to personalize the UI."
            placement = TooltipPlacement.Top
            highlightStyle = HighlightStyle.Spotlight(
                shape = SpotlightShape.RoundedRect(12.dp),
                padding = SpotlightPadding(8.dp),
            )
        }
    }

    val tourAdvanced = rememberWaypointState<AdvancedTarget>(
        tourId = "seq-advanced",
        persistence = persistence,
    ) {
        step(AdvancedTarget.Shortcuts) {
            title = "Power moves"
            description = "Keyboard shortcuts speed up every action."
            placement = TooltipPlacement.Top
            highlightStyle = HighlightStyle.Spotlight(
                shape = SpotlightShape.RoundedRect(12.dp),
                padding = SpotlightPadding(8.dp),
            )
        }
        step(AdvancedTarget.Insights) {
            title = "Deep insights"
            description = "Review trends across all your activity."
            placement = TooltipPlacement.Top
            highlightStyle = HighlightStyle.Spotlight(
                shape = SpotlightShape.RoundedRect(12.dp),
                padding = SpotlightPadding(8.dp),
            )
        }
    }

    val sequence = rememberWaypointSequenceState(tourBasics, tourCustomize, tourAdvanced)
    WaypointSequenceEffect(sequence)

    ResetOnLeave {
        sequence.reset()
        tourBasics.stop()
        tourCustomize.stop()
        tourAdvanced.stop()
    }

    DemoScaffold(
        title = "Tour Sequences",
        description = "Three mini-tours chained together. Each persists independently and the sequence auto-advances when a tour completes.",
        onBack = onBack,
        onStartTour = { sequence.start() },
        fabVisible = !sequence.isActive,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SequenceStatusBar(
                sequence = sequence,
                tours = listOf(
                    "Basics" to tourBasics,
                    "Customize" to tourCustomize,
                    "Advanced" to tourAdvanced,
                ),
            )

            WaypointMaterial3Host(state = tourBasics) {
                BasicsSection(tourBasics)
            }

            WaypointMaterial3Host(state = tourCustomize) {
                CustomizeSection(tourCustomize)
            }

            WaypointMaterial3Host(state = tourAdvanced) {
                AdvancedSection(tourAdvanced)
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
            ) {
                Button(
                    onClick = { sequence.start() },
                    modifier = Modifier.weight(1f),
                ) { Text("Start sequence") }
                OutlinedButton(
                    onClick = { sequence.reset() },
                    modifier = Modifier.weight(1f),
                ) { Text("Reset all") }
            }
        }
    }
}

@Composable
private fun SequenceStatusBar(
    sequence: com.mohamedrejeb.waypoint.core.WaypointSequenceState,
    tours: List<Pair<String, WaypointState<*>>>,
) {
    Surface(
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            tours.forEach { (label, tour) ->
                val isActive = sequence.currentTour === tour
                val isDone = tour.hasCompleted
                val prefix = when {
                    isActive -> "▶ "
                    isDone -> "✓ "
                    else -> "• "
                }
                AssistChip(
                    onClick = {},
                    label = { Text("$prefix$label") },
                )
            }
        }
    }
}

@Composable
private fun BasicsSection(state: WaypointState<BasicsTarget>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Basics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                CircleTarget(
                    icon = Icons.Rounded.AccountCircle,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.waypointTarget(state, BasicsTarget.Profile),
                )
                CircleTarget(
                    icon = Icons.Rounded.BookmarkBorder,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.waypointTarget(state, BasicsTarget.Bookmark),
                )
            }
        }
    }
}

@Composable
private fun CustomizeSection(state: WaypointState<CustomizeTarget>) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Customize",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RowPill(
                    label = "Theme",
                    icon = Icons.Rounded.Tune,
                    modifier = Modifier
                        .weight(1f)
                        .waypointTarget(state, CustomizeTarget.Theme),
                )
                RowPill(
                    label = "Accent",
                    icon = Icons.Rounded.ColorLens,
                    modifier = Modifier
                        .weight(1f)
                        .waypointTarget(state, CustomizeTarget.Accent),
                )
            }
        }
    }
}

@Composable
private fun AdvancedSection(state: WaypointState<AdvancedTarget>) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Advanced",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RowPill(
                    label = "Shortcuts",
                    icon = Icons.Rounded.Bolt,
                    modifier = Modifier
                        .weight(1f)
                        .waypointTarget(state, AdvancedTarget.Shortcuts),
                )
                RowPill(
                    label = "Insights",
                    icon = Icons.Rounded.Insights,
                    modifier = Modifier
                        .weight(1f)
                        .waypointTarget(state, AdvancedTarget.Insights),
                )
            }
        }
    }
}

@Composable
private fun CircleTarget(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint)
    }
}

@Composable
private fun RowPill(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp,
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
        ) {
            Icon(imageVector = icon, contentDescription = null)
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
