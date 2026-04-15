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
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.waypoint.core.ResolvedPlacement
import com.mohamedrejeb.waypoint.core.StepScope

/**
 * Default Material3-styled tooltip for Waypoint tours.
 *
 * Reads colors, typography, and dimensions from [WaypointMaterial3Theme].
 * If no theme is provided, falls back to Material3 defaults.
 */
@Composable
public fun WaypointMaterial3Tooltip(
    stepScope: StepScope,
    resolvedPlacement: ResolvedPlacement,
    title: String?,
    description: String?,
    modifier: Modifier = Modifier,
    skipText: String = "Skip",
    nextText: String = "Next",
    backText: String = "Back",
    finishText: String = "Finish",
    showProgress: Boolean = true,
) {
    val colors = WaypointMaterial3Theme.colors
    val typography = WaypointMaterial3Theme.typography
    val dims = WaypointMaterial3Theme.dimensions

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
                text = "${stepScope.currentStepIndex + 1} of ${stepScope.totalSteps}",
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
            TextButton(onClick = stepScope.onSkip) {
                Text(
                    text = skipText,
                    style = typography.button,
                    color = colors.skipButton,
                )
            }

            // Right side: Back + Next/Finish
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (!stepScope.isFirstStep) {
                    TextButton(onClick = stepScope.onPrevious) {
                        Text(
                            text = backText,
                            style = typography.button,
                            color = colors.secondaryButton,
                        )
                    }
                }

                TextButton(onClick = stepScope.onNext) {
                    Text(
                        text = if (stepScope.isLastStep) finishText else nextText,
                        style = typography.button,
                        color = colors.primaryButton,
                    )
                }
            }
        }
    }
}
