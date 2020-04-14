package com.doancnpm.edoctor.koin


import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.afollestad.rxkprefs.RxkPrefs
import com.afollestad.rxkprefs.rxkPrefs
import com.doancnpm.edoctor.BuildConfig
import com.doancnpm.edoctor.data.ErrorMapper
import com.doancnpm.edoctor.data.local.UserLocalSource
import com.doancnpm.edoctor.data.local.UserLocalSourceImpl
import com.doancnpm.edoctor.data.local.model.UserLocalJsonAdapter
import com.doancnpm.edoctor.data.remote.ApiService
import com.doancnpm.edoctor.data.remote.AuthInterceptor
import com.doancnpm.edoctor.data.remote.response.ErrorResponseJsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
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

val API_QUALIFIER = named("com.doancnpm.edoctor.api")

val dataModule = module {
  /*
   * Remote
   */

  single(API_QUALIFIER) { "https://node-auth-081098.herokuapp.com/" }

  single(API_QUALIFIER) { provideRetrofit(get(API_QUALIFIER), get(), get()) }

  single(API_QUALIFIER) { provideApiService(get(API_QUALIFIER)) }

  single { provideMoshi() }

  single { provideOkHttpClient(get()) }

  single { provideErrorMapper(get()) }

  factory { provideErrorResponseJsonAdapter(get()) }

  factory { provideAuthInterceptor(get()) }

  /*
   * Local
   */

  single<UserLocalSource> { UserLocalSourceImpl(get(), get(), get(), get()) }

  single { provideSharedPreferences(androidApplication()) }

  single { provideRxkPrefs(get()) }

  factory { provideUserLocalJsonAdapter(get()) }
}

private fun provideUserLocalJsonAdapter(moshi: Moshi): UserLocalJsonAdapter {
  return UserLocalJsonAdapter(moshi)
}

private fun provideRxkPrefs(sharedPreferences: SharedPreferences): RxkPrefs {
  return rxkPrefs(sharedPreferences)
}

private fun provideSharedPreferences(context: Context): SharedPreferences {
  return PreferenceManager.getDefaultSharedPreferences(context)
}

private fun provideAuthInterceptor(userLocalSource: UserLocalSource): AuthInterceptor {
  return AuthInterceptor(userLocalSource)
}

private fun provideErrorResponseJsonAdapter(moshi: Moshi): ErrorResponseJsonAdapter {
  return ErrorResponseJsonAdapter(moshi)
}

private fun provideErrorMapper(adapter: ErrorResponseJsonAdapter): ErrorMapper {
  return ErrorMapper(adapter)
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

private fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
  return OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .readTimeout(10, TimeUnit.SECONDS)
    .writeTimeout(10, TimeUnit.SECONDS)
    .addInterceptor(
      HttpLoggingInterceptor()
        .apply { level = if (BuildConfig.DEBUG) Level.BODY else Level.NONE }
    )
    .addInterceptor(authInterceptor)
    .build()
}