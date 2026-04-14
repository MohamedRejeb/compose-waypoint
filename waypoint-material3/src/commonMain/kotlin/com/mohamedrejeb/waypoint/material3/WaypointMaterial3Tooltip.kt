package com.mohamedrejeb.waypoint.material3

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import com.mohamedrejeb.waypoint.core.WaypointStep

/**
 * Default Material3-styled tooltip for Waypoint tours.
 *
 * Renders the step's title, description, progress indicator, and navigation buttons
 * using Material3 theming.
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
    val shape = RoundedCornerShape(16.dp)

    Column(
        modifier = modifier
            .widthIn(min = 200.dp, max = 320.dp)
            .shadow(elevation = 8.dp, shape = shape)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Progress indicator
        if (showProgress) {
            Text(
                text = "${stepScope.currentStepIndex + 1} of ${stepScope.totalSteps}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Title
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        // Description
        if (description != null) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Navigation buttons
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left side: Skip button
            TextButton(
                onClick = stepScope.onSkip,
            ) {
                Text(
                    text = skipText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Right side: Back + Next/Finish
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (!stepScope.isFirstStep) {
                    TextButton(
                        onClick = stepScope.onPrevious,
                    ) {
                        Text(
                            text = backText,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                TextButton(
                    onClick = stepScope.onNext,
                ) {
                    Text(
                        text = if (stepScope.isLastStep) finishText else nextText,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}
