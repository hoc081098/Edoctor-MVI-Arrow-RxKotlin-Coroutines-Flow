package com.doancnpm.edoctor.domain.entity

import java.util.Date

data class Notification(
  val id: Long,
  val type: String,
  val title: String,
  val body: String,
  val image: String?,
  val orderId: Long?,
  val createdAt: Date
)