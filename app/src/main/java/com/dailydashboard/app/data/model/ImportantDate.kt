package com.dailydashboard.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ImportantDate(
    val id: String = "",
    val name: String = "",
    val date: String = "",
    val color: String = "",
    val type: String = "",
    val note: String? = null,
)
