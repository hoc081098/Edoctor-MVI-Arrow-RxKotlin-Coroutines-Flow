package com.doancnpm.edoctor.data.remote.response

import com.squareup.moshi.Json

data class BaseResponse<Data: Any?>(
  @Json(name = "data") val `data`: Data,
  @Json(name = "messages") val messages: String, // Register almost completed. Please verify your phone
  @Json(name = "status_code") val statusCode: Int, // 200
  @Json(name = "success") val success: Boolean, // true
)