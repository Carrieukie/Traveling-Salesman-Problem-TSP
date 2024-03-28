package com.karis.travellingsalesman.data.repository

import com.google.android.gms.maps.model.LatLng
import com.karis.travellingsalesman.data.network.api.RoutesApiService
import com.karis.travellingsalesman.data.network.models.requests.GetPolyLineRequest
import com.karis.travellingsalesman.data.network.models.responses.PolylineResponse
import com.karis.travellingsalesman.domain.repository.PolylinesRepository
import com.karis.travellingsalesman.utils.NetworkResult
import com.karis.travellingsalesman.utils.asyncAll
import com.karis.travellingsalesman.utils.decodeEncodedPolyline
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
    ): Flow<NetworkResult<List<LatLng>>> = flow {

        // Emit a loading state
        emit(NetworkResult.Loading(null))

        coroutineScope {
            // Execute all network requests concurrently
            val tourPolyLinesResult = asyncAll(tour) { getPolyLine(it) }.awaitAll()

            // Check if all network requests were successful
            val allSuccess = tourPolyLinesResult.all { it is NetworkResult.Success }

            if (allSuccess) {
                // Extract and process successful results
                val polyLines = tourPolyLinesResult
                    .mapNotNull { networkResult ->
                        // Extract the data from successful results
                        (networkResult as NetworkResult.Success).data
                            // Extract and convert routes to PolyLines and decode the encoded polylines
                            ?.routes
                            ?.flatMap { polyLineResponseItem ->
                                polyLineResponseItem
                                    .polyline
                                    ?.encodedPolyline
                                    ?.decodeEncodedPolyline()
                                    ?: emptyList()
                            }
                    }
                    .flatten()
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

    private suspend fun getPolyLine(getPolyLineRequest: GetPolyLineRequest): NetworkResult<PolylineResponse> {
        return safeApiCall {
            routesApiService.getPolyline(getPolyLineRequest = getPolyLineRequest)
        }
    }

}