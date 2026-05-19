package com.dailydashboard.app.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    // Auth
    @Serializable data object Login : Screen

    // Main tabs
    @Serializable data object Dashboard : Screen
    @Serializable data object Calendar : Screen
    @Serializable data object Course : Screen
    @Serializable data object Csgo : Screen
    @Serializable data object Settings : Screen

    // Sub pages
    @Serializable data object TodoList : Screen
    @Serializable data object ImportantDates : Screen
    @Serializable data class CourseEdit(val courseId: String? = null) : Screen
}
