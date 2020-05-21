package com.doancnpm.edoctor.data.remote

import androidx.annotation.IntRange
import com.doancnpm.edoctor.data.remote.body.LoginUserBody
import com.doancnpm.edoctor.data.remote.body.RegisterUserBody
import com.doancnpm.edoctor.data.remote.response.BaseResponse
import com.doancnpm.edoctor.data.remote.response.CategoriesResponse
import com.doancnpm.edoctor.data.remote.response.LoginUserResponse
import com.doancnpm.edoctor.data.remote.response.RegisterUserResponse
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

  companion object Factory {
    operator fun invoke(retrofit: Retrofit) = retrofit.create<ApiService>()
  }
}