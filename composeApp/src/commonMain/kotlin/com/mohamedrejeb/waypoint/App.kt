package com.mohamedrejeb.waypoint

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohamedrejeb.waypoint.core.HighlightStyle
import com.mohamedrejeb.waypoint.core.OverlayClickBehavior
import com.mohamedrejeb.waypoint.core.SpotlightPadding
import com.mohamedrejeb.waypoint.core.SpotlightShape
import com.mohamedrejeb.waypoint.core.StepScope
import com.mohamedrejeb.waypoint.core.TargetInteraction
import com.mohamedrejeb.waypoint.core.TooltipPlacement
import com.mohamedrejeb.waypoint.core.WaypointHost
import com.mohamedrejeb.waypoint.core.WaypointTrigger
import com.mohamedrejeb.waypoint.core.rememberWaypointState
import com.mohamedrejeb.waypoint.core.waypointTarget
import com.mohamedrejeb.waypoint.material3.WaypointMaterial3Host
import com.mohamedrejeb.waypoint.material3.WaypointMaterial3Theme
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first

enum class DemoTargets {
    MenuButton,
    SearchButton,
    ProfileButton,
    SearchField,
    FabButton,
    WelcomeCard,
    SettingsToggle,
    StatsCard,
    ScrolledItem,
    BottomAction,
}

