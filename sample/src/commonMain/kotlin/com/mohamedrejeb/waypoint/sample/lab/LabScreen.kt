package com.mohamedrejeb.waypoint.sample.lab

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.waypoint.core.WaypointHost
import com.mohamedrejeb.waypoint.core.WaypointState
import com.mohamedrejeb.waypoint.core.rememberWaypointState
import com.mohamedrejeb.waypoint.material3.WaypointMaterial3Tooltip
import com.mohamedrejeb.waypoint.sample.kit.AppBar
import com.mohamedrejeb.waypoint.sample.kit.KitButton
import com.mohamedrejeb.waypoint.sample.kit.KitButtonStyle
import com.mohamedrejeb.waypoint.sample.kit.KitCard
import com.mohamedrejeb.waypoint.sample.kit.KitChip
import com.mohamedrejeb.waypoint.sample.kit.ScreenColumn
import com.mohamedrejeb.waypoint.sample.kit.SectionTitle
import com.mohamedrejeb.waypoint.sample.theme.Dimens
import com.mohamedrejeb.waypoint.sample.theme.SampleTheme
import com.mohamedrejeb.waypoint.sample.tour.EventLog
import com.mohamedrejeb.waypoint.sample.tour.TripTooltip

private val TwoPaneMinWidth = 840.dp
private val TwoPaneMaxWidth = 1080.dp
private val PreviewHeight = 400.dp

/**
 * A playground: one small tour whose every option is driven by the controls.
 * The steps are rebuilt in place whenever the config changes, so a running
 * tour follows the controls live.
 */
@Composable
fun LabScreen(onBack: () -> Unit) {
    var config by remember { mutableStateOf(LabConfig()) }
    val log = remember { EventLog() }
    val colors = SampleTheme.colors

    // The config and the theme are keys: a change rebuilds the steps and
    // swaps them into the same state, keeping the current step.
    val state = rememberWaypointState(config, colors, tourId = "lab", analytics = log) {
        val style = config.toHighlightStyle(accent = colors.accent, scrim = colors.scrim)
        fun step(target: LabTarget, stepTitle: String, text: String) = step(target) {
            title = stepTitle
            description = text
            highlightStyle = style
            blockOutside = config.toBlockOutside()
            interaction = config.interaction
            placement = config.placement
        }
        step(LabTarget.Avatar, "The highlight", "Style, shape, effect and padding apply to every step.")
        step(LabTarget.Title, "The placement", "Pick a side, or leave it on Auto and the tooltip finds room.")
        step(LabTarget.Action, "The interaction", "Let taps through and the button counts them.")
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val twoPane = maxWidth >= TwoPaneMinWidth
        Column(modifier = Modifier.fillMaxSize()) {
            AppBar(
                title = "Lab",
                onBack = onBack,
                maxWidth = if (twoPane) TwoPaneMaxWidth else Dimens.ContentMaxWidth,
            )
            if (twoPane) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    Row(
                        modifier = Modifier
                            .widthIn(max = TwoPaneMaxWidth)
                            .fillMaxSize()
                            .padding(horizontal = Dimens.ScreenPadding),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                    ) {
                        PaneColumn(modifier = Modifier.weight(1f)) {
                            PreviewPane(state = state, config = config)
                            RunControls(state = state)
                            EventLogCard(state = state, log = log)
                        }
                        PaneColumn(modifier = Modifier.weight(1f)) {
                            LabControls(config = config, onChange = { config = it })
                        }
                    }
                }
            } else {
                ScreenColumn {
                    PreviewPane(state = state, config = config)
                    RunControls(state = state)
                    LabControls(config = config, onChange = { config = it })
                    EventLogCard(state = state, log = log)
                }
            }
        }
    }
}

@Composable
private fun PaneColumn(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        content()
    }
}

/**
 * Only the preview is inside the host, so the tour's overlay covers this
 * pane and the controls stay usable while it runs.
 */
@Composable
private fun PreviewPane(
    state: WaypointState<LabTarget>,
    config: LabConfig,
) {
    WaypointHost(
        state = state,
        modifier = Modifier
            .fillMaxWidth()
            .height(PreviewHeight)
            .clip(RoundedCornerShape(Dimens.RadiusLarge))
            .background(SampleTheme.colors.surface),
        overlayClickBehavior = config.toOverlayClickBehavior(),
        tooltipContent = { scope ->
            when (config.tooltip) {
                LabTooltip.Custom -> TripTooltip(scope)
                // The ready-made tooltip of waypoint-material3, themed by MaterialTheme.
                LabTooltip.Material3 -> WaypointMaterial3Tooltip(stepScope = scope)
            }
        },
    ) {
        LabPreview(state = state)
    }
}

@Composable
private fun RunControls(state: WaypointState<LabTarget>) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        KitButton(
            text = if (state.isActive) "Stop tour" else "Start tour",
            onClick = { if (state.isActive) state.stop() else state.start() },
        )
        KitButton(
            text = if (state.isPaused) "Resume" else "Pause",
            style = KitButtonStyle.Soft,
            enabled = state.isActive,
            onClick = { if (state.isPaused) state.resume() else state.pause() },
        )
        Box(modifier = Modifier.weight(1f))
        repeat(state.steps.size) { index ->
            KitChip(
                text = "${index + 1}",
                selected = state.currentStepIndex == index,
                enabled = state.isActive && !state.isPaused,
                onClick = { state.goToStep(index) },
            )
        }
    }
}

@Composable
private fun EventLogCard(
    state: WaypointState<LabTarget>,
    log: EventLog,
) {
    val colors = SampleTheme.colors
    SectionTitle("Events")
    KitCard(modifier = Modifier.fillMaxWidth()) {
        if (log.events.isEmpty()) {
            Text(
                text = "Start the tour to see the analytics callbacks.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.inkMuted,
            )
        }
        log.events.forEach { event ->
            Text(
                text = event,
                style = MaterialTheme.typography.bodySmall,
                color = colors.ink,
                modifier = Modifier.padding(vertical = 2.dp),
            )
        }
        Text(
            text = "ended: ${state.lastEndReason ?: "not yet"}",
            style = MaterialTheme.typography.labelMedium,
            color = colors.inkMuted,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}
