package com.mohamedrejeb.waypoint.core

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The shared [padBounds] helper must resolve start/end padding against the
 * layout direction, consistently for every highlight renderer.
 */
class PadBoundsTest {

    private val density = Density(1f)
    private val bounds = Rect(100f, 100f, 200f, 200f)
    private val padding = SpotlightPadding(start = 10.dp, top = 2.dp, end = 20.dp, bottom = 4.dp)

    @Test
    fun `LTR maps start to left and end to right`() {
        val padded = padBounds(bounds, padding, density, LayoutDirection.Ltr)

        assertEquals(90f, padded.left)
        assertEquals(98f, padded.top)
        assertEquals(220f, padded.right)
        assertEquals(204f, padded.bottom)
    }

    @Test
    fun `RTL maps start to right and end to left`() {
        val padded = padBounds(bounds, padding, density, LayoutDirection.Rtl)

        assertEquals(80f, padded.left, "end padding applies to the left in RTL")
        assertEquals(98f, padded.top)
        assertEquals(210f, padded.right, "start padding applies to the right in RTL")
        assertEquals(204f, padded.bottom)
    }

    @Test
    fun `density scales padding`() {
        val padded = padBounds(bounds, padding, Density(2f), LayoutDirection.Ltr)

        assertEquals(80f, padded.left)
        assertEquals(96f, padded.top)
        assertEquals(240f, padded.right)
        assertEquals(208f, padded.bottom)
    }
}
