package com.doancnpm.edoctor.domain.entity

import androidx.annotation.DrawableRes

data class Card(
  val id: String,
  val type: String,
  val country: String,
  val last4: String,
  val expiredMonth: Int,
  val expiredYear: Int,
  val cardHolderName: String,
  @DrawableRes val imageDrawableId: Int,
)