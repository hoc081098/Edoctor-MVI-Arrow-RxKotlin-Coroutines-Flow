package com.doancnpm.edoctor.data.remote

import com.doancnpm.edoctor.data.local.UserLocalSource
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val userLocalSource: UserLocalSource) : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request()

    val customHeaders = request.headers.values("@")
    val newRequest = when {
      "NoAuth" in customHeaders -> request
      else -> {
        when (val token = userLocalSource.token()) {
          null -> request
          else -> request
            .newBuilder()
            .addHeader("x-access-token", token)
            .build()
        }
      }
    }

    return newRequest.newBuilder()
      .removeHeader("@")
      .build()
      .let(chain::proceed)
  }
}