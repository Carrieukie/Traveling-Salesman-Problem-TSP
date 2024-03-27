package com.karis.travellingsalesman.data.network.api

import com.karis.travellingsalesman.data.network.models.requests.GetRoutesRequest
import com.karis.travellingsalesman.data.network.models.responses.GetPolylineResponse
import retrofit2.http.Body
import retrofit2.http.GET

interface RoutesApiService {
    @GET("directions/v2:computeRoutes")
    fun getPolyline(
        @Body getRoutesRequest: GetRoutesRequest
    ): GetPolylineResponse
}
