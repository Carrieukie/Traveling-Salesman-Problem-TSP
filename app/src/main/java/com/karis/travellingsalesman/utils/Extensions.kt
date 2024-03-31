package com.karis.travellingsalesman.utils

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async

fun convertSecondsToTime(seconds: Long): String {
    var remainingSeconds = seconds

    val years = remainingSeconds / (60 * 60 * 24 * 365)
    remainingSeconds %= 60 * 60 * 24 * 365

    val months = remainingSeconds / (60 * 60 * 24 * 30)
    remainingSeconds %= 60 * 60 * 24 * 30

    val weeks = remainingSeconds / (60 * 60 * 24 * 7)
    remainingSeconds %= 60 * 60 * 24 * 7

    val days = remainingSeconds / (60 * 60 * 24)
    remainingSeconds %= 60 * 60 * 24

    val hours = remainingSeconds / (60 * 60)
    remainingSeconds %= 60 * 60

    val minutes = remainingSeconds / 60
    remainingSeconds %= 60

    return buildString {
        append("(")
        if (years > 0) append("$years yrs ")
        if (months > 0) append("$months mths ")
        if (weeks > 0) append("$weeks wks ")
        if (days > 0) append("$days dys ")
        if (hours > 0) append("$hours hr ")
        if (minutes > 0) append("$minutes min")
        append(")")
    }
}

/**
 * Utility function to execute asynchronous tasks for each item in a list.
 * @param list The list of items to process asynchronously.
 * @param block The suspend function to apply to each item.
 * @return A list of deferred results representing the asynchronous computations.
 */
fun <T, V> CoroutineScope.asyncAll(list: List<T>, block: suspend (T) -> V): List<Deferred<V>> {
    // Map each item to a deferred result representing the asynchronous computation.
    return list.map { item ->
        async { block.invoke(item) }
    }
}

/**
 * Decodes an encoded polyline string into a list of LatLng points.
 * @return The list of LatLng points decoded from the encoded polyline.
 */
fun String.decodeEncodedPolyline(): List<LatLng> {
    val poly = mutableListOf<LatLng>()
    var index = 0
    val len = length
    var lat = 0
    var lng = 0

    // Decode each point in the polyline
    while (index < len) {
        var b: Int
        var shift = 0
        var result = 0

        // Decode latitude
        do {
            b = get(index++).code - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)

        val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lat += dlat

        shift = 0
        result = 0

        // Decode longitude
        do {
            b = get(index++).code - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)

        val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lng += dlng

        // Create LatLng point and add to the list
        val p = LatLng(
            lat.toDouble() / 1E5,
            lng.toDouble() / 1E5
        )
        poly.add(p)
    }
    return poly
}


fun List<LatLng>.getCenterLatLngs(): LatLngBounds {
    val centerBuilder: LatLngBounds.Builder = LatLngBounds.builder()
    forEach { centerBuilder.include(LatLng(it.latitude, it.longitude)) }
    return centerBuilder.build()
}

private data class CameraViewCoord(
    val yMax: Double,
    val yMin: Double,
    val xMax: Double,
    val xMin: Double
)

fun List<LatLng>.calculateCameraViewPoints(pctView: Double = .25): List<LatLng> {
    val coordMax = findMaxMins()
    val dy = coordMax.yMax - coordMax.yMin
    val dx = coordMax.xMax - coordMax.xMin
    val yT = (dy * pctView) + coordMax.yMax
    val yB = coordMax.yMin - (dy * pctView)
    val xR = (dx * pctView) + coordMax.xMax
    val xL = coordMax.xMin - (dx * pctView)
    return listOf(
        LatLng(coordMax.xMax, yT),
        LatLng(coordMax.xMin, yB),
        LatLng(xR, coordMax.yMax),
        LatLng(xL, coordMax.yMin)
    )
}

private fun List<LatLng>.findMaxMins(): CameraViewCoord {
    check(size > 0) { "Cannot calculate the view coordinates of nothing." }
    var viewCoord: CameraViewCoord? = null
    for(point in this) {
        viewCoord = CameraViewCoord(
            yMax = viewCoord?.yMax?.let { yMax ->
                if (point.longitude > yMax) {
                    point.longitude
                } else {
                    yMax
                }
            } ?: point.longitude,
            yMin = viewCoord?.yMin?.let { yMin->
                if (point.longitude < yMin) {
                    point.longitude
                } else {
                    yMin
                }
            } ?: point.longitude,
            xMax = viewCoord?.xMax?.let { xMax->
                if (point.latitude > xMax) {
                    point.latitude
                } else {
                    xMax
                }
            } ?: point.latitude,
            xMin = viewCoord?.xMin?.let { xMin->
                if (point.latitude < xMin) {
                    point.latitude
                } else {
                    xMin
                }
            } ?: point.latitude,
        )
    }
    return viewCoord ?: throw IllegalStateException("viewCoord cannot be null.")
}
