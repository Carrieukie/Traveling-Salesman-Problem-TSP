package com.karis.travellingsalesman.utils

import okio.IOException
import org.json.JSONObject
import retrofit2.HttpException
import kotlin.coroutines.cancellation.CancellationException

suspend fun <T> safeApiCall(apiCall: suspend () -> T): NetworkResult<T> {
    return try {
        NetworkResult.Success(apiCall.invoke())
    }
//    catch (cancellationException: CancellationException) {
//        throw cancellationException
//    }
    catch (throwable: Throwable) {
        when (throwable) {
            is IOException -> {
                NetworkResult.Error(
                    errorCode = null,
                    errorMessage = throwable.message ?: "Network Error",
                    data = null
                )
            }

            is HttpException -> {
                val code = throwable.code()

                val errorResponse = throwable.response()?.errorBody()?.string()
                val errorJson = JSONObject(errorResponse ?: "{}")
                val errorMessage = errorJson.optString("error_message")

                NetworkResult.Error(
                    errorCode = code,
                    errorMessage = errorMessage ?: "Server Error",
                )
            }

            else -> {
                NetworkResult.Error(
                    errorCode = null,
                    errorMessage = throwable.message ?: "Unknown Error",
                    data = null
                )
            }
        }
    }
}