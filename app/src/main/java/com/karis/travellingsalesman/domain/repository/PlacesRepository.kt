package com.karis.travellingsalesman.domain.repository

import com.karis.travellingsalesman.domain.models.Place
import com.karis.travellingsalesman.utils.NetworkResult
import kotlinx.coroutines.flow.Flow

interface PlacesRepository {
    suspend fun fetchPlaces(key: String, input: String): Flow<NetworkResult<List<Place>>>
}