package com.dailydashboard.app.ui.screens.csgo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun CsgoScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("CS:GO 赛事页面")
    }
}
