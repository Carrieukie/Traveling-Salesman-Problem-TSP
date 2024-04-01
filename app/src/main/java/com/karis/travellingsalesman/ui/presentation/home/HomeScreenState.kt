package com.karis.travellingsalesman.ui.presentation.home

import com.google.android.gms.maps.model.LatLng
import com.google.maps.model.DistanceMatrix
import com.karis.travellingsalesman.domain.models.Point
import com.karis.travellingsalesman.utils.TSPResult

data class HomeScreenState(
    val distanceMatrix: DistanceMatrix? = null,
    val tspResult: TSPResult? = null,
    val points: Map<Int, Point> = mapOf(0 to Point(0, "")),
//    val points: LinkedHashMap<Int, Point> = linkedMapOf(0 to Point(0, "")),
    val decodedPolyLines: List<LatLng> = emptyList(),
    val tourLatLng: List<Point>? = null,
    val isOptimizingRoute: Boolean = false,
    val loadingMessage: String = "",
    val currentBottomsheetIndex: Int = 0
) {
    fun isButtonEnabled(): Boolean {
        return points.size > 1 && points.values.all { it.selectedSuggestion != null }
    }

}


