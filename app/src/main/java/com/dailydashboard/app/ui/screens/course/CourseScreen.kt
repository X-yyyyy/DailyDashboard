package com.dailydashboard.app.ui.screens.course

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dailydashboard.app.ui.components.common.LoadingIndicator
import com.dailydashboard.app.ui.components.course.CourseGrid
import com.dailydashboard.app.ui.viewmodel.CourseViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CourseScreen(
    viewModel: CourseViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedWeek by remember { mutableStateOf(1) }

    LaunchedEffect(Unit) {
        selectedWeek = uiState.currentWeek
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { if (selectedWeek > 1) selectedWeek-- }) {
                Icon(Icons.Default.ChevronLeft, "上一周")
            }
            Text(
                text = "第 ${selectedWeek} 周",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            IconButton(onClick = { if (selectedWeek < uiState.semesterTotalWeeks) selectedWeek++ }) {
                Icon(Icons.Default.ChevronRight, "下一周")
            }
        }

        if (uiState.isLoading) {
            LoadingIndicator()
        } else {
            CourseGrid(
                courses = uiState.courses,
                timeSlots = uiState.timeSlots,
                currentWeek = selectedWeek,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
