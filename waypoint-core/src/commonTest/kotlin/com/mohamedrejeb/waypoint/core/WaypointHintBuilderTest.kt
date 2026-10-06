package com.mohamedrejeb.waypoint.core

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WaypointHintBuilderTest {

    private enum class HintKey { Search, Filter, Profile }

    // -- DSL correctness --

    @Test
    fun `hint DSL sets title and description`() {
        val scope = WaypointHintScope<HintKey>()
        scope.hint(HintKey.Search) {
            title = "Search"
            description = "Tap to filter"
        }

        val hint = scope.hints.single()

        assertEquals(HintKey.Search, hint.key)
        assertEquals("Search", hint.title)
        assertEquals("Tap to filter", hint.description)
    }

    @Test
    fun `hint DSL unspecified fields use defaults`() {
        val scope = WaypointHintScope<HintKey>()
        scope.hint(HintKey.Search)

        val hint = scope.hints.single()

        assertEquals(HintKey.Search, hint.key)
        assertNull(hint.title)
        assertNull(hint.description)
        assertEquals(TooltipPlacement.Auto, hint.placement)
        assertEquals(Alignment.TopEnd, hint.beaconAlignment)
        assertEquals(DpOffset.Zero, hint.beaconOffset)
        assertTrue(
            hint.beaconStyle is BeaconStyle.Pulse,
            "Default beacon style should be Pulse",
        )
    }

    @Test
    fun `hint DSL custom placement and beacon properties`() {
        val scope = WaypointHintScope<HintKey>()
        scope.hint(HintKey.Filter) {
            placement = TooltipPlacement.Top
            beaconAlignment = Alignment.BottomStart
            beaconOffset = DpOffset(4.dp, 8.dp)
            beaconStyle = BeaconStyle.Dot()
        }

        val hint = scope.hints.single()

        assertEquals(TooltipPlacement.Top, hint.placement)
        assertEquals(Alignment.BottomStart, hint.beaconAlignment)
        assertEquals(DpOffset(4.dp, 8.dp), hint.beaconOffset)
        assertTrue(hint.beaconStyle is BeaconStyle.Dot)
    }

    @Test
    fun `multiple hint calls collect hints in order`() {
        val scope = WaypointHintScope<HintKey>()
        scope.hint(HintKey.Search) { title = "S" }
        scope.hint(HintKey.Filter) { title = "F" }
        scope.hint(HintKey.Profile) { title = "P" }

        val hints = scope.hints

        assertEquals(3, hints.size)
        assertEquals(HintKey.Search, hints[0].key)
        assertEquals(HintKey.Filter, hints[1].key)
        assertEquals(HintKey.Profile, hints[2].key)
    }

    @Test
    fun `duplicate keys are all kept in the list`() {
        val scope = WaypointHintScope<HintKey>()
        scope.hint(HintKey.Search) { title = "first" }
        scope.hint(HintKey.Search) { title = "second" }

        val hints = scope.hints

        assertEquals(2, hints.size)
        assertEquals("first", hints[0].title)
        assertEquals("second", hints[1].title)
    }

    @Test
    fun `empty scope produces empty list`() {
        val scope = WaypointHintScope<HintKey>()

        assertTrue(scope.hints.isEmpty())
    }
}
