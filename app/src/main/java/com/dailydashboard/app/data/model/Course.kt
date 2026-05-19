package com.dailydashboard.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Course(
    val id: String = "",
    val name: String = "",
    val teacher: String = "",
    val location: String = "",
    val type: String = "",
    val color: String = "",
    val schedules: List<Schedule> = emptyList(),
)

@Serializable
data class Schedule(
    @SerialName("dayOfWeek") val dayOfWeek: Int = 1,
    @SerialName("startSlot") val startSlot: Int = 1,
    val duration: Int = 1,
    @SerialName("weekType") val weekType: String = "all",
    @SerialName("customWeeks") val customWeeks: List<Int> = emptyList(),
)
