package com.doancnpm.edoctor.domain.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
  val id: Long,
  val phone: String,
  val fullName: String,
  val roleId: RoleId,
  val status: Status,
  val avatar: String?,
  val birthday: String?,
) : Parcelable {
  enum class RoleId { CUSTOMER, DOCTOR }
  enum class Status {
    INACTIVE,
    ACTIVE,
    PENDING
  }
}