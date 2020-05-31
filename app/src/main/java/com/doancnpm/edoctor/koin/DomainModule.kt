package com.doancnpm.edoctor.koin

import com.doancnpm.edoctor.data.repository.CategoryRepositoryImpl
import com.doancnpm.edoctor.data.repository.ServiceRepositoryImpl
import com.doancnpm.edoctor.data.repository.UserRepositoryImpl
import com.doancnpm.edoctor.domain.dispatchers.AppDispatchers
import com.doancnpm.edoctor.domain.dispatchers.AppDispatchersImpl
import com.doancnpm.edoctor.domain.dispatchers.AppSchedulers
import com.doancnpm.edoctor.domain.dispatchers.AppSchedulersImpl
import com.doancnpm.edoctor.domain.repository.CategoryRepository
import com.doancnpm.edoctor.domain.repository.ServiceRepository
import com.doancnpm.edoctor.domain.repository.UserRepository
import org.koin.dsl.module

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
      get()
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
}