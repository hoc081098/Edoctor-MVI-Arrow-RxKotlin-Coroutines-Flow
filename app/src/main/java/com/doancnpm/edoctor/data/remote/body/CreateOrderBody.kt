package com.doancnpm.edoctor.data.remote.body

import com.squareup.moshi.Json

data class CreateOrderBody(
  @Json(name = "end_time") val endTime: String, // 2020-10-20 10:59:30
  @Json(name = "lat") val lat: Double, // 102.4932
  @Json(name = "lng") val lng: Double, // 292.231
  @Json(name = "note") val note: String?, // This is a note
  @Json(name = "original_price") val originalPrice: Double, // 100.0
  @Json(name = "pay_card_id") val payCardId: String, // abc-xyz
  @Json(name = "promotion_id") val promotionId: Long?, // 22
  @Json(name = "service_id") val serviceId: Long, // 20
  @Json(name = "start_time") val startTime: String, // 2020-10-08 10:59:59
  @Json(name = "total") val total: Double, // 95.2
)