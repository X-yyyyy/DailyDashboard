package com.dailydashboard.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dailydashboard.app.navigation.AppNavGraph
import com.dailydashboard.app.navigation.Screen
import com.dailydashboard.app.navigation.bottomNavItems

private val tabRoutes = setOf(
    Screen.Dashboard::class.qualifiedName,
    Screen.Calendar::class.qualifiedName,
    Screen.Course::class.qualifiedName,
    Screen.Csgo::class.qualifiedName,
    Screen.Settings::class.qualifiedName,
)

@Composable
fun MainScaffold(isLoggedIn: Boolean) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in tabRoutes

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
            ) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val route = item.screen::class.qualifiedName
                        val isSelected = currentRoute == route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(item.screen) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        AppNavGraph(
            navController = navController,
            isLoggedIn = isLoggedIn,
            modifier = Modifier.padding(innerPadding),
        )
    }
}
