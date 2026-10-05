package com.mohamedrejeb.waypoint.core

import androidx.compose.runtime.Stable

/**
 * Scope provided to hint tooltip content composables.
 *
 * Exposes the hint's title/description, where the tooltip sits, and two ways
 * to close the tooltip: [dismiss] removes the hint permanently (and persists
 * if configured), while [close] only hides the tooltip, leaving the beacon
 * visible.
 *
 * Implemented by the library only; it may gain members in any release.
 */
@Stable
public interface HintScope {
    /** Optional title text configured on the hint */
    public val title: String?

    /** Optional description text configured on the hint */
    public val description: String?

    /** Resolved side of the hint target the tooltip sits on */
    public val placement: ResolvedPlacement

    /** Mark the hint as dismissed, persist if configured, hide the beacon permanently */
    public fun dismiss()

    /** Close the tooltip without dismissing the hint. Beacon stays visible. */
    public fun close()
}

internal class HintScopeImpl(
    override val title: String?,
    override val description: String?,
    override val placement: ResolvedPlacement,
    private val onDismiss: () -> Unit,
    private val onClose: () -> Unit,
) : HintScope {
    override fun dismiss() {
        onDismiss()
    }

    override fun close() {
        onClose()
    }
}
