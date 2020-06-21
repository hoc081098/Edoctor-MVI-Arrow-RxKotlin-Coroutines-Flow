package com.doancnpm.edoctor.data.remote

import androidx.annotation.IntRange
import com.doancnpm.edoctor.data.remote.body.*
import com.doancnpm.edoctor.data.remote.response.*
import okhttp3.MultipartBody
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

  @PUT("logout")
  @FormUrlEncoded
  suspend fun logout(
    @Field("device_token") deviceToken: String
  ): BaseResponse<Any>

  @PUT("profile")
  suspend fun updateProfile(@Body body: UpdateProfileBody): BaseResponse<LoginUserResponse.User>

  @GET("users/{user_id}")
  suspend fun getUserDetail(@Path("user_id") userId: Long): BaseResponse<LoginUserResponse.User>
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
    @IntRange(from = 1) @Query("category_id") categoryId: Long,
    @IntRange(from = 1) @Query("page") page: Int,
    @IntRange(from = 1) @Query("per_page") perPage: Int,
  ): BaseResponse<ServicesResponse>
  //endregion

  //region Order
  @POST("orders")
  suspend fun createOrder(@Body body: CreateOrderBody): BaseResponse<CreateOrderResponse>

  @GET("orders")
  suspend fun getOrders(
    @IntRange(from = 1) @Query("page") page: Int,
    @IntRange(from = 1) @Query("per_page") perPage: Int,
    @Query("service_name") serviceName: String?,
    @Query("date") date: String?,
    @Query("order_id") orderId: Long?,
    @QueryMap(encoded = true) statuses: Map<String, String>,
  ): BaseResponse<OrdersResponse>

  @PUT("orders/cancel/{order_id}")
  suspend fun cancelOrder(@Path("order_id") orderId: Long): BaseResponse<Any>

  @GET("orders/find-doctors/{order_id}")
  suspend fun findDoctor(@Path("order_id") orderId: Long): BaseResponse<Any>

  @GET("orders/qr-code/{order_id}")
  suspend fun getQrCode(@Path("order_id") orderId: Long): BaseResponse<String>
  //endregion

  //region Promotion
  @GET("promotions")
  suspend fun getPromotions(
    @IntRange(from = 1) @Query("page") page: Int,
    @IntRange(from = 1) @Query("per_page") perPage: Int,
  ): BaseResponse<PromotionsResponse>
  //endregion

  //region Card
  @POST("cards")
  suspend fun addCard(@Body body: AddCardBody): BaseResponse<CardResponse>

  @GET("cards")
  suspend fun getCards(): BaseResponse<List<CardResponse>>

  @DELETE("cards/{id}")
  suspend fun removeCard(@Path("id") id: String): BaseResponse<Any>
  //endregion

  //region Notifications
  @GET("notifications")
  suspend fun getNotifications(
    @IntRange(from = 1) @Query("page") page: Int,
    @IntRange(from = 1) @Query("per_page") perPage: Int,
  ): BaseResponse<NotificationsResponse>
  //endregion

  @Multipart
  @POST("files")
  suspend fun uploadFile(@Part body: MultipartBody.Part): BaseResponse<Image>

  companion object Factory {
    operator fun invoke(retrofit: Retrofit) = retrofit.create<ApiService>()
  }
}