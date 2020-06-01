package com.doancnpm.edoctor.data.remote

import androidx.annotation.IntRange
import com.doancnpm.edoctor.data.remote.body.LoginUserBody
import com.doancnpm.edoctor.data.remote.body.RegisterUserBody
import com.doancnpm.edoctor.data.remote.response.*
import retrofit2.Retrofit
import retrofit2.create
import retrofit2.http.*

interface ApiService {

  //region User
  @Headers("@: NoAuth")
  @POST("login")
  suspend fun loginUser(@Body userBody: LoginUserBody): BaseResponse<LoginUserResponse>

  @Headers("@: NoAuth")
  @POST("register")
  suspend fun registerUser(@Body userBody: RegisterUserBody): BaseResponse<RegisterUserResponse>

  @Headers("@: NoAuth")
  @POST("resend-code")
  @FormUrlEncoded
  suspend fun resendCode(@Field("phone") phone: String): BaseResponse<List<Any>>

  @Headers("@: NoAuth")
  @POST("verify")
  @FormUrlEncoded
  suspend fun verifyUser(
    @Field("phone") phone: String,
    @Field("code") code: String,
  ): BaseResponse<List<Any>>
  //endregion

  //region Category
  @GET("categories")
  suspend fun getCategories(
    @IntRange(from = 1)
    @Query("page")
    page: Int,
    @Query("per_page")
    @IntRange(from = 1)
    perPage: Int,
  ): BaseResponse<CategoriesResponse>
  //endregion

  //region Service
  @GET("services")
  suspend fun getServicesByCategory(
    @IntRange(from = 1) @Query("category_id") categoryId: Int,
    @IntRange(from = 1) @Query("page") page: Int,
    @IntRange(from = 1) @Query("per_page") perPage: Int,
  ): BaseResponse<ServicesResponse>
  //endregion

  companion object Factory {
    operator fun invoke(retrofit: Retrofit) = retrofit.create<ApiService>()
  }
}