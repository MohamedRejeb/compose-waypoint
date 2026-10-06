package com.mohamedrejeb.waypoint.sample.lab

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.waypoint.core.TargetInteraction
import com.mohamedrejeb.waypoint.core.TooltipPlacement
import com.mohamedrejeb.waypoint.sample.kit.KitChip
import com.mohamedrejeb.waypoint.sample.kit.KitSlider
import com.mohamedrejeb.waypoint.sample.kit.SegmentedControl
import com.mohamedrejeb.waypoint.sample.theme.SampleTheme
import kotlin.math.roundToInt

/**
 * One control per option of [LabConfig]. Every change is reported as a new
 * copy of the config.
 */
@Composable
fun LabControls(
    config: LabConfig,
    onChange: (LabConfig) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Control("Highlight style") {
            ChipRow(
                options = LabHighlight.entries,
                selected = config.highlight,
                label = { it.label },
                onSelect = { onChange(config.copy(highlight = it)) },
            )
        }
        Control("Cutout shape") {
            SegmentedControl(
                options = LabShape.entries,
                selected = config.shape,
                onSelect = { onChange(config.copy(shape = it)) },
                label = { it.label },
                enabled = config.supportsShape,
            )
        }
        Control("Spotlight effect") {
            SegmentedControl(
                options = LabEffect.entries,
                selected = config.effect,
                onSelect = { onChange(config.copy(effect = it)) },
                label = { it.label },
                enabled = config.supportsSpotlightOptions,
            )
        }
        Control("Padding ${config.padding.roundToInt()}dp") {
            KitSlider(
                value = config.padding,
                onValueChange = { onChange(config.copy(padding = it)) },
                valueRange = 0f..LabConfig.MaxPadding,
                enabled = config.supportsShape,
            )
        }
        Control("Scrim ${(config.scrimAlpha * 100).roundToInt()}%") {
            KitSlider(
                value = config.scrimAlpha,
                onValueChange = { onChange(config.copy(scrimAlpha = it)) },
                valueRange = 0f..LabConfig.MaxScrimAlpha,
                enabled = config.supportsSpotlightOptions,
            )
        }
        Control("Outside the highlight") {
            SegmentedControl(
                options = LabBlock.entries,
                selected = config.block,
                onSelect = { onChange(config.copy(block = it)) },
                label = { it.label },
            )
        }
        Control("Target interaction") {
            SegmentedControl(
                options = TargetInteraction.entries,
                selected = config.interaction,
                onSelect = { onChange(config.copy(interaction = it)) },
                label = { it.label },
            )
        }
        Control("Tooltip placement") {
            ChipRow(
                options = TooltipPlacement.entries,
                selected = config.placement,
                label = { it.name },
                onSelect = { onChange(config.copy(placement = it)) },
            )
        }
        Control("Tap outside") {
            SegmentedControl(
                options = LabOverlayClick.entries,
                selected = config.overlayClick,
                onSelect = { onChange(config.copy(overlayClick = it)) },
                label = { it.label },
            )
        }
        Control("Tooltip") {
            SegmentedControl(
                options = LabTooltip.entries,
                selected = config.tooltip,
                onSelect = { onChange(config.copy(tooltip = it)) },
                label = { it.label },
            )
        }
    }
}

private val TargetInteraction.label: String
    get() = when (this) {
        TargetInteraction.None -> "Blocked"
        TargetInteraction.ClickToAdvance -> "Tap advances"
        TargetInteraction.PassThrough -> "Pass through"
    }

@Composable
private fun Control(
    label: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = SampleTheme.colors.inkMuted,
        )
        content()
    }
}

@Composable
private fun <T> ChipRow(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { option ->
            KitChip(
                text = label(option),
                selected = option == selected,
                onClick = { onSelect(option) },
            )
        }
    }
}
