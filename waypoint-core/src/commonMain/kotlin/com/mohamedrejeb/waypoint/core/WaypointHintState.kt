package com.mohamedrejeb.waypoint.core

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * State holder for a set of persistent [WaypointHint]s.
 *
 * Unlike [WaypointState] which drives sequential tour progression, hints are
 * independent ambient beacons. Each hint stays visible until the user dismisses
 * it, at which point the dismissal can be persisted via [WaypointPersistence].
 *
 * Create via [rememberWaypointHintState].
 *
 * @param K the type of the hint key (typically an enum)
 */
@Stable
public class WaypointHintState<K> @PublishedApi internal constructor(
    hints: List<WaypointHint<K>>,
    private val persistence: WaypointPersistence? = null,
    private val groupId: String? = null,
) {
    /** Immutable list of hints in this group */
    public val hints: List<WaypointHint<K>> = hints

    // SnapshotStateList used as a set via uniqueness checks so reads and writes
    // participate in snapshot observation without requiring the experimental
    // SnapshotStateSet API.
    private val dismissedKeys = mutableStateListOf<K>()

    /** The key of the currently open hint tooltip, or null if none is open */
    public var openHintKey: K? by mutableStateOf(null)
        private set

    init {
        if (persistence != null && groupId != null) {
            hints.forEach { hint ->
                if (persistence.isCompleted(persistenceId(hint.key))) {
                    dismissedKeys.add(hint.key)
                }
            }
        }
    }

    /** Returns true if the hint identified by [key] has been dismissed */
    public fun isDismissed(key: K): Boolean = key in dismissedKeys

    /** Returns the hint identified by [key], or null if not registered */
    public fun find(key: K): WaypointHint<K>? = hints.firstOrNull { it.key == key }

    /** Opens the tooltip for [key]. Only one hint tooltip can be open at a time. */
    public fun open(key: K) {
        if (isDismissed(key)) return
        openHintKey = key
    }

    /** Closes any open tooltip without dismissing the underlying hint */
    public fun close() {
        openHintKey = null
    }

    /** Permanently dismiss [key] and persist the state if configured */
    public fun dismiss(key: K) {
        if (key !in dismissedKeys) {
            dismissedKeys.add(key)
        }
        if (openHintKey == key) {
            openHintKey = null
        }
        if (persistence != null && groupId != null) {
            persistence.markCompleted(persistenceId(key))
        }
    }

    /** Un-dismiss [key] so its beacon shows again */
    public fun reset(key: K) {
        dismissedKeys.remove(key)
        if (persistence != null && groupId != null) {
            persistence.reset(persistenceId(key))
        }
    }

    /** Un-dismiss every hint in this state */
    public fun resetAll() {
        val keys = dismissedKeys.toList()
        dismissedKeys.clear()
        openHintKey = null
        if (persistence != null && groupId != null) {
            keys.forEach { persistence.reset(persistenceId(it)) }
        }
    }

    internal fun restoreDismissed(keys: Collection<K>) {
        dismissedKeys.clear()
        keys.forEach { key ->
            if (key !in dismissedKeys) {
                dismissedKeys.add(key)
            }
        }
    }

    internal fun restoreOpenHintKey(key: K?) {
        openHintKey = key
    }

    internal fun dismissedSnapshot(): List<K> = dismissedKeys.toList()

    private fun persistenceId(key: K): String = "$groupId:$key"
}
