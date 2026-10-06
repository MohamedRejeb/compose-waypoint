package com.mohamedrejeb.waypoint.core

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Visual effect applied around (or instead of) the hard-edge spotlight cutout.
 *
 * Attach to [HighlightStyle.Spotlight.effect] to decorate the cutout with a
 * glow, a soft-edge gradient, or custom draw code. Non-spotlight highlight
 * styles ignore this property.
 */
public sealed interface SpotlightEffect {

    /** No effect. Hard-edge cutout (default, matches classic product-tour look). */
    @Immutable
    public data object None : SpotlightEffect

    /**
     * Colored halo radiating outward from the cutout edge. Rendered on top
     * of the scrim after cutouts are punched.
     *
     * @param color halo color
     * @param radius distance the halo extends beyond the cutout edge
     * @param alpha peak alpha at the cutout edge (fades linearly to 0 at [radius])
     */
    @Immutable
    public data class Glow(
        val color: Color = Color.White,
        val radius: Dp = 24.dp,
        val alpha: Float = 0.6f,
    ) : SpotlightEffect

    /**
     * Soft gradient edge between the transparent cutout and the scrim.
     * Replaces the hard cutout with a radial/rounded-rect gradient fade.
     *
     * @param fadeWidth gradient transition band radiating outward from the
     *   cutout edge; the area outside ([fadeWidth]) keeps the full scrim.
     */
    @Immutable
    public data class SoftEdge(
        val fadeWidth: Dp = 16.dp,
    ) : SpotlightEffect

    /**
     * Fully custom effect. Drawn after cutouts are punched. Receives the
     * padded target bounds in the overlay's local coordinate space.
     *
     * The lambda runs once per target (primary + each additional target).
     */
    public data class Custom(
        val draw: DrawScope.(targetBounds: Rect) -> Unit,
    ) : SpotlightEffect
}
