package com.example.areadirectory.data.model

data class District(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val altNames: List<String> = emptyList()
)
