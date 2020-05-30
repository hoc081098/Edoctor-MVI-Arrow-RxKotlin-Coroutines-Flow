package com.doancnpm.edoctor.data.remote.body

import com.squareup.moshi.Json

data class LoginUserBody(
  @Json(name = "device_token") val deviceToken: String, // abcabc
  @Json(name = "password") val password: String, // 123456
  @Json(name = "phone") val phone: String, // +84363438135
)