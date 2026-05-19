package com.dailydashboard.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CalendarEvent(
    val id: String = "",
    val title: String = "",
    val date: String = "",
    val time: String? = null,
    val note: String? = null,
)
