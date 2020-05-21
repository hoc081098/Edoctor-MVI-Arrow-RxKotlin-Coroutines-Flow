package com.doancnpm.edoctor.data.remote.body

import com.squareup.moshi.Json

data class RegisterUserBody(
  @Json(name = "birthday") val birthday: String?, // 1998-10-08
  @Json(name = "full_name") val fullName: String, // Hoc 1
  @Json(name = "password") val password: String, // 123456
  @Json(name = "phone") val phone: String, // +84363438135
  @Json(name = "role_id") val roleId: Int, // 2
)