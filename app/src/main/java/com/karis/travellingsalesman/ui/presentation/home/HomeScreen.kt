package com.karis.travellingsalesman.ui.presentation.home

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.karis.travellingsalesman.R
import com.karis.travellingsalesman.domain.models.Point
import com.karis.travellingsalesman.utils.convertSecondsToTime
import com.karis.travellingsalesman.utils.observeAsEvents
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeScreenViewModel: HomeScreenViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val nairobi = LatLng(-1.286389, 36.817223)
    val scope = rememberCoroutineScope()

    val homeScreenState = homeScreenViewModel.mainActivityState.collectAsState()
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(nairobi, 10f)
    }

    observeAsEvents(flow = homeScreenViewModel.oneShotEvents) { homeScreenUiEvent ->
        when (homeScreenUiEvent) {
            is HomeScreenUiEvents.ShowSnackBar -> {
                Toast.makeText(context, homeScreenUiEvent.message, Toast.LENGTH_SHORT).show()
            }

            is HomeScreenUiEvents.SendGoogleMapsCameraUpdate -> {
                scope.launch {
                    cameraPositionState.animate(
                        update = homeScreenUiEvent.cameraUpdate
                    )
                }
            }

            else -> {}
        }
    }

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = SheetState(
            density = LocalDensity.current,
            initialValue = SheetValue.Expanded,
            skipPartiallyExpanded = false,
        )
    )

    BottomSheetScaffold(
        modifier = Modifier,
        scaffoldState = scaffoldState,
        sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        sheetContent = {
            BottomSheet(
                homeScreenState = homeScreenState,
                onEvent = homeScreenViewModel::onEvent,
            )
        },
    ) {
        // A surface container using the 'background' color from the theme
        Column(
            modifier = Modifier
                .padding(bottom = 12.dp)
                .fillMaxSize(),
        ) {
            val nightModeMap = MapStyleOptions.loadRawResourceStyle(
                context,
                R.raw.map_style_night
            )
            val mapProperties = MapProperties(
                isMyLocationEnabled = false,
                mapStyleOptions = nightModeMap,
            )

            val uiSettings = remember {
                MapUiSettings(
                    myLocationButtonEnabled = false,
                    zoomControlsEnabled = false,
                    mapToolbarEnabled = false
                )
            }

            GoogleMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                cameraPositionState = cameraPositionState,
                properties = mapProperties,
                uiSettings = uiSettings
            ) {
                val tour = homeScreenState.value.tourLatLng ?: mutableListOf()
                for (i in tour.indices) {
                    val point = homeScreenState.value.points[i]
                    point?.latLng?.let { latLng ->
                        Marker(
                            icon = BitmapDescriptorFactory.defaultMarker(),
                            state = rememberMarkerState(position = latLng),
                            snippet = homeScreenState.value.points[i]?.selectedSuggestion?.name.orEmpty(),
                            title = if (i == 0) "Start Point" else "Point $i"
                        )
                    }
                }

                Polyline(
                    points = homeScreenState.value.decodedPolyLines,
                    clickable = true,
                    geodesic = true,
                    jointType = JointType.BEVEL,
                    zIndex = 1f,
                    color = MaterialTheme.colorScheme.primary,
                    width = 10f
                )

            }
        }
    }
}

@Composable
fun BottomSheet(
    homeScreenState: State<HomeScreenState>,
    onEvent: (HomeScreenUiEvents) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        when (homeScreenState.value.currentBottomsheetIndex) {
            0 -> RouteOptimization(homeScreenState, onEvent)
            1 -> RouteOptimizationResults(homeScreenState, onEvent)
        }
    }
}

@Composable
private fun RouteOptimization(
    homeScreenState: State<HomeScreenState>,
    onEvent: (HomeScreenUiEvents) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .animateContentSize()
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp)
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Route Optimization",
                style = MaterialTheme.typography.titleMedium
            )
        }

        if (homeScreenState.value.isOptimizingRoute) {
            item {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
            item {
                Text(
                    text = homeScreenState.value.loadingMessage,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        val items = homeScreenState.value.points.values.toList()
        items(items.size) { index ->
            val point = homeScreenState.value.points.values.toList()[index]
            DestinationPoint(
                point = point,
                homeScreenState = homeScreenState,
                onEvent = onEvent,
                index = index
            )
        }

        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable {
                        onEvent(HomeScreenUiEvents.AddPoint)
                    }
                    .fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Add Point", style = MaterialTheme.typography.bodyMedium)
            }
        }

        item {
            HorizontalDivider()
        }

        item {
            Button(
                modifier = Modifier
                    .fillMaxWidth(),
                enabled = homeScreenState.value.isButtonEnabled(),
                shape = MaterialTheme.shapes.large,
                onClick = {
                    onEvent(HomeScreenUiEvents.OptimizeRoute)
                }) {
                Text(
                    text = "Optimize",
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}

@Composable
fun DestinationPoint(
    point: Point,
    homeScreenState: State<HomeScreenState>,
    onEvent: (HomeScreenUiEvents) -> Unit,
    index: Int
) {
    Column(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .fillMaxWidth()
    ) {

        Text(
            text = if (point.id == 0) "Start Point" else "Point $index",
            style = MaterialTheme.typography.titleSmall
        )
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                textStyle = MaterialTheme.typography.bodyMedium,
                value = homeScreenState.value.points[point.id]?.name.orEmpty(),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .weight(1f),
                onValueChange = {
                    onEvent(HomeScreenUiEvents.UpdatePointSearchText(point.id, it))
                    onEvent(HomeScreenUiEvents.GetPrediction(point.id, it))
                },
                placeholder = {
                    Text(
                        text = "Search location",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            )
            val pointSearchTextEmpty = homeScreenState
                .value
                .points[point.id]
                ?.name
                .orEmpty()
                .isEmpty()

            Icon(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .clickable {
                        if (!pointSearchTextEmpty) {
                            onEvent(HomeScreenUiEvents.ClearPointName(point.id))
                        } else {
                            onEvent(HomeScreenUiEvents.RemovePoint(point.id))
                        }
                    },
                imageVector = if (!pointSearchTextEmpty) Icons.Default.Clear else Icons.Default.Delete,
                contentDescription = "",
                tint = if (!pointSearchTextEmpty) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
        }

        point.suggestionSuggestions.forEach { place ->
            Text(
                text = "${place.name} ",
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .clickable {
                        onEvent(HomeScreenUiEvents.SelectPlace(point.id, place, true))
                    },
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

}

@Composable
fun RouteOptimizationResults(
    homeScreenState: State<HomeScreenState>,
    onEvent: (HomeScreenUiEvents) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .animateContentSize()
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp)
            .wrapContentHeight(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Route Optimization",
                style = MaterialTheme.typography.titleMedium
            )
        }

        if (homeScreenState.value.isOptimizingRoute) {
            item {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
            item {
                Text(
                    text = homeScreenState.value.loadingMessage,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        item {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                val distance = homeScreenState.value.tspResult?.distance ?: 0
                val timeTaken = homeScreenState.value.tspResult?.time ?: 0

                Text(
                    text = "${distance / 1000}km",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = convertSecondsToTime(timeTaken),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        item {
            Text(
                text = "Fastest(time based) route now due to traffic conditions.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        item {
            Button(
                modifier = Modifier
                    .fillMaxWidth(),
                enabled = homeScreenState.value.isButtonEnabled(),
                shape = MaterialTheme.shapes.large,
                onClick = {
                    onEvent(HomeScreenUiEvents.SetBottomSheetIndex(0))
                }) {
                Text(
                    text = "Modify Points",
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}
