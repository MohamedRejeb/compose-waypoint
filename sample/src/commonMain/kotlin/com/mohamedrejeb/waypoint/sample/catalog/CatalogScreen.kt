package com.mohamedrejeb.waypoint.sample.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.waypoint.sample.navigation.Route

private val Violet = Color(0xFF7C3AED)
private val Teal = Color(0xFF14B8A6)
private val Amber = Color(0xFFF59E0B)

private val demoItems = listOf(
    DemoItem(
        title = "Onboarding Tour",
        description = "Step-by-step onboarding flow with spotlight highlights and navigation controls.",
        icon = Icons.Rounded.PlayArrow,
        iconTint = Violet,
        tags = listOf("Spotlight", "Placement", "Navigation"),
    ),
    DemoItem(
        title = "Feature Discovery",
        description = "Highlight new features with beacons and show-once persistence.",
        icon = Icons.Rounded.Star,
        iconTint = Teal,
        tags = listOf("Beacon", "Persistence", "Show Once"),
    ),
    DemoItem(
        title = "Interactive Tutorial",
        description = "Tutorials that advance on user interaction with conditional steps.",
        icon = Icons.Rounded.Edit,
        iconTint = Amber,
        tags = listOf("AdvanceOn", "AllowClick", "Conditional"),
    ),
    DemoItem(
        title = "Multi-Target Spotlight",
        description = "Spotlight multiple targets simultaneously with custom tooltips.",
        icon = Icons.Rounded.Info,
        iconTint = Violet,
        tags = listOf("Multi-Target", "Custom Tooltip"),
    ),
    DemoItem(
        title = "Highlight Gallery",
        description = "Explore every highlight style: spotlight, pulse, border, ripple, and custom.",
        icon = Icons.Rounded.Build,
        iconTint = Teal,
        tags = listOf("Spotlight", "Pulse", "Border", "Ripple", "Custom"),
    ),
    DemoItem(
        title = "Theming Playground",
        description = "Customize tour appearance with theme colors and typography.",
        icon = Icons.Rounded.Settings,
        iconTint = Amber,
        tags = listOf("Theme", "Colors", "Typography"),
    ),
    DemoItem(
        title = "Analytics Dashboard",
        description = "Track tour events and completion analytics in real time.",
        icon = Icons.Rounded.Info,
        iconTint = Violet,
        tags = listOf("Analytics", "Events", "Tour ID"),
    ),
    DemoItem(
        title = "Modal Tours",
        description = "Run tours inside dialogs, sheets, and auto-scrolling containers.",
        icon = Icons.Rounded.Build,
        iconTint = Teal,
        tags = listOf("Dialog", "Sheet", "Auto-Scroll"),
    ),
)

private val demoRoutes: List<Route> = listOf(
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
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = { Text("Waypoint") },
                scrollBehavior = scrollBehavior,
            )
        },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { contentPadding ->
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(minSize = 300.dp),
            contentPadding = PaddingValues(
                top = contentPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding() + 16.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalItemSpacing = 12.dp,
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
                )
            }
        }
    }
}
