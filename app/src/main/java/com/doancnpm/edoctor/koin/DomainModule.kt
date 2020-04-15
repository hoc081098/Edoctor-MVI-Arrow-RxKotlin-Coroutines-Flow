package com.doancnpm.edoctor.koin

import com.doancnpm.edoctor.data.repository.UserRepositoryImpl
import com.doancnpm.edoctor.domain.dispatchers.AppDispatchers
import com.doancnpm.edoctor.domain.dispatchers.AppDispatchersImpl
import com.doancnpm.edoctor.domain.dispatchers.AppSchedulers
import com.doancnpm.edoctor.domain.dispatchers.AppSchedulersImpl
import com.doancnpm.edoctor.domain.repository.UserRepository
import org.koin.dsl.module

val domainModule = module {
  /**
   * AppDispatchers + AppSchedulers
   */

  single<AppDispatchers> { AppDispatchersImpl(get()) }
  single<AppSchedulers> { AppSchedulersImpl() }

  single<UserRepository> { UserRepositoryImpl(get(API_QUALIFIER), get(), get(), get(), get()) }
}