package com.karis.travellingsalesman.domain

import com.google.maps.model.DistanceMatrix
import com.karis.travellingsalesman.utils.NetworkResult
import kotlinx.coroutines.flow.Flow

interface DistanceMatrixRepository {
     fun getDistanceMatrix(locations: List<String>): Flow<NetworkResult<DistanceMatrix>>
}