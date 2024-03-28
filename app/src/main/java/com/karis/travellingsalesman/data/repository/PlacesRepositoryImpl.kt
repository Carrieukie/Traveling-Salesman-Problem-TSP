package com.karis.travellingsalesman.data.repository

import com.google.android.gms.maps.model.LatLng
import com.karis.travellingsalesman.data.network.api.PlacesApiService
import com.karis.travellingsalesman.domain.repository.PlacesRepository
import com.karis.travellingsalesman.domain.models.Place
import com.karis.travellingsalesman.utils.NetworkResult
import com.karis.travellingsalesman.utils.safeApiCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class PlacesRepositoryImpl @Inject constructor(
    private val placesApiService: PlacesApiService
) : PlacesRepository {
    override suspend fun fetchPlaces(key: String, input: String): Flow<NetworkResult<List<Place>>> =
        flow {
            emit(NetworkResult.Loading(mutableListOf()))
            val response = safeApiCall {
                val placesDto = placesApiService.fetchPlaces(key, input)
                placesDto.toPlacesList()
            }
            emit(response)
        }

    override suspend fun fetchPlaceGeometry(input: String): Flow<NetworkResult<LatLng>> = flow {
        emit(NetworkResult.Loading(null))
        val response = safeApiCall {
            val geometryResponse = placesApiService.fetchPlaceGeometry(input)

            check(!geometryResponse.candidates.isNullOrEmpty()) {
                "No coordinates found for $input"
            }
            val latitude = geometryResponse.candidates?.get(0)?.geometry?.location?.lat
            val longitude = geometryResponse.candidates?.get(0)?.geometry?.location?.lng

            check(latitude != null || longitude != null) {
                error("No coordinates found for $input")
            }
            LatLng(
                /* latitude = */ latitude ?: error("No latitude found for $input"),
                /* longitude = */ longitude ?: error("No longitude found for $input"),
            )

        }
        emit(response)
    }

}
