package com.mohamedrejeb.waypoint.sample.lab

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.waypoint.core.HighlightStyle
import com.mohamedrejeb.waypoint.core.OverlayClickBehavior
import com.mohamedrejeb.waypoint.core.SpotlightEffect
import com.mohamedrejeb.waypoint.core.SpotlightPadding
import com.mohamedrejeb.waypoint.core.SpotlightShape
import com.mohamedrejeb.waypoint.core.TargetInteraction
import com.mohamedrejeb.waypoint.core.TooltipPlacement

enum class LabHighlight(val label: String) {
    Spotlight("Spotlight"),
    Pulse("Pulse"),
    Border("Border"),
    Ripple("Ripple"),
    None("No highlight"),
}

enum class LabShape(val label: String) {
    Rounded("Rounded"),
    Rect("Rect"),
    Circle("Circle"),
    Pill("Pill"),
}

enum class LabEffect(val label: String) {
    None("Plain"),
    Glow("Glow"),
    SoftEdge("Soft edge"),
}

enum class LabBlock(val label: String) {
    Default("Host default"),
    On("Blocked"),
    Off("Open"),
}

enum class LabOverlayClick(val label: String) {
    Nothing("Nothing"),
    NextStep("Next step"),
    Dismiss("Dismiss"),
}

enum class LabTooltip(val label: String) {
    Custom("Custom"),
    Material3("Material3"),
}

enum class LabTarget { Avatar, Title, Action }

/**
 * Everything the Lab's controls can change. The Lab's tour is rebuilt from
 * this value, so it is immutable: a control produces a modified copy.
 */
@Immutable
data class LabConfig(
    val highlight: LabHighlight = LabHighlight.Spotlight,
    val shape: LabShape = LabShape.Rounded,
    val effect: LabEffect = LabEffect.None,
    /** Space between the target and the highlight, in dp. */
    val padding: Float = DefaultPadding,
    val scrimAlpha: Float = DefaultScrimAlpha,
    val block: LabBlock = LabBlock.Default,
    val interaction: TargetInteraction = TargetInteraction.None,
    // Below the target by default, which keeps the tooltip inside the preview.
    val placement: TooltipPlacement = TooltipPlacement.Bottom,
    val overlayClick: LabOverlayClick = LabOverlayClick.Nothing,
    val tooltip: LabTooltip = LabTooltip.Custom,
) {
    /** The cutout shape only exists for the styles that draw one. */
    val supportsShape: Boolean
        get() = highlight == LabHighlight.Spotlight ||
            highlight == LabHighlight.Pulse ||
            highlight == LabHighlight.Border

    /** Effects and the scrim belong to the spotlight. */
    val supportsSpotlightOptions: Boolean
        get() = highlight == LabHighlight.Spotlight

    companion object {
        const val DefaultPadding = 4f
        const val MaxPadding = 24f
        const val DefaultScrimAlpha = 0.6f
        const val MaxScrimAlpha = 0.9f
    }
}

private val RoundedCutoutRadius = 16.dp
private val GlowRadius = 24.dp

fun LabConfig.toSpotlightShape(): SpotlightShape = when (shape) {
    LabShape.Rounded -> SpotlightShape.RoundedRect(RoundedCutoutRadius)
    LabShape.Rect -> SpotlightShape.Rect
    LabShape.Circle -> SpotlightShape.Circle
    LabShape.Pill -> SpotlightShape.Pill
}

fun LabConfig.toHighlightStyle(accent: Color, scrim: Color): HighlightStyle {
    val cutout = toSpotlightShape()
    val space = SpotlightPadding(all = padding.dp)
    return when (highlight) {
        LabHighlight.Spotlight -> HighlightStyle.Spotlight(
            shape = cutout,
            padding = space,
            overlayColor = scrim,
            overlayAlpha = scrimAlpha,
            effect = when (effect) {
                LabEffect.None -> SpotlightEffect.None
                LabEffect.Glow -> SpotlightEffect.Glow(color = accent, radius = GlowRadius)
                LabEffect.SoftEdge -> SpotlightEffect.SoftEdge()
            },
        )
        LabHighlight.Pulse -> HighlightStyle.Pulse(color = accent, shape = cutout, padding = space)
        LabHighlight.Border -> HighlightStyle.Border(color = accent, shape = cutout, padding = space)
        LabHighlight.Ripple -> HighlightStyle.Ripple(color = accent)
        LabHighlight.None -> HighlightStyle.None
    }
}

/** Null leaves the decision to the host. */
fun LabConfig.toBlockOutside(): Boolean? = when (block) {
    LabBlock.Default -> null
    LabBlock.On -> true
    LabBlock.Off -> false
}

fun LabConfig.toOverlayClickBehavior(): OverlayClickBehavior = when (overlayClick) {
    LabOverlayClick.Nothing -> OverlayClickBehavior.Nothing
    LabOverlayClick.NextStep -> OverlayClickBehavior.NextStep
    LabOverlayClick.Dismiss -> OverlayClickBehavior.Dismiss
}
