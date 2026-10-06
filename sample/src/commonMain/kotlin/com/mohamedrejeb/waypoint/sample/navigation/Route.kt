package com.mohamedrejeb.waypoint.sample.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey {
    @Serializable data object Trips : Route
    @Serializable data object NewTrip : Route
    @Serializable data class Trip(val id: String) : Route
    @Serializable data object Lab : Route
}
