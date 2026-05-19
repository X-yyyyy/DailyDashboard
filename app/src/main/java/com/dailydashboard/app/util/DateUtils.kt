package com.dailydashboard.app.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object DateUtils {
    fun today(): LocalDate = LocalDate.now()
    fun todayString(): String = today().format(DateTimeFormatter.ISO_DATE)
    fun formatDate(date: LocalDate): String = date.format(DateTimeFormatter.ofPattern("MM/dd"))
    fun formatDateFull(date: LocalDate): String = date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))

    fun parseDate(dateStr: String): LocalDate = LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE)

    fun currentDayOfWeek(): Int = today().dayOfWeek.value

    fun daysUntil(target: LocalDate): Long = ChronoUnit.DAYS.between(today(), target)

    fun isSameDay(d1: LocalDate, d2: LocalDate): Boolean = d1 == d2

    val chineseDayNames = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
}
