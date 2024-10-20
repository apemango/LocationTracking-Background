package com.phone.tracker.ui.navigation


sealed class NavRoutes(val route: String) {
    object Home : NavRoutes("Home1Screen")
    object Details: NavRoutes("")
}
