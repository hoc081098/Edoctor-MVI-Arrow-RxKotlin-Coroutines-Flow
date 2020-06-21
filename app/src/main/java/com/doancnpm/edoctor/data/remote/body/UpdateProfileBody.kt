package com.doancnpm.edoctor.data.remote.body

import com.squareup.moshi.Json

data class UpdateProfileBody(
  @Json(name = "full_name")
  val fullName: String?, // Nguyen Thai Hoc
  @Json(name = "birthday")
  val birthday: String?, // 1998-10-08
  @Json(name = "avatar")
  val avatar: Long? // 20
)