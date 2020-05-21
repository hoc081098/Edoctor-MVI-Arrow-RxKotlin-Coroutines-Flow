package com.doancnpm.edoctor.data.remote.interceptor

import com.doancnpm.edoctor.data.local.UserLocalSource
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import java.net.HttpURLConnection.HTTP_FORBIDDEN
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED

class AuthInterceptor(private val userLocalSource: UserLocalSource) : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request()

    val customHeaders = request.headers.values("@")
    val newRequest = when {
      "NoAuth" in customHeaders -> request
      else -> {
        when (val token =
          runBlocking { userLocalSource.token() }.also { Timber.d("Current token: $it") }) {
          null -> request
          else -> request
            .newBuilder()
            .addHeader("x-access-token", token)
            .build()
        }
      }
    }

    val response = newRequest.newBuilder()
      .removeHeader("@")
      .build()
      .let(chain::proceed)

    if (response.code in arrayOf(HTTP_UNAUTHORIZED, HTTP_FORBIDDEN)) {
      runBlocking { userLocalSource.removeUserAndToken() }
      Timber.d("Response code is 401 or 404. Removed token. Logout")
    }

    return response
  }
}