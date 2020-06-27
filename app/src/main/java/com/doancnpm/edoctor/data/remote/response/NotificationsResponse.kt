package com.doancnpm.edoctor.data.remote.response

import com.squareup.moshi.Json

data class NotificationsResponse(
  @Json(name = "notifications")
  val notifications: List<Notification>,
  @Json(name = "pagination")
  val pagination: Pagination
) {
  data class Notification(
    @Json(name = "id")
    val id: Long, // 98
    @Json(name = "type")
    val type: String, // New mission
    @Json(name = "title")
    val title: String, // New mission !!!
    @Json(name = "body")
    val body: String, // Petrus Hoc asked new mission for Edna
    @Json(name = "image")
    val image: String?, // system/services/service.jpg
    @Json(name = "order_id")
    val orderId: Long?, // 65
    @Json(name = "created_at")
    val createdAt: String // 2020-06-15 09:37:14
  )
}