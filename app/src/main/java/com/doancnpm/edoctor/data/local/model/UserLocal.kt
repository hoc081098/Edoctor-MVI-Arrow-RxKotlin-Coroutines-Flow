package com.doancnpm.edoctor.data.local.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserLocal(
  @Json(name = "id") val id: Int,
  @Json(name = "full_name") val fullName: String,
  @Json(name = "phone") val phone: String,
  @Json(name = "role_id") val roleId: Int,
  @Json(name = "status") val status: Int,
  @Json(name = "avatar") val avatar: String?,
  @Json(name = "birthday") val birthday: String?,
)