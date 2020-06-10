package com.doancnpm.edoctor.domain.entity

import java.util.*

data class Promotion(
  val id: Long,
  val name: String,
  val discount: Double,
  val startDate: Date,
  val endDate: Date,
)