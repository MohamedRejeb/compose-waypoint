package com.mohamedrejeb.waypoint.sample.trip

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mohamedrejeb.waypoint.sample.data.Stop

val StayOptions: List<String> = listOf("Day stop", "1 night", "2 nights")

/**
 * UI state of the Trip screen. It lives above the screen because the tour
 * opens and closes the "Add stop" sheet from its steps.
 */
@Stable
class TripUiState {
    var sheetOpen by mutableStateOf(false)
    var newStopName by mutableStateOf("")
    var stay by mutableStateOf(StayOptions.first())

    /** Stops added from the sheet, shown after the trip's own stops. */
    var addedStops by mutableStateOf(emptyList<Stop>())
        private set

    /** Adds the stop being edited in the sheet, then closes the sheet. */
    fun addStop() {
        val name = newStopName.trim()
        if (name.isNotEmpty()) {
            addedStops = addedStops + Stop(name = name, stay = stay, x = 0f, y = 0f)
        }
        sheetOpen = false
        newStopName = ""
        stay = StayOptions.first()
    }

    fun reset() {
        addedStops = emptyList()
        sheetOpen = false
        newStopName = ""
        stay = StayOptions.first()
    }
}
