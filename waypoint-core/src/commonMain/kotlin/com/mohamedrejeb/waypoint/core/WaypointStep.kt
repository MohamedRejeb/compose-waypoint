package com.mohamedrejeb.waypoint.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable

/**
 * Defines a single step in a Waypoint tour.
 *
 * @param K the type of the target key (typically an enum)
 */
@Immutable
public data class WaypointStep<K>(
    /** The key identifying the target composable for this step */
    val targetKey: K,
    /** Optional title text displayed in the tooltip */
    val title: String? = null,
    /** Optional description text displayed in the tooltip */
    val description: String? = null,
    /** Custom composable content for the tooltip (overrides title/description) */
    val content: (@Composable (StepScope) -> Unit)? = null,
    /** Where the tooltip should be placed relative to the target */
    val placement: TooltipPlacement = TooltipPlacement.Auto,
    /** Shape of the spotlight cutout */
    val spotlightShape: SpotlightShape = SpotlightShape.Default,
    /** Padding around the target within the spotlight */
    val spotlightPadding: SpotlightPadding = SpotlightPadding.Default,
    /** How the target responds to interaction during this step */
    val interaction: TargetInteraction = TargetInteraction.None,
    /** Condition evaluated at runtime to determine if this step should be shown */
    val showIf: (() -> Boolean)? = null,
    /** Callback invoked when this step becomes active */
    val onEnter: (() -> Unit)? = null,
    /** Callback invoked when this step is exited */
    val onExit: (() -> Unit)? = null,
)
