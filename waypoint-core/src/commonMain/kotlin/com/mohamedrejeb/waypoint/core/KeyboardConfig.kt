package com.mohamedrejeb.waypoint.core

import androidx.compose.runtime.Immutable
import androidx.compose.ui.input.key.Key

/**
 * Configuration for keyboard navigation during a tour.
 *
 * Keyboard navigation is useful on Desktop and Web where physical
 * keyboards are available. On mobile (Android/iOS) it typically
 * has no effect since there is no hardware keyboard.
 */
@Immutable
public data class KeyboardConfig(
    /** Keys that advance to the next step */
    val nextKeys: Set<Key> = setOf(Key.DirectionRight, Key.Enter),
    /** Keys that go back to the previous step */
    val previousKeys: Set<Key> = setOf(Key.DirectionLeft),
    /** Keys that dismiss/cancel the tour */
    val dismissKeys: Set<Key> = setOf(Key.Escape),
    /** Whether keyboard navigation is enabled */
    val enabled: Boolean = true,
) {
    public companion object {
        /** Default keyboard config with arrow keys, Enter, and Escape */
        public val Default: KeyboardConfig = KeyboardConfig()

        /** Keyboard navigation disabled */
        public val Disabled: KeyboardConfig = KeyboardConfig(enabled = false)
    }
}
