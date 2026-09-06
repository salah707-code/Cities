package com.example.areadirectory.data.model

data class Governorate(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val altNames: List<String> = emptyList(),
    val districts: List<District> = emptyList()
)
