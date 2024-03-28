package com.karis.travellingsalesman.data.network.api

import com.karis.travellingsalesman.BuildConfig
import com.karis.travellingsalesman.data.network.models.responses.GeometryResponse
import com.karis.travellingsalesman.data.network.models.responses.PlacesResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface PlacesApiService {
    @GET("maps/api/place/autocomplete/json")
    suspend fun fetchPlaces(
        @Query("key") key: String,
        @Query("input") input: String
    ): PlacesResponse

    @GET("maps/api/place/findplacefromtext/json")
    suspend fun fetchPlaceGeometry(
        @Query("input") input: String,
        @Query("key") key: String = BuildConfig.MAPS_API_KEY,
        @Query("fields") inputType: String = "geometry",
        @Query("inputtype") fields: String = "textquery"
    ): GeometryResponse
}