package com.mohamedrejeb.waypoint.core

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Defines how a target element is visually highlighted during a tour step.
 *
 * Each style controls the full-screen layer rendered between the screen content
 * and the tooltip. The highlight mechanism is independent from the tooltip --
 * any style can be combined with any tooltip content.
 */
public sealed interface HighlightStyle {

    /**
     * Dimmed overlay with a transparent cutout around the target.
     * This is the classic product-tour look and the default.
     */
    public data class Spotlight(
        val shape: SpotlightShape = SpotlightShape.Default,
        val padding: SpotlightPadding = SpotlightPadding.Default,
        val overlayColor: Color = Color.Black,
        val overlayAlpha: Float = 0.6f,
    ) : HighlightStyle

    /**
     * Animated pulsing shape around the target. No dimming overlay.
     * The shape breathes (scales) to draw attention.
     *
     * @param filled when true, draws a filled shape instead of a stroke border
     */
    public data class Pulse(
        val color: Color,
        val shape: SpotlightShape = SpotlightShape.Default,
        val padding: SpotlightPadding = SpotlightPadding.Default,
        val borderWidth: Dp = 3.dp,
        val filled: Boolean = false,
        val pulseScale: Float = 1.15f,
        val durationMillis: Int = 1200,
    ) : HighlightStyle

    /**
     * Static colored shape around the target. No animation, no overlay.
     *
     * @param filled when true, draws a filled shape instead of a stroke border
     */
    public data class Border(
        val color: Color,
        val shape: SpotlightShape = SpotlightShape.Default,
        val padding: SpotlightPadding = SpotlightPadding.Default,
        val borderWidth: Dp = 2.dp,
        val filled: Boolean = false,
    ) : HighlightStyle

    /**
     * Expanding concentric rings radiating from the target center.
     *
     * @param filled when true, draws filled circles instead of stroke rings
     */
    public data class Ripple(
        val color: Color,
        val ringCount: Int = 3,
        val durationMillis: Int = 2000,
        val maxRadius: Dp = 60.dp,
        val filled: Boolean = false,
    ) : HighlightStyle

    /**
     * No visual highlight. Only the tooltip is shown.
     */
    public data object None : HighlightStyle

    /**
     * Fully custom highlight. The user provides a composable that receives
     * the target bounds and can render anything.
     *
     * @param content composable receiving raw target bounds and animated (interpolated) bounds
     */
    public data class Custom(
        val content: @Composable (targetBounds: Rect, animatedBounds: Rect) -> Unit,
    ) : HighlightStyle

    public companion object {
        public val Default: HighlightStyle = Spotlight()
    }
}
