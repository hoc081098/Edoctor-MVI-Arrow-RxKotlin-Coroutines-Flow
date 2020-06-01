package com.doancnpm.edoctor.domain.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Category(
  val id: Int,
  val name: String,
  val description: String,
  val image: String
) : Parcelable