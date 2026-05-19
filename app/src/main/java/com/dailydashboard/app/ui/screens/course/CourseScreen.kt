package com.dailydashboard.app.ui.screens.course

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun CourseScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("课表页面")
    }
}
