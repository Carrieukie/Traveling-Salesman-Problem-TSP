package com.karis.travellingsalesman.data.network.api

import com.karis.travellingsalesman.BuildConfig
import com.karis.travellingsalesman.data.network.models.requests.GetPolyLineRequest
import com.karis.travellingsalesman.data.network.models.responses.GetPolylineResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface RoutesApiService {
    @POST("directions/v2:computeRoutes")
    suspend fun getPolyline(
        @Header("X-Goog-Api-Key") apiKey: String = BuildConfig.MAPS_API_KEY,
        @Header("X-Goog-FieldMask") fieldMask: String =
            "routes.duration,routes.distanceMeters,routes.polyline.encodedPolyline",
        @Body getPolyLineRequest: GetPolyLineRequest
    ): GetPolylineResponse
}

