package com.karis.travellingsalesman.domain.repository

import com.karis.travellingsalesman.data.network.models.requests.GetPolyLineRequest
import com.karis.travellingsalesman.data.network.models.responses.GetPolylineResponse
import com.karis.travellingsalesman.domain.models.PolyLine
import com.karis.travellingsalesman.utils.NetworkResult
import kotlinx.coroutines.flow.Flow

interface PolylinesRepository {
    suspend fun getTourPolyline(tour: List<GetPolyLineRequest>): Flow<NetworkResult<List<PolyLine>>>
}