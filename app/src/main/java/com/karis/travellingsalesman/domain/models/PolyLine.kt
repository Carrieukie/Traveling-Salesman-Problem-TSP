package com.karis.travellingsalesman.domain.models

import com.karis.travellingsalesman.data.network.models.responses.PolyLineItem
import com.karis.travellingsalesman.data.network.models.responses.EncodedPolyLine

data class PolyLine(
    val duration: String? = null,
    val distanceMeters: Int? = null,
    val polyline: EncodedPolyLine? = null
)

fun PolyLineItem.toPolyLine() = PolyLine(
    duration = duration,
    distanceMeters = distanceMeters,
    polyline = polyline
)
