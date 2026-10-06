package com.mohamedrejeb.waypoint.sample.kit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.waypoint.sample.theme.Dimens
import com.mohamedrejeb.waypoint.sample.theme.SampleTheme

/**
 * Bottom sheet in the sample's colors. It has no built-in drag handle: the
 * content draws its own with [SheetHandle], so everything in the sheet is
 * part of the content and a tour hosted there can dim all of it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitSheet(
    onDismiss: () -> Unit,
    scrimColor: Color = BottomSheetDefaults.ScrimColor,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = SampleTheme.colors
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.background,
        contentColor = colors.ink,
        scrimColor = scrimColor,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = Dimens.RadiusLarge, topEnd = Dimens.RadiusLarge),
        content = content,
    )
}

/** The grab bar at the top of a sheet. */
@Composable
fun SheetHandle(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(top = 12.dp, bottom = 8.dp)
            .size(width = 36.dp, height = 4.dp)
            .clip(CircleShape)
            .background(SampleTheme.colors.outline),
    )
}
