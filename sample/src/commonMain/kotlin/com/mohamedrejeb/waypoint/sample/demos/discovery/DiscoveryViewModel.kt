package com.mohamedrejeb.waypoint.sample.demos.discovery

import androidx.lifecycle.ViewModel
import com.mohamedrejeb.waypoint.core.WaypointPersistence
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class DiscoveryViewModel : ViewModel() {
    private val _state = MutableStateFlow(DiscoveryState())
    val state: StateFlow<DiscoveryState> = _state

    val persistence = InMemoryPersistence()

    fun onEvent(event: DiscoveryEvent) {
        when (event) {
            DiscoveryEvent.DismissMessaging -> _state.update { it.copy(hasSeenMessaging = true) }
            DiscoveryEvent.DismissFilters -> _state.update { it.copy(hasSeenFilters = true) }
            DiscoveryEvent.DismissDarkMode -> _state.update { it.copy(hasSeenDarkMode = true) }
            DiscoveryEvent.ResetAll -> {
                _state.update { DiscoveryState() }
                persistence.resetAll()
            }
        }
    }
}

class InMemoryPersistence : WaypointPersistence {
    private val completed = mutableSetOf<String>()
    override fun isCompleted(tourId: String) = tourId in completed
    override fun markCompleted(tourId: String) { completed += tourId }
    override fun reset(tourId: String) { completed -= tourId }
    override fun resetAll() { completed.clear() }
}
