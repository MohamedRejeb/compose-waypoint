package com.mohamedrejeb.waypoint.sample.demos.tutorial

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class TutorialViewModel : ViewModel() {
    private val _state = MutableStateFlow(TutorialState())
    val state: StateFlow<TutorialState> = _state

    fun onEvent(event: TutorialEvent) {
        when (event) {
            is TutorialEvent.NameChanged -> _state.update { it.copy(name = event.value) }
            is TutorialEvent.EmailChanged -> _state.update { it.copy(email = event.value) }
            is TutorialEvent.PlanSelected -> _state.update { it.copy(plan = event.plan) }
            is TutorialEvent.TermsToggled -> _state.update { it.copy(agreedToTerms = event.agreed) }
            TutorialEvent.Submit -> _state.update { it.copy(isSubmitted = true) }
            TutorialEvent.Reset -> _state.update { TutorialState() }
        }
    }
}
