package com.doancnpm.edoctor.data.remote.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ErrorResponse(
  @Json(name = "message") val message: String,
  @Json(name = "status_code") val statusCode: Int,
)