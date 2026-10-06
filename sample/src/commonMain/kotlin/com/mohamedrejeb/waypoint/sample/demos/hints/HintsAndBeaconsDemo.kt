package com.mohamedrejeb.waypoint.sample.demos.hints

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.waypoint.core.BeaconStyle
import com.mohamedrejeb.waypoint.core.TooltipPlacement
import com.mohamedrejeb.waypoint.core.WaypointBeacon
import com.mohamedrejeb.waypoint.core.WaypointPersistence
import com.mohamedrejeb.waypoint.core.rememberWaypointHintState
import com.mohamedrejeb.waypoint.core.rememberWaypointState
import com.mohamedrejeb.waypoint.core.waypointTarget
import com.mohamedrejeb.waypoint.material3.WaypointMaterial3Hint
import com.mohamedrejeb.waypoint.material3.WaypointMaterial3Host
import com.mohamedrejeb.waypoint.sample.components.DemoScaffold
import com.mohamedrejeb.waypoint.sample.components.FlatCard
import com.mohamedrejeb.waypoint.sample.components.ResetOnLeave
import com.mohamedrejeb.waypoint.sample.components.ScreenPadding
import com.mohamedrejeb.waypoint.sample.components.SectionLabel

private enum class DiscoveryTarget { AiAssist, Inbox }

private enum class HintKey { Search, Filters, Favorites }

/**
 * In-memory persistence held at file level so dismissed hints stay dismissed
 * when you navigate away and come back.
 */
private object HintPersistence : WaypointPersistence {
    private val completed = mutableSetOf<String>()
    override fun isCompleted(tourId: String): Boolean = tourId in completed
    override fun markCompleted(tourId: String) { completed.add(tourId) }
    override fun reset(tourId: String) { completed.remove(tourId) }
    override fun resetAll() { completed.clear() }
}

/**
 * Beacons and hints, attention without a full tour: a pulsing beacon that
 * launches a mini tour, and persistent hints the user dismisses one by one.
 */
@Composable
fun HintsAndBeaconsDemo(onBack: () -> Unit) {
    DemoScaffold(
        title = "Hints & beacons",
        description = "Standalone beacons and dismissable hints that live outside sequential tours.",
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
            FeatureDiscoverySection()
            PersistentHintsSection()
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun FeatureDiscoverySection() {
    var aiSeen by remember { mutableStateOf(false) }

    val state = rememberWaypointState<DiscoveryTarget> {
        step(DiscoveryTarget.AiAssist) {
            title = "Meet AI assist"
            description = "Drafts replies and summarizes long threads for you."
            placement = TooltipPlacement.Bottom
        }
        step(DiscoveryTarget.Inbox) {
            title = "It works everywhere"
            description = "Ask for help from any screen, including your inbox."
            placement = TooltipPlacement.Bottom
        }
    }

    ResetOnLeave { state.stop() }

    WaypointMaterial3Host(
        state = state,
        onTourComplete = { aiSeen = true },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionLabel("Feature discovery")
            FlatCard {
                Text(
                    text = "A beacon marks the new feature, tap it to start a mini tour.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {},
                        modifier = Modifier.waypointTarget(state, DiscoveryTarget.Inbox),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Inbox,
                            contentDescription = "Inbox",
                        )
                    }
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Rounded.CalendarMonth,
                            contentDescription = "Calendar",
                        )
                    }
                    WaypointBeacon(
                        visible = !aiSeen,
                        style = BeaconStyle.Pulse(color = MaterialTheme.colorScheme.tertiary),
                        onClick = { state.start() },
                    ) {
                        IconButton(
                            onClick = {},
                            modifier = Modifier.waypointTarget(state, DiscoveryTarget.AiAssist),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = "AI assist",
                            )
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    if (aiSeen) {
                        TextButton(onClick = { aiSeen = false }) {
                            Text("Reset")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PersistentHintsSection() {
    val hintState = rememberWaypointHintState<HintKey>(
        persistence = HintPersistence,
        groupId = "sample-hints",
    ) {
        hint(HintKey.Search) {
            title = "Search"
            description = "Find anything fast"
        }
        hint(HintKey.Filters) {
            title = "Filters"
            description = "Narrow down long lists"
        }
        hint(HintKey.Favorites) {
            title = "Favorites"
            description = "Keep things handy"
        }
    }

    ResetOnLeave { hintState.close() }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionLabel("Persistent hints")
        FlatCard {
            Text(
                text = "Tap a beacon, then Got it - the hint stays dismissed.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                WaypointMaterial3Hint(state = hintState, key = HintKey.Search) {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search",
                        )
                    }
                }
                WaypointMaterial3Hint(state = hintState, key = HintKey.Filters) {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Rounded.Tune,
                            contentDescription = "Filters",
                        )
                    }
                }
                WaypointMaterial3Hint(state = hintState, key = HintKey.Favorites) {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Rounded.FavoriteBorder,
                            contentDescription = "Favorites",
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { hintState.resetAll() }) {
                    Text("Reset all")
                }
            }
        }
    }
}
