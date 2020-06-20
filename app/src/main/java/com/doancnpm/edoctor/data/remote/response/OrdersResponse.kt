package com.doancnpm.edoctor.data.remote.response

import com.squareup.moshi.Json

data class OrdersResponse(
  @Json(name = "orders") val orders: List<Order>,
  @Json(name = "pagination") val pagination: Pagination
) {
  data class Order(
    @Json(name = "id") val id: Long, // 67
    @Json(name = "customer_id") val customerId: Long, // 18
    @Json(name = "doctor_id") val doctorId: Long?, // null
    @Json(name = "service_id") val serviceId: Long, // 15
    @Json(name = "start_time") val startTime: String, // 2020-06-23 15:26:27
    @Json(name = "end_time") val endTime: String, // 2020-06-24 15:26:31
    @Json(name = "note") val note: String?, // null
    @Json(name = "original_price") val originalPrice: Int, // 4308000
    @Json(name = "promotion_id") val promotionId: Long?, // null
    @Json(name = "total") val total: Int, // 4308000
    @Json(name = "pay_card_id") val payCardId: String, // pm_1Gu7vZC98CIg3r8PIs6r6aZq
    @Json(name = "status") val status: Int, // 1
    @Json(name = "lat") val lat: Double, // 16.0723965
    @Json(name = "lng") val lng: Double, // 108.1508566
    @Json(name = "address") val address: String, // 920/6 Tôn Đức Thắng, Hoà Khánh Bắc, Liên Chiểu, Đà Nẵng 550000, Vietnam
    @Json(name = "created_at") val createdAt: String, // 2020-06-15 15:26:48
    @Json(name = "service") val service: ServicesResponse.Service,
    @Json(name = "doctor") val doctor: LoginUserResponse.User?,
    @Json(name = "customer") val customer: LoginUserResponse.User,
  )
}