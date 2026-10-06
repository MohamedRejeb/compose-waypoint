package com.mohamedrejeb.waypoint.sample.lab

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.waypoint.core.WaypointState
import com.mohamedrejeb.waypoint.core.waypointTarget
import com.mohamedrejeb.waypoint.sample.kit.KitButton
import com.mohamedrejeb.waypoint.sample.theme.SampleTheme

/**
 * The scene the Lab's tour runs on: three targets of different shapes. The
 * button counts its taps, which shows whether a step lets them through.
 */
@Composable
fun LabPreview(
    state: WaypointState<LabTarget>,
    modifier: Modifier = Modifier,
) {
    val colors = SampleTheme.colors
    var taps by remember { mutableIntStateOf(0) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 36.dp, start = 24.dp, end = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Box(
            modifier = Modifier
                .waypointTarget(state, LabTarget.Avatar)
                .size(64.dp)
                .clip(CircleShape)
                .background(colors.accent),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.Flight,
                contentDescription = null,
                tint = colors.onAccent,
                modifier = Modifier.size(28.dp),
            )
        }
        Text(
            text = "Weekend in Porto",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = colors.ink,
            modifier = Modifier.waypointTarget(state, LabTarget.Title),
        )
        KitButton(
            text = if (taps == 0) "Book" else "Booked $taps",
            onClick = { taps++ },
            modifier = Modifier.waypointTarget(state, LabTarget.Action),
        )
    }
}
