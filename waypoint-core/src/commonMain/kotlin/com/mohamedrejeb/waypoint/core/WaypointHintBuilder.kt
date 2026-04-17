package com.mohamedrejeb.waypoint.core

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpOffset

/**
 * DSL builder for configuring a single [WaypointHint].
 */
public class WaypointHintBuilder<K> internal constructor(private val key: K) {
    /** Optional title text */
    public var title: String? = null

    /** Optional description text */
    public var description: String? = null

    /** Tooltip placement relative to the hint target */
    public var placement: TooltipPlacement = TooltipPlacement.Auto

    /** Visual style for the hint beacon */
    public var beaconStyle: BeaconStyle = BeaconStyle.Pulse()

    /** Where the beacon is positioned relative to the target content */
    public var beaconAlignment: Alignment = Alignment.TopEnd

    /** Additional offset applied after [beaconAlignment] */
    public var beaconOffset: DpOffset = DpOffset.Zero

    internal fun build(): WaypointHint<K> = WaypointHint(
        key = key,
        title = title,
        description = description,
        placement = placement,
        beaconStyle = beaconStyle,
        beaconAlignment = beaconAlignment,
        beaconOffset = beaconOffset,
    )
}

/**
 * Scope for declaring hints inside [rememberWaypointHintState].
 */
public class WaypointHintScope<K> internal constructor() {
    internal val hints: MutableList<WaypointHint<K>> = mutableListOf()

    /**
     * Declare a hint identified by [key].
     */
    public fun hint(key: K, block: WaypointHintBuilder<K>.() -> Unit = {}) {
        hints.add(WaypointHintBuilder<K>(key).apply(block).build())
    }
}
