package com.mohamedrejeb.waypoint.core

@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "This Waypoint API is experimental and may change without notice."
)
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.TYPEALIAS,
)
public annotation class ExperimentalWaypointApi
