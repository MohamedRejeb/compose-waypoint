package com.mohamedrejeb.waypoint.material3

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.waypoint.core.HintScope

/**
 * Default Material3-styled tooltip content for [com.mohamedrejeb.waypoint.core.WaypointHint].
 *
 * Reads colors, typography, and dimensions from [WaypointMaterial3Theme]. Renders an
 * optional title, optional description, and a single "Got it" action that calls
 * [HintScope.dismiss]. An optional close button in the top-right invokes
 * [HintScope.close] to hide the tooltip without dismissing the hint.
 *
 * @param hintScope scope providing title/description and dismiss/close actions
 * @param modifier modifier for the tooltip container
 * @param labels texts: [WaypointMaterial3Labels.gotIt] for the dismiss button
 * and [WaypointMaterial3Labels.close] as the close icon's content description
 * @param showCloseButton whether to render a close-only button in the header row
 * @param showArrow whether to draw an arrow pointing at the hint target
 */
@Composable
public fun WaypointMaterial3HintTooltip(
    hintScope: HintScope,
    modifier: Modifier = Modifier,
    labels: WaypointMaterial3Labels = WaypointMaterial3Labels.Default,
    showCloseButton: Boolean = false,
    showArrow: Boolean = true,
) {
    val colors = WaypointMaterial3Theme.colors
    val typography = WaypointMaterial3Theme.typography
    val dims = WaypointMaterial3Theme.dimensions

    val title = hintScope.title
    val description = hintScope.description

    OptionalTooltipArrowBox(
        showArrow = showArrow,
        arrowColor = colors.tooltipBackground,
    ) {
        HintTooltipCard(
            modifier = modifier,
            colors = colors,
            typography = typography,
            dims = dims,
            title = title,
            description = description,
            gotItText = labels.gotIt,
            showCloseButton = showCloseButton,
            closeContentDescription = labels.close,
            hintScope = hintScope,
        )
    }
}

@Composable
private fun HintTooltipCard(
    modifier: Modifier,
    colors: WaypointMaterial3Colors,
    typography: WaypointMaterial3Typography,
    dims: WaypointMaterial3Dimensions,
    title: String?,
    description: String?,
    gotItText: String,
    showCloseButton: Boolean,
    closeContentDescription: String,
    hintScope: HintScope,
) {
    Column(
        modifier = modifier
            .widthIn(min = dims.tooltipMinWidth, max = dims.tooltipMaxWidth)
            .shadow(elevation = dims.tooltipElevation, shape = dims.tooltipShape)
            .clip(dims.tooltipShape)
            .background(colors.tooltipBackground)
            .padding(dims.tooltipPadding),
        verticalArrangement = Arrangement.spacedBy(dims.contentSpacing),
    ) {
        if (title != null || showCloseButton) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (title != null) {
                    Text(
                        text = title,
                        style = typography.title,
                        color = colors.title,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                } else {
                    Box(modifier = Modifier.weight(1f))
                }
                if (showCloseButton) {
                    IconButton(
                        onClick = { hintScope.close() },
                        modifier = Modifier.size(24.dp),
                    ) {
                        CloseIcon(
                            tint = colors.title,
                            contentDescription = closeContentDescription,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }

        if (description != null) {
            Text(
                text = description,
                style = typography.description,
                color = colors.description,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = { hintScope.dismiss() }) {
                Text(
                    text = gotItText,
                    style = typography.button,
                    color = colors.primaryButton,
                )
            }
        }
    }
}

@Composable
private fun CloseIcon(
    tint: androidx.compose.ui.graphics.Color,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    Canvas(
        modifier = modifier.semantics { this.contentDescription = contentDescription },
    ) {
        val strokeWidth = size.minDimension * 0.12f
        val inset = size.minDimension * 0.2f
        drawLine(
            color = tint,
            start = Offset(inset, inset),
            end = Offset(size.width - inset, size.height - inset),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = tint,
            start = Offset(size.width - inset, inset),
            end = Offset(inset, size.height - inset),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}
