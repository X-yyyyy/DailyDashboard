package com.dailydashboard.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailydashboard.app.data.model.Course
import com.dailydashboard.app.data.model.TimeSlot
import com.dailydashboard.app.data.repository.CourseRepository
import com.dailydashboard.app.data.repository.TimeSlotRepository
import com.dailydashboard.app.data.repository.SemesterRepository
import com.dailydashboard.app.util.SemesterCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CourseUiState(
    val isLoading: Boolean = true,
    val courses: List<Course> = emptyList(),
    val timeSlots: List<TimeSlot> = emptyList(),
    val semesterStartDate: String = "",
    val semesterTotalWeeks: Int = 20,
    val currentWeek: Int = 1,
)

class CourseViewModel(
    private val courseRepository: CourseRepository,
    private val timeSlotRepository: TimeSlotRepository,
    private val semesterRepository: SemesterRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CourseUiState())
    val uiState: StateFlow<CourseUiState> = _uiState

    private var userId = ""
    private var idToken = ""

    fun init(userId: String, idToken: String) {
        this.userId = userId
        this.idToken = idToken
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            courseRepository.refresh(userId, idToken)
            timeSlotRepository.refresh(userId, idToken)
            semesterRepository.refresh(userId, idToken)

            val semester = semesterRepository.semester.value
            _uiState.value = CourseUiState(
                isLoading = false,
                courses = courseRepository.items.value,
                timeSlots = timeSlotRepository.items.value,
                semesterStartDate = semester.startDate,
                semesterTotalWeeks = semester.totalWeeks,
                currentWeek = SemesterCalculator.currentWeek(semester.startDate, semester.totalWeeks),
            )
        }
    }
}
