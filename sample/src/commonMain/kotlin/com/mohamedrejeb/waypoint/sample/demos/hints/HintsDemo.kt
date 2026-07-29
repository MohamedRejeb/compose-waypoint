package com.mohamedrejeb.waypoint.sample.demos.hints

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.waypoint.core.BeaconStyle
import com.mohamedrejeb.waypoint.core.TooltipPlacement
import com.mohamedrejeb.waypoint.core.WaypointPersistence
import com.mohamedrejeb.waypoint.core.rememberWaypointHintState
import com.mohamedrejeb.waypoint.material3.WaypointMaterial3Hint
import com.mohamedrejeb.waypoint.sample.components.DemoScaffold
import com.mohamedrejeb.waypoint.sample.components.ResetOnLeave

private enum class HintKey { Search, Filters, Favorite, Share, Notifications }

/**
 * In-memory persistence — dismissed state survives within the session but
 * not across app restarts. Replace with a real implementation (DataStore /
 * NSUserDefaults / localStorage) for durable persistence.
 */
private class InMemoryPersistence : WaypointPersistence {
    private val completed = mutableSetOf<String>()
    override fun isCompleted(tourId: String): Boolean = tourId in completed
    override fun markCompleted(tourId: String) { completed.add(tourId) }
    override fun reset(tourId: String) { completed.remove(tourId) }
    override fun resetAll() { completed.clear() }
}

@Composable
fun HintsDemo(onBack: () -> Unit) {
    val persistence = remember { InMemoryPersistence() }
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary

    val hintState = rememberWaypointHintState<HintKey>(
        persistence = persistence,
        groupId = "hints-demo",
    ) {
        hint(HintKey.Search) {
            title = "Search anything"
            description = "Tap here to quickly find items across the app."
            placement = TooltipPlacement.Bottom
            beaconStyle = BeaconStyle.Pulse(color = primary)
            beaconAlignment = Alignment.TopEnd
        }
        hint(HintKey.Filters) {
            title = "Refine results"
            description = "Narrow down the list with filters and sort options."
            placement = TooltipPlacement.Bottom
            beaconStyle = BeaconStyle.Dot(color = tertiary)
            beaconAlignment = Alignment.TopEnd
        }
        hint(HintKey.Favorite) {
            title = "Save for later"
            description = "Tap the heart to add an item to your favorites."
            placement = TooltipPlacement.Top
            beaconStyle = BeaconStyle.Pulse(color = Color(0xFFE11D48))
            beaconAlignment = Alignment.TopEnd
        }
        hint(HintKey.Share) {
            title = "Share with friends"
            description = "Send a link or copy to the clipboard in a single tap."
            placement = TooltipPlacement.Top
            beaconStyle = BeaconStyle.Pulse(color = primary)
            beaconAlignment = Alignment.TopEnd
        }
        hint(HintKey.Notifications) {
            title = "Stay up to date"
            description = "Configure notifications to never miss updates."
            placement = TooltipPlacement.Start
            beaconStyle = BeaconStyle.Dot(color = tertiary)
            beaconAlignment = Alignment.TopEnd
        }
    }

    ResetOnLeave { hintState.resetAll() }

    DemoScaffold(
        title = "Persistent Hints",
        description = "Tap a beacon to open its tooltip. \"Got it\" dismisses permanently.",
        onBack = onBack,
        onStartTour = {},
        fabVisible = false,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = padding.calculateBottomPadding() + 16.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            HintRowCard(title = "Search & Filters") {
                WaypointMaterial3Hint(state = hintState, key = HintKey.Search) {
                    FilledIconButton(onClick = {}) {
                        Icon(Icons.Rounded.Search, contentDescription = "Search")
                    }
                }
                Spacer(Modifier.size(12.dp))
                WaypointMaterial3Hint(state = hintState, key = HintKey.Filters) {
                    FilledIconButton(onClick = {}) {
                        Icon(Icons.Rounded.FilterList, contentDescription = "Filters")
                    }
                }
            }

            HintRowCard(title = "Actions on Items") {
                WaypointMaterial3Hint(state = hintState, key = HintKey.Favorite) {
                    FilledIconButton(onClick = {}) {
                        Icon(Icons.Rounded.Favorite, contentDescription = "Favorite")
                    }
                }
                Spacer(Modifier.size(12.dp))
                WaypointMaterial3Hint(state = hintState, key = HintKey.Share) {
                    FilledIconButton(onClick = {}) {
                        Icon(Icons.Rounded.Share, contentDescription = "Share")
                    }
                }
            }

            HintRowCard(title = "Notifications") {
                WaypointMaterial3Hint(state = hintState, key = HintKey.Notifications) {
                    FilledIconButton(onClick = {}) {
                        Icon(Icons.Rounded.Notifications, contentDescription = "Notifications")
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { hintState.resetAll() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Reset all hints")
            }
        }
    }
}

@Composable
private fun HintRowCard(title: String, content: @Composable () -> Unit) {
    Card(elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                content()
            }
        }
    }
}
