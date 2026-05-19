package com.dailydashboard.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.dailydashboard.app.ui.screens.auth.LoginScreen
import com.dailydashboard.app.ui.screens.calendar.CalendarScreen
import com.dailydashboard.app.ui.screens.course.CourseScreen
import com.dailydashboard.app.ui.screens.csgo.CsgoScreen
import com.dailydashboard.app.ui.screens.dashboard.DashboardScreen
import com.dailydashboard.app.ui.screens.importantdates.ImportantDatesScreen
import com.dailydashboard.app.ui.screens.settings.SettingsScreen
import com.dailydashboard.app.ui.screens.todo.TodoScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    isLoggedIn: Boolean,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) Screen.Dashboard else Screen.Login,
        modifier = modifier,
    ) {
        composable<Screen.Login> {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard) {
                        popUpTo(Screen.Login) { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.Dashboard> { DashboardScreen(navController) }
        composable<Screen.Calendar> { CalendarScreen() }
        composable<Screen.Course> { CourseScreen() }
        composable<Screen.Csgo> { CsgoScreen() }
        composable<Screen.Settings> { SettingsScreen() }
        composable<Screen.TodoList> { TodoScreen() }
        composable<Screen.ImportantDates> { ImportantDatesScreen() }

        composable<Screen.CourseEdit> { backStackEntry ->
            val args = backStackEntry.toRoute<Screen.CourseEdit>()
            // CourseEditScreen(courseId = args.courseId)
        }
    }
}
