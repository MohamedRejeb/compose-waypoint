package com.mohamedrejeb.waypoint.sample.demos.discovery

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mohamedrejeb.waypoint.core.BeaconStyle
import com.mohamedrejeb.waypoint.core.HighlightStyle
import com.mohamedrejeb.waypoint.core.WaypointBeacon
import com.mohamedrejeb.waypoint.core.rememberWaypointState
import com.mohamedrejeb.waypoint.core.waypointTarget
import com.mohamedrejeb.waypoint.material3.WaypointMaterial3Host
import com.mohamedrejeb.waypoint.sample.components.DemoScaffold

private val VioletPrimary = Color(0xFF7C3AED)
private val TealSecondary = Color(0xFF14B8A6)
private val AmberTertiary = Color(0xFFF59E0B)

private enum class MessagingTarget { Card }
private enum class FiltersTarget { Card }
private enum class DarkModeTarget { Card }

@Composable
fun FeatureDiscoveryDemo(onBack: () -> Unit) {
    val viewModel = viewModel { DiscoveryViewModel() }
    val state by viewModel.state.collectAsState()

    val messagingState = rememberWaypointState(
        tourId = "messaging",
        persistence = viewModel.persistence,
    ) {
        step(MessagingTarget.Card) {
            title = "Smart Messaging"
            description = "AI-powered messaging with smart replies, thread summaries, and real-time translation."
            highlightStyle = HighlightStyle.Border(color = VioletPrimary)
        }
    }

    val filtersState = rememberWaypointState(
        tourId = "filters",
        persistence = viewModel.persistence,
    ) {
        step(FiltersTarget.Card) {
            title = "Advanced Filters"
            description = "Create complex filter chains with saved presets and smart suggestions."
            highlightStyle = HighlightStyle.Pulse(color = TealSecondary)
        }
    }

    val darkModeState = rememberWaypointState(
        tourId = "dark-mode",
        persistence = viewModel.persistence,
    ) {
        step(DarkModeTarget.Card) {
            title = "Dark Mode"
            description = "Automatic dark mode with custom accent colors and OLED black option."
            highlightStyle = HighlightStyle.Spotlight()
        }
    }

    DemoScaffold(
        title = "Feature Discovery",
        description = "Beacons highlight new features. Tap a beacon to learn more.",
        onBack = onBack,
        onStartTour = {},
        fabVisible = false,
    ) { padding ->
        WaypointMaterial3Host(
            state = messagingState,
            onTourComplete = { viewModel.onEvent(DiscoveryEvent.DismissMessaging) },
        ) {
            WaypointMaterial3Host(
                state = filtersState,
                onTourComplete = { viewModel.onEvent(DiscoveryEvent.DismissFilters) },
            ) {
                WaypointMaterial3Host(
                    state = darkModeState,
                    onTourComplete = { viewModel.onEvent(DiscoveryEvent.DismissDarkMode) },
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                    ) {
                        Text(
                            text = "What's New",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp),
                        )

                        FeatureCard(
                            icon = Icons.Default.ChatBubble,
                            iconColor = VioletPrimary,
                            title = "Smart Messaging",
                            description = "AI-powered replies and thread summaries",
                            beaconVisible = !state.hasSeenMessaging,
                            beaconStyle = BeaconStyle.Pulse(color = VioletPrimary),
                            onBeaconClick = { messagingState.start() },
                            targetModifier = Modifier.waypointTarget(messagingState, MessagingTarget.Card),
                        )

                        FeatureCard(
                            icon = Icons.Default.FilterList,
                            iconColor = TealSecondary,
                            title = "Advanced Filters",
                            description = "Complex filter chains with saved presets",
                            beaconVisible = !state.hasSeenFilters,
                            beaconStyle = BeaconStyle.Pulse(color = TealSecondary),
                            onBeaconClick = { filtersState.start() },
                            targetModifier = Modifier.waypointTarget(filtersState, FiltersTarget.Card),
                        )

                        FeatureCard(
                            icon = Icons.Default.DarkMode,
                            iconColor = AmberTertiary,
                            title = "Dark Mode",
                            description = "Automatic switching with custom accents",
                            beaconVisible = !state.hasSeenDarkMode,
                            beaconStyle = BeaconStyle.Dot(color = AmberTertiary),
                            onBeaconClick = { darkModeState.start() },
                            targetModifier = Modifier.waypointTarget(darkModeState, DarkModeTarget.Card),
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = {
                                viewModel.onEvent(DiscoveryEvent.ResetAll)
                                messagingState.resetCompletion()
                                filtersState.resetCompletion()
                                darkModeState.resetCompletion()
                            },
                        ) {
                            Text("Reset All")
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    description: String,
    beaconVisible: Boolean,
    beaconStyle: BeaconStyle,
    onBeaconClick: () -> Unit,
    targetModifier: Modifier,
) {
    WaypointBeacon(
        visible = beaconVisible,
        style = beaconStyle,
        onClick = onBeaconClick,
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .then(targetModifier),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconColor.copy(alpha = 0.12f)),
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp),
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Learn More",
                        style = MaterialTheme.typography.labelMedium,
                        color = iconColor,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}
