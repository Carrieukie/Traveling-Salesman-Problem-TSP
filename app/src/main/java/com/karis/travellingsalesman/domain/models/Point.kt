package com.karis.travellingsalesman.domain.models

import com.karis.travellingsalesman.data.network.models.requests.Destination
import com.karis.travellingsalesman.data.network.models.requests.GetPolyLineRequest
import com.karis.travellingsalesman.data.network.models.requests.Origin

data class Point(
    val id: Int,
    val name: String,
    val placeSuggestions: List<Place> = emptyList(),
    val selectedPlace: Place? = null
)

fun List<Point>.toGetPolyLineRequest(): List<GetPolyLineRequest> =
    (0 until size - 1).map {
        GetPolyLineRequest(
            origin = Origin(this[it].selectedPlace?.name.orEmpty()),
            destination = Destination(this[it + 1].selectedPlace?.name.orEmpty()),
        )
    }
