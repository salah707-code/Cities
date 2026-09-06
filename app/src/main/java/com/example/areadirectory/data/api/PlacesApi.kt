package com.example.areadirectory.data.api

import com.example.areadirectory.data.model.SearchRequest
import com.example.areadirectory.data.model.SearchResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface PlacesApi {
    @POST("search")
    suspend fun searchPlaces(@Body request: SearchRequest): SearchResponseDto
}
