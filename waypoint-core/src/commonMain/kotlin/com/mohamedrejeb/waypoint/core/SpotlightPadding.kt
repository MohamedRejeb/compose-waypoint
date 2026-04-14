package com.mohamedrejeb.waypoint.core

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Padding around the target element within the spotlight cutout.
 */
@Immutable
public data class SpotlightPadding(
    val start: Dp = 4.dp,
    val top: Dp = 4.dp,
    val end: Dp = 4.dp,
    val bottom: Dp = 4.dp,
) {
    public constructor(all: Dp) : this(start = all, top = all, end = all, bottom = all)

    public constructor(horizontal: Dp, vertical: Dp) : this(
        start = horizontal,
        top = vertical,
        end = horizontal,
        bottom = vertical,
    )

    public companion object {
        public val Default: SpotlightPadding = SpotlightPadding(all = 4.dp)
        public val None: SpotlightPadding = SpotlightPadding(all = 0.dp)
    }
}
