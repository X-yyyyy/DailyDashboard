package com.dailydashboard.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TimeSlot(
    val id: String = "",
    val slot: Int = 0,
    @SerialName("startTime") val startTime: String = "",
    @SerialName("endTime") val endTime: String = "",
)
