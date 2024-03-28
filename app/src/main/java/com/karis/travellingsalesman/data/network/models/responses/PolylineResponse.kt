package com.karis.travellingsalesman.data.network.models.responses

import com.google.gson.annotations.SerializedName

data class PolylineResponse(

	@field:SerializedName("routes")
	val routes: List<PolyLineItem> = emptyList()
)
data class PolyLineItem(

	@field:SerializedName("duration")
	val duration: String? = null,

	@field:SerializedName("distanceMeters")
	val distanceMeters: Int? = null,

	@field:SerializedName("polyline")
	val polyline: EncodedPolyLine? = null
)

data class EncodedPolyLine(

	@field:SerializedName("encodedPolyline")
	val encodedPolyline: String? = null
)
