package com.karis.travellingsalesman.data.network.models.requests

import com.google.gson.annotations.SerializedName

data class GetRoutesRequest(

	@field:SerializedName("routingPreference")
	val routingPreference: String? = null,

	@field:SerializedName("polylineQuality")
	val polylineQuality: String? = null,

	@field:SerializedName("origin")
	val origin: Origin? = null,

	@field:SerializedName("destination")
	val destination: Destination? = null,

	@field:SerializedName("travelMode")
	val travelMode: String? = null
)

data class Origin(

	@field:SerializedName("address")
	val address: String? = null
)

data class Destination(

	@field:SerializedName("address")
	val address: String? = null
)
