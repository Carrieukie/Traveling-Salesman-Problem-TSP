package com.karis.travellingsalesman.data.repository

import com.karis.travellingsalesman.data.network.api.RoutesApiService
import com.karis.travellingsalesman.data.network.models.requests.GetRoutesRequest
import com.karis.travellingsalesman.domain.RoutesRepository
import com.karis.travellingsalesman.utils.NetworkResult
import com.karis.travellingsalesman.utils.safeApiCall
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class RoutesRepositoryImpl @Inject constructor(
    private val routesApiService: RoutesApiService
) : RoutesRepository {
    override fun getPolyLine(getRoutesRequest: GetRoutesRequest) = flow {
        emit(NetworkResult.Loading(null))
        val response = safeApiCall {
            routesApiService.getPolyline(getRoutesRequest)
        }
        emit(response)
    }
}