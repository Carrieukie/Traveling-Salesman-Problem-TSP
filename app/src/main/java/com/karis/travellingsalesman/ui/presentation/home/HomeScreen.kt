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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import com.karis.travellingsalesman.domain.models.Place
import com.karis.travellingsalesman.domain.models.Point
import com.karis.travellingsalesman.utils.convertSecondsToTime
import com.karis.travellingsalesman.utils.observeAsEvents

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeScreenViewModel: HomeScreenViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val homeScreenState = homeScreenViewModel.mainActivityState.collectAsState()

    observeAsEvents(flow = homeScreenViewModel.oneShotEvents) {
        when (it) {
            is HomeScreenUiEvents.ShowSnackBar -> {
                Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
            }

            else -> {}
        }
    }

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = SheetState(
            initialValue = SheetValue.Expanded,
            skipPartiallyExpanded = false,
            density = LocalDensity.current
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
            val nairobi = LatLng(-1.286389, 36.817223)
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(nairobi, 12f)
            }
            GoogleMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                cameraPositionState = cameraPositionState
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BottomSheet(
    homeScreenState: State<HomeScreenState>,
    onEvent: (HomeScreenUiEvents) -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = {
        2
    })
    HorizontalPager(
        modifier = Modifier
            .animateContentSize()
            .wrapContentHeight(),
        state = pagerState,
        verticalAlignment = Alignment.Top,
        ) { page ->
        when(page){
            0 -> RouteOptimization(homeScreenState, onEvent)
            1 -> RouteOptimizationResults(homeScreenState)
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
                onClick = {
                    onEvent(HomeScreenUiEvents.OptimizeRoute)
                }) {
                Text(text = "Optimize")
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
                value = homeScreenState.value.points[point.id]?.selectedPlace?.name ?: "",
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .weight(1f),
                onValueChange = {
                    onEvent(HomeScreenUiEvents.GetPrediction(point.id, it))
                    onEvent(
                        HomeScreenUiEvents.SelectPlace(
                            point.id,
                            Place(id = point.id.toString(), name = it)
                        )
                    )
                },
                placeholder = {
                    Text(
                        text = "Search location",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            )
            Icon(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .clickable {
                        onEvent(HomeScreenUiEvents.RemovePoint(point.id))
                    },
                imageVector = Icons.Default.Close,
                contentDescription = ""
            )

        }

        if (point.placeSuggestions.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .height(140.dp)
            ) {
                items(point.placeSuggestions) { place ->
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
    }
}

@Composable
fun RouteOptimizationResults(
    homeScreenState: State<HomeScreenState>
) {
    LazyColumn(
        modifier = Modifier
            .animateContentSize()
            .fillMaxWidth()
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
                text = "Fastest route now due to traffic conditions.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

