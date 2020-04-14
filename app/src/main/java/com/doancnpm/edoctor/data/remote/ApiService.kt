package com.doancnpm.edoctor.data.remote

import com.doancnpm.edoctor.data.remote.response.TokenResponse
import com.doancnpm.edoctor.data.remote.response.UserResponse
import retrofit2.Retrofit
import retrofit2.create
import retrofit2.http.*

interface ApiService {

  @Headers("@: NoAuth")
  @POST("users/authenticate")
  suspend fun login(@Header("Authorization") authorization: String): TokenResponse

  @GET("users/{email}")
  suspend fun getUserProfile(@Path("email") email: String): UserResponse

  companion object Factory {
    operator fun invoke(retrofit: Retrofit) = retrofit.create<ApiService>()
  }
}