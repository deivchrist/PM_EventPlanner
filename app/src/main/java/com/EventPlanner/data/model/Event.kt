package com.EventPlanner.data.model

data class Event(
    val id: String = "",
    val userId: String = "", // Para filtrar por usuario
    val title: String = "",
    val date: String = "",
    val description: String = ""
)