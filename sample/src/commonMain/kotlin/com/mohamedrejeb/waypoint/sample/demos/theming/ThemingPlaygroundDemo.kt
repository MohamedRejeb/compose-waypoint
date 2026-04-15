package com.mohamedrejeb.waypoint.sample.demos.theming

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mohamedrejeb.waypoint.core.rememberWaypointState
import com.mohamedrejeb.waypoint.core.waypointTarget
import com.mohamedrejeb.waypoint.material3.WaypointMaterial3Host
import com.mohamedrejeb.waypoint.material3.WaypointMaterial3Theme
import com.mohamedrejeb.waypoint.sample.components.DemoScaffold

private enum class ThemingTarget { SearchBar, NotificationBell, SettingsButton }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ThemingPlaygroundDemo(onBack: () -> Unit) {
    val viewModel = viewModel { ThemingViewModel() }
    val themeState by viewModel.state.collectAsState()

    val tourState = rememberWaypointState {
        step(ThemingTarget.SearchBar) {
            title = "Search"
            description = "Find anything in your workspace quickly."
        }
        step(ThemingTarget.NotificationBell) {
            title = "Notifications"
            description = "Stay up to date with alerts and messages."
        }
        step(ThemingTarget.SettingsButton) {
            title = "Settings"
            description = "Customize your experience and preferences."
        }
    }

    DemoScaffold(
        title = "Theming Playground",
        description = "Customize tooltip appearance and preview the tour live.",
        onBack = onBack,
        onStartTour = { tourState.start() },
    ) { padding ->
        WaypointMaterial3Theme(
            colors = WaypointMaterial3Theme.colors(
                tooltipBackground = themeState.tooltipBackground,
                title = themeState.titleColor,
                description = themeState.descriptionColor,
                primaryButton = themeState.primaryButtonColor,
            ),
            dimensions = WaypointMaterial3Theme.dimensions(
                tooltipShape = RoundedCornerShape(themeState.tooltipCornerRadius),
                tooltipElevation = themeState.tooltipElevation,
                tooltipPadding = themeState.tooltipPadding,
            ),
        ) {
            WaypointMaterial3Host(state = tourState) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    // -- Preview section --
                    Text(
                        text = "Preview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                        ) {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .waypointTarget(tourState, ThemingTarget.SearchBar),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp),
                                    )
                                    Text(
                                        text = "Search...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(start = 8.dp),
                                    )
                                }
                            }

                            IconButton(
                                onClick = {},
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .waypointTarget(tourState, ThemingTarget.NotificationBell),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                )
                            }

                            IconButton(
                                onClick = {},
                                modifier = Modifier
                                    .waypointTarget(tourState, ThemingTarget.SettingsButton),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                )
                            }
                        }
                    }

                    // -- Controls section --
                    Text(
                        text = "Theme Controls",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )

                    ColorSwatchRow(
                        label = "Background Color",
                        colors = listOf(
                            Color(0xFF1B1B2F),
                            Color.White,
                            Color(0xFF2D2D3F),
                            Color(0xFFF5F0FF),
                            Color(0xFF0F172A),
                            Color(0xFFFFF7ED),
                        ),
                        selected = themeState.tooltipBackground,
                        onSelect = { viewModel.onEvent(ThemingEvent.BackgroundChanged(it)) },
                    )

                    ColorSwatchRow(
                        label = "Title Color",
                        colors = listOf(
                            Color.White,
                            Color.Black,
                            Color(0xFF7C3AED),
                            Color(0xFF14B8A6),
                        ),
                        selected = themeState.titleColor,
                        onSelect = { viewModel.onEvent(ThemingEvent.TitleColorChanged(it)) },
                    )

                    ColorSwatchRow(
                        label = "Description Color",
                        colors = listOf(
                            Color(0xFFB0B0B0),
                            Color(0xFF666666),
                            Color.White,
                            Color.Black,
                        ),
                        selected = themeState.descriptionColor,
                        onSelect = { viewModel.onEvent(ThemingEvent.DescriptionColorChanged(it)) },
                    )

                    ColorSwatchRow(
                        label = "Button Color",
                        colors = listOf(
                            Color(0xFF7C3AED),
                            Color(0xFF14B8A6),
                            Color(0xFFF59E0B),
                            Color(0xFFEF4444),
                        ),
                        selected = themeState.primaryButtonColor,
                        onSelect = { viewModel.onEvent(ThemingEvent.ButtonColorChanged(it)) },
                    )

                    SliderControl(
                        label = "Corner Radius",
                        value = themeState.tooltipCornerRadius.value,
                        range = 0f..32f,
                        onValueChange = { viewModel.onEvent(ThemingEvent.CornerRadiusChanged(it.dp)) },
                    )

                    SliderControl(
                        label = "Elevation",
                        value = themeState.tooltipElevation.value,
                        range = 0f..24f,
                        onValueChange = { viewModel.onEvent(ThemingEvent.ElevationChanged(it.dp)) },
                    )

                    SliderControl(
                        label = "Padding",
                        value = themeState.tooltipPadding.value,
                        range = 8f..32f,
                        onValueChange = { viewModel.onEvent(ThemingEvent.PaddingChanged(it.dp)) },
                    )

                    OutlinedButton(
                        onClick = { viewModel.onEvent(ThemingEvent.ResetDefaults) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Reset Defaults")
                    }

                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColorSwatchRow(
    label: String,
    colors: List<Color>,
    selected: Color,
    onSelect: (Color) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            colors.forEach { color ->
                ColorSwatch(
                    color = color,
                    isSelected = color == selected,
                    onClick = { onSelect(color) },
                )
            }
        }
    }
}

@Composable
private fun ColorSwatch(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                },
                shape = CircleShape,
            )
            .clickable(onClick = onClick),
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = if (color == Color.White || color == Color(0xFFF5F0FF) || color == Color(0xFFFFF7ED)) {
                    Color.Black
                } else {
                    Color.White
                },
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun SliderControl(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = "${value.toInt()} dp",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
        )
    }
}
