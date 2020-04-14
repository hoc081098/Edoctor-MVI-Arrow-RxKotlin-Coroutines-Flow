package com.doancnpm.edoctor.data

import android.database.sqlite.SQLiteException
import arrow.core.left
import com.doancnpm.edoctor.data.remote.response.ErrorResponse
import com.doancnpm.edoctor.domain.entity.AppError
import com.doancnpm.edoctor.domain.entity.DomainResult
import retrofit2.HttpException
import retrofit2.Retrofit
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object Mappers {

}

class ErrorMapper(private val retrofit: Retrofit) {
  /**
   * Transform [throwable] to [AppError]
   */
  private fun map(throwable: Throwable): AppError {
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
          .let { body ->
            retrofit
              .responseBodyConverter<ErrorResponse>(
                ErrorResponse::class.java,
                ErrorResponse::class.java.annotations,
              )
              .convert(body)!!
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
  fun <T> mapAsLeft(throwable: Throwable): DomainResult<T> = map(throwable).left()
}