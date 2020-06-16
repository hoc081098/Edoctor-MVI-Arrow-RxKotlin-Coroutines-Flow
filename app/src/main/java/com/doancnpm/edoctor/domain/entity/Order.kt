package com.doancnpm.edoctor.domain.entity

import java.util.Date

data class Order(
  val id: Long,
  val customerId: Long,
  val doctorId: Long?,
  val serviceId: Long,
  val startTime: Date,
  val endTime: Date,
  val note: String?,
  val originalPrice: Int,
  val promotionId: Long?,
  val total: Int,
  val payCardId: String,
  val status: Status,
  val location: Location,
  val address: String,
  val createdAt: Date,
  val service: Service,
  val doctor: User,
  val customer: User,
) {
  enum class Status {
    PENDING_STATUS,
    ACCEPTED_STATUS,
    CUSTOMER_CANCELED_STATUS,
    DOCTOR_CANCELED_STATUS,
    PROCESSING_STATUS,
    DONE_STATUS,
  }
}