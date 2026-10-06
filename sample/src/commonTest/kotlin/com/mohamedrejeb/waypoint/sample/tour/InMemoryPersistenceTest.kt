package com.mohamedrejeb.waypoint.sample.tour

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InMemoryPersistenceTest {

    @Test
    fun unknownTourIsNotCompleted() {
        assertFalse(InMemoryPersistence().isCompleted("a"))
    }

    @Test
    fun markCompletedIsRemembered() {
        val persistence = InMemoryPersistence()
        persistence.markCompleted("a")
        assertTrue(persistence.isCompleted("a"))
        assertFalse(persistence.isCompleted("b"))
    }

    @Test
    fun resetClearsOnlyThatTour() {
        val persistence = InMemoryPersistence()
        persistence.markCompleted("a")
        persistence.markCompleted("b")
        persistence.reset("a")
        assertFalse(persistence.isCompleted("a"))
        assertTrue(persistence.isCompleted("b"))
    }

    @Test
    fun resetAllClearsEverything() {
        val persistence = InMemoryPersistence()
        persistence.markCompleted("a")
        persistence.markCompleted("b")
        persistence.resetAll()
        assertFalse(persistence.isCompleted("a"))
        assertFalse(persistence.isCompleted("b"))
    }

    @Test
    fun restoresFromSavedIds() {
        val saved = InMemoryPersistence().apply { markCompleted("a") }.completedIds
        val restored = InMemoryPersistence(saved)
        assertTrue(restored.isCompleted("a"))
        assertFalse(restored.isCompleted("b"))
    }
}
