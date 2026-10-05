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
 * and the tooltip. The highlight mechanism is independent from the tooltip,
 * any style can be combined with any tooltip content.
 */
public sealed interface HighlightStyle {

    /**
     * Dimmed overlay with a transparent cutout around the target.
     * This is the classic product-tour look and the default.
     *
     * It is also the only style that blocks pointer input: touches outside
     * the cutouts never reach the app (see [OverlayClickBehavior] for what a
     * tap there does), and [TargetInteraction] decides what happens inside
     * them. Every other style leaves the whole screen interactive. For
     * blocking without dimming use `Spotlight(overlayAlpha = 0f)`.
     *
     * @param effect optional decoration applied around or instead of the
     *   hard-edge cutout (glow, soft edge, or custom draw). Defaults to
     *   [SpotlightEffect.None], preserving the classic hard cutout.
     */
    public data class Spotlight(
        val shape: SpotlightShape = SpotlightShape.Default,
        val padding: SpotlightPadding = SpotlightPadding.Default,
        val overlayColor: Color = Color.Black,
        val overlayAlpha: Float = 0.6f,
        val effect: SpotlightEffect = SpotlightEffect.None,
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
     * @param strokeWidth ring stroke width (ignored when [filled] is true)
     */
    public data class Ripple(
        val color: Color,
        val ringCount: Int = 3,
        val durationMillis: Int = 2000,
        val maxRadius: Dp = 60.dp,
        val filled: Boolean = false,
        val strokeWidth: Dp = 2.dp,
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
}
