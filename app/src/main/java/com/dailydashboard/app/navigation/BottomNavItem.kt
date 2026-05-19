package com.dailydashboard.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val screen: Screen,
)

val bottomNavItems = listOf(
    BottomNavItem("首页", Icons.Default.Home, Screen.Dashboard),
    BottomNavItem("日程", Icons.Default.CalendarMonth, Screen.Calendar),
    BottomNavItem("课表", Icons.Default.TableChart, Screen.Course),
    BottomNavItem("赛事", Icons.Default.VideogameAsset, Screen.Csgo),
    BottomNavItem("设置", Icons.Default.Settings, Screen.Settings),
)
