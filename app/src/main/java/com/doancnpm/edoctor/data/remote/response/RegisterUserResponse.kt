package com.doancnpm.edoctor.data.remote.response


import com.squareup.moshi.Json

data class RegisterUserResponse(
  @Json(name = "avatar") val avatar: String? = null, // null
  @Json(name = "full_name") val fullName: String, // Hoc 1
  @Json(name = "id") val id: Long, // 17
  @Json(name = "phone") val phone: String, // +84363438135
  @Json(name = "role_id") val roleId: Int // 2
)