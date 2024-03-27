package com.karis.travellingsalesman.data.repository

import com.google.maps.DistanceMatrixApi
import com.google.maps.GeoApiContext
import com.google.maps.model.TravelMode
import com.karis.travellingsalesman.domain.DistanceMatrixRepository
import com.karis.travellingsalesman.utils.NetworkResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class DistanceMatrixRepositoryImpl @Inject constructor(
    private val geoApiContext: GeoApiContext
) : DistanceMatrixRepository {
    override fun getDistanceMatrix(locations: List<String>) = flow {
        emit(NetworkResult.Loading())
        try {
            val typedLocationArray = locations.toTypedArray()
            val distanceMatrix = DistanceMatrixApi
                .getDistanceMatrix(geoApiContext, typedLocationArray, typedLocationArray)
                .mode(TravelMode.DRIVING) // You can change the travel mode as per your requirement
                .await()

            emit(NetworkResult.Success(distanceMatrix))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(
                NetworkResult.Error(
                    errorCode = e.hashCode(),
                    errorMessage = e.message
                )
            )
        }
    }.flowOn(Dispatchers.IO)
}