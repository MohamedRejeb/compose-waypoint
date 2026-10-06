package com.mohamedrejeb.waypoint.sample.data

import androidx.compose.runtime.Immutable

/** A stop on a trip. [x] and [y] place it on the route map, both in 0..1. */
@Immutable
data class Stop(
    val name: String,
    val stay: String,
    val x: Float,
    val y: Float,
)

@Immutable
data class Trip(
    val id: String,
    val title: String,
    val dates: String,
    val stops: List<Stop>,
)

val SampleTrips: List<Trip> = listOf(
    Trip(
        id = "lisbon-porto",
        title = "Lisbon to Porto",
        dates = "12 - 20 May",
        stops = listOf(
            Stop("Lisbon", "2 nights", 0.06f, 0.86f),
            Stop("Sintra", "Day stop", 0.14f, 0.66f),
            Stop("Óbidos", "Day stop", 0.22f, 0.74f),
            Stop("Nazaré", "1 night", 0.30f, 0.54f),
            Stop("Tomar", "Day stop", 0.38f, 0.62f),
            Stop("Coimbra", "1 night", 0.46f, 0.44f),
            Stop("Aveiro", "Day stop", 0.54f, 0.52f),
            Stop("Viseu", "1 night", 0.62f, 0.34f),
            Stop("Douro Valley", "2 nights", 0.70f, 0.42f),
            Stop("Amarante", "Day stop", 0.78f, 0.26f),
            Stop("Guimarães", "1 night", 0.86f, 0.32f),
            Stop("Porto", "2 nights", 0.94f, 0.14f),
        ),
    ),
    Trip(
        id = "amalfi",
        title = "Amalfi Coast",
        dates = "3 - 9 June",
        stops = listOf(
            Stop("Naples", "1 night", 0.12f, 0.30f),
            Stop("Sorrento", "2 nights", 0.40f, 0.62f),
            Stop("Positano", "2 nights", 0.64f, 0.44f),
            Stop("Amalfi", "1 night", 0.88f, 0.66f),
        ),
    ),
    Trip(
        id = "atlas",
        title = "Atlas Mountains",
        dates = "18 - 24 September",
        stops = listOf(
            Stop("Marrakech", "2 nights", 0.14f, 0.70f),
            Stop("Imlil", "2 nights", 0.46f, 0.36f),
            Stop("Aït Benhaddou", "1 night", 0.84f, 0.58f),
        ),
    ),
)
