package com.doancnpm.edoctor.data.remote.response

import com.squareup.moshi.Json

data class CardResponse(
  @Json(name = "id")
  val id: String, // pm_1GtcmFC98CIg3r8PJfU4XI74
  @Json(name = "type")
  val type: String, // visa
  @Json(name = "country")
  val country: String, // US
  @Json(name = "last4")
  val last4: String, // 4242
  @Json(name = "exp_month")
  val expMonth: Int, // 3
  @Json(name = "exp_year")
  val expYear: Int, // 2022
  @Json(name = "card_holder_name")
  val cardHolderName: String // Petrus name
)