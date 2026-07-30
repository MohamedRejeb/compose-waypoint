package com.mohamedrejeb.waypoint.sample.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey {
    @Serializable data object Catalog : Route
    @Serializable data object Onboarding : Route
    @Serializable data object InteractiveTutorial : Route
    @Serializable data object ModalTours : Route
    @Serializable data object HighlightGallery : Route
    @Serializable data object HintsAndBeacons : Route
    @Serializable data object TourSequences : Route
    @Serializable data object Theming : Route
}
