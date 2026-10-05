package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Where [TooltipArrowBox] actually draws its arrow, checked on pixels: at the
 * geometry offset measured from the content's physical left edge, also under
 * RTL, and with a base as wide as [TooltipArrowBox]'s `arrowWidth` says.
 */
@OptIn(ExperimentalTestApi::class)
class TooltipArrowBoxPixelUiTest {

    private val arrowSize = 10.dp
    private val cardWidth = 200.dp
    private val cardHeight = 50.dp
    private val arrowColor = Color.Red

    private class Scene(val image: ImageBitmap, val density: Float) {
        fun px(dp: Dp): Int = (dp.value * density).toInt()
        fun isArrowAt(x: Int, y: Int): Boolean = image.toPixelMap()[x, y] == Color.Red
    }

    private fun runArrowScene(
        placement: ResolvedPlacement,
        arrowOffsetPx: Float,
        layoutDirection: LayoutDirection = LayoutDirection.Ltr,
        arrowWidth: Dp? = null,
        block: Scene.() -> Unit,
    ) = runComposeUiTest {
        var density = 1f
        setContent {
            density = LocalDensity.current.density
            CompositionLocalProvider(
                LocalLayoutDirection provides layoutDirection,
                LocalTooltipArrowGeometry provides TooltipArrowGeometry(placement, arrowOffsetPx),
            ) {
                Box(Modifier.wrapContentSize().background(Color.White).testTag("scene")) {
                    Card(arrowWidth)
                }
            }
        }
        waitForIdle()

        Scene(onNodeWithTag("scene").captureToImage(), density).block()
    }

    @Composable
    private fun Card(arrowWidth: Dp?) {
        if (arrowWidth == null) {
            TooltipArrowBox(arrowColor = arrowColor, arrowSize = arrowSize) {
                Box(Modifier.size(cardWidth, cardHeight).background(Color.Blue))
            }
        } else {
            TooltipArrowBox(arrowColor = arrowColor, arrowSize = arrowSize, arrowWidth = arrowWidth) {
                Box(Modifier.size(cardWidth, cardHeight).background(Color.Blue))
            }
        }
    }

    @Test
    fun `bottom placement draws the arrow at the offset in LTR`() = runArrowScene(
        placement = ResolvedPlacement.Bottom,
        arrowOffsetPx = 40f,
    ) {
        assertEquals(true, isArrowAt(40, px(arrowSize) / 2), "arrow tip missing at the offset")
        assertEquals(false, isArrowAt(px(cardWidth) - 40, px(arrowSize) / 2), "arrow drawn at the mirrored offset")
    }

    @Test
    fun `bottom placement draws the arrow at the same physical offset in RTL`() = runArrowScene(
        placement = ResolvedPlacement.Bottom,
        arrowOffsetPx = 40f,
        layoutDirection = LayoutDirection.Rtl,
    ) {
        assertEquals(true, isArrowAt(40, px(arrowSize) / 2), "arrow tip missing at the offset under RTL")
        assertEquals(false, isArrowAt(px(cardWidth) - 40, px(arrowSize) / 2), "arrow mirrored under RTL")
    }

    @Test
    fun `top placement draws the arrow below the card at the same physical offset in RTL`() = runArrowScene(
        placement = ResolvedPlacement.Top,
        arrowOffsetPx = 40f,
        layoutDirection = LayoutDirection.Rtl,
    ) {
        val y = px(cardHeight) + px(arrowSize) / 2
        assertEquals(true, isArrowAt(40, y), "arrow tip missing at the offset under RTL")
        assertEquals(false, isArrowAt(px(cardWidth) - 40, y), "arrow mirrored under RTL")
    }

    @Test
    fun `end placement keeps the arrow at the vertical offset in RTL`() = runArrowScene(
        placement = ResolvedPlacement.End,
        arrowOffsetPx = 15f,
        layoutDirection = LayoutDirection.Rtl,
    ) {
        // In RTL the row puts the arrow after the card, on its right edge,
        // pointing right at a target that sits to the right.
        val x = px(cardWidth) + 1
        assertEquals(true, isArrowAt(x, 15), "arrow missing on the right edge at the offset")
        assertEquals(false, isArrowAt(x, px(cardHeight) - 15), "arrow at the mirrored vertical offset")
    }

    @Test
    fun `default arrow base is twice the arrow size`() = runArrowScene(
        placement = ResolvedPlacement.Bottom,
        arrowOffsetPx = 100f,
    ) {
        val y = px(arrowSize) - 1
        assertEquals(true, isArrowAt(100 + px(arrowSize) / 2, y), "inside the default base")
        assertEquals(false, isArrowAt(100 + px(arrowSize) + px(arrowSize) / 2, y), "outside the default base")
    }

    @Test
    fun `arrowWidth widens the base without changing the protrusion`() = runArrowScene(
        placement = ResolvedPlacement.Bottom,
        arrowOffsetPx = 100f,
        arrowWidth = 40.dp,
    ) {
        val y = px(arrowSize) - 1
        assertEquals(true, isArrowAt(100 + px(arrowSize) + px(arrowSize) / 2, y), "inside the wide base")
        assertEquals(false, isArrowAt(100 + px(arrowSize) * 2 + px(arrowSize) / 2, y), "outside the wide base")
        assertEquals(true, image.height == px(arrowSize) + px(cardHeight), "protrusion changed: ${image.height}")
    }
}
