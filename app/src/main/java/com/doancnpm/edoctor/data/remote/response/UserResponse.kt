package com.doancnpm.edoctor.data.remote.response

import com.squareup.moshi.Json
import java.util.*

data class UserResponse(
  @Json(name = "name") val name: String,
  @Json(name = "email") val email: String,
  @Json(name = "created_at") val createdAt: Date,
  @Json(name = "image_url") val imageUrl: String,
)