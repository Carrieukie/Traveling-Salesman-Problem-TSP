package com.karis.travellingsalesman.data.network.api

import com.karis.travellingsalesman.data.network.models.responses.PlacesResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface PlacesApiService {
    @GET("maps/api/place/autocomplete/json")
    suspend fun fetchPlaces(
        @Query("key") key: String,
        @Query("input") input: String
    ): PlacesResponse
}