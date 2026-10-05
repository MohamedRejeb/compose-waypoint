package com.mohamedrejeb.waypoint.material3

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.waypoint.core.StepScope
import com.mohamedrejeb.waypoint.core.TooltipArrowBox

/**
 * Default Material3-styled tooltip for Waypoint tours.
 *
 * Reads colors, typography, and dimensions from [WaypointMaterial3Theme].
 * If no theme is provided, falls back to Material3 defaults.
 *
 * The title, description and placement come from [stepScope]. When the step
 * has a target, an arrow pointing at it is drawn automatically (disable via
 * [showArrow]); a step without a target renders the same card with no arrow.
 * While the step advances automatically ([StepScope.advancesAutomatically])
 * the Next/Finish button is hidden; Skip and Back stay.
 *
 * @param stepScope scope of the step being shown, as handed to tooltip content
 * @param modifier modifier for the tooltip card
 * @param labels button and progress texts
 * @param showProgress whether to show the progress text
 * @param showArrow whether to draw an arrow pointing at the target
 */
@Composable
public fun WaypointMaterial3Tooltip(
    stepScope: StepScope,
    modifier: Modifier = Modifier,
    labels: WaypointMaterial3Labels = WaypointMaterial3Labels.Default,
    showProgress: Boolean = true,
    showArrow: Boolean = true,
) {
    val colors = WaypointMaterial3Theme.colors
    val typography = WaypointMaterial3Theme.typography
    val dims = WaypointMaterial3Theme.dimensions
    val title = stepScope.title
    val description = stepScope.description

    OptionalTooltipArrowBox(
        showArrow = showArrow,
        arrowColor = colors.tooltipBackground,
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
            // Progress indicator
            if (showProgress) {
                Text(
                    text = labels.progress(stepScope.currentStepNumber, stepScope.totalSteps),
                    style = typography.progress,
                    color = colors.progress,
                )
            }

            // Title
            if (title != null) {
                Text(
                    text = title,
                    style = typography.title,
                    color = colors.title,
                )
            }

            // Description
            if (description != null) {
                Text(
                    text = description,
                    style = typography.description,
                    color = colors.description,
                )
            }

            // Navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Left side: Skip button
                TextButton(onClick = stepScope::skip) {
                    Text(
                        text = labels.skip,
                        style = typography.button,
                        color = colors.skipButton,
                    )
                }

                // Right side: Back + Next/Finish
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (!stepScope.isFirstStep) {
                        TextButton(onClick = stepScope::previous) {
                            Text(
                                text = labels.back,
                                style = typography.button,
                                color = colors.secondaryButton,
                            )
                        }
                    }

                    // A step that advances by itself has no Next button.
                    if (!stepScope.advancesAutomatically) {
                        TextButton(onClick = stepScope::next) {
                            Text(
                                text = if (stepScope.isLastStep) labels.finish else labels.next,
                                style = typography.button,
                                color = colors.primaryButton,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Wraps [content] in a [TooltipArrowBox] when [showArrow] is set, otherwise
 * renders it bare.
 */
@Composable
internal fun OptionalTooltipArrowBox(
    showArrow: Boolean,
    arrowColor: Color,
    content: @Composable () -> Unit,
) {
    if (showArrow) {
        TooltipArrowBox(arrowColor = arrowColor, content = content)
    } else {
        content()
    }
}
