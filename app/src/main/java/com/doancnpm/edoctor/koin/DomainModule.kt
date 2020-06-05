package com.doancnpm.edoctor.koin

import com.doancnpm.edoctor.data.repository.CategoryRepositoryImpl
import com.doancnpm.edoctor.data.repository.LocationRepositoryImpl
import com.doancnpm.edoctor.data.repository.ServiceRepositoryImpl
import com.doancnpm.edoctor.data.repository.UserRepositoryImpl
import com.doancnpm.edoctor.domain.dispatchers.AppDispatchers
import com.doancnpm.edoctor.domain.dispatchers.AppDispatchersImpl
import com.doancnpm.edoctor.domain.dispatchers.AppSchedulers
import com.doancnpm.edoctor.domain.dispatchers.AppSchedulersImpl
import com.doancnpm.edoctor.domain.repository.CategoryRepository
import com.doancnpm.edoctor.domain.repository.LocationRepository
import com.doancnpm.edoctor.domain.repository.ServiceRepository
import com.doancnpm.edoctor.domain.repository.UserRepository
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
      get(API_URL_QUALIFIER),
      get(),
      get(),
      get(),
      get(),
      get(),
    )
  }

  single<CategoryRepository> {
    CategoryRepositoryImpl(
      get(API_URL_QUALIFIER),
      get(),
      get(),
      get(BASE_URL_QUALIFIER),
    )
  }

  single<ServiceRepository> {
    ServiceRepositoryImpl(
      get(API_URL_QUALIFIER),
      get(),
      get(),
      get(BASE_URL_QUALIFIER),
    )
  }

  factory<LocationRepository> {
    LocationRepositoryImpl(
      get(),
      androidApplication()
    )
  }
}