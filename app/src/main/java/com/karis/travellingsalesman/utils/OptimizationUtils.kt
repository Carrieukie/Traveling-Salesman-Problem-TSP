package com.karis.travellingsalesman.utils

import com.google.maps.model.DistanceMatrix

fun DistanceMatrix.getAdjacencyMatrix(
    adjacencyMatrixCreationType: AdjacencyMatrixCreationType = AdjacencyMatrixCreationType.DISTANCE
): Array<LongArray>? {
    return try {
        Array(rows.size) { i ->
            LongArray(rows.size) { j ->
                val value = when (adjacencyMatrixCreationType) {
                    AdjacencyMatrixCreationType.DISTANCE -> rows[i].elements[j].distance.inMeters
                    AdjacencyMatrixCreationType.DURATION -> rows[i].elements[j].duration.inSeconds
                }
                value
            }
        }
    } catch (throwable: Throwable) {
        null
    }
}
fun heldKarpgetShortestTimePath(
    timeAdjacencyMatrix: Array<LongArray>,
    distanceAdjacencyMatrix: Array<LongArray>
): TSPResult {
    val n = timeAdjacencyMatrix.size

    val maxTime = Long.MAX_VALUE / 2
    val dp = Array(1 shl n) { LongArray(n) { maxTime } }
    val path = Array(1 shl n) { IntArray(n) { -1 } }
    dp[1][0] = 0

    for (mask in 1 until (1 shl n)) {
        for (i in 0 until n) {
            if (mask and (1 shl i) == 0) continue
            for (j in 0 until n) {
                if (i == j || mask and (1 shl j) == 0) continue
                if (dp[mask xor (1 shl i)][j] + timeAdjacencyMatrix[j][i] < dp[mask][i]) {
                    dp[mask][i] = dp[mask xor (1 shl i)][j] + timeAdjacencyMatrix[j][i]
                    path[mask][i] = j
                }
            }
        }
    }

    val finalMask = (1 shl n) - 1
    val finalPath = IntArray(n + 1)
    var cur = 0

    for (i in 1 until n) {
        if (dp[finalMask][i] + timeAdjacencyMatrix[i][0] < dp[finalMask][cur]) {
            cur = i
        }
    }

    var mask = finalMask
    for (i in n - 1 downTo 1) {
        finalPath[i] = cur
        val next = path[mask][cur]
        mask = mask xor (1 shl cur)
        cur = next
    }
    finalPath[0] = cur


    // Calculate total distance and time
    var totalDistance = 0L
    var totalTime = 0L
    for (i in 0 until finalPath.size - 1) {
        totalDistance += distanceAdjacencyMatrix[finalPath[i]][finalPath[i + 1]]
        totalTime += timeAdjacencyMatrix[finalPath[i]][finalPath[i + 1]]
    }

    return TSPResult(finalPath.toList(), totalDistance, totalTime)
}


data class TSPResult(
    val path: List<Int>,
    val distance: Long,
    val time: Long
)

enum class AdjacencyMatrixCreationType {
    DISTANCE,
    DURATION
}