@OptIn(FlowPreview::class)
@Composable
fun App() {
    MaterialTheme {
        var tourStarted by remember { mutableStateOf(false) }
        var showAdvanced by remember { mutableStateOf(true) }
        var enterCount by remember { mutableStateOf(0) }
        var searchQuery by remember { mutableStateOf("") }

        val waypointState = rememberWaypointState {
            // Step 1: Spotlight + Circle shape + forced Top placement
            step(DemoTargets.MenuButton) {
                title = "Navigation Menu"
                description = "Open the drawer to explore sections."
                placement = TooltipPlacement.Bottom
                highlightStyle = HighlightStyle.Spotlight(
                    shape = SpotlightShape.Circle,
                )
            }

            // Step 2: Pulse + Circle + ClickToAdvance (tap target to proceed)
            step(DemoTargets.SearchButton) {
                title = "Search"
                description = "Tap the search icon to continue!"
                placement = TooltipPlacement.Bottom
                interaction = TargetInteraction.ClickToAdvance
                highlightStyle = HighlightStyle.Pulse(
                    color = Color(0xFF6200EE),
                    shape = SpotlightShape.Circle,
                    padding = SpotlightPadding(4.dp),
                )
            }

            // Step 3: Ripple + onEnter callback
            step(DemoTargets.ProfileButton) {
                title = "Your Profile"
                description = "Manage your account settings."
                highlightStyle = HighlightStyle.Ripple(
                    color = Color(0xFF03DAC5),
                    maxRadius = 40.dp,
                )
                onEnter { enterCount++ }
            }

            // Step 4: Event-driven progression -- auto-advances when user types
            step(DemoTargets.SearchField) {
                title = "Try Searching"
                description = "Type something and pause to auto-advance..."
                interaction = TargetInteraction.AllowClick
                highlightStyle = HighlightStyle.Spotlight(
                    shape = SpotlightShape.RoundedRect(cornerRadius = 8.dp),
                )
                advanceOn = WaypointTrigger.Custom {
                    // Debounce: wait for user to stop typing for 800ms
                    snapshotFlow { searchQuery }
                        .filter { it.isNotEmpty() }
                        .debounce(800)
                        .first()
                }
            }

            // Step 5: Conditional step -- only shown when showAdvanced is true
            step(DemoTargets.SettingsToggle) {
                title = "Advanced Settings"
                description = "This step only appears when advanced mode is on."
                placement = TooltipPlacement.End
                highlightStyle = HighlightStyle.Spotlight(
                    shape = SpotlightShape.Pill,
                    padding = SpotlightPadding(horizontal = 8.dp, vertical = 4.dp),
                )
                showIf { showAdvanced }
            }

            // Step 5: No highlight -- just a tooltip
            step(DemoTargets.WelcomeCard) {
                title = "Welcome Card"
                description = "This step uses no highlight at all -- just the tooltip."
                highlightStyle = HighlightStyle.None
            }

            // Step 6: Border + Rect shape + Start placement
            step(DemoTargets.StatsCard) {
                title = "Your Stats"
                description = "Track your progress here."
                placement = TooltipPlacement.Start
                highlightStyle = HighlightStyle.Border(
                    color = Color(0xFFFF5722),
                    shape = SpotlightShape.RoundedRect(cornerRadius = 12.dp),
                    borderWidth = 3.dp,
                )
            }

            // Step 7: Auto-scroll demo -- target is below the fold
            step(DemoTargets.ScrolledItem) {
                title = "Scrolled Into View"
                description = "This item was off-screen. Waypoint auto-scrolled it into view!"
                highlightStyle = HighlightStyle.Spotlight(
                    shape = SpotlightShape.RoundedRect(cornerRadius = 8.dp),
                    padding = SpotlightPadding(8.dp),
                )
            }

            // Step 8: Custom highlight (animated pointer)
            step(DemoTargets.FabButton) {
                title = "Create New"
                description = "Tap here to add items."
                highlightStyle = HighlightStyle.Custom { _, animatedBounds ->
                    val density = LocalDensity.current
                    val bounceOffset by rememberInfiniteTransition().animateFloat(
                        initialValue = 0f,
                        targetValue = -20f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse,
                        ),
                    )
                    Text(
                        text = "\uD83D\uDC47",
                        fontSize = 28.sp,
                        modifier = Modifier.offset {
                            IntOffset(
                                x = (animatedBounds.center.x - with(density) { 14.sp.toPx() }).toInt(),
                                y = (animatedBounds.top - with(density) { 40.dp.toPx() } + bounceOffset).toInt(),
                            )
                        },
                    )
                }
            }

            // Step 9: Custom tooltip content + Spotlight with large padding
            step(DemoTargets.BottomAction) {
                highlightStyle = HighlightStyle.Spotlight(
                    shape = SpotlightShape.RoundedRect(cornerRadius = 24.dp),
                    padding = SpotlightPadding(12.dp),
                    overlayAlpha = 0.75f,
                )
                content { scope: StepScope ->
                    Card(
                        modifier = Modifier.width(280.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1B1B2F),
                        ),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "Tour Complete!",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "You've seen all the features.\nStep entered callback fired $enterCount time(s).",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                            )
                            Spacer(Modifier.height(16.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = scope.onPrevious) {
                                    Text("Back", color = Color.White)
                                }
                                Button(onClick = scope.onNext) {
                                    Text("Finish")
                                }
                            }
                        }
                    }
                }
            }
        }

        LaunchedEffect(tourStarted) {
            if (tourStarted) {
                waypointState.start()
            }
        }

        WaypointMaterial3Theme(
            colors = WaypointMaterial3Theme.colors(
                tooltipBackground = Color(0xFF1A1A2E),
                title = Color.White,
                description = Color(0xFFB8B8D0),
                progress = Color(0xFF7C7C9A),
                primaryButton = Color(0xFF8B5CF6),
                secondaryButton = Color(0xFF7C3AED),
                skipButton = Color(0xFF6B7280),
            ),
            dimensions = WaypointMaterial3Theme.dimensions(
                tooltipShape = RoundedCornerShape(20.dp),
                tooltipElevation = 16.dp,
                tooltipPadding = 24.dp,
                contentSpacing = 10.dp,
            ),
        ) {
            WaypointMaterial3Host(
                state = waypointState,
                overlayClickBehavior = OverlayClickBehavior.Dismiss,
                onTourComplete = { tourStarted = false },
                onTourCancel = { tourStarted = false },
            ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // -- Top bar --
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .waypointTarget(waypointState, DemoTargets.MenuButton)
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("\u2630", fontSize = 20.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }

                        Text(
                            text = "Waypoint Demo",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )

                        Box(
                            modifier = Modifier
                                .waypointTarget(waypointState, DemoTargets.SearchButton)
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("\uD83D\uDD0D", fontSize = 18.sp)
                        }

                        Spacer(Modifier.size(8.dp))

                        Box(
                            modifier = Modifier
                                .waypointTarget(waypointState, DemoTargets.ProfileButton)
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("M", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }

                    // -- Scrollable content area --
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        // Search field (event-driven trigger target)
                        androidx.compose.material3.OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Search") },
                            placeholder = { Text("Type to search...") },
                            singleLine = true,
                            modifier = Modifier
                                .waypointTarget(waypointState, DemoTargets.SearchField)
                                .fillMaxWidth(),
                        )

                        // Settings toggle (conditional step target)
                        Row(
                            modifier = Modifier
                                .waypointTarget(waypointState, DemoTargets.SettingsToggle)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Advanced mode", style = MaterialTheme.typography.bodyLarge)
                            Switch(checked = showAdvanced, onCheckedChange = { showAdvanced = it })
                        }

                        // Welcome card (no-highlight target)
                        Card(
                            modifier = Modifier
                                .waypointTarget(waypointState, DemoTargets.WelcomeCard)
                                .fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text("Waypoint", style = MaterialTheme.typography.headlineLarge)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Product Tour for Compose Multiplatform",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        // Stats card (border highlight target)
                        Card(
                            modifier = Modifier
                                .waypointTarget(waypointState, DemoTargets.StatsCard)
                                .fillMaxWidth(),
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Stats", style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                ) {
                                    StatItem(value = "12", label = "Tours")
                                    StatItem(value = "48", label = "Steps")
                                    StatItem(value = "99%", label = "Complete")
                                }
                            }
                        }

                        // Start tour button
                        Button(
                            onClick = { tourStarted = true },
                            enabled = !waypointState.isActive,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Start Tour")
                        }

                        // Spacer to push content below the fold
                        Spacer(Modifier.height(200.dp))

                        // Scrolled item -- below the fold to demo auto-scroll
                        Card(
                            modifier = Modifier
                                .waypointTarget(waypointState, DemoTargets.ScrolledItem)
                                .fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            ),
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Hidden Item",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "This card is below the initial viewport. The tour auto-scrolls to reveal it.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                )
                            }
                        }

                        // Bottom action button (custom tooltip target)
                        Button(
                            onClick = {},
                            modifier = Modifier
                                .waypointTarget(waypointState, DemoTargets.BottomAction)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                        ) {
                            Text("Get Started", modifier = Modifier.padding(vertical = 4.dp))
                        }

                        Spacer(Modifier.height(80.dp)) // room for FAB
                    }
                }

                // FAB (custom highlight target)
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
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
