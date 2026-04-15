package com.mohamedrejeb.waypoint.sample.catalog

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.waypoint.sample.navigation.Route

private val Violet = Color(0xFF7C3AED)
private val Teal = Color(0xFF14B8A6)
private val Amber = Color(0xFFF59E0B)

private val demoItems = listOf(
    DemoItem(
        title = "Onboarding Tour",
        description = "Step-by-step onboarding flow with spotlight highlights and navigation controls.",
        icon = Icons.Default.PlayArrow,
        iconTint = Violet,
        tags = listOf("Spotlight", "Placement", "Navigation"),
    ),
    DemoItem(
        title = "Feature Discovery",
        description = "Highlight new features with beacons and show-once persistence.",
        icon = Icons.Default.Star,
        iconTint = Teal,
        tags = listOf("Beacon", "Persistence", "Show Once"),
    ),
    DemoItem(
        title = "Interactive Tutorial",
        description = "Tutorials that advance on user interaction with conditional steps.",
        icon = Icons.Default.Edit,
        iconTint = Amber,
        tags = listOf("AdvanceOn", "AllowClick", "Conditional"),
    ),
    DemoItem(
        title = "Multi-Target Spotlight",
        description = "Spotlight multiple targets simultaneously with custom tooltips.",
        icon = Icons.Default.Info,
        iconTint = Violet,
        tags = listOf("Multi-Target", "Custom Tooltip"),
    ),
    DemoItem(
        title = "Highlight Gallery",
        description = "Explore every highlight style: spotlight, pulse, border, ripple, and custom.",
        icon = Icons.Default.Build,
        iconTint = Teal,
        tags = listOf("Spotlight", "Pulse", "Border", "Ripple", "Custom"),
    ),
    DemoItem(
        title = "Theming Playground",
        description = "Customize tour appearance with theme colors and typography.",
        icon = Icons.Default.Settings,
        iconTint = Amber,
        tags = listOf("Theme", "Colors", "Typography"),
    ),
    DemoItem(
        title = "Analytics Dashboard",
        description = "Track tour events and completion analytics in real time.",
        icon = Icons.Default.Info,
        iconTint = Violet,
        tags = listOf("Analytics", "Events", "Tour ID"),
    ),
    DemoItem(
        title = "Modal Tours",
        description = "Run tours inside dialogs, sheets, and auto-scrolling containers.",
        icon = Icons.Default.Build,
        iconTint = Teal,
        tags = listOf("Dialog", "Sheet", "Auto-Scroll"),
    ),
)

private val demoRoutes = listOf(
    Route.Onboarding,
    Route.FeatureDiscovery,
    Route.InteractiveTutorial,
    Route.MultiTarget,
    Route.HighlightGallery,
    Route.ThemingPlayground,
    Route.AnalyticsDashboard,
    Route.ModalTours,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    onDemoClick: (Route) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Waypoint") },
            )
        },
        modifier = modifier,
    ) { contentPadding ->
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(minSize = 300.dp),
            contentPadding = contentPadding,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        ) {
            items(
                items = demoItems,
                key = { it.title },
            ) { item ->
                val index = demoItems.indexOf(item)
                DemoCard(
                    item = item,
                    onClick = { onDemoClick(demoRoutes[index]) },
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
        }
    }
}
