package com.mohamedrejeb.waypoint.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable

/**
 * Creates and remembers a [WaypointHintState] configured via the DSL [builder].
 *
 * The returned state survives Android configuration changes (rotation, theme, etc.)
 * via [rememberSaveable]. Dismissed keys and the currently-open hint are saved as
 * their [toString] representations; on restore they're matched back against the
 * hint list.
 *
 * ```kotlin
 * val hints = rememberWaypointHintState<MyHintKeys>(
 *     persistence = SharedPrefsPersistence(prefs),
 *     groupId = "home-hints",
 * ) {
 *     hint(MyHintKeys.NewFeature) {
 *         title = "Try our new feature"
 *         description = "Tap the beacon to learn more"
 *     }
 * }
 * ```
 *
 * @param persistence optional persistence for remembering dismissed hints across sessions
 * @param groupId optional identifier used to namespace persistence keys
 * @param builder DSL block to declare hints
 */
@Composable
public fun <K> rememberWaypointHintState(
    persistence: WaypointPersistence? = null,
    groupId: String? = null,
    builder: WaypointHintScope<K>.() -> Unit,
): WaypointHintState<K> {
    val hints = remember { WaypointHintScope<K>().apply(builder).hints.toList() }

    val saver: Saver<WaypointHintState<K>, Any> = remember(hints, persistence, groupId) {
        listSaver(
            save = { state ->
                listOf(
                    state.dismissedSnapshot().map { it.toString() },
                    state.openHintKey?.toString(),
                )
            },
            restore = { saved ->
                @Suppress("UNCHECKED_CAST")
                val dismissedStrings = saved[0] as List<String>
                val openString = saved[1] as String?

                val restoredState = WaypointHintState(hints, persistence, groupId)

                val dismissedKeys = hints.mapNotNull { hint ->
                    if (hint.key.toString() in dismissedStrings) hint.key else null
                }
                restoredState.restoreDismissed(dismissedKeys)

                if (openString != null) {
                    val matching = hints.firstOrNull { it.key.toString() == openString }
                    if (matching != null && !restoredState.isDismissed(matching.key)) {
                        restoredState.restoreOpenHintKey(matching.key)
                    }
                }

                restoredState
            },
        )
    }

    return rememberSaveable(saver = saver) {
        WaypointHintState(hints, persistence, groupId)
    }
}
