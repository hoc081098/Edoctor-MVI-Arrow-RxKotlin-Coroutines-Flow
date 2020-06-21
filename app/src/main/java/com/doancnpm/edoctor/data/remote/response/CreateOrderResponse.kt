package com.doancnpm.edoctor.data.remote.response


import com.squareup.moshi.Json

data class CreateOrderResponse(
  @Json(name = "customer_id")
  val customerId: Long, // 22
  @Json(name = "service_id")
  val serviceId: Long, // 15
  @Json(name = "start_time")
  val startTime: String, // 2020-06-29 12:55:53
  @Json(name = "end_time")
  val endTime: String, // 2020-06-30 14:55:57
  @Json(name = "note")
  val note: String?, // null
  @Json(name = "original_price")
  val originalPrice: Int, // 4308000
  @Json(name = "total")
  val total: Int, // 4308000
  @Json(name = "promotion_id")
  val promotionId: Long?, // null
  @Json(name = "pay_card_id")
  val payCardId: String, // pm_1GvfQPIL28bOVvPxdYPYq1yx
  @Json(name = "lat")
  val lat: Double, // 37.4219983
  @Json(name = "lng")
  val lng: Double, // -122.084
  @Json(name = "address")
  val address: String, // 1600 Amphitheatre Pkwy, Mountain View, CA 94043, USA
  @Json(name = "updated_at")
  val updatedAt: String, // 2020-06-20 14:56:49
  @Json(name = "created_at")
  val createdAt: String, // 2020-06-20 14:56:49
  @Json(name = "id")
  val id: Long // 113
)