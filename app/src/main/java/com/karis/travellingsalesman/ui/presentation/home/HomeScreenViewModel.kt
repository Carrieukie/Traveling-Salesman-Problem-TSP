package com.karis.travellingsalesman.ui.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.maps.model.DistanceMatrix
import com.karis.travellingsalesman.BuildConfig
import com.karis.travellingsalesman.domain.DistanceMatrixRepository
import com.karis.travellingsalesman.domain.PlacesRepository
import com.karis.travellingsalesman.models.Place
import com.karis.travellingsalesman.utils.AdjacencyMatrixCreationType
import com.karis.travellingsalesman.utils.NetworkResult
import com.karis.travellingsalesman.utils.TSPResult
import com.karis.travellingsalesman.utils.getAdjacencyMatrix
import com.karis.travellingsalesman.utils.heldKarpgetShortestTimePath
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val computeRouteRepository: DistanceMatrixRepository,
    private val placesRepository: PlacesRepository,

) : ViewModel() {

    private val _mainActivityState = MutableStateFlow(HomeScreenState())
    val mainActivityState = _mainActivityState.asStateFlow()

    private val _oneShotEvents = Channel<HomeScreenUiEvents>()
    val oneShotEvents = _oneShotEvents.receiveAsFlow()

    /**
     * Handles events coming from the home screen UI.
     * @param event The event to be handled.
     */
    fun onEvent(
        event: HomeScreenUiEvents
    ) {
        // Process different types of events
        when (event) {
            is HomeScreenUiEvents.GetPrediction -> getPrediction(
                pointId = event.id,
                input = event.it
            )

            is HomeScreenUiEvents.SelectPlace -> selectPlace(
                pointId = event.id,
                place = event.place,
                shouldClearSuggestions = event.shouldClearSuggestions
            )

            is HomeScreenUiEvents.AddPoint -> addPoint()

            is HomeScreenUiEvents.ShowSnackBar -> sendSnackBarEvent(
                message = event.message
            )

            is HomeScreenUiEvents.RemovePoint -> removePoint(
                pointId = event.id
            )

            is HomeScreenUiEvents.OptimizeRoute -> getDistanceMatrix(
                locations = _mainActivityState
                    .value
                    .points
                    .values
                    .mapNotNull { it.selectedPlace?.name }
            )
        }
    }


    /**
     * Retrieves the distance matrix for the provided locations and updates the UI.
     * @param locations The list of locations for which the distance matrix is requested.
     */
    private fun getDistanceMatrix(locations: List<String>) {
        // Launch a coroutine in IO context
        viewModelScope.launch(Dispatchers.IO) {
            // Fetch the distance matrix from the computeRouteRepository
            computeRouteRepository
                .getDistanceMatrix(locations)
                .collect { networkResult ->
                    // Handle different network results
                    when (networkResult) {
                        is NetworkResult.Error -> {
                            // If there's an error, display an appropriate snackbar message
                            val message = networkResult.errorMessage ?: "Error"
                            sendSnackBarEvent(message)
                        }

                        is NetworkResult.Loading -> {
                            // Indicate loading state in the UI
                            _mainActivityState.update {
                                it.copy(isGettingDistanceMatrix = true)
                            }
                        }

                        is NetworkResult.Success -> {
                            // Update the main activity state with the retrieved distance matrix
                            _mainActivityState.update {
                                it.copy(
                                    distanceMatrix = networkResult.data,
                                    isGettingDistanceMatrix = false
                                )
                            }
                            // Calculate and update the shortest path based on the new distance matrix
                            getShortestPath(networkResult.data)
                        }
                    }
                }
        }
    }


    /**
     * Retrieves the shortest path using the Held-Karp algorithm based on the provided distance matrix.
     * If the distance matrix is null, displays a snackbar event indicating the unavailability.
     * @param distanceMatrix The distance matrix used to calculate the shortest path.
     */
    private fun getShortestPath(distanceMatrix: DistanceMatrix?) {
        // Check if the distance matrix is null
        if (distanceMatrix == null) {
            // Display a snackbar event indicating unavailability
            sendSnackBarEvent("No distance matrix available")
            return
        }

        // Launch a coroutine in viewModelScope
        viewModelScope.launch {
            // Generate an adjacency matrix from the distance matrix
            val timeAdjacencyMatrix = distanceMatrix.getAdjacencyMatrix(
                adjacencyMatrixCreationType = AdjacencyMatrixCreationType.DURATION
            )

            val distanceAdjacencyMatrix = distanceMatrix.getAdjacencyMatrix(
                adjacencyMatrixCreationType = AdjacencyMatrixCreationType.DISTANCE
            )

            // Get the shortest path using the Held-Karp algorithm
            val tspResult = heldKarpgetShortestTimePath(
                timeAdjacencyMatrix = timeAdjacencyMatrix,
                distanceAdjacencyMatrix = distanceAdjacencyMatrix
            )

            // Update the main activity state with the new shortest path
            _mainActivityState.update {
                it.copy(
                    tspResult = tspResult
                )
            }
        }
    }

    /**
     * Sends a snackbar event with the given message to be displayed on the UI.
     * @param message The message to be displayed in the snackbar.
     */
    private fun sendSnackBarEvent(message: String) {
        // Launch a coroutine in viewModelScope
        viewModelScope.launch {
            // Send a one-shot event to show the snackbar with the provided message
            _oneShotEvents.send(HomeScreenUiEvents.ShowSnackBar(message))
        }
    }

    /**
     * Fetches place predictions based on the input string and updates the UI accordingly.
     * @param pointId The ID of the point for which predictions are being fetched.
     * @param input The input string for which predictions are requested.
     */
    private fun getPrediction(
        pointId: Int,
        input: String
    ) {
        // Launch a coroutine in viewModelScope
        viewModelScope.launch {
            // Fetch places predictions from the repository
            placesRepository.fetchPlaces(BuildConfig.MAPS_API_KEY, input)
                .collect { networkResult ->
                    // Handle different network results
                    when (networkResult) {
                        is NetworkResult.Error -> {
                            // If there's an error, display an appropriate snackbar message
                            val message = networkResult.errorMessage ?: "Error"
                            sendSnackBarEvent(message)
                        }

                        is NetworkResult.Loading -> {
                            // Indicate loading state in the UI
                            _mainActivityState.update {
                                it.copy(isGettingDistanceMatrix = true)
                            }
                        }

                        is NetworkResult.Success -> {
                            // Update point suggestions with the retrieved data
                            updatePointSuggestions(
                                pointId = pointId,
                                placeSuggestions = networkResult.data ?: emptyList()
                            )
                            // Update UI to reflect the end of loading state
                            _mainActivityState.update {
                                it.copy(
                                    isGettingDistanceMatrix = false
                                )
                            }
                        }
                    }
                }
        }
    }


    /**
     * Updates the suggestions for a specific point with the provided list of place suggestions.
     * @param pointId The ID of the point for which suggestions are being updated.
     * @param placeSuggestions The list of place suggestions to update for the point.
     */
    private fun updatePointSuggestions(
        pointId: Int,
        placeSuggestions: List<Place>
    ) {
        // Retrieve the point from the current state
        val point = _mainActivityState.value.points[pointId]

        // Check if the point exists
        if (point == null) {
            // If the point doesn't exist, display a snackbar message and return
            sendSnackBarEvent("Point not found")
            return
        }

        // Create a new point with updated place suggestions
        val updatedPoint = point.copy(
            placeSuggestions = placeSuggestions
        )

        // Update the main activity state with the new point
        _mainActivityState.update {
            it.copy(
                points = it.points + (pointId to updatedPoint)
            )
        }
    }


    /**
     * Adds a new point to the main activity state.
     * The new point is automatically assigned an ID greater than the existing maximum ID.
     */
    private fun addPoint() {
        // Retrieve the current points from the main activity state
        val points = _mainActivityState.value.points

        // Determine the ID for the new point
        val newPointId = points.keys.maxOrNull()?.plus(1) ?: 0

        // Create a new point with the determined ID and a default name
        val newPoint = Point(newPointId, "Point $newPointId")

        // Update the main activity state with the new point added
        _mainActivityState.update {
            it.copy(
                points = it.points + (newPointId to newPoint)
            )
        }
    }


    /**
     * Selects a place for a specific point in the main activity state.
     * @param pointId The ID of the point for which the place is being selected.
     * @param place The selected place.
     * @param shouldClearSuggestions Flag indicating whether to clear the place suggestions for the point.
     */
    private fun selectPlace(
        pointId: Int,
        place: Place,
        shouldClearSuggestions: Boolean
    ) {
        // Retrieve the point from the main activity state
        val point = _mainActivityState.value.points[pointId]

        // Check if the point exists
        if (point == null) {
            // If the point doesn't exist, display a snackbar message and return
            sendSnackBarEvent("Point not found")
            return
        }

        // Create an updated point with the selected place and updated suggestions list
        val updatedPoint = point.copy(
            selectedPlace = place,
            placeSuggestions = if (shouldClearSuggestions) emptyList() else point.placeSuggestions
        )

        // Update the main activity state with the updated point
        _mainActivityState.update {
            it.copy(
                points = it.points + (pointId to updatedPoint)
            )
        }
    }

    /**
     * Removes a point from the main activity state.
     * @param pointId The ID of the point to be removed.
     */
    fun removePoint(pointId: Int) {
        // Check if the point to be removed is the start point (ID 0)
        if (pointId == 0) {
            // If the point to be removed is the start point, display a snackbar message and return
            sendSnackBarEvent("Cannot remove start point")
            return
        }

        // Retrieve the current points from the main activity state
        val points = _mainActivityState.value.points

        // Filter out the point to be removed
        val updatedPoints = points.filterKeys { it != pointId }

        // Update the main activity state with the updated points
        _mainActivityState.update {
            it.copy(
                points = updatedPoints
            )
        }
    }


}

data class HomeScreenState(
    val distanceMatrix: DistanceMatrix? = null,
    val isGettingDistanceMatrix: Boolean = false,
    val tspResult: TSPResult? = null,
    val points: Map<Int, Point> = mapOf(0 to Point(0, "Point 0"))
)

fun HomeScreenState.isButtonEnabled(): Boolean {
    return points.size > 1 && points.values.all { it.selectedPlace != null }
}

data class Point(
    val id: Int,
    val name: String,
    val placeSuggestions: List<Place> = emptyList(),
    val selectedPlace: Place? = null
)
