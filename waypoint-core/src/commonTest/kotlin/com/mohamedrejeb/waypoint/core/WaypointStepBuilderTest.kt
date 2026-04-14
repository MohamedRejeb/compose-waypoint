package com.mohamedrejeb.waypoint.core

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WaypointStepBuilderTest {

    @Test
    fun `builds steps in order`() {
        val builder = WaypointStepBuilder<String>()
        builder.step("a")
        builder.step("b")
        builder.step("c")

        val steps = builder.build()

        assertEquals(3, steps.size)
        assertEquals("a", steps[0].targetKey)
        assertEquals("b", steps[1].targetKey)
        assertEquals("c", steps[2].targetKey)
    }

    @Test
    fun `step with title and description`() {
        val builder = WaypointStepBuilder<String>()
        builder.step("target") {
            title = "Welcome"
            description = "This is a tour"
        }

        val step = builder.build().single()

        assertEquals("Welcome", step.title)
        assertEquals("This is a tour", step.description)
    }

    @Test
    fun `step with custom placement`() {
        val builder = WaypointStepBuilder<String>()
        builder.step("target") {
            placement = TooltipPlacement.Top
        }

        val step = builder.build().single()

        assertEquals(TooltipPlacement.Top, step.placement)
    }

    @Test
    fun `step with highlight style spotlight circle`() {
        val builder = WaypointStepBuilder<String>()
        builder.step("target") {
            highlightStyle = HighlightStyle.Spotlight(shape = SpotlightShape.Circle)
        }

        val step = builder.build().single()
        val style = step.highlightStyle

        assertTrue(style is HighlightStyle.Spotlight)
        assertEquals(SpotlightShape.Circle, style.shape)
    }

    @Test
    fun `step with highlight style spotlight rounded rect`() {
        val builder = WaypointStepBuilder<String>()
        builder.step("target") {
            highlightStyle = HighlightStyle.Spotlight(
                shape = SpotlightShape.RoundedRect(cornerRadius = 16.dp),
            )
        }

        val step = builder.build().single()
        val style = step.highlightStyle

        assertTrue(style is HighlightStyle.Spotlight)
        val shape = style.shape
        assertTrue(shape is SpotlightShape.RoundedRect)
        assertEquals(16.dp, shape.cornerRadius)
    }

    @Test
    fun `step with highlight style spotlight custom padding`() {
        val builder = WaypointStepBuilder<String>()
        builder.step("target") {
            highlightStyle = HighlightStyle.Spotlight(
                padding = SpotlightPadding(all = 12.dp),
            )
        }

        val step = builder.build().single()
        val style = step.highlightStyle

        assertTrue(style is HighlightStyle.Spotlight)
        assertEquals(SpotlightPadding(12.dp, 12.dp, 12.dp, 12.dp), style.padding)
    }

    @Test
    fun `step with highlight style pulse`() {
        val builder = WaypointStepBuilder<String>()
        builder.step("target") {
            highlightStyle = HighlightStyle.Pulse(color = androidx.compose.ui.graphics.Color.Blue)
        }

        val step = builder.build().single()

        assertTrue(step.highlightStyle is HighlightStyle.Pulse)
    }

    @Test
    fun `step with highlight style none`() {
        val builder = WaypointStepBuilder<String>()
        builder.step("target") {
            highlightStyle = HighlightStyle.None
        }

        val step = builder.build().single()

        assertEquals(HighlightStyle.None, step.highlightStyle)
    }

    @Test
    fun `step with interaction override`() {
        val builder = WaypointStepBuilder<String>()
        builder.step("target") {
            interaction = TargetInteraction.ClickToAdvance
        }

        val step = builder.build().single()

        assertEquals(TargetInteraction.ClickToAdvance, step.interaction)
    }

    @Test
    fun `step with showIf condition`() {
        var flag = true
        val builder = WaypointStepBuilder<String>()
        builder.step("target") {
            showIf { flag }
        }

        val step = builder.build().single()

        assertEquals(true, step.showIf?.invoke())
        flag = false
        assertEquals(false, step.showIf?.invoke())
    }

    @Test
    fun `step with callbacks`() {
        var entered = false
        var exited = false
        val builder = WaypointStepBuilder<String>()
        builder.step("target") {
            onEnter { entered = true }
            onExit { exited = true }
        }

        val step = builder.build().single()

        step.onEnter?.invoke()
        assertTrue(entered)

        step.onExit?.invoke()
        assertTrue(exited)
    }

    @Test
    fun `empty builder produces empty list`() {
        val builder = WaypointStepBuilder<String>()

        val steps = builder.build()

        assertTrue(steps.isEmpty())
    }

    @Test
    fun `step defaults have correct values`() {
        val builder = WaypointStepBuilder<String>()
        builder.step("target")

        val step = builder.build().single()

        assertEquals("target", step.targetKey)
        assertNull(step.title)
        assertNull(step.description)
        assertNull(step.content)
        assertEquals(TooltipPlacement.Auto, step.placement)
        assertEquals(HighlightStyle.Default, step.highlightStyle)
        assertEquals(TargetInteraction.None, step.interaction)
        assertNull(step.showIf)
        assertNull(step.onEnter)
        assertNull(step.onExit)
    }
}
