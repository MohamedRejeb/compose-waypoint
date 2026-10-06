package com.mohamedrejeb.waypoint.sample.navigation

import com.mohamedrejeb.waypoint.core.WaypointState
import com.mohamedrejeb.waypoint.sample.data.SampleTrips
import com.mohamedrejeb.waypoint.sample.tour.SampleTours

/** The screen a chapter is started on. */
internal fun SampleTours.routeOf(tour: WaypointState<*>): Route = when (tour) {
    planTrip -> Route.NewTrip
    knowTrip -> Route.Trip(SampleTrips.first().id)
    else -> Route.Trips
}

private fun SampleTours.isHostedOn(tour: WaypointState<*>, route: Route): Boolean = when (tour) {
    planTrip -> route is Route.NewTrip
    knowTrip -> route is Route.Trip
    else -> route is Route.Trips
}

/**
 * Ends every running chapter whose screen is not [route]. A tour only makes
 * sense on the screen that hosts it, so navigating away ends it, and
 * WaypointSequenceEffect then ends the sequence too.
 */
internal fun SampleTours.stopToursNotOn(route: Route) {
    chapters
        .map { it.state }
        .filter { it.isActive && !isHostedOn(it, route) }
        .forEach { it.stop() }
}
