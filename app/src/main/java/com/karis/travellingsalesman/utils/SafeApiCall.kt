package com.karis.travellingsalesman.utils

suspend fun <T> safeApiCall(apiCall: suspend () -> T): NetworkResult<T> {
    return try {
        NetworkResult.Success(apiCall.invoke())
    } catch (e: Exception) {
        NetworkResult.Error(
            errorCode = e.hashCode(),
            errorMessage = e.message,
        )
    }
}