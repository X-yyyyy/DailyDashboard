package com.dailydashboard.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TodoItem(
    val id: String = "",
    val content: String = "",
    val done: Boolean = false,
    @SerialName("dueDate") val dueDate: String? = null,
    @SerialName("createdAt") val createdAt: String? = null,
)
