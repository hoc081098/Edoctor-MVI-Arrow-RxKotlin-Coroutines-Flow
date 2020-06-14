package com.doancnpm.edoctor.data.remote.body

import com.squareup.moshi.Json

data class AddCardBody(
  @Json(name = "card_holder_name")
  val cardHolderName: String, // Card holder name
  @Json(name = "number")
  val number: String, // 000011112222333
  @Json(name = "exp_month")
  val expMonth: Int, // 3
  @Json(name = "exp_year")
  val expYear: Int, // 21
  @Json(name = "cvc")
  val cvc: Int // 113
)