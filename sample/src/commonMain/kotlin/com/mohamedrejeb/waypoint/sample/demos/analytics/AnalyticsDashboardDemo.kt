package com.mohamedrejeb.waypoint.sample.demos.analytics

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mohamedrejeb.waypoint.core.TooltipPlacement
import com.mohamedrejeb.waypoint.core.rememberWaypointState
import com.mohamedrejeb.waypoint.core.waypointTarget
import com.mohamedrejeb.waypoint.material3.WaypointMaterial3Host
import com.mohamedrejeb.waypoint.sample.components.DemoScaffold

private enum class AnalyticsTarget { Search, Profile, Settings, Summary }

private val BadgeGreen = Color(0xFF16A34A)
private val BadgeViolet = Color(0xFF7C3AED)
private val BadgeRed = Color(0xFFDC2626)
private val BadgeTeal = Color(0xFF0D9488)
private val BadgeAmber = Color(0xFFD97706)

@Composable
fun AnalyticsDashboardDemo(onBack: () -> Unit) {
    val viewModel = viewModel { AnalyticsViewModel() }
    val state by viewModel.state.collectAsState()

    val waypointState = rememberWaypointState(
        tourId = "demo-analytics",
        analytics = viewModel,
    ) {
        step(AnalyticsTarget.Search) {
            title = "Search"
            description = "Find content across the app"
            placement = TooltipPlacement.Bottom
        }
        step(AnalyticsTarget.Profile) {
            title = "Profile"
            description = "View and edit your profile"
            placement = TooltipPlacement.Bottom
        }
        step(AnalyticsTarget.Settings) {
            title = "Settings"
            description = "Customize your experience"
            placement = TooltipPlacement.Bottom
        }
        step(AnalyticsTarget.Summary) {
            title = "Today's Summary"
            description = "Your daily activity overview"
            placement = TooltipPlacement.Top
        }
    }

    DemoScaffold(
        title = "Analytics Dashboard",
        description = "Real-time event log showing WaypointAnalytics callbacks",
        onBack = onBack,
        onStartTour = { waypointState.start() },
    ) { padding ->
        WaypointMaterial3Host(
            state = waypointState,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                // Top section: mock app toolbar + content card
                MockToolbar(waypointState)

                SummaryCard(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .waypointTarget(waypointState, AnalyticsTarget.Summary),
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Bottom section: event log
                EventLogPanel(
                    events = state.events,
                    onClear = { viewModel.onEvent(AnalyticsEvent.ClearLog) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
            }
        }
    }
}

@Composable
private fun MockToolbar(
    state: com.mohamedrejeb.waypoint.core.WaypointState<AnalyticsTarget>,
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
            IconButton(
                onClick = { /* no-op */ },
                modifier = Modifier.waypointTarget(state, AnalyticsTarget.Search),
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                )
            }

            Text(
                text = "Analytics Demo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .waypointTarget(state, AnalyticsTarget.Profile),
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(
                onClick = { /* no-op */ },
                modifier = Modifier.waypointTarget(state, AnalyticsTarget.Settings),
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Today's Summary",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                SummaryItem(label = "Views", value = "1,248")
                SummaryItem(label = "Actions", value = "86")
                SummaryItem(label = "Sessions", value = "342")
            }
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String) {
    Column {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
        )
    }
}

@Composable
private fun EventLogPanel(
    events: List<AnalyticsLogEntry>,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(events.size) {
        if (events.isNotEmpty()) {
            listState.animateScrollToItem(events.lastIndex)
        }
    }

    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        // Header row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "Event Log",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Event count badge
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            ) {
                Text(
                    text = "${events.size}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            TextButton(onClick = onClear) {
                Text("Clear")
            }
        }

        if (events.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                Text(
                    text = "Start the tour to see events appear here",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 4.dp, bottom = 8.dp),
            ) {
                items(
                    items = events,
                    key = { it.index },
                ) { entry ->
                    EventLogRow(entry)
                }
            }
        }
    }
}

@Composable
private fun EventLogRow(entry: AnalyticsLogEntry) {
    val badgeColor = badgeColorForType(entry.type)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            text = "#${entry.index}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(32.dp),
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(badgeColor.copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 3.dp),
        ) {
            Text(
                text = entry.type,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = badgeColor,
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = entry.details,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
    }
}

private fun badgeColorForType(type: String): Color =
    when (type) {
        "Tour Started" -> BadgeGreen
        "Tour Completed" -> BadgeViolet
        "Tour Cancelled" -> BadgeRed
        "Step Viewed" -> BadgeTeal
        "Step Completed" -> BadgeAmber
        else -> Color.Gray
    }
