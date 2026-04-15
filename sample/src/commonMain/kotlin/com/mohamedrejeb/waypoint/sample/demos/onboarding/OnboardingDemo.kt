package com.mohamedrejeb.waypoint.sample.demos.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.waypoint.core.HighlightStyle
import com.mohamedrejeb.waypoint.core.SpotlightPadding
import com.mohamedrejeb.waypoint.core.SpotlightShape
import com.mohamedrejeb.waypoint.core.TargetInteraction
import com.mohamedrejeb.waypoint.core.TooltipPlacement
import com.mohamedrejeb.waypoint.core.rememberWaypointState
import com.mohamedrejeb.waypoint.core.waypointTarget
import com.mohamedrejeb.waypoint.material3.WaypointMaterial3Host
import com.mohamedrejeb.waypoint.sample.components.DemoScaffold

private enum class OnboardingTarget {
    Search,
    Bell,
    Welcome,
    Stats,
    Fab,
    TaskList,
}

@Composable
fun OnboardingDemo(onBack: () -> Unit) {
    val state = rememberWaypointState {
        step(OnboardingTarget.Search) {
            title = "Search"
            description = "Find tasks, projects, and teammates"
            placement = TooltipPlacement.Bottom
            highlightStyle = HighlightStyle.Spotlight(shape = SpotlightShape.Circle)
        }
        step(OnboardingTarget.Bell) {
            title = "Notifications"
            description = "Stay updated with alerts and mentions"
            placement = TooltipPlacement.Bottom
            highlightStyle = HighlightStyle.Spotlight(
                shape = SpotlightShape.RoundedRect(12.dp),
                padding = SpotlightPadding(all = 8.dp),
            )
        }
        step(OnboardingTarget.Welcome) {
            title = "Your Dashboard"
            description = "Quick overview of your day"
            placement = TooltipPlacement.Bottom
            highlightStyle = HighlightStyle.Spotlight(shape = SpotlightShape.Pill)
        }
        step(OnboardingTarget.Stats) {
            title = "Statistics"
            description = "Track your productivity at a glance"
            placement = TooltipPlacement.Top
            highlightStyle = HighlightStyle.Spotlight(
                shape = SpotlightShape.RoundedRect(8.dp),
            )
        }
        step(OnboardingTarget.Fab) {
            title = "Create Task"
            description = "Tap to add a new task"
            placement = TooltipPlacement.Start
            highlightStyle = HighlightStyle.Spotlight(shape = SpotlightShape.Circle)
            interaction = TargetInteraction.ClickToAdvance
        }
        step(OnboardingTarget.TaskList) {
            title = "Task List"
            description = "Your pending tasks appear here"
            placement = TooltipPlacement.Top
            highlightStyle = HighlightStyle.Spotlight(
                shape = SpotlightShape.RoundedRect(12.dp),
            )
        }
    }

    DemoScaffold(
        title = "Onboarding Tour",
        description = "A first-launch walkthrough showcasing spotlight, placements, and navigation",
        onBack = onBack,
        onStartTour = { state.start() },
    ) { padding ->
        WaypointMaterial3Host(
            state = state,
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState()),
                ) {
                    TopBar(state)
                    WelcomeCard(state)
                    StatsRow(state)
                    TaskList(state)
                    // Extra space so FAB doesn't overlap last task card
                    Spacer(modifier = Modifier.height(80.dp))
                }

                FloatingActionButton(
                    onClick = { /* no-op */ },
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 16.dp)
                        .waypointTarget(state, OnboardingTarget.Fab),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Task",
                    )
                }
            }
        }
    }
}

@Composable
private fun TopBar(
    state: com.mohamedrejeb.waypoint.core.WaypointState<OnboardingTarget>,
) {
    Surface(
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            // Profile avatar
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
            ) {
                Text(
                    text = "A",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Home",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )

            IconButton(
                onClick = { /* no-op */ },
                modifier = Modifier.waypointTarget(state, OnboardingTarget.Search),
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                )
            }

            IconButton(
                onClick = { /* no-op */ },
                modifier = Modifier.waypointTarget(state, OnboardingTarget.Bell),
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                )
            }
        }
    }
}

@Composable
private fun WelcomeCard(
    state: com.mohamedrejeb.waypoint.core.WaypointState<OnboardingTarget>,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .waypointTarget(state, OnboardingTarget.Welcome),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
        ) {
            Text(
                text = "Welcome back, Alex!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "You have 3 new tasks today",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
            )
        }
    }
}

@Composable
private fun StatsRow(
    state: com.mohamedrejeb.waypoint.core.WaypointState<OnboardingTarget>,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .waypointTarget(state, OnboardingTarget.Stats),
    ) {
        StatCard(label = "Tasks", value = "12", modifier = Modifier.weight(1f))
        StatCard(label = "Done", value = "8", modifier = Modifier.weight(1f))
        StatCard(label = "Streak", value = "5 days", modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = modifier,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private data class TaskItem(
    val title: String,
    val description: String,
    val done: Boolean,
)

private val tasks = listOf(
    TaskItem("Review pull request", "Frontend refactor #142", done = true),
    TaskItem("Write unit tests", "Cover auth module edge cases", done = true),
    TaskItem("Design sync meeting", "Discuss new dashboard layout", done = false),
    TaskItem("Update API docs", "Add v2 endpoint documentation", done = false),
)

@Composable
private fun TaskList(
    state: com.mohamedrejeb.waypoint.core.WaypointState<OnboardingTarget>,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .waypointTarget(state, OnboardingTarget.TaskList),
    ) {
        tasks.forEach { task ->
            TaskCard(task)
        }
    }
}

@Composable
private fun TaskCard(task: TaskItem) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Icon(
                imageVector = if (task.done) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                contentDescription = null,
                tint = if (task.done) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline
                },
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
