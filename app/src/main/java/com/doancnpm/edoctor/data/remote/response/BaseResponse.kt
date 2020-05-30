package com.doancnpm.edoctor.data.remote.response

import com.doancnpm.edoctor.domain.entity.AppError
import com.squareup.moshi.Json

data class BaseResponse<Data : Any>(
  @Json(name = "data") private val `data`: Data,
  @Json(name = "messages") val messages: String, // Register almost completed. Please verify your phone
  @Json(name = "status_code") val statusCode: Int, // 200
  @Json(name = "success") val success: Boolean, // true
) {

  /**
   * Return data in [this@unwrapResponse] if [this@unwrapResponse.success] is true.
   * Otherwise, throws [AppError.Remote.ServerError]
   * @param  this@unwrap that needed to unwrap
   * @return Data in response
   * @throws AppError.Remote.ServerError
   */
  fun unwrap(): Data {
    return if (success) {
      data
    } else {
      throw AppError.Remote.ServerError(
        errorMessage = "Response is not success",
        statusCode = -1
      )
    }
  }
}