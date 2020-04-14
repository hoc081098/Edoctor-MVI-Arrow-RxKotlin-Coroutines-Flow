package com.doancnpm.edoctor.domain.entity

import arrow.core.Either

sealed class AppError(cause: Throwable) : Throwable(cause) {

}

typealias DomainResult<T> = Either<AppError, T>