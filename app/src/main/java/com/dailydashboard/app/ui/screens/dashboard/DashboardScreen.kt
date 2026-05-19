package com.dailydashboard.app.ui.screens.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun DashboardScreen(navController: NavController) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("首页仪表盘")
    }
}
