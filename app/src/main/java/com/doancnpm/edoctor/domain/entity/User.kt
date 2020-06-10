package com.doancnpm.edoctor.domain.entity

data class User(
  val id: Long,
  val phone: String,
  val fullName: String,
  val roleId: RoleId,
  val status: Int,
  val avatar: String?,
  val birthday: String?,
) {
  enum class RoleId { CUSTOMER, DOCTOR }
}