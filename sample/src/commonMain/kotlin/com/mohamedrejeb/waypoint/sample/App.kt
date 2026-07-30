package com.mohamedrejeb.waypoint.sample

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.mohamedrejeb.waypoint.sample.catalog.CatalogScreen
import com.mohamedrejeb.waypoint.sample.demos.highlights.HighlightGalleryDemo
import com.mohamedrejeb.waypoint.sample.demos.hints.HintsAndBeaconsDemo
import com.mohamedrejeb.waypoint.sample.demos.modals.ModalToursDemo
import com.mohamedrejeb.waypoint.sample.demos.onboarding.OnboardingDemo
import com.mohamedrejeb.waypoint.sample.demos.sequences.TourSequencesDemo
import com.mohamedrejeb.waypoint.sample.demos.theming.ThemingDemo
import com.mohamedrejeb.waypoint.sample.demos.tutorial.InteractiveTutorialDemo
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
                entry<Route.InteractiveTutorial> {
                    InteractiveTutorialDemo(onBack = { backStack.removeLastOrNull() })
                }
                entry<Route.ModalTours> {
                    ModalToursDemo(onBack = { backStack.removeLastOrNull() })
                }
                entry<Route.HighlightGallery> {
                    HighlightGalleryDemo(onBack = { backStack.removeLastOrNull() })
                }
                entry<Route.HintsAndBeacons> {
                    HintsAndBeaconsDemo(onBack = { backStack.removeLastOrNull() })
                }
                entry<Route.TourSequences> {
                    TourSequencesDemo(onBack = { backStack.removeLastOrNull() })
                }
                entry<Route.Theming> {
                    ThemingDemo(onBack = { backStack.removeLastOrNull() })
                }
            },
        )
    }
}
