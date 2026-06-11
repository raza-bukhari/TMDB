package com.example.tmdb.core.network

import com.example.tmdb.domain.model.AppError
import com.example.tmdb.domain.model.AppException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.delay
import retrofit2.HttpException

/** Maps transport failures to typed [AppError]s at the data-layer edge. */
fun Throwable.toAppError(): AppError = when (this) {
    is HttpException -> when (code()) {
        401 -> AppError.InvalidToken
        404 -> AppError.NotFound
        429 -> AppError.RateLimited
        else -> AppError.Unknown(this)
    }
    is IOException -> AppError.Offline
    else -> AppError.Unknown(this)
}

private const val MAX_RETRIES = 2
private const val BASE_DELAY_MS = 500L

/**
 * Runs a TMDB call, returning failures as [AppException]-typed [Result]s.
 * Transient failures (offline, 429) retry up to [MAX_RETRIES] times with
 * exponential backoff (500ms, 1s) — bounded so UI error states appear promptly.
 */
suspend fun <T> tmdbCall(block: suspend () -> T): Result<T> {
    var attempt = 0
    while (true) {
        try {
            return Result.success(block())
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (t: Throwable) {
            val error = t.toAppError()
            val transient = error is AppError.Offline || error is AppError.RateLimited
            if (!transient || attempt == MAX_RETRIES) {
                return Result.failure(AppException(error))
            }
            delay(BASE_DELAY_MS shl attempt)
            attempt++
        }
    }
}
