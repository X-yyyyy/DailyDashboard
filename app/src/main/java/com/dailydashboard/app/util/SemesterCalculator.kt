package com.dailydashboard.app.util

import java.time.LocalDate
import java.time.temporal.ChronoUnit

object SemesterCalculator {
    fun currentWeek(startDate: String, totalWeeks: Int = 20): Int {
        if (startDate.isBlank()) return 1
        val start = LocalDate.parse(startDate)
        val today = LocalDate.now()
        val weeks = ChronoUnit.WEEKS.between(start, today).toInt() + 1
        return weeks.coerceIn(1, totalWeeks)
    }

    fun shouldShowCourse(
        weekType: String,
        currentWeek: Int,
        customWeeks: List<Int> = emptyList(),
    ): Boolean = when (weekType) {
        "all" -> true
        "odd" -> currentWeek % 2 == 1
        "even" -> currentWeek % 2 == 0
        "custom" -> currentWeek in customWeeks
        else -> true
    }
}
