package com.mohamedrejeb.waypoint.sample.trips

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.waypoint.sample.kit.KitButton
import com.mohamedrejeb.waypoint.sample.kit.KitCard
import com.mohamedrejeb.waypoint.sample.kit.KitIconButton
import com.mohamedrejeb.waypoint.sample.kit.ListRow
import com.mohamedrejeb.waypoint.sample.theme.SampleTheme
import com.mohamedrejeb.waypoint.sample.tour.Chapter
import com.mohamedrejeb.waypoint.sample.tour.SampleTours

private enum class ChapterStatus(val label: String) {
    Completed("completed"),
    Running("in progress"),
    Pending("not started"),
}

/**
 * The onboarding checklist. Each row is one tour of the sequence: its
 * completion comes from the tour's persistence, and Continue starts the
 * sequence at the first chapter that is not done yet.
 */
@Composable
fun GettingStartedCard(
    tours: SampleTours,
    onStartChapter: (Chapter) -> Unit,
    onContinue: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SampleTheme.colors
    val done = tours.chapters.count { it.state.hasCompleted }
    KitCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Getting started",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.ink,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "$done of ${tours.chapters.size}",
                style = MaterialTheme.typography.labelLarge,
                color = colors.inkMuted,
            )
        }
        Spacer(Modifier.height(14.dp))
        tours.chapters.forEach { chapter ->
            ChapterRow(
                chapter = chapter,
                status = when {
                    chapter.state.hasCompleted -> ChapterStatus.Completed
                    chapter.state.isActive -> ChapterStatus.Running
                    else -> ChapterStatus.Pending
                },
                onStart = { onStartChapter(chapter) },
            )
            Spacer(Modifier.height(12.dp))
        }
        Spacer(Modifier.height(2.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            KitButton(
                text = if (tours.sequence.isCompleted) "All done" else "Continue",
                enabled = !tours.sequence.isCompleted,
                onClick = onContinue,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "Reset progress",
                style = MaterialTheme.typography.labelLarge,
                color = colors.inkMuted,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onReset)
                    .padding(horizontal = 12.dp, vertical = 12.dp),
            )
        }
    }
}

@Composable
private fun ChapterRow(
    chapter: Chapter,
    status: ChapterStatus,
    onStart: () -> Unit,
) {
    val completed = status == ChapterStatus.Completed
    ListRow(
        title = chapter.title,
        subtitle = chapter.subtitle,
        leading = { StatusMark(status = status, label = "${chapter.title} ${status.label}") },
        trailing = {
            KitIconButton(
                icon = if (completed) Icons.Rounded.Replay else Icons.Rounded.PlayArrow,
                contentDescription = "Play ${chapter.title}",
                onClick = onStart,
                container = SampleTheme.colors.background,
            )
        },
    )
}

@Composable
private fun StatusMark(status: ChapterStatus, label: String) {
    val colors = SampleTheme.colors
    val shapeModifier = when (status) {
        ChapterStatus.Completed -> Modifier.background(colors.accent, CircleShape)
        ChapterStatus.Running -> Modifier.border(2.dp, colors.accent, CircleShape)
        ChapterStatus.Pending -> Modifier.border(2.dp, colors.inkMuted, CircleShape)
    }
    Box(
        modifier = Modifier
            .size(28.dp)
            .then(shapeModifier)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        if (status == ChapterStatus.Completed) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = colors.onAccent,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
