package com.doancnpm.edoctor.data.remote.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeocoderErrorResponse(
  @Json(name = "error_message") val errorMessage: String, // The provided API key is invalid.
  @Json(name = "results") val results: List<Any>,
  @Json(name = "status") val status: String // REQUEST_DENIED
)