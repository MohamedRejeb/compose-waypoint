package com.mohamedrejeb.waypoint

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohamedrejeb.waypoint.core.SpotlightShape
import com.mohamedrejeb.waypoint.core.rememberWaypointState
import com.mohamedrejeb.waypoint.core.waypointTarget
import com.mohamedrejeb.waypoint.material3.WaypointMaterial3Host

enum class DemoTargets {
    MenuButton,
    SearchButton,
    ProfileButton,
    FabButton,
    ContentArea,
}

@Composable
fun App() {
    MaterialTheme {
        var tourStarted by remember { mutableStateOf(false) }

        val waypointState = rememberWaypointState {
            step(DemoTargets.MenuButton) {
                title = "Navigation Menu"
                description = "Tap here to open the navigation drawer and explore different sections."
                spotlightShape = SpotlightShape.Circle
            }
            step(DemoTargets.SearchButton) {
                title = "Search"
                description = "Quickly find what you're looking for using the search feature."
                spotlightShape = SpotlightShape.Circle
            }
            step(DemoTargets.ProfileButton) {
                title = "Your Profile"
                description = "View and manage your account settings and preferences."
                spotlightShape = SpotlightShape.Circle
            }
            step(DemoTargets.FabButton) {
                title = "Create New"
                description = "Tap this button to create a new item. You can add notes, tasks, and more."
                spotlightShape = SpotlightShape.RoundedRect(cornerRadius = 16.dp)
            }
            step(DemoTargets.ContentArea) {
                title = "Welcome to Waypoint!"
                description = "This is your main content area. Here you'll see all your items and recent activity."
                spotlightShape = SpotlightShape.RoundedRect(cornerRadius = 12.dp)
            }
        }

        LaunchedEffect(tourStarted) {
            if (tourStarted) {
                waypointState.start()
            }
        }

        WaypointMaterial3Host(
            state = waypointState,
            onTourComplete = { tourStarted = false },
            onTourCancel = { tourStarted = false },
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Top bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Menu button
                        Box(
                            modifier = Modifier
                                .waypointTarget(waypointState, DemoTargets.MenuButton)
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "\u2630",
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }

                        Text(
                            text = "Waypoint Demo",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )

                        // Search button
                        Box(
                            modifier = Modifier
                                .waypointTarget(waypointState, DemoTargets.SearchButton)
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "\uD83D\uDD0D",
                                fontSize = 18.sp,
                            )
                        }

                        Spacer(Modifier.size(8.dp))

                        // Profile button
                        Box(
                            modifier = Modifier
                                .waypointTarget(waypointState, DemoTargets.ProfileButton)
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "M",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    }

                    // Content area
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Column(
                            modifier = Modifier
                                .waypointTarget(waypointState, DemoTargets.ContentArea)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "Waypoint",
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Product Tour Library for Compose Multiplatform",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        Spacer(Modifier.height(32.dp))

                        Button(
                            onClick = { tourStarted = true },
                            enabled = !waypointState.isActive,
                        ) {
                            Text("Start Tour")
                        }
                    }
                }

                // FAB
                FloatingActionButton(
                    onClick = {},
                    modifier = Modifier
                        .waypointTarget(waypointState, DemoTargets.FabButton)
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                ) {
                    Text("+", fontSize = 24.sp)
                }
            }
        }
    }
}
