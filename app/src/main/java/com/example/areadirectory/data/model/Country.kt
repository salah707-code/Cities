package com.example.areadirectory.data.model

data class Country(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val altNames: List<String> = emptyList(),
    val governorates: List<Governorate> = emptyList()
)
