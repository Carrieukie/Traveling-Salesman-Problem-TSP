package com.karis.travellingsalesman.domain

import com.karis.travellingsalesman.data.network.models.requests.GetRoutesRequest
import com.karis.travellingsalesman.utils.NetworkResult
import kotlinx.coroutines.flow.Flow

interface RoutesRepository {
    fun getPolyLine(getRoutesRequest: GetRoutesRequest): Flow<NetworkResult<out Unit>>
}