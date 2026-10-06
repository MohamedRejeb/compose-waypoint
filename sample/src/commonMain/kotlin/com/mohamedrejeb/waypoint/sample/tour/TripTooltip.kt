package com.mohamedrejeb.waypoint.sample.tour

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.waypoint.core.HighlightStyle
import com.mohamedrejeb.waypoint.core.HintScope
import com.mohamedrejeb.waypoint.core.SpotlightEffect
import com.mohamedrejeb.waypoint.core.SpotlightShape
import com.mohamedrejeb.waypoint.core.StepScope
import com.mohamedrejeb.waypoint.core.TooltipArrowBox
import com.mohamedrejeb.waypoint.sample.theme.SampleTheme

private val TooltipMaxWidth = 300.dp
private val TooltipRadius = 22.dp
private val GlowRadius = 16.dp
private const val SecondaryAlpha = 0.85f
private const val InactiveDotAlpha = 0.4f

/**
 * The tooltip of every tour in the app, built on waypoint-core only:
 * title, description, progress dots and navigation.
 *
 * @param showNext whether Next (or Done) is offered. By default it is hidden
 *   on a step that advances from the user's own action.
 */
@Composable
fun TripTooltip(
    scope: StepScope,
    modifier: Modifier = Modifier,
    showNext: Boolean = !scope.advancesAutomatically,
) {
    TooltipCard(modifier = modifier) {
        TooltipTexts(title = scope.title, description = scope.description)
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (scope.totalSteps > 1) {
                ProgressDots(current = scope.currentStepIndex, total = scope.totalSteps)
            }
            Spacer(Modifier.weight(1f))
            if (!scope.isLastStep) {
                TooltipTextButton(text = "Skip", onClick = scope::skip)
            }
            if (!scope.isFirstStep) {
                TooltipTextButton(text = "Back", onClick = scope::previous)
            }
            if (showNext) {
                Spacer(Modifier.width(6.dp))
                TooltipPill(
                    text = if (scope.isLastStep) "Done" else "Next",
                    onClick = scope::next,
                )
            }
        }
    }
}

/** Tooltip of a standalone hint: the texts and one dismiss action. */
@Composable
fun TripHintTooltip(
    scope: HintScope,
    modifier: Modifier = Modifier,
) {
    TooltipCard(modifier = modifier) {
        TooltipTexts(title = scope.title, description = scope.description)
        Spacer(Modifier.height(12.dp))
        TooltipPill(
            text = "Got it",
            onClick = scope::dismiss,
            modifier = Modifier.align(Alignment.End),
        )
    }
}

/**
 * Default highlight of the app's tours: a rounded spotlight whose scrim
 * follows the theme and stays up between steps, with an accent glow around
 * the cutout in the dark theme.
 */
@Composable
fun tripHighlightStyle(): HighlightStyle {
    val colors = SampleTheme.colors
    return remember(colors) {
        HighlightStyle.Spotlight(
            shape = SpotlightShape.RoundedRect(18.dp),
            overlayColor = colors.scrim,
            overlayAlpha = colors.scrimAlpha,
            // Keeps the screen dimmed and blocked while a step is getting
            // ready (a route loading, a sheet opening), so the dim does not
            // drop out and come back between two steps.
            coverWhilePending = true,
            effect = if (colors.isDark) {
                SpotlightEffect.Glow(color = colors.accent, radius = GlowRadius)
            } else {
                SpotlightEffect.None
            },
        )
    }
}

/** The rounded container with the arrow that points at the target. */
@Composable
internal fun TooltipCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = SampleTheme.colors
    TooltipArrowBox(arrowColor = colors.tooltipContainer, modifier = modifier) {
        Column(
            modifier = Modifier
                .widthIn(max = TooltipMaxWidth)
                .clip(RoundedCornerShape(TooltipRadius))
                .background(colors.tooltipContainer)
                .padding(16.dp),
            content = content,
        )
    }
}

@Composable
internal fun TooltipTexts(title: String?, description: String?) {
    val colors = SampleTheme.colors
    if (title != null) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = colors.tooltipContent,
        )
    }
    if (description != null) {
        Spacer(Modifier.height(4.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.tooltipContent.copy(alpha = SecondaryAlpha),
        )
    }
}

@Composable
private fun ProgressDots(current: Int, total: Int) {
    val color = SampleTheme.colors.tooltipContent
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(total) { index ->
            val isCurrent = index == current
            Box(
                modifier = Modifier
                    .size(width = if (isCurrent) 16.dp else 6.dp, height = 6.dp)
                    .clip(CircleShape)
                    .background(if (isCurrent) color else color.copy(alpha = InactiveDotAlpha)),
            )
        }
    }
}

@Composable
internal fun TooltipTextButton(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = SampleTheme.colors.tooltipContent.copy(alpha = SecondaryAlpha),
        modifier = Modifier
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
    )
}

@Composable
internal fun TooltipPill(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SampleTheme.colors
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(colors.tooltipActionContainer)
            .clickable(role = Role.Button, onClick = onClick)
            .heightIn(min = 36.dp)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = colors.tooltipActionContent,
        )
    }
}
