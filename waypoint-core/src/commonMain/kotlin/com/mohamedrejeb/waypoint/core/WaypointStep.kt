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
    /** How the target is visually highlighted; null inherits the host-level style */
    val highlightStyle: HighlightStyle? = null,
    /** How the target responds to interaction during this step */
    val interaction: TargetInteraction = TargetInteraction.None,
    /** How this step advances to the next (Next button, or custom trigger) */
    val advanceOn: WaypointTrigger = WaypointTrigger.Default,
    /** Additional targets to highlight alongside the primary target */
    val additionalTargets: List<K> = emptyList(),
    /** Condition evaluated at runtime to determine if this step should be shown */
    val showIf: (() -> Boolean)? = null,
    /** Callback invoked when this step becomes active */
    val onEnter: (() -> Unit)? = null,
    /** Callback invoked when this step is exited */
    val onExit: (() -> Unit)? = null,
    /**
     * Suspend function launched when this step becomes active, typically used to
     * open a Dialog/Sheet or scroll content so the target can register. While the
     * step's target is not yet laid out, the highlight/tooltip are held back until
     * this completes. If the target is already visible when the step is entered,
     * the step is shown immediately while beforeShow runs, which keeps navigation
     * between already-visible targets flicker-free.
     */
    val beforeShow: (suspend () -> Unit)? = null,
)
