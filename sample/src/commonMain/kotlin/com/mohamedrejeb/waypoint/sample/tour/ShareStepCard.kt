package com.mohamedrejeb.waypoint.sample.tour

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.waypoint.core.StepScope
import com.mohamedrejeb.waypoint.sample.theme.SampleTheme

/** Per-step custom content: the closing card of chapter 3. */
@Composable
internal fun ShareStepCard(scope: StepScope) {
    val colors = SampleTheme.colors
    TooltipCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.tooltipActionContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Celebration,
                    contentDescription = null,
                    tint = colors.tooltipActionContent,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = "Ready to share",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = colors.tooltipContent,
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Send the plan to your travel companions from here. " +
                "This card is this step's own content, not the shared tooltip.",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.tooltipContent.copy(alpha = 0.85f),
        )
        Spacer(Modifier.height(14.dp))
        TooltipPill(
            text = "Done",
            onClick = scope::next,
            modifier = Modifier.align(Alignment.End),
        )
    }
}
