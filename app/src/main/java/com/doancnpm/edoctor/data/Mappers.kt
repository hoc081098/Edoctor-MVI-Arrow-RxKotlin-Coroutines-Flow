package com.doancnpm.edoctor.data

import android.database.sqlite.SQLiteException
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.data.local.model.UserLocal
import com.doancnpm.edoctor.data.remote.response.*
import com.doancnpm.edoctor.domain.entity.*
import com.doancnpm.edoctor.domain.entity.User.RoleId.CUSTOMER
import com.doancnpm.edoctor.domain.entity.User.RoleId.DOCTOR
import com.doancnpm.edoctor.utils.parseDate_yyyyMMdd
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

private fun Int.toRoleId(): User.RoleId {
  return when (this) {
    2 -> CUSTOMER
    3 -> DOCTOR
    else -> error("Cannot convert user roleId: $this")
  }
}

private fun Int.toUserStatus(): User.Status {
  return when (this) {
    0 -> User.Status.INACTIVE
    1 -> User.Status.ACTIVE
    2 -> User.Status.PENDING
    else -> error("Cannot convert user status: $this")
  }
}

fun LoginUserResponse.User.toUserLocal(baseUrl: String): UserLocal {
  return UserLocal(
    id = id,
    fullName = fullName,
    phone = phone,
    roleId = roleId,
    status = status,
    avatar = avatar?.let { "$baseUrl$it" },
    birthday = birthday,
  )
}

fun UserLocal.toUserDomain(): User {
  return User(
    id = id,
    fullName = fullName,
    phone = phone,
    roleId = roleId.toRoleId(),
    status = status.toUserStatus(),
    avatar = avatar,
    birthday = birthday?.let { parseDate_yyyyMMdd(it) },
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

fun CardResponse.toCardDomain(): Card {
  return Card(
    id = id,
    type = type,
    country = country,
    last4 = last4,
    expiredMonth = expMonth,
    expiredYear = expYear,
    cardHolderName = cardHolderName,
    imageDrawableId = when (type) {
      "visa" -> R.drawable.visacard
      "mastercard" -> R.drawable.mastercard
      else -> error("Not support type: $type")
    }
  )
}

fun NotificationsResponse.Notification.toNotificationDomain(baseUrl: String): Notification {
  return Notification(
    id = id,
    type = type,
    title = title,
    body = body,
    image = baseUrl + image,
    orderId = orderId,
    createdAt = parseDate_yyyyMMdd_HHmmss(createdAt)!!,
  )
}

fun Order.Status.toInt(): Int {
  return when (this) {
    Order.Status.PENDING_STATUS -> 1
    Order.Status.ACCEPTED_STATUS -> 2
    Order.Status.CUSTOMER_CANCELED_STATUS -> 3
    Order.Status.DOCTOR_CANCELED_STATUS -> 4
    Order.Status.PROCESSING_STATUS -> 5
    Order.Status.DONE_STATUS -> 6
  }
}

private fun Int.toOrderStatus(): Order.Status {
  return when (this) {
    1 -> Order.Status.PENDING_STATUS
    2 -> Order.Status.ACCEPTED_STATUS
    3 -> Order.Status.CUSTOMER_CANCELED_STATUS
    4 -> Order.Status.DOCTOR_CANCELED_STATUS
    5 -> Order.Status.PROCESSING_STATUS
    6 -> Order.Status.DONE_STATUS
    else -> error("Cannot convert order status: $this")
  }
}

fun OrdersResponse.Order.toOrderDomain(baseUrl: String): Order {
  return Order(
    id = id,
    serviceId = serviceId,
    startTime = parseDate_yyyyMMdd_HHmmss(startTime)!!,
    endTime = parseDate_yyyyMMdd_HHmmss(endTime)!!,
    note = note,
    originalPrice = originalPrice,
    promotionId = promotionId,
    total = total,
    payCardId = payCardId,
    status = status.toOrderStatus(),
    location = Location(
      latitude = lat,
      longitude = lng,
    ),
    address = address,
    createdAt = parseDate_yyyyMMdd_HHmmss(createdAt)!!,
    service = service.toServiceDomain(baseUrl),
    doctor = doctor?.toUserLocal(baseUrl)?.toUserDomain(),
    customer = customer.toUserLocal(baseUrl).toUserDomain(),
  )
}

fun CreateOrderResponse.toOrderDomain(baseUrl: String): Order {
  return Order(
    id = id,
    serviceId = serviceId,
    startTime = parseDate_yyyyMMdd_HHmmss(startTime)!!,
    endTime = parseDate_yyyyMMdd_HHmmss(endTime)!!,
    note = note,
    originalPrice = originalPrice,
    promotionId = promotionId,
    total = total,
    payCardId = payCardId,
    status = Order.Status.PENDING_STATUS,
    location = Location(
      latitude = lat,
      longitude = lng,
    ),
    address = address,
    createdAt = parseDate_yyyyMMdd_HHmmss(createdAt)!!,
    service = null,
    doctor = null,
    customer = null,
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