package com.mohamedrejeb.waypoint.sample.demos.tutorial

data class TutorialState(
    val name: String = "",
    val email: String = "",
    val plan: Plan? = null,
    val agreedToTerms: Boolean = false,
    val isSubmitted: Boolean = false,
)

enum class Plan(val label: String, val price: String) {
    Free("Free", "$0/mo"),
    Pro("Pro", "$12/mo"),
    Enterprise("Enterprise", "$49/mo"),
}

sealed interface TutorialEvent {
    data class NameChanged(val value: String) : TutorialEvent
    data class EmailChanged(val value: String) : TutorialEvent
    data class PlanSelected(val plan: Plan) : TutorialEvent
    data class TermsToggled(val agreed: Boolean) : TutorialEvent
    data object Submit : TutorialEvent
    data object Reset : TutorialEvent
}
