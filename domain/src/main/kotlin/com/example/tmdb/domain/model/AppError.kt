package com.example.tmdb.domain.model

/**
 * Typed failures crossing layer boundaries. The data layer maps transport
 * exceptions to these at its edge; presentation renders them, never raw exceptions.
 */
sealed interface AppError {
    /** No connectivity or host unreachable. */
    data object Offline : AppError

    /** TMDB rejected the bearer token (HTTP 401). */
    data object InvalidToken : AppError

    /** Resource does not exist (HTTP 404). */
    data object NotFound : AppError

    /** TMDB rate limit hit (HTTP 429); retry later. */
    data object RateLimited : AppError

    data class Unknown(val cause: Throwable? = null) : AppError
}

/** Carrier so typed errors can travel inside [kotlin.Result] failures. */
class AppException(val error: AppError) : Exception(error.toString())

/** The [AppError] inside a failed [Result], or [AppError.Unknown] for foreign exceptions. */
fun Result<*>.appErrorOrNull(): AppError? =
    exceptionOrNull()?.let { (it as? AppException)?.error ?: AppError.Unknown(it) }
