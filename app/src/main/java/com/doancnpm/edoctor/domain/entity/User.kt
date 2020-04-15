package com.doancnpm.edoctor.domain.entity

import java.util.*

data class User(
  val name: String,
  val email: String,
  val createdAt: Date,
  val imageUrl: String,
  val token: String,
)