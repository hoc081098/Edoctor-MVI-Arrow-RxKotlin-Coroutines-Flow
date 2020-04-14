package com.doancnpm.edoctor.domain.entity

import arrow.core.Either

sealed class AppError(cause: Throwable?) : Throwable(cause) {
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
}

typealias DomainResult<T> = Either<AppError, T>