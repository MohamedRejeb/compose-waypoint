package com.mohamedrejeb.waypoint.sample.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey {
    @Serializable data object Catalog : Route
    @Serializable data object Onboarding : Route
    @Serializable data object FeatureDiscovery : Route
    @Serializable data object InteractiveTutorial : Route
    @Serializable data object MultiTarget : Route
    @Serializable data object HighlightGallery : Route
    @Serializable data object ThemingPlayground : Route
    @Serializable data object AnalyticsDashboard : Route
    @Serializable data object ModalTours : Route
}
