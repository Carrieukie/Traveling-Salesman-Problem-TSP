package com.karis.travellingsalesman.domain.models

import com.google.android.gms.maps.model.LatLng
import com.karis.travellingsalesman.data.network.models.requests.Destination
import com.karis.travellingsalesman.data.network.models.requests.GetPolyLineRequest
import com.karis.travellingsalesman.data.network.models.requests.Origin

data class Point(
    val id: Int,
    val name: String,
    val placeSuggestions: List<Place> = emptyList(),
    val selectedPlace: Place? = null,
    val latLng: LatLng? = null,
)

fun List<Point>.toGetPolyLineRequest(): List<GetPolyLineRequest> =
    (0 until size - 1).map {
        GetPolyLineRequest(
            origin = Origin(this[it].selectedPlace?.name.orEmpty()),
            destination = Destination(this[it + 1].selectedPlace?.name.orEmpty()),
        )
    }
