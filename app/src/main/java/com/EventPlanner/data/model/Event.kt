package com.EventPlanner.data.model

import com.google.firebase.Timestamp

data class Event(
    val id: String = "",
    val userId: String = "", // Para filtrar por usuario
    val title: String = "",
    val date: String = "",
    val description: String = "",
    val createdAt: Timestamp? = null
)