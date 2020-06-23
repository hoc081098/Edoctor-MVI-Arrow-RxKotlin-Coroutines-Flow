package com.doancnpm.edoctor.data.remote

import com.doancnpm.edoctor.data.remote.response.GeocoderResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocoderApiService {
  @GET("geocode/json")
  suspend fun getAddressForCoordinates(
    @Query("latlng") latlng: String,
    @Query("key") key: String,
  ): Response<GeocoderResponse>

  companion object Factory {
    operator fun invoke(retrofit: Retrofit): GeocoderApiService = retrofit.create()
  }
}