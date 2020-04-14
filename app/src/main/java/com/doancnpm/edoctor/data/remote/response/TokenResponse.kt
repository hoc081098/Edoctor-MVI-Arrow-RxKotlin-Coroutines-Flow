package com.doancnpm.edoctor.data.remote.response

import com.squareup.moshi.Json

data class TokenResponse(
  @Json(name = "token") val token: String?,
  @Json(name = "message") val message: String,
)