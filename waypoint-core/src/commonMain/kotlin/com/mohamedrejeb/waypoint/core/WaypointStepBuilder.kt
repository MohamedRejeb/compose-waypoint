package com.mohamedrejeb.waypoint.core

import androidx.compose.runtime.Composable

/**
 * DSL builder for constructing a list of [WaypointStep]s.
 */
public class WaypointStepBuilder<K> internal constructor() {
    private val steps = mutableListOf<WaypointStep<K>>()

    /**
     * Add a step targeting the composable identified by [targetKey].
     */
    public fun step(targetKey: K, block: StepBuilder<K>.() -> Unit = {}) {
        val builder = StepBuilder<K>(targetKey)
        builder.block()
        steps.add(builder.build())
    }

    internal fun build(): List<WaypointStep<K>> = steps.toList()
}

/**
 * Builder for configuring a single [WaypointStep].
 */
public class StepBuilder<K> internal constructor(private val targetKey: K) {
    /** Optional title text */
    public var title: String? = null

    /** Optional description text */
    public var description: String? = null

    /** Custom composable content (overrides title/description) */
    public var content: (@Composable (StepScope) -> Unit)? = null

    /** Tooltip placement relative to target */
    public var placement: TooltipPlacement = TooltipPlacement.Auto

    /** Spotlight cutout shape */
    public var spotlightShape: SpotlightShape = SpotlightShape.Default

    /** Padding around the target in the spotlight */
    public var spotlightPadding: SpotlightPadding = SpotlightPadding.Default

    /** How the target responds to interaction */
    public var interaction: TargetInteraction = TargetInteraction.None

    private var showIf: (() -> Boolean)? = null
    private var onEnter: (() -> Unit)? = null
    private var onExit: (() -> Unit)? = null

    /** Set a condition for when this step should be shown */
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

    /** Set custom composable content for the tooltip */
    public fun content(block: @Composable (StepScope) -> Unit) {
        content = block
    }

    internal fun build(): WaypointStep<K> = WaypointStep(
        targetKey = targetKey,
        title = title,
        description = description,
        content = content,
        placement = placement,
        spotlightShape = spotlightShape,
        spotlightPadding = spotlightPadding,
        interaction = interaction,
        showIf = showIf,
        onEnter = onEnter,
        onExit = onExit,
    )
}
