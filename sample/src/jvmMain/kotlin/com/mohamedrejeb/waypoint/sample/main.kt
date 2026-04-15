package com.mohamedrejeb.waypoint.sample

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Waypoint Sample") {
        App()
    }
}
