package com.karis.travellingsalesman.data.repository

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
}
