package com.doancnpm.edoctor.data.remote.response

import com.squareup.moshi.Json

data class GeocoderResponse(
  @Json(name = "results")
  val results: List<Result>,
  @Json(name = "status")
  val status: String // OK
) {
  data class Result(
    @Json(name = "formatted_address")
    val formattedAddress: String, // 277 Bedford Ave, Brooklyn, NY 11211, USA
  )
}