package com.mohamedrejeb.waypoint

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform