package com.doancnpm.edoctor.koin


import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.doancnpm.edoctor.BuildConfig
import com.doancnpm.edoctor.data.ErrorMapper
import com.doancnpm.edoctor.data.local.UserLocalSource
import com.doancnpm.edoctor.data.local.UserLocalSourceImpl
import com.doancnpm.edoctor.data.local.model.UserLocalJsonAdapter
import com.doancnpm.edoctor.data.remote.ApiService
import com.doancnpm.edoctor.data.remote.interceptor.ApiKeyInterceptor
import com.doancnpm.edoctor.data.remote.interceptor.AuthInterceptor
import com.doancnpm.edoctor.data.remote.response.ErrorResponseJsonAdapter
import com.doancnpm.edoctor.domain.dispatchers.AppDispatchers
import com.google.firebase.iid.FirebaseInstanceId
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import org.koin.android.ext.koin.androidApplication
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit

val API_URL_QUALIFIER = named("com.doancnpm.edoctor.api_url")
val BASE_URL_QUALIFIER = named("com.doancnpm.edoctor.base_url")
private val API_KEY_QUALIFIER = named("com.doancnpm.edoctor.api_key")

val dataModule = module {
  /*
   * Remote
   */

  factory(BASE_URL_QUALIFIER) { BuildConfig.BASE_URL }

  factory(API_URL_QUALIFIER) { BuildConfig.API_URL }

  factory(API_KEY_QUALIFIER) { BuildConfig.API_KEY }

  factory(API_URL_QUALIFIER) {
    provideRetrofit(
      baseUrl = get(API_URL_QUALIFIER),
      moshi = get(),
      client = get(),
    )
  }

  factory {
    provideOkHttpClient(
      authInterceptor = get(),
      apiKeyInterceptor = get(),
    )
  }

  factory { provideErrorResponseJsonAdapter(moshi = get()) }

  factory { provideAuthInterceptor(userLocalSource = get()) }

  factory { provideApiKeyInterceptor(apiKey = get(API_KEY_QUALIFIER)) }

  single(API_URL_QUALIFIER) { provideApiService(retrofit = get(API_URL_QUALIFIER)) }

  single { provideErrorMapper(errorResponseJsonAdapter = get()) }

  /*
   * Local
   */

  single<UserLocalSource> {
    UserLocalSourceImpl(
      sharedPreferences = get(),
      dispatchers = get(),
      userLocalJsonAdapter = get(),
      schedulers = get(),
    )
  }

  single { provideSharedPreferences(context = androidApplication()) }

  factory { provideUserLocalJsonAdapter(moshi = get()) }

  /*
   * App
   */
  single { CoroutineScope(get<AppDispatchers>().io + SupervisorJob()) }

  single { provideMoshi() }

  /*
   * Firebase
   */
  factory { FirebaseInstanceId.getInstance() }
}

private fun provideUserLocalJsonAdapter(moshi: Moshi): UserLocalJsonAdapter {
  return UserLocalJsonAdapter(moshi)
}

private fun provideSharedPreferences(context: Context): SharedPreferences {
  return PreferenceManager.getDefaultSharedPreferences(context)
}

private fun provideAuthInterceptor(userLocalSource: UserLocalSource): AuthInterceptor {
  return AuthInterceptor(
    userLocalSource
  )
}

private fun provideApiKeyInterceptor(apiKey: String): ApiKeyInterceptor {
  return ApiKeyInterceptor(apiKey)
}

private fun provideErrorResponseJsonAdapter(moshi: Moshi): ErrorResponseJsonAdapter {
  return ErrorResponseJsonAdapter(moshi)
}

private fun provideErrorMapper(errorResponseJsonAdapter: ErrorResponseJsonAdapter): ErrorMapper {
  return ErrorMapper(errorResponseJsonAdapter)
}

private fun provideApiService(retrofit: Retrofit): ApiService = ApiService(retrofit)

private fun provideMoshi(): Moshi {
  return Moshi
    .Builder()
    .add(KotlinJsonAdapterFactory())
    .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
    .build()
}

private fun provideRetrofit(baseUrl: String, moshi: Moshi, client: OkHttpClient): Retrofit {
  return Retrofit.Builder()
    .client(client)
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(baseUrl)
    .build()
}

private fun provideOkHttpClient(
  authInterceptor: AuthInterceptor,
  apiKeyInterceptor: ApiKeyInterceptor,
): OkHttpClient {
  return OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .readTimeout(10, TimeUnit.SECONDS)
    .writeTimeout(10, TimeUnit.SECONDS)
    .addInterceptor(
      HttpLoggingInterceptor()
        .apply { level = if (BuildConfig.DEBUG) Level.BODY else Level.NONE }
    )
    .addInterceptor(authInterceptor)
    .addInterceptor(apiKeyInterceptor)
    .build()
}