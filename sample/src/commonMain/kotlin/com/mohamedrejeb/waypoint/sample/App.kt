package com.mohamedrejeb.waypoint.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.mohamedrejeb.waypoint.sample.catalog.CatalogScreen
import com.mohamedrejeb.waypoint.sample.demos.discovery.FeatureDiscoveryDemo
import com.mohamedrejeb.waypoint.sample.demos.onboarding.OnboardingDemo
import com.mohamedrejeb.waypoint.sample.navigation.Route
import com.mohamedrejeb.waypoint.sample.theme.SampleTheme

@Composable
fun App() {
    SampleTheme {
        val backStack = remember { NavBackStack(Route.Catalog as Route) }

        NavDisplay(
            backStack = backStack,
            entryProvider = entryProvider {
                entry<Route.Catalog> {
                    CatalogScreen(
                        onDemoClick = { route -> backStack.add(route) },
                    )
                }
                entry<Route.Onboarding> {
                    OnboardingDemo(onBack = { backStack.removeLastOrNull() })
                }
                entry<Route.FeatureDiscovery> {
                    FeatureDiscoveryDemo(onBack = { backStack.removeLastOrNull() })
                }
                entry<Route.InteractiveTutorial> {
                    DemoPlaceholder("Interactive Tutorial") { backStack.removeLastOrNull() }
                }
                entry<Route.MultiTarget> {
                    DemoPlaceholder("Multi-Target Spotlight") { backStack.removeLastOrNull() }
                }
                entry<Route.HighlightGallery> {
                    DemoPlaceholder("Highlight Gallery") { backStack.removeLastOrNull() }
                }
                entry<Route.ThemingPlayground> {
                    DemoPlaceholder("Theming Playground") { backStack.removeLastOrNull() }
                }
                entry<Route.AnalyticsDashboard> {
                    DemoPlaceholder("Analytics Dashboard") { backStack.removeLastOrNull() }
                }
                entry<Route.ModalTours> {
                    DemoPlaceholder("Modal Tours") { backStack.removeLastOrNull() }
                }
            },
        )
    }
}

@Composable
private fun DemoPlaceholder(title: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onBack) {
                Text("← Back")
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        Text(
            text = "This demo will be implemented in a later task.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
