package com.mohamedrejeb.waypoint.core

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

class RtlPositionTest {

    // Standard test values (matching PositionProviderTest)
    private val windowSize = IntSize(1000, 800)
    private val tooltipSize = IntSize(200, 100)
    private val spacing = 12f
    private val margin = 16f

    // Target in the middle — plenty of space on all sides
    private val centeredTarget = Rect(400f, 300f, 500f, 350f)

    private fun createProvider(
        targetBounds: Rect = centeredTarget,
        placement: TooltipPlacement = TooltipPlacement.Auto,
    ) = WaypointPositionProvider(
        targetBounds = targetBounds,
        requestedPlacement = placement,
        spacingPx = spacing,
        screenMarginPx = margin,
    )

    private fun WaypointPositionProvider.calculate(
        layoutDirection: LayoutDirection = LayoutDirection.Ltr,
    ) = calculatePosition(
        anchorBounds = IntRect.Zero,
        windowSize = windowSize,
        layoutDirection = layoutDirection,
        popupContentSize = tooltipSize,
    )

    // -- Start placement --

    @Test
    fun `Start placement positions tooltip to the left in LTR`() {
        val provider = createProvider(placement = TooltipPlacement.Start)

        val offset = provider.calculate(LayoutDirection.Ltr)

        assertEquals(ResolvedPlacement.Start, provider.resolvedPlacement)
        val expectedX = (centeredTarget.left - tooltipSize.width - spacing).toInt()
        assertEquals(expectedX, offset.x)
    }

    @Test
    fun `Start placement positions tooltip to the right in RTL`() {
        val provider = createProvider(placement = TooltipPlacement.Start)

        val offset = provider.calculate(LayoutDirection.Rtl)

        assertEquals(ResolvedPlacement.Start, provider.resolvedPlacement)
        val expectedX = (centeredTarget.right + spacing).toInt()
        assertEquals(expectedX, offset.x)
    }

    // -- End placement --

    @Test
    fun `End placement positions tooltip to the right in LTR`() {
        val provider = createProvider(placement = TooltipPlacement.End)

        val offset = provider.calculate(LayoutDirection.Ltr)

        assertEquals(ResolvedPlacement.End, provider.resolvedPlacement)
        val expectedX = (centeredTarget.right + spacing).toInt()
        assertEquals(expectedX, offset.x)
    }

    @Test
    fun `End placement positions tooltip to the left in RTL`() {
        val provider = createProvider(placement = TooltipPlacement.End)

        val offset = provider.calculate(LayoutDirection.Rtl)

        assertEquals(ResolvedPlacement.End, provider.resolvedPlacement)
        val expectedX = (centeredTarget.left - tooltipSize.width - spacing).toInt()
        assertEquals(expectedX, offset.x)
    }

    // -- Auto placement in RTL --

    @Test
    fun `Auto placement picks Start in RTL when target is near left edge`() {
        // Target near left edge: in RTL, spaceStart = windowWidth - right = 1000 - 60 = 940
        // spaceEnd = target.left = 10
        // Start has far more space → should pick Start
        val leftEdgeTarget = Rect(10f, 350f, 60f, 400f)
        val provider = createProvider(
            targetBounds = leftEdgeTarget,
            placement = TooltipPlacement.Auto,
        )

        provider.calculate(LayoutDirection.Rtl)

        assertEquals(ResolvedPlacement.Start, provider.resolvedPlacement)
    }

    // -- Vertical centering preserved in RTL --

    @Test
    fun `Start placement vertically centers tooltip on target in RTL`() {
        val provider = createProvider(placement = TooltipPlacement.Start)

        val offset = provider.calculate(LayoutDirection.Rtl)

        val expectedY = (centeredTarget.center.y - tooltipSize.height / 2f).toInt()
        assertEquals(expectedY, offset.y)
    }

    @Test
    fun `End placement vertically centers tooltip on target in RTL`() {
        val provider = createProvider(placement = TooltipPlacement.End)

        val offset = provider.calculate(LayoutDirection.Rtl)

        val expectedY = (centeredTarget.center.y - tooltipSize.height / 2f).toInt()
        assertEquals(expectedY, offset.y)
    }

    // -- RTL padding (SpotlightPadding) --

    @Test
    fun `SpotlightPadding default has symmetric start and end`() {
        val padding = SpotlightPadding.Default
        assertEquals(padding.start, padding.end)
    }

    @Test
    fun `SpotlightPadding preserves asymmetric values`() {
        val padding = SpotlightPadding(start = 8.dp, top = 4.dp, end = 16.dp, bottom = 4.dp)
        assertEquals(8.dp, padding.start)
        assertEquals(16.dp, padding.end)
        assertEquals(4.dp, padding.top)
        assertEquals(4.dp, padding.bottom)
    }
}
