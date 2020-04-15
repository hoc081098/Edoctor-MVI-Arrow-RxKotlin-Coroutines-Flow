package com.doancnpm.edoctor.data.local.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.*

@JsonClass(generateAdapter = true)
data class UserLocal(
  @Json(name = "name") val name: String,
  @Json(name = "email") val email: String,
  @Json(name = "created_at") val createdAt: Date,
  @Json(name = "image_url") val imageUrl: String,
)