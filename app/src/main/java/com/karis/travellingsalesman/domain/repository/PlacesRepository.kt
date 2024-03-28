package com.karis.travellingsalesman.domain.repository

import com.google.android.gms.maps.model.LatLng
import com.karis.travellingsalesman.domain.models.Suggestion
import com.karis.travellingsalesman.utils.NetworkResult
import kotlinx.coroutines.flow.Flow

interface PlacesRepository {
    suspend fun fetchPlaces(key: String, input: String): Flow<NetworkResult<List<Suggestion>>>
    suspend fun fetchPlaceGeometry(input: String): Flow<NetworkResult<LatLng>>
}