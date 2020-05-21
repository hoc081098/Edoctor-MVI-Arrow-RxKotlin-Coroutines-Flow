package com.doancnpm.edoctor.domain.entity

data class User(
  val id: Int,
  val phone: String,
  val fullName: String,
  val roleId: Int,
  val status: Int,
  val avatar: String?,
  val birthday: String?,
)