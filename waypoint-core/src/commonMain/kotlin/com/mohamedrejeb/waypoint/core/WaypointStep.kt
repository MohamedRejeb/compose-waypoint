package com.mohamedrejeb.waypoint.core

import androidx.compose.runtime.Composable

/**
 * Defines a single step in a Waypoint tour.
 *
 * @param K the type of the target key (typically an enum)
 */
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
     * Whether pointer input outside the highlighted areas is blocked during
     * this step, with any highlight style. Null inherits the host's
     * `blockOutside`. See [TargetInteraction] for what happens inside.
     */
    val blockOutside: Boolean? = null,
    /**
     * Event-driven progression: once the step is shown (and the tour is not
     * paused) this is awaited, and the tour advances when it returns. It is
     * cancelled if the step is exited first. The Next button and keyboard
     * shortcuts keep working alongside it. Null means manual navigation only.
     *
     * The trigger is armed on every entry except the user's Back navigation
     * ([WaypointState.previous]): entered that way, the step shows with manual
     * navigation, so a condition that already holds does not bounce the user
     * forward again. `start`, `next`, [WaypointState.goToStep] and
     * [WaypointState.goTo] in either direction arm it, so an app that sends
     * the user back to redo a precondition keeps the trigger live.
     * [StepScope.advancesAutomatically] tells tooltip content which case it is
     * in.
     *
     * An exception thrown here propagates to the composition of the primary
     * host, nothing is swallowed.
     */
    val advanceOn: (suspend () -> Unit)? = null,
    /** Additional targets to highlight alongside the primary target; ignored without a target */
    val additionalTargets: List<K> = emptyList(),
    /**
     * Condition deciding whether this step is shown. Evaluated on every
     * navigation and also during composition, to compute progress and the
     * first/last flags of the tooltip, so it must be cheap and free of side
     * effects. Read snapshot state inside it to have changes picked up.
     */
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
     *
     * An exception thrown here propagates to the composition of the primary
     * host, nothing is swallowed.
     */
    val beforeShow: (suspend () -> Unit)? = null,
) {
    /** True when touches inside the highlighted areas should reach the app. */
    internal val isPassThrough: Boolean
        get() = targetKey != null && interaction == TargetInteraction.PassThrough
}
