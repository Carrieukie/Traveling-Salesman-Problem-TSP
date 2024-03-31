package com.karis.travellingsalesman.ui.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.model.DistanceMatrix
import com.karis.travellingsalesman.BuildConfig
import com.karis.travellingsalesman.domain.models.Suggestion
import com.karis.travellingsalesman.domain.models.Point
import com.karis.travellingsalesman.domain.models.toGetPolyLineRequest
import com.karis.travellingsalesman.domain.repository.DistanceMatrixRepository
import com.karis.travellingsalesman.domain.repository.PlacesRepository
import com.karis.travellingsalesman.domain.repository.PolylinesRepository
import com.karis.travellingsalesman.utils.AdjacencyMatrixCreationType
import com.karis.travellingsalesman.utils.NetworkResult
import com.karis.travellingsalesman.utils.calculateCameraViewPoints
import com.karis.travellingsalesman.utils.getAdjacencyMatrix
import com.karis.travellingsalesman.utils.getCenterLatLngs
import com.karis.travellingsalesman.utils.heldKarpgetShortestTimePath
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val computeRouteRepository: DistanceMatrixRepository,
    private val placesRepository: PlacesRepository,
    private val polylineRepository: PolylinesRepository
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

            is HomeScreenUiEvents.SelectPlace -> selectSuggestion(
                pointId = event.id,
                suggestion = event.suggestion,
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
                    .mapNotNull { it.selectedSuggestion?.name }
            )

            is HomeScreenUiEvents.FetchGeolocationData -> getFetchPlaceGeometry(
                input = event.input,
                pointId = event.id
            )

            is HomeScreenUiEvents.UpdatePointSearchText -> {
                updatePointSearchText(
                    input = event.text,
                    pointId = event.id
                )

            }

            is HomeScreenUiEvents.ClearPointName -> {
                clearPointText(event.id)
            }

            is HomeScreenUiEvents.SendGoogleMapsCameraUpdate -> {
                sendCameraAnimateEvent()
            }
        }
    }

    /**
     * Sends a one-shot event to update the camera on the Google Maps.
     * @param cameraUpdate The camera update to be sent.
     */
    private fun sendCameraAnimateEvent() {
        // Launch a coroutine in the viewModelScope
        viewModelScope.launch {
            val bounds = _mainActivityState.value
                .tourLatLng
                ?.calculateCameraViewPoints()
                ?.getCenterLatLngs()

            val cameraUpdate = CameraUpdateFactory.newLatLngBounds(
                /* bounds = */ bounds ?: return@launch,
                /* padding = */ 30
            )

            // Send a one-shot event containing the Google Maps camera update
            _oneShotEvents.send(HomeScreenUiEvents.SendGoogleMapsCameraUpdate(cameraUpdate))
        }
    }

    /**
     * Retrieves the distance matrix for the provided locations and updates the UI.
     * @param locations The list of locations for which the distance matrix is requested.
     */
    private fun getDistanceMatrix(locations: List<String>) = intent {

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

                        reduce {
                            copy(
                                isOptimizingRoute = false,
                            )
                        }
                    }

                    is NetworkResult.Loading -> {
                        // Indicate loading state in the UI
                        reduce {
                            copy(
                                isOptimizingRoute = true,
                                loadingMessage = "Holding on tight while we fetch the awesomeness."
                            )

                        }

                    }

                    is NetworkResult.Success -> {
                        // Update the main activity state with the retrieved distance matrix
                        reduce {
                            copy(
                                distanceMatrix = networkResult.data,
                                isOptimizingRoute = false
                            )
                        }
                        // Calculate and update the shortest path based on the new distance matrix
                        getShortestPath(networkResult.data)
                    }
                }
            }
    }


    /**
     * Retrieves the shortest path using the Held-Karp algorithm based on the provided distance matrix.
     * If the distance matrix is null, displays a snackbar event indicating the unavailability.
     * @param distanceMatrix The distance matrix used to calculate the shortest path.
     */
    private fun getShortestPath(distanceMatrix: DistanceMatrix?) = intent {

        reduce {
            copy(
                isOptimizingRoute = true,
                loadingMessage = "Navigating through the digital streets and " +
                        "alleys to find the shortest path."
            )
        }

        // Check if the distance matrix is null
        if (distanceMatrix == null) {
            // Display a snackbar event indicating unavailability
            sendSnackBarEvent("No distance matrix available")
            reduce { copy(isOptimizingRoute = false)}
            return@intent
        }

        // Generate an adjacency matrix from the distance matrix
        val timeAdjacencyMatrix = distanceMatrix.getAdjacencyMatrix(
            adjacencyMatrixCreationType = AdjacencyMatrixCreationType.DURATION
        )

        val distanceAdjacencyMatrix = distanceMatrix.getAdjacencyMatrix(
            adjacencyMatrixCreationType = AdjacencyMatrixCreationType.DISTANCE
        )

        if (timeAdjacencyMatrix == null || distanceAdjacencyMatrix == null) {
            // Display a snackbar event indicating unavailability
            sendSnackBarEvent("Some point are not reachable, please check your input")
            return@intent
        }

        // Get the shortest path using the Held-Karp algorithm
        val tspResult = heldKarpgetShortestTimePath(
            timeAdjacencyMatrix = timeAdjacencyMatrix,
            distanceAdjacencyMatrix = distanceAdjacencyMatrix
        )

        // Update the main activity state with the new shortest path
        reduce {
            copy(
                tspResult = tspResult,
                isOptimizingRoute = false
            )
        }

        getPolyline()
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
    ) = intent {
        // Launch a coroutine in viewModelScope
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

                    }

                    is NetworkResult.Success -> {
                        // Update point suggestions with the retrieved data
                        updatePointSuggestions(
                            pointId = pointId,
                            suggestionSuggestions = networkResult.data ?: emptyList()
                        )
                        // Update UI to reflect the end of loading state

                        reduce {
                            copy(
                            )
                        }
                    }
                }
            }

    }

    /**
     * Fetches geometry information for a given place input and updates the latitude and longitude of a point.
     *
     * @param input The input string representing the place.
     * @param pointId The ID of the point to update.
     */
    private fun getFetchPlaceGeometry(input: String, pointId: Int) = intent {
        placesRepository.fetchPlaceGeometry(input)
            .collect { networkResult ->
                when (networkResult) {
                    is NetworkResult.Error -> {
                        val message = networkResult.errorMessage ?: "Error"
                        sendSnackBarEvent(message)
                    }

                    is NetworkResult.Loading -> {
                        // Handle loading state if needed
                    }

                    is NetworkResult.Success -> {
                        updatePointLatLng(
                            pointId = pointId,
                            latLng = networkResult.data ?: LatLng(0.0, 0.0)
                        )
                    }
                }
            }
    }


    private fun getPolyline() = intent {
        // Retrieve the TSP tour from the current state, or an empty list if not available
        val tspTour = _mainActivityState.value.tspResult?.path ?: emptyList()

        // Extract the points corresponding to the tour indices from the current state
        val points = tspTour.mapNotNull {
            _mainActivityState.value.points.values.toList().getOrNull(it)
        }

        // Initiate a coroutine for asynchronous operations
        // Fetch the polyline data from the repository based on the points
        polylineRepository.getTourPolyline(points.toGetPolyLineRequest())
            .collect { networkResult ->
                // Handle different states of the network request result
                when (networkResult) {
                    is NetworkResult.Error -> {
                        // If there's an error, show a snack bar with the error message
                        val message = networkResult.errorMessage ?: "Error"
                        sendSnackBarEvent(message)

                        reduce {
                            copy(
                                isOptimizingRoute = false
                            )
                        }
                    }

                    is NetworkResult.Loading -> {
                        // If the request is still loading, update the state to indicate so
                        reduce {
                            copy(
                                isOptimizingRoute = true,
                                loadingMessage = "Assembling the pieces of your epic journey. " +
                                        "The best routes are worth the wait!"
                            )
                        }
                    }

                    is NetworkResult.Success -> {
                        // If the request is successful, update the state with the received polyline data
                        reduce {
                            val tourLatLng = points.mapNotNull { it.latLng }
                            copy(
                                decodedPolyLines = networkResult.data ?: emptyList(),
                                tourLatLng = tourLatLng,
                                isOptimizingRoute = false
                            )
                        }
                        // Send a one-shot event to update the Google Maps camera
                        sendCameraAnimateEvent()
                    }
                }
            }
    }

    /**
     * Updates the suggestions for a specific point with the provided list of place suggestions.
     * @param pointId The ID of the point for which suggestions are being updated.
     * @param suggestionSuggestions The list of place suggestions to update for the point.
     */
    private fun updatePointSuggestions(
        pointId: Int,
        suggestionSuggestions: List<Suggestion>
    ) = intent {
        // Retrieve the point from the current state
        val point = _mainActivityState.value.points[pointId]

        // Check if the point exists
        if (point == null) {
            // If the point doesn't exist, display a snackbar message and return
            sendSnackBarEvent("Point not found")
            return@intent
        }

        // Create a new point with updated place suggestions
        val updatedPoint = point.copy(
            suggestionSuggestions = suggestionSuggestions
        )

        // Update the main activity state with the new point
        reduce {
            copy(
                points = points + (pointId to updatedPoint)
            )
        }
    }

    /**
     * Updates the latitude and longitude of a specific point.
     * @param pointId The ID of the point to update.
     * @param latLng The new latitude and longitude coordinates.
     */
    private fun updatePointLatLng(
        pointId: Int,
        latLng: LatLng
    ) = intent {
        // Retrieve the point with the given ID.
        val point = _mainActivityState.value.points[pointId]
        // Check if the point exists.
        if (point == null) {
            // If the point is not found, show a snack bar notification.
            sendSnackBarEvent("Point not found")
            return@intent
        }
        // Update the point with the new latitude and longitude.
        val updatedPoint = point.copy(
            latLng = latLng
        )
        // Reduce the state by updating the points list with the modified point.
        reduce {
            copy(
                points = points + (pointId to updatedPoint)
            )
        }
    }


    /**
     * Adds a new point to the main activity state.
     * The new point is automatically assigned an ID greater than the existing maximum ID.
     */
    private fun addPoint() = intent {
        // Retrieve the current points from the main activity state
        val points = _mainActivityState.value.points

        // Determine the ID for the new point
        val newPointId = points.keys.maxOrNull()?.plus(1) ?: 0

        // Create a new point with the determined ID and a default name
        val newPoint = Point(newPointId, "")

        // Update the main activity state with the new point added
        reduce {
            copy(
                points = points + (newPointId to newPoint)
            )
        }

    }


    /**
     * Selects a place for a specific point in the main activity state.
     * @param pointId The ID of the point for which the place is being selected.
     * @param suggestion The selected place.
     * @param shouldClearSuggestions Flag indicating whether to clear the place suggestions for the point.
     */
    private fun selectSuggestion(
        pointId: Int,
        suggestion: Suggestion,
        shouldClearSuggestions: Boolean
    ) = intent {
        // Retrieve the point from the main activity state
        val point = _mainActivityState.value.points[pointId]

        // Check if the point exists
        if (point == null) {
            // If the point doesn't exist, display a snackbar message and return
            sendSnackBarEvent("Point not found")
            return@intent
        }

        // Create an updated point with the selected place and updated suggestions list
        val updatedPoint = point.copy(
            name = suggestion.name,
            selectedSuggestion = suggestion,
            suggestionSuggestions = if (shouldClearSuggestions) emptyList() else point.suggestionSuggestions
        )

        // Update the main activity state with the updated point
        reduce {
            copy(
                points = points + (pointId to updatedPoint)
            )
        }

        if (shouldClearSuggestions) {
            onEvent(HomeScreenUiEvents.FetchGeolocationData(pointId, suggestion.name))
        }
    }

    /**
     * Updates the search text for a specific point.
     * @param input The new search text.
     * @param pointId The ID of the point to update.
     */
    private fun updatePointSearchText(input: String, pointId: Int) {
        val point = _mainActivityState.value.points[pointId]
        if (point == null) {
            // If the point is not found, show a snack bar notification.
            sendSnackBarEvent("Point not found")
            return
        }
        // Update the point with the new search text.
        val updatedPoint = point.copy(
            name = input
        )
        // Reduce the state by updating the points list with the modified point.
        _mainActivityState.update {
            it.copy(
                points = it.points + (pointId to updatedPoint)
            )
        }
    }

    /**
     * Clears the text and suggestions associated with a given point.
     * If the point with the specified ID is not found, it sends a snackbar event.
     * @param pointId The ID of the point to clear.
     */
    private fun clearPointText(pointId: Int) = intent {
        // Retrieve the point with the specified ID from the current state
        val point = _mainActivityState.value.points[pointId]

        // Check if the point is null (not found)
        if (point == null) {
            // Send a snackbar event indicating that the point was not found
            sendSnackBarEvent("Point not found")
            // Exit the function early
            return@intent
        }

        // Create an updated copy of the point with cleared text and suggestions
        val updatedPoint = point.copy(
            name = "", // Clear the name
            suggestionSuggestions = emptyList(), // Clear the suggestions
        )

        // Update the main activity state by replacing the old point with the updated one
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
    private fun removePoint(pointId: Int) = intent {
        // Check if the point to be removed is the start point (ID 0)
        if (pointId == 0) {
            // If the point to be removed is the start point, display a snackbar message and return
            sendSnackBarEvent("Cannot remove start point")
            return@intent
        }

        // Retrieve the current points from the main activity state
        val points = _mainActivityState.value.points

        // Filter out the point to be removed
        val updatedPoints = points.filterKeys { it != pointId }

        // Update the main activity state with the updated points
        reduce {
            copy(
                points = updatedPoints
            )
        }
    }

    /**
     * Runs suspend function in a single couroutine context to prevent race conditions
     * @param transform: Any suspend function that returns Unit
     *
     *
     * @return Unit
     */
    private fun intent(transform: suspend () -> Unit) {
        viewModelScope.launch(SINGLE_THREAD) {
            transform()
        }
    }

    /**
     * This reducer reduces state in a single thread context to avoid race conditions
     * on the State when more than one threads are changing it
     */
    private suspend fun reduce(reducer: HomeScreenState.() -> HomeScreenState) {
        withContext(SINGLE_THREAD) {
            _mainActivityState.update {
                it.reducer()
            }
        }
    }

    companion object {
        @OptIn(DelicateCoroutinesApi::class)
        private val SINGLE_THREAD = newSingleThreadContext("mvi")
    }

}

