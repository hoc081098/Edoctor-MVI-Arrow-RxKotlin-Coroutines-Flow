package com.doancnpm.edoctor.data.remote.response

import com.squareup.moshi.Json

data class LoginUserResponse(
  @Json(name = "token")
  val token: String, // eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOlwvXC81NC44NC4xOTUuMTU1XC9hcGlcL3YxXC9sb2dpbiIsImlhdCI6MTU5MDA2Mjc1OSwibmJmIjoxNTkwMDYyNzU5LCJqdGkiOiJFbWZSZzNmYXVTR1k4QkhiIiwic3ViIjoxNiwicHJ2IjoiMjNiZDVjODk0OWY2MDBhZGIzOWU3MDFjNDAwODcyZGI3YTU5NzZmNyJ9.-lgqLaW9mBFnW6p04W8NdOFc5NvF-Tj7rJ6KAv8aofY
  @Json(name = "user")
  val user: User
) {
  data class User(
    @Json(name = "avatar")
    val avatar: String? = null, // null
    @Json(name = "birthday")
    val birthday: String?, // null
    @Json(name = "full_name")
    val fullName: String, // Son Vo
    @Json(name = "id")
    val id: Int, // 16
    @Json(name = "phone")
    val phone: String, // +84354658717
    @Json(name = "role_id")
    val roleId: Int, // 2
    @Json(name = "status")
    val status: Int // 1
  )
}