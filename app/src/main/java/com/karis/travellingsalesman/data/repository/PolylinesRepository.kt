package com.karis.travellingsalesman.data.repository

import com.karis.travellingsalesman.data.network.api.RoutesApiService
import com.karis.travellingsalesman.data.network.models.requests.GetPolyLineRequest
import com.karis.travellingsalesman.data.network.models.responses.GetPolylineResponse
import com.karis.travellingsalesman.domain.models.PolyLine
import com.karis.travellingsalesman.domain.models.toPolyLine
import com.karis.travellingsalesman.domain.repository.PolylinesRepository
import com.karis.travellingsalesman.utils.NetworkResult
import com.karis.travellingsalesman.utils.asyncAll
import com.karis.travellingsalesman.utils.safeApiCall
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class PolyLinesRepositoryImpl @Inject constructor(
    private val routesApiService: RoutesApiService
) : PolylinesRepository {

    override suspend fun getTourPolyline(
        tour: List<GetPolyLineRequest>
    ): Flow<NetworkResult<List<PolyLine>>> = flow {

        // Emit a loading state
        emit(NetworkResult.Loading(null))

        coroutineScope {
            // Execute all network requests concurrently
            val getTourPolyLinesResult = asyncAll(tour) { getPolyLine(it) }.awaitAll()

            // Check if all network requests were successful
            val allSuccess = getTourPolyLinesResult.all { it is NetworkResult.Success }

            if (allSuccess) {
                // Extract and process successful results
                val polyLines = getTourPolyLinesResult
                    .mapNotNull { networkResult ->
                        // Extract the data from successful results
                        (networkResult as NetworkResult.Success).data
                    }
                    .flatMap { polyLineResponse ->
                        // Extract and convert routes to PolyLines
                        polyLineResponse.routes.map { polyLineResponseItem ->
                            polyLineResponseItem.toPolyLine()
                        }
                    }

                // Emit successful result
                emit(NetworkResult.Success(polyLines))
            } else {
                // Emit error result
                emit(
                    NetworkResult.Error(errorMessage = "Failed to get all polyLines", data = null)
                )
            }
        }
    }


    private suspend fun getPolyLine(getPolyLineRequest: GetPolyLineRequest): NetworkResult<GetPolylineResponse> {
        return safeApiCall {
            routesApiService.getPolyline(getPolyLineRequest)
        }
    }
}