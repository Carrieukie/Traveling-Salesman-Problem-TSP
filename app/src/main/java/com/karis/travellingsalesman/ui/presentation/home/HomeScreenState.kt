package com.karis.travellingsalesman.ui.presentation.home

import com.google.android.gms.maps.model.LatLng
import com.google.maps.model.DistanceMatrix
import com.karis.travellingsalesman.domain.models.Point
import com.karis.travellingsalesman.domain.models.PolyLine
import com.karis.travellingsalesman.utils.TSPResult

data class HomeScreenState(
    val distanceMatrix: DistanceMatrix? = null,
    val isGettingDistanceMatrix: Boolean = false,
    val tspResult: TSPResult? = null,
    val points: Map<Int, Point> = mapOf(0 to Point(0, "Point 0")),
    val decodedPolyLines: List<LatLng> = emptyList()
)

fun HomeScreenState.isButtonEnabled(): Boolean {
    return points.size > 1 && points.values.all { it.selectedPlace != null }
}
