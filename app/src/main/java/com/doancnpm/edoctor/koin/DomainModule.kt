package com.doancnpm.edoctor.koin

import com.doancnpm.edoctor.data.repository.*
import com.doancnpm.edoctor.domain.dispatchers.AppDispatchers
import com.doancnpm.edoctor.domain.dispatchers.AppDispatchersImpl
import com.doancnpm.edoctor.domain.dispatchers.AppSchedulers
import com.doancnpm.edoctor.domain.dispatchers.AppSchedulersImpl
import com.doancnpm.edoctor.domain.repository.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import kotlin.time.ExperimentalTime

@ExperimentalCoroutinesApi
@ExperimentalTime
val domainModule = module {
  /**
   * AppDispatchers + AppSchedulers
   */

  single<AppDispatchers> { AppDispatchersImpl(get()) }
  single<AppSchedulers> { AppSchedulersImpl() }

  single<UserRepository>(createdAtStart = true) {
    UserRepositoryImpl(
      apiService = get(API_URL_QUALIFIER),
      errorMapper = get(),
      dispatchers = get(),
      userLocalSource = get(),
      firebaseInstanceId = get(),
      appCoroutineScope = get(),
    )
  }

  factory<CategoryRepository> {
    CategoryRepositoryImpl(
      apiService = get(API_URL_QUALIFIER),
      errorMapper = get(),
      dispatchers = get(),
      baseUrl = get(BASE_URL_QUALIFIER),
    )
  }

  factory<ServiceRepository> {
    ServiceRepositoryImpl(
      apiService = get(API_URL_QUALIFIER),
      errorMapper = get(),
      dispatchers = get(),
      baseUrl = get(BASE_URL_QUALIFIER),
    )
  }

  factory<LocationRepository> {
    LocationRepositoryImpl(
      errorMapper = get(),
      application = androidApplication()
    )
  }

  factory<OrderRepository> {
    OrderRepositoryImpl(
      apiService = get(),
      errorMapper = get(),
    )
  }

  factory<PromotionRepository> {
    PromotionRepositoryImpl(
      apiService = get(),
      errorMapper = get(),
    )
  }
}