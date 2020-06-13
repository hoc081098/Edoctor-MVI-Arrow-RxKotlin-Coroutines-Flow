package com.doancnpm.edoctor.domain.entity

data class Card(
  val holderName: String,
  val number: String,
  val expiredMonth: Int,
  val expiredYear: Int,
  val cvc: Int,
)