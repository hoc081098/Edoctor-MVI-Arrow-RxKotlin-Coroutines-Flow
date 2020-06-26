package com.doancnpm.edoctor.domain.entity

import arrow.core.Either

sealed class AppError(cause: Throwable?) : Throwable(cause) {
  object WrongRole : AppError(null)

  sealed class Remote(cause: Throwable?) : AppError(cause) {
    data class NetworkError(override val cause: Throwable) : Remote(cause)

    data class ServerError(
      val errorMessage: String,
      val statusCode: Int,
      override val cause: Throwable? = null,
    ) : Remote(cause)

  }

  sealed class Local(cause: Throwable?) : AppError(cause) {
    data class DatabaseError(override val cause: Throwable) : AppError(cause)
  }

  data class UnexpectedError(
    val errorMessage: String,
    override val cause: Throwable,
  ) : AppError(cause)

  sealed class LocationError(cause: Throwable?) : AppError(cause) {
    object TimeoutGetCurrentLocation : LocationError(null)
    data class LocationSettingsDisabled(val throwable: Throwable) : LocationError(throwable)
    object GeocoderEmptyResult : LocationError(null)
  }
}

fun AppError.getMessage(): String {
  return when (this) {
    is AppError.Remote.NetworkError -> "Network error"
    is AppError.Remote.ServerError -> "Server error: $errorMessage"
    is AppError.Local.DatabaseError -> "Database error"
    is AppError.UnexpectedError -> "Unexpected error"
    AppError.LocationError.TimeoutGetCurrentLocation -> "Timeout to get current location. Please try again!"
    is AppError.LocationError.LocationSettingsDisabled -> "Location settings disabled. Please enable to continue!"
    AppError.LocationError.GeocoderEmptyResult -> "Cannot get address from coordinates"
    AppError.WrongRole -> "This app only supports customer roles"
  }
}

typealias DomainResult<T> = Either<AppError, T>

@Suppress("NOTHING_TO_INLINE")
inline fun <R> AppError.leftResult(): Either<AppError, R> = Either.Left(this)

@Suppress("NOTHING_TO_INLINE")
inline fun <R> R.rightResult(): Either<AppError, R> = Either.Right(this)