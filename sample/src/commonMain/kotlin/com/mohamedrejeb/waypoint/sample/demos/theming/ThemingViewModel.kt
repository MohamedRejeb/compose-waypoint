package com.mohamedrejeb.waypoint.sample.demos.theming

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class ThemingViewModel : ViewModel() {
    private val _state = MutableStateFlow(ThemingState())
    val state: StateFlow<ThemingState> = _state

    fun onEvent(event: ThemingEvent) {
        when (event) {
            is ThemingEvent.BackgroundChanged -> _state.update { it.copy(tooltipBackground = event.color) }
            is ThemingEvent.TitleColorChanged -> _state.update { it.copy(titleColor = event.color) }
            is ThemingEvent.DescriptionColorChanged -> _state.update { it.copy(descriptionColor = event.color) }
            is ThemingEvent.ButtonColorChanged -> _state.update { it.copy(primaryButtonColor = event.color) }
            is ThemingEvent.CornerRadiusChanged -> _state.update { it.copy(tooltipCornerRadius = event.dp) }
            is ThemingEvent.ElevationChanged -> _state.update { it.copy(tooltipElevation = event.dp) }
            is ThemingEvent.PaddingChanged -> _state.update { it.copy(tooltipPadding = event.dp) }
            ThemingEvent.ResetDefaults -> _state.update { ThemingState() }
        }
    }
}
