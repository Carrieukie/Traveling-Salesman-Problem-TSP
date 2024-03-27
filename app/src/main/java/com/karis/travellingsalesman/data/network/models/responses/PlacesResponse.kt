package com.karis.travellingsalesman.data.network.models.responses

import com.google.gson.annotations.SerializedName
import com.karis.travellingsalesman.models.Place
import kotlin.random.Random

data class PlacesResponse(

    @field:SerializedName("predictions")
	val predictions: List<PredictionsItem?>? = null,

    @field:SerializedName("status")
	val status: String? = null
){
	fun toPlacesList() = predictions?.mapNotNull { it?.toPlace() } ?: emptyList()
}
data class MainTextMatchedSubstringsItem(

	@field:SerializedName("offset")
	val offset: Int? = null,

	@field:SerializedName("length")
	val length: Int? = null
)

data class StructuredFormatting(

    @field:SerializedName("main_text_matched_substrings")
	val mainTextMatchedSubstrings: List<MainTextMatchedSubstringsItem?>? = null,

    @field:SerializedName("secondary_text")
	val secondaryText: String? = null,

    @field:SerializedName("main_text")
	val mainText: String? = null
)

data class PredictionsItem(

    @field:SerializedName("reference")
	val reference: String? = null,

    @field:SerializedName("types")
	val types: List<String?>? = null,

    @field:SerializedName("matched_substrings")
	val matchedSubstrings: List<MatchedSubstringsItem?>? = null,

    @field:SerializedName("terms")
	val terms: List<TermsItem?>? = null,

    @field:SerializedName("structured_formatting")
	val structuredFormatting: StructuredFormatting? = null,

    @field:SerializedName("description")
	val description: String? = null,

    @field:SerializedName("place_id")
	val placeId: String? = null
){
	fun toPlace(): Place {
		return Place(id = placeId ?: Random.nextInt().toString(), name = description ?: "", secondaryText = structuredFormatting?.secondaryText ?: structuredFormatting?.mainText ?: "")
	}
}

data class TermsItem(

	@field:SerializedName("offset")
	val offset: Int? = null,

	@field:SerializedName("value")
	val value: String? = null
)

data class MatchedSubstringsItem(

	@field:SerializedName("offset")
	val offset: Int? = null,

	@field:SerializedName("length")
	val length: Int? = null
)
