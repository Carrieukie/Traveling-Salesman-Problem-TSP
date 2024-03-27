package com.karis.travellingsalesman.data.network.models.requests

import com.google.gson.annotations.SerializedName

data class GetPolyLineRequest(

    @field:SerializedName("origin")
    val origin: Origin? = null,

    @field:SerializedName("destination")
    val destination: Destination? = null,

    @field:SerializedName("routingPreference")
    val routingPreference: String? = "traffic_aware",

    @field:SerializedName("polylineQuality")
    val polylineQuality: String? = "high_quality",

    @field:SerializedName("travelMode")
    val travelMode: String? = "drive"
)

data class Origin(

    @field:SerializedName("address")
    val address: String? = null
)

data class Destination(

    @field:SerializedName("address")
    val address: String? = null
)
