package com.karis.travellingsalesman.ui.presentation.home

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
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
                val tour = homeScreenState.value.pointsTour ?: mutableListOf()
                for (i in tour.indices) {
                    val point = homeScreenState.value.points[i]
                    point?.latLng?.let { latLng ->
                        Marker(
                            icon = BitmapDescriptorFactory.defaultMarker(),
                            state = rememberMarkerState(position = latLng),
                            snippet = point.name,
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
                    color = MaterialTheme.colorScheme.surface,
                    width = 10f
                )

            }
        }
    }
}

