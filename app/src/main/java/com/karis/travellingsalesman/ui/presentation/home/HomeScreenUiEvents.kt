package com.karis.travellingsalesman.ui.presentation.home

import com.karis.travellingsalesman.domain.models.Suggestion

sealed interface HomeScreenUiEvents {
    data class ShowSnackBar(val message: String) : HomeScreenUiEvents
    data class GetPrediction(val id: Int, val it: String) : HomeScreenUiEvents
    data class SelectPlace(
        val id: Int,
        val suggestion: Suggestion,
        val shouldClearSuggestions: Boolean = false
    ): HomeScreenUiEvents
    data class RemovePoint(val id: Int) : HomeScreenUiEvents
    object AddPoint : HomeScreenUiEvents
    object OptimizeRoute : HomeScreenUiEvents

    data class FetchGeolocationData(val id: Int, val input: String) : HomeScreenUiEvents

    data class UpdatePointSearchText(val id: Int, val text: String) : HomeScreenUiEvents
}
