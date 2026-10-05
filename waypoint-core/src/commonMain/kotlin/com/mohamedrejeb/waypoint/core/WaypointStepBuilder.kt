package com.mohamedrejeb.waypoint.core

import androidx.compose.runtime.Composable

/**
 * DSL builder for constructing a list of [WaypointStep]s.
 *
 * With [rememberWaypointState] the builder block runs once, so the steps keep
 * whatever they captured at that time. Read changing values inside the step
 * lambdas, and resolve localized strings inside tooltip content. See
 * [rememberWaypointState] for details.
 */
public class WaypointStepBuilder<K> internal constructor() {
    private val steps = mutableListOf<WaypointStep<K>>()

    /**
     * Add a step targeting the composable identified by [targetKey].
     */
    public fun step(targetKey: K, block: StepBuilder<K>.() -> Unit = {}) {
        steps.add(StepBuilder(targetKey).apply(block).build())
    }

    /**
     * Add a step without a target. Its tooltip is shown centered over the
     * primary [WaypointHost], which suits intro and outro cards.
     */
    public fun step(block: StepBuilder<K>.() -> Unit) {
        steps.add(StepBuilder<K>(targetKey = null).apply(block).build())
    }

    internal fun build(): List<WaypointStep<K>> = steps.toList()
}

/**
 * Builder for configuring a single [WaypointStep].
 */
public class StepBuilder<K> internal constructor(private val targetKey: K?) {
    /**
     * Optional title text. A plain string fixed when the step is built: it does
     * not follow a later locale change. For localized text, resolve the string
     * inside tooltip content instead.
     */
    public var title: String? = null

    /** Optional description text. Fixed when the step is built, like [title]. */
    public var description: String? = null

    /** Tooltip placement relative to target */
    public var placement: TooltipPlacement = TooltipPlacement.Auto

    /** How the target is visually highlighted; null inherits the host-level style */
    public var highlightStyle: HighlightStyle? = null

    /** How the target responds to interaction; ignored for a step without a target */
    public var interaction: TargetInteraction = TargetInteraction.None

    /** Additional targets to highlight alongside the primary target; ignored for a step without a target */
    public var additionalTargets: List<K> = emptyList()

    private var content: (@Composable (StepScope) -> Unit)? = null
    private var advanceOn: (suspend () -> Unit)? = null
    private var showIf: (() -> Boolean)? = null
    private var onEnter: (() -> Unit)? = null
    private var onExit: (() -> Unit)? = null
    private var beforeShow: (suspend () -> Unit)? = null

    /**
     * Set a condition for when this step should be shown. It is evaluated on
     * every navigation and during composition (progress, first/last flags), so
     * keep it cheap and side-effect free, and read current state inside it
     * rather than capturing a value read earlier.
     */
    public fun showIf(condition: () -> Boolean) {
        showIf = condition
    }

    /** Set a callback for when this step becomes active */
    public fun onEnter(action: () -> Unit) {
        onEnter = action
    }

    /** Set a callback for when this step is exited */
    public fun onExit(action: () -> Unit) {
        onExit = action
    }

    /**
     * Set a suspend function run when this step becomes active, before it is
     * shown: the highlight and tooltip stay hidden until it returns. Typically
     * used to open a Dialog/Sheet or scroll so the target can register, or to
     * wait for the UI to settle (`beforeShow { delay(300) }`).
     */
    public fun beforeShow(action: suspend () -> Unit) {
        beforeShow = action
    }

    /**
     * Advance to the next step automatically when [await] returns. It is
     * awaited once the step is shown and cancelled if the step is exited
     * first. The Next button and keyboard shortcuts keep working alongside it.
     * Armed only when the step is entered moving forward; going back into the
     * step shows it with manual navigation (see [WaypointStep.advanceOn]).
     *
     * ```kotlin
     * step(Targets.SearchField) {
     *     title = "Try searching"
     *     advanceOn { snapshotFlow { query }.first { it.isNotEmpty() } }
     * }
     * ```
     */
    public fun advanceOn(await: suspend () -> Unit) {
        advanceOn = await
    }

    /** Set custom composable content for the tooltip (overrides the host's tooltip for this step) */
    public fun content(block: @Composable (StepScope) -> Unit) {
        content = block
    }

    internal fun build(): WaypointStep<K> = WaypointStep(
        targetKey = targetKey,
        title = title,
        description = description,
        content = content,
        placement = placement,
        highlightStyle = highlightStyle,
        interaction = interaction,
        advanceOn = advanceOn,
        additionalTargets = additionalTargets,
        showIf = showIf,
        onEnter = onEnter,
        onExit = onExit,
        beforeShow = beforeShow,
    )
}
