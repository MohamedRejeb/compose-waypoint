package com.mohamedrejeb.waypoint.sample.demos.onboarding

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.waypoint.core.TargetInteraction
import com.mohamedrejeb.waypoint.core.TooltipPlacement
import com.mohamedrejeb.waypoint.core.WaypointAnalytics
import com.mohamedrejeb.waypoint.core.WaypointState
import com.mohamedrejeb.waypoint.core.rememberWaypointState
import com.mohamedrejeb.waypoint.core.waypointTarget
import com.mohamedrejeb.waypoint.material3.WaypointMaterial3Host
import com.mohamedrejeb.waypoint.sample.components.DemoScaffold
import com.mohamedrejeb.waypoint.sample.components.FlatCard
import com.mohamedrejeb.waypoint.sample.components.ResetOnLeave
import com.mohamedrejeb.waypoint.sample.components.ScreenPadding
import com.mohamedrejeb.waypoint.sample.components.SectionLabel

private enum class OnboardingTarget { Search, Notifications, Stats, FirstTask }

/**
 * The core Waypoint experience: a spotlight tour over a small mock app, with
 * every analytics callback echoed into the log card below.
 */
@Composable
fun OnboardingDemo(onBack: () -> Unit) {
    val events = remember { mutableStateListOf<String>() }
    val analytics = remember {
        object : WaypointAnalytics {
            private fun log(event: String) {
                events.add(0, event)
                if (events.size > 4) events.removeAt(events.lastIndex)
            }

            override fun onTourStarted(tourId: String?, totalSteps: Int) =
                log("tour_started steps=$totalSteps")

            override fun onStepViewed(tourId: String?, stepIndex: Int, targetKey: Any?) =
                log("step_viewed $targetKey")

            override fun onStepCompleted(tourId: String?, stepIndex: Int, targetKey: Any?) =
                log("step_completed $targetKey")

            override fun onTourCompleted(tourId: String?, totalSteps: Int) =
                log("tour_completed")

            override fun onTourCancelled(tourId: String?, stepIndex: Int, totalSteps: Int) =
                log("tour_cancelled at=$stepIndex")
        }
    }

    val state = rememberWaypointState(analytics = analytics) {
        step(OnboardingTarget.Search) {
            title = "Search"
            description = "Find tasks, projects, and teammates from one place."
            placement = TooltipPlacement.Bottom
        }
        step(OnboardingTarget.Notifications) {
            title = "Notifications"
            description = "Mentions and updates land here."
            placement = TooltipPlacement.Bottom
        }
        step(OnboardingTarget.Stats) {
            title = "Your week at a glance"
            description = "Progress across all projects, updated live."
        }
        step(OnboardingTarget.FirstTask) {
            title = "Try it"
            description = "Tap the highlighted task to finish the tour."
            interaction = TargetInteraction.ClickToAdvance
            placement = TooltipPlacement.Top
        }
    }

    ResetOnLeave {
        state.stop()
        events.clear()
    }

    DemoScaffold(
        title = "Onboarding tour",
        description = "A first-launch walkthrough. Watch the analytics log at the bottom while you move through it.",
        onBack = onBack,
        onStartTour = { state.start() },
        startTourVisible = !state.isActive,
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
                MockAppHeader(state = state)
                MockStats(
                    modifier = Modifier.waypointTarget(state, OnboardingTarget.Stats),
                )
                MockTasks(state = state)
                Spacer(Modifier.height(4.dp))
                AnalyticsLog(events = events)
                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun MockAppHeader(state: WaypointState<OnboardingTarget>) {
    FlatCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "A",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
            Text(
                text = "Good morning, Alex",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
            )
            IconButton(
                onClick = {},
                modifier = Modifier.waypointTarget(state, OnboardingTarget.Search),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Search",
                )
            }
            IconButton(
                onClick = {},
                modifier = Modifier.waypointTarget(state, OnboardingTarget.Notifications),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Notifications,
                    contentDescription = "Notifications",
                )
            }
        }
    }
}

@Composable
private fun MockStats(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatTile(value = "12", label = "Open", modifier = Modifier.weight(1f))
        StatTile(value = "8", label = "Done", modifier = Modifier.weight(1f))
        StatTile(value = "5d", label = "Streak", modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatTile(value: String, label: String, modifier: Modifier = Modifier) {
    FlatCard(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MockTasks(state: WaypointState<OnboardingTarget>) {
    val tasks = listOf(
        Triple("Review pull request", "Frontend refactor #142", true),
        Triple("Write release notes", "Version 1.4.0", false),
        Triple("Design sync", "New dashboard layout", false),
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionLabel("Today")
        tasks.forEachIndexed { index, (title, subtitle, done) ->
            FlatCard(
                modifier = if (index == 0) {
                    Modifier
                        .fillMaxWidth()
                        .waypointTarget(state, OnboardingTarget.FirstTask)
                } else {
                    Modifier.fillMaxWidth()
                },
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (done) Icons.Rounded.CheckCircle else Icons.Outlined.Circle,
                        contentDescription = null,
                        tint = if (done) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                    )
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalyticsLog(events: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionLabel("Analytics log")
        FlatCard(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            if (events.isEmpty()) {
                Text(
                    text = "Start the tour to see WaypointAnalytics callbacks.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                events.forEach { event ->
                    Text(
                        text = event,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 2.dp),
                    )
                }
            }
        }
    }
}
