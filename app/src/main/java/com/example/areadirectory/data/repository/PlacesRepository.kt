package com.example.areadirectory.data.repository

import com.example.areadirectory.data.api.PlacesApi
import com.example.areadirectory.data.model.Place
import com.example.areadirectory.data.model.SearchRequest

interface PlacesRepository {
    suspend fun searchPlaces(
        district: String,
        governorate: String,
        country: String,
        category: String
    ): List<Place>
}

class PlacesRepositoryImpl(
    private val placesApi: PlacesApi
) : PlacesRepository {

    override suspend fun searchPlaces(
        district: String,
        governorate: String,
        country: String,
        category: String
    ): List<Place> {
        val request = SearchRequest(
            country = country.trim(),
            governorate = governorate.trim(),
            district = district.trim(),
            category = category.trim()
        )

        val response = placesApi.searchPlaces(request)
        val rawResults = response.results ?: emptyList()

        // Deduplicate using Place ID
        val seenIds = mutableSetOf<String>()
        val places = mutableListOf<Place>()

        for (item in rawResults) {
            val placeId = item.placeId?.trim()
            if (!placeId.isNullOrEmpty() && seenIds.add(placeId)) {
                val name = item.name?.trim()?.takeIf { it.isNotEmpty() } ?: "مكان غير مسمى"
                val address = item.address?.trim()?.takeIf { it.isNotEmpty() } ?: "$district، $governorate"
                val mapsUrl = item.mapsUrl?.trim()?.takeIf { it.isNotEmpty() }
                    ?: "https://www.google.com/maps/place/?q=place_id:$placeId"

                places.add(
                    Place(
                        id = placeId,
                        name = name,
                        address = address,
                        mapsUrl = mapsUrl
                    )
                )
            }
        }

        return places
    }
}
