package com.doancnpm.edoctor.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response

class ApiKeyInterceptor(private val apiKey: String) : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    return chain.request()
      .newBuilder()
      .addHeader("Api-Key", apiKey)
      .build()
      .let(chain::proceed)
  }
}