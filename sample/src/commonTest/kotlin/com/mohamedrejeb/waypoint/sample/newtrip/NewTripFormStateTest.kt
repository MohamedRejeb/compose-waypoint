package com.mohamedrejeb.waypoint.sample.newtrip

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NewTripFormStateTest {

    @Test
    fun nameNeedsTwoRealCharacters() {
        val form = NewTripFormState()
        form.name = " a "
        assertFalse(form.isNameValid)
        form.name = "ab"
        assertTrue(form.isNameValid)
    }

    @Test
    fun destinationMustNotBeBlank() {
        val form = NewTripFormState()
        form.destination = "   "
        assertFalse(form.isDestinationValid)
        form.destination = "Porto"
        assertTrue(form.isDestinationValid)
    }

    @Test
    fun resetRestoresEveryField() {
        val form = NewTripFormState().apply {
            name = "Trip"
            destination = "Porto"
            style = TravelStyle.Packed
            routeLoading = true
            routeVisible = true
            created = true
        }
        form.reset()
        assertEquals("", form.name)
        assertEquals("", form.destination)
        assertNull(form.style)
        assertFalse(form.routeLoading)
        assertFalse(form.routeVisible)
        assertFalse(form.created)
    }
}
