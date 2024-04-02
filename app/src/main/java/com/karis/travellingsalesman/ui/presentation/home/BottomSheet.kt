package com.karis.travellingsalesman.ui.presentation.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karis.travellingsalesman.domain.models.Point
import com.karis.travellingsalesman.utils.convertSecondsToTime


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
            Text(
                text = "Your Tour",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 14.sp
                ),
                color = MaterialTheme.colorScheme.primary,
            )
            val tour = homeScreenState.value.pointsTour ?: mutableListOf()
            for (i in tour.indices) {
                val point = homeScreenState.value.pointsTour?.get(i)
                point?.let {
                    Text(
                        text = "${i + 1}. ${point.name}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
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
