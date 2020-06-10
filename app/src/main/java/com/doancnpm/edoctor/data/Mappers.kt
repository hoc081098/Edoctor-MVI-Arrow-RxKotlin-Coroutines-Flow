package com.doancnpm.edoctor.data

import android.database.sqlite.SQLiteException
import com.doancnpm.edoctor.data.local.model.UserLocal
import com.doancnpm.edoctor.data.remote.response.*
import com.doancnpm.edoctor.domain.entity.*
import com.doancnpm.edoctor.domain.entity.User.RoleId.CUSTOMER
import com.doancnpm.edoctor.domain.entity.User.RoleId.DOCTOR
import com.doancnpm.edoctor.utils.parseDate_yyyyMMdd_HHmmss
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import android.location.Location as AndroidLocation

//region Mappers
fun User.RoleId.toInt(): Int {
  return when (this) {
    CUSTOMER -> 2
    DOCTOR -> 3
  }
}

fun Int.toRoleId(): User.RoleId {
  return when (this) {
    2 -> CUSTOMER
    3 -> DOCTOR
    else -> error("Cannot convert roleId")
  }
}

fun LoginUserResponse.User.toUserLocal(): UserLocal {
  return UserLocal(
    id = id,
    fullName = fullName,
    phone = phone,
    roleId = roleId,
    status = status,
    avatar = avatar,
    birthday = birthday,
  )
}

fun UserLocal.toUserDomain(): User {
  return User(
    id = id,
    fullName = fullName,
    phone = phone,
    roleId = roleId.toRoleId(),
    status = status,
    avatar = avatar,
    birthday = birthday,
  )
}

fun CategoriesResponse.Category.toCategoryDomain(baseUrl: String): Category {
  return Category(
    id = id,
    name = name,
    description = description,
    image = baseUrl + image.url,
  )
}

fun ServicesResponse.Service.toServiceDomain(baseUrl: String): Service {
  return Service(
    id = id,
    name = name,
    description = description,
    image = baseUrl + image.url,
    price = price
  )
}

fun AndroidLocation.toLocationDomain(): Location {
  return Location(
    latitude = latitude,
    longitude = longitude,
  )
}

fun PromotionsResponse.Promotion.toPromotionDomain(): Promotion {
  return Promotion(
    id = id,
    name = name,
    discount = discount,
    startDate = parseDate_yyyyMMdd_HHmmss(startDate)!!,
    endDate = parseDate_yyyyMMdd_HHmmss(endDate)!!,
  )
}

//endregion

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
          is SocketException -> AppError.Remote.NetworkError(throwable)
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
              errorMessage = response.messages,
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