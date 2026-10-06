package com.mohamedrejeb.waypoint.sample.demos.sequences

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.waypoint.core.TooltipPlacement
import com.mohamedrejeb.waypoint.core.WaypointPersistence
import com.mohamedrejeb.waypoint.core.WaypointSequenceEffect
import com.mohamedrejeb.waypoint.core.WaypointSequenceState
import com.mohamedrejeb.waypoint.core.WaypointState
import com.mohamedrejeb.waypoint.core.rememberWaypointSequenceState
import com.mohamedrejeb.waypoint.core.rememberWaypointState
import com.mohamedrejeb.waypoint.core.waypointTarget
import com.mohamedrejeb.waypoint.material3.WaypointMaterial3Host
import com.mohamedrejeb.waypoint.sample.components.DemoScaffold
import com.mohamedrejeb.waypoint.sample.components.FlatCard
import com.mohamedrejeb.waypoint.sample.components.MockRow
import com.mohamedrejeb.waypoint.sample.components.ResetOnLeave
import com.mohamedrejeb.waypoint.sample.components.ScreenPadding
import com.mohamedrejeb.waypoint.sample.components.SectionLabel

private enum class BasicsTarget { Projects, Activity }

private enum class CustomizeTarget { Theme, Accent }

private enum class AdvancedTarget { Automations }

/**
 * In-memory persistence held at file level so chapter progress survives
 * navigating away and coming back.
 */
private object SequencePersistence : WaypointPersistence {
    private val completed = mutableSetOf<String>()
    override fun isCompleted(tourId: String): Boolean = tourId in completed
    override fun markCompleted(tourId: String) { completed.add(tourId) }
    override fun reset(tourId: String) { completed.remove(tourId) }
    override fun resetAll() { completed.clear() }
}

/**
 * Three mini-tours chained into one flow with WaypointSequenceState. Each
 * chapter persists independently, so restarting skips what is already done.
 */
@Composable
fun TourSequencesDemo(onBack: () -> Unit) {
    val basics = rememberWaypointState<BasicsTarget>(
        tourId = "sequence-basics",
        persistence = SequencePersistence,
    ) {
        step(BasicsTarget.Projects) {
            title = "Your projects"
            description = "Everything you are working on lives here."
            placement = TooltipPlacement.Bottom
        }
        step(BasicsTarget.Activity) {
            title = "Recent activity"
            description = "Catch up on changes since your last visit."
            placement = TooltipPlacement.Bottom
        }
    }

    val customize = rememberWaypointState<CustomizeTarget>(
        tourId = "sequence-customize",
        persistence = SequencePersistence,
    ) {
        step(CustomizeTarget.Theme) {
            title = "Pick a theme"
            description = "Light, dark, or follow the system."
            placement = TooltipPlacement.Bottom
        }
        step(CustomizeTarget.Accent) {
            title = "Accent color"
            description = "Make the app feel like yours."
            placement = TooltipPlacement.Bottom
        }
    }

    val advanced = rememberWaypointState<AdvancedTarget>(
        tourId = "sequence-advanced",
        persistence = SequencePersistence,
    ) {
        step(AdvancedTarget.Automations) {
            title = "Automations"
            description = "Let rules handle the repetitive work."
            placement = TooltipPlacement.Bottom
        }
    }

    val sequence = rememberWaypointSequenceState(basics, customize, advanced)
    WaypointSequenceEffect(sequence)

    ResetOnLeave { sequence.stop() }

    DemoScaffold(
        title = "Tour sequences",
        description = "Three chapters chained into one flow. Completing a chapter auto-starts the next, and finished chapters are skipped on restart.",
        onBack = onBack,
        onStartTour = { sequence.start() },
        startTourVisible = !sequence.isActive,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SequenceProgressCard(
                sequence = sequence,
                chapters = listOf(
                    "Basics" to basics,
                    "Customize" to customize,
                    "Advanced" to advanced,
                ),
                onReset = { sequence.reset() },
            )
            WaypointMaterial3Host(state = basics) {
                BasicsSection(state = basics)
            }
            WaypointMaterial3Host(state = customize) {
                CustomizeSection(state = customize)
            }
            WaypointMaterial3Host(state = advanced) {
                AdvancedSection(state = advanced)
            }
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun SequenceProgressCard(
    sequence: WaypointSequenceState,
    chapters: List<Pair<String, WaypointState<*>>>,
    onReset: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionLabel("Progress")
        FlatCard {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                chapters.forEach { (label, tour) ->
                    ChapterPill(
                        label = label,
                        done = tour.hasCompleted,
                        active = sequence.currentTour === tour,
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            TextButton(
                onClick = onReset,
                modifier = Modifier.align(Alignment.End),
            ) {
                Text("Reset progress")
            }
        }
    }
}

@Composable
private fun ChapterPill(
    label: String,
    done: Boolean,
    active: Boolean,
) {
    val container = when {
        done -> MaterialTheme.colorScheme.primaryContainer
        active -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceContainer
    }
    val content = when {
        done -> MaterialTheme.colorScheme.onPrimaryContainer
        active -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(shape = CircleShape, color = container) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            if (done) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = content,
                    modifier = Modifier.size(14.dp),
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = content,
            )
        }
    }
}

@Composable
private fun BasicsSection(state: WaypointState<BasicsTarget>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionLabel("Chapter 1, Basics")
        FlatCard {
            MockRow(
                icon = Icons.Rounded.Folder,
                title = "Projects",
                subtitle = "4 active",
                modifier = Modifier.waypointTarget(state, BasicsTarget.Projects),
            )
            Spacer(Modifier.height(12.dp))
            MockRow(
                icon = Icons.Rounded.Timeline,
                title = "Activity",
                subtitle = "12 updates today",
                modifier = Modifier.waypointTarget(state, BasicsTarget.Activity),
            )
        }
    }
}

@Composable
private fun CustomizeSection(state: WaypointState<CustomizeTarget>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionLabel("Chapter 2, Customize")
        FlatCard {
            MockRow(
                icon = Icons.Rounded.Palette,
                title = "Theme",
                subtitle = "Light, dark, or system",
                modifier = Modifier.waypointTarget(state, CustomizeTarget.Theme),
            )
            Spacer(Modifier.height(12.dp))
            MockRow(
                icon = Icons.Rounded.ColorLens,
                title = "Accent",
                subtitle = "Pick a highlight color",
                modifier = Modifier.waypointTarget(state, CustomizeTarget.Accent),
            )
        }
    }
}

@Composable
private fun AdvancedSection(state: WaypointState<AdvancedTarget>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionLabel("Chapter 3, Advanced")
        FlatCard {
            MockRow(
                icon = Icons.Rounded.Bolt,
                title = "Automations",
                subtitle = "Rules that run for you",
                modifier = Modifier.waypointTarget(state, AdvancedTarget.Automations),
            )
            Spacer(Modifier.height(12.dp))
            MockRow(
                icon = Icons.Rounded.Extension,
                title = "Integrations",
                subtitle = "Connect other tools",
            )
        }
    }
}
