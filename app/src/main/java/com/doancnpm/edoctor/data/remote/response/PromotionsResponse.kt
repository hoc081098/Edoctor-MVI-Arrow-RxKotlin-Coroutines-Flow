package com.doancnpm.edoctor.data.remote.response

import com.squareup.moshi.Json
import java.util.*

data class PromotionsResponse(
  @Json(name = "pagination") val pagination: Pagination,
  @Json(name = "promotions") val promotions: List<Promotion>
) {
  data class Promotion(
    @Json(name = "discount") val discount: Double, // 0.15
    @Json(name = "end_date") val endDate: String, // 2020-06-11 00:00:00
    @Json(name = "id") val id: Long, // 5
    @Json(name = "name") val name: String, // Corrine
    @Json(name = "start_date") val startDate: String, // 2020-05-31 00:00:00
  )
}