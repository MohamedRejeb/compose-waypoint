package com.mohamedrejeb.waypoint.sample.catalog

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ListAlt
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.ViewDay
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.waypoint.sample.components.CardCornerRadius
import com.mohamedrejeb.waypoint.sample.components.ContentMaxWidth
import com.mohamedrejeb.waypoint.sample.components.IconBadge
import com.mohamedrejeb.waypoint.sample.components.ScreenPadding
import com.mohamedrejeb.waypoint.sample.navigation.Route

private data class DemoItem(
    val route: Route,
    val icon: ImageVector,
    val title: String,
    val description: String,
)

@Composable
fun CatalogScreen(
    onDemoClick: (Route) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val demos = listOf(
        DemoItem(
            route = Route.Onboarding,
            icon = Icons.Rounded.Explore,
            title = "Onboarding tour",
            description = "Spotlight, placements, arrows, and a live analytics log",
        ),
        DemoItem(
            route = Route.InteractiveTutorial,
            icon = Icons.Rounded.TouchApp,
            title = "Interactive tutorial",
            description = "Steps that advance on input, conditions, and async gates",
        ),
        DemoItem(
            route = Route.ModalTours,
            icon = Icons.Rounded.ViewDay,
            title = "Dialogs & sheets",
            description = "One tour crossing into a dialog, a sheet, and a long list",
        ),
        DemoItem(
            route = Route.HighlightGallery,
            icon = Icons.Rounded.Layers,
            title = "Highlight styles",
            description = "Every highlight, shape, and effect, plus multi-target",
        ),
        DemoItem(
            route = Route.HintsAndBeacons,
            icon = Icons.Rounded.NotificationsActive,
            title = "Hints & beacons",
            description = "Pulsing markers and dismissable hints that stay dismissed",
        ),
        DemoItem(
            route = Route.TourSequences,
            icon = Icons.AutoMirrored.Rounded.ListAlt,
            title = "Tour sequences",
            description = "Three chapters chained with auto-advance and progress",
        ),
        DemoItem(
            route = Route.Theming,
            icon = Icons.Rounded.Palette,
            title = "Theming",
            description = "Preset tooltip themes previewed on a real tour",
        ),
    )

    // Cycle badge tints so the list feels alive while staying flat.
    val badgeColors = listOf(
        scheme.primaryContainer to scheme.onPrimaryContainer,
        scheme.secondaryContainer to scheme.onSecondaryContainer,
        scheme.tertiaryContainer to scheme.onTertiaryContainer,
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
        LazyColumn(
            modifier = Modifier.widthIn(max = ContentMaxWidth),
            contentPadding = PaddingValues(
                start = ScreenPadding,
                end = ScreenPadding,
                top = 32.dp,
                bottom = 32.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "header") {
                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                    Text(
                        text = "Waypoint",
                        style = MaterialTheme.typography.displaySmall,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Product tours for Compose Multiplatform",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            items(demos.size, key = { demos[it].title }) { index ->
                val item = demos[index]
                val (container, tint) = badgeColors[index % badgeColors.size]
                DemoRow(
                    item = item,
                    badgeContainer = container,
                    badgeTint = tint,
                    onClick = { onDemoClick(item.route) },
                )
            }
        }
    }
}

@Composable
private fun DemoRow(
    item: DemoItem,
    badgeContainer: Color,
    badgeTint: Color,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(CardCornerRadius),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconBadge(
                icon = item.icon,
                container = badgeContainer,
                tint = badgeTint,
                size = 44.dp,
            )
            Column(modifier = Modifier.padding(start = 14.dp)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
