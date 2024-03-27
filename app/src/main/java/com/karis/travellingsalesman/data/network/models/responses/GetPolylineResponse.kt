package com.karis.travellingsalesman.data.network.models.responses

import com.google.gson.annotations.SerializedName

data class GetPolylineResponse(

	@field:SerializedName("routes")
	val routes: List<RoutesItem>? = null
)

data class Polyline(

	@field:SerializedName("encodedPolyline")
	val encodedPolyline: String? = null
)

data class RoutesItem(

	@field:SerializedName("duration")
	val duration: String? = null,

	@field:SerializedName("distanceMeters")
	val distanceMeters: Int? = null,

	@field:SerializedName("polyline")
	val polyline: Polyline? = null
)
