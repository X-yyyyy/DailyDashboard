package com.dailydashboard.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Semester(
    val id: String = "current",
    val name: String = "",
    @SerialName("startDate") val startDate: String = "",
    @SerialName("totalWeeks") val totalWeeks: Int = 20,
)
