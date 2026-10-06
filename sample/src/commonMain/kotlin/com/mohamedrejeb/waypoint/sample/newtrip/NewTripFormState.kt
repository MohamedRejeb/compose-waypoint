package com.mohamedrejeb.waypoint.sample.newtrip

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class TravelStyle(val label: String) {
    Relaxed("Relaxed"),
    Balanced("Balanced"),
    Packed("Packed"),
}

const val MinTripNameLength = 2

/**
 * State of the New trip form. It lives above the screen because the steps of
 * the hands-on tutorial read it (to advance) and write it (the route gate).
 */
@Stable
class NewTripFormState {
    var name by mutableStateOf("")
    var destination by mutableStateOf("")
    var style by mutableStateOf<TravelStyle?>(null)
    var routeLoading by mutableStateOf(false)
    var routeVisible by mutableStateOf(false)
    var created by mutableStateOf(false)

    val isNameValid: Boolean
        get() = name.trim().length >= MinTripNameLength

    val isDestinationValid: Boolean
        get() = destination.isNotBlank()

    fun reset() {
        name = ""
        destination = ""
        style = null
        routeLoading = false
        routeVisible = false
        created = false
    }
}
