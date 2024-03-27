package com.karis.travellingsalesman.domain.repository

import com.google.maps.model.DistanceMatrix
import com.karis.travellingsalesman.utils.NetworkResult
import kotlinx.coroutines.flow.Flow

interface DistanceMatrixRepository {
     fun getDistanceMatrix(locations: List<String>): Flow<NetworkResult<DistanceMatrix>>
}