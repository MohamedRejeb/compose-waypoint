package com.mohamedrejeb.waypoint.sample.demos.discovery

data class DiscoveryState(
    val hasSeenMessaging: Boolean = false,
    val hasSeenFilters: Boolean = false,
    val hasSeenDarkMode: Boolean = false,
)

sealed interface DiscoveryEvent {
    data object DismissMessaging : DiscoveryEvent
    data object DismissFilters : DiscoveryEvent
    data object DismissDarkMode : DiscoveryEvent
    data object ResetAll : DiscoveryEvent
}
