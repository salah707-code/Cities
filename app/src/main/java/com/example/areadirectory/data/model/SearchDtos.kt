package com.example.areadirectory.data.model

import com.squareup.moshi.Json

data class SearchRequest(
    @Json(name = "country") val country: String,
    @Json(name = "governorate") val governorate: String,
    @Json(name = "district") val district: String,
    @Json(name = "category") val category: String
)

data class SearchResponseDto(
    @Json(name = "district") val district: String? = null,
    @Json(name = "category") val category: String? = null,
    @Json(name = "count") val count: Int? = null,
    @Json(name = "results") val results: List<PlaceDto>? = null,
    @Json(name = "error") val error: String? = null,
    @Json(name = "message") val message: String? = null
)

data class PlaceDto(
    @Json(name = "placeId") val placeId: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "address") val address: String? = null,
    @Json(name = "mapsUrl") val mapsUrl: String? = null
)

data class LocalDataDto(
    @Json(name = "countries") val countries: List<CountryDto>? = null,
    @Json(name = "categories") val categories: List<CategoryDto>? = null
)

data class CountryDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "nameAr") val nameAr: String? = null,
    @Json(name = "nameEn") val nameEn: String? = null,
    @Json(name = "altNames") val altNames: List<String>? = null,
    @Json(name = "governorates") val governorates: List<GovernorateDto>? = null
)

data class GovernorateDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "nameAr") val nameAr: String? = null,
    @Json(name = "nameEn") val nameEn: String? = null,
    @Json(name = "altNames") val altNames: List<String>? = null,
    @Json(name = "districts") val districts: List<DistrictDto>? = null
)

data class DistrictDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "nameAr") val nameAr: String? = null,
    @Json(name = "nameEn") val nameEn: String? = null,
    @Json(name = "altNames") val altNames: List<String>? = null
)

data class CategoryDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "nameAr") val nameAr: String? = null,
    @Json(name = "nameEn") val nameEn: String? = null,
    @Json(name = "googleQuery") val googleQuery: String? = null
)
