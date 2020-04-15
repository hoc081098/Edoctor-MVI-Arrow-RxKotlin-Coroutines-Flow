package com.doancnpm.edoctor.data

import android.database.sqlite.SQLiteException
import com.doancnpm.edoctor.data.local.model.UserLocal
import com.doancnpm.edoctor.data.remote.response.ErrorResponseJsonAdapter
import com.doancnpm.edoctor.data.remote.response.UserResponse
import com.doancnpm.edoctor.domain.entity.AppError
import com.doancnpm.edoctor.domain.entity.DomainResult
import com.doancnpm.edoctor.domain.entity.leftResult
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object Mappers {
  fun userReponseToUserLocal(userResponse: UserResponse): UserLocal {
    return UserLocal(
      name = userResponse.name,
      email = userResponse.email,
      createdAt = userResponse.createdAt,
      imageUrl = userResponse.imageUrl,
    )
  }
}

class ErrorMapper(private val errorResponseJsonAdapter: ErrorResponseJsonAdapter) {
  /**
   * Transform [throwable] to [AppError]
   */
  fun map(throwable: Throwable): AppError {
    return when (throwable) {
      is AppError -> throwable
      is SQLiteException -> {
        AppError.Local.DatabaseError(throwable)
      }
      is IOException -> {
        when (throwable) {
          is UnknownHostException -> AppError.Remote.NetworkError(throwable)
          is SocketTimeoutException -> AppError.Remote.NetworkError(throwable)
          else -> AppError.UnexpectedError(
            cause = throwable,
            errorMessage = "Unknown IOException: $throwable"
          )
        }
      }
      is HttpException -> {
        throwable.response()!!
          .takeUnless { it.isSuccessful }!!
          .errorBody()!!
          .use { body ->
            body.use { errorResponseJsonAdapter.fromJson(it.string()) }!!
          }
          .let { response ->
            AppError.Remote.ServerError(
              errorMessage = response.message,
              statusCode = response.statusCode,
              cause = throwable,
            )
          }
      }
      else -> {
        AppError.UnexpectedError(
          cause = throwable,
          errorMessage = "Unknown Throwable: $throwable"
        )
      }
    }
  }

  /**
   * Transform [throwable] to left branch of [DomainResult]
   */
  fun <T> mapAsLeft(throwable: Throwable) = map(throwable).leftResult<T>()
}