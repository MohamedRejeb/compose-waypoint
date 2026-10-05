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
    /**
     * The key identifying the target composable for this step, or null for a
     * step without a target: its tooltip is shown centered over the primary
     * [WaypointHost] (useful for intro and outro cards).
     */
    val targetKey: K? = null,
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
    /** How the target responds to interaction during this step; ignored without a target */
    val interaction: TargetInteraction = TargetInteraction.None,
    /**
     * Event-driven progression: once the step is shown (and the tour is not
     * paused) this is awaited, and the tour advances when it returns. It is
     * cancelled if the step is exited first. The Next button and keyboard
     * shortcuts keep working alongside it. Null means manual navigation only.
     */
    val advanceOn: (suspend () -> Unit)? = null,
    /** Additional targets to highlight alongside the primary target; ignored without a target */
    val additionalTargets: List<K> = emptyList(),
    /** Condition evaluated at runtime to determine if this step should be shown */
    val showIf: (() -> Boolean)? = null,
    /** Callback invoked when this step becomes active */
    val onEnter: (() -> Unit)? = null,
    /** Callback invoked when this step is exited */
    val onExit: (() -> Unit)? = null,
    /**
     * Suspend function run when this step becomes active, before it is shown:
     * the highlight and tooltip stay hidden until it returns. Typically used to
     * open a Dialog/Sheet or scroll content so the target can register, or to
     * wait for the UI to settle (`beforeShow = { delay(300) }`). A gate that
     * returns without suspending never hides an already-visible target, which
     * keeps navigation between such steps flicker-free.
     */
    val beforeShow: (suspend () -> Unit)? = null,
) {
    /** True when touches inside the highlighted areas should reach the app. */
    internal val isPassThrough: Boolean
        get() = targetKey != null && interaction == TargetInteraction.PassThrough
}
