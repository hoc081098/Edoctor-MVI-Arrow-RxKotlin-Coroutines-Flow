package com.doancnpm.edoctor.domain.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.Date

@Parcelize
data class User(
  val id: Long,
  val phone: String,
  val fullName: String,
  val roleId: RoleId,
  val status: Status,
  val avatar: String?,
  val birthday: Date?,
) : Parcelable {
  enum class RoleId { CUSTOMER, DOCTOR }
  enum class Status {
    INACTIVE,
    ACTIVE,
    PENDING
  }
}