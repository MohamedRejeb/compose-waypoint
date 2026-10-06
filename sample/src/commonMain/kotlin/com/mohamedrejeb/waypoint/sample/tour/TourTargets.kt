package com.mohamedrejeb.waypoint.sample.tour

/** Targets of chapter 1, on the Trips screen. */
enum class TripsTarget { Search, FirstTrip, NewTrip, ThemeSwitch, Checklist }

/** Targets of chapter 2, on the New trip screen. */
enum class NewTripTarget { Name, Destination, Style, Route, Create }

/** Targets of chapter 3, on the Trip screen and inside its sheet. */
enum class TripTarget { MapStop, StopRow, FarStop, AddStop, SheetName, SheetStay, Share }

/** Standalone hints on the Trips app bar. */
enum class TripsHint { Search, Filter }
