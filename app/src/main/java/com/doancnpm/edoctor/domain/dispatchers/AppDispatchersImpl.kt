package com.doancnpm.edoctor.domain.dispatchers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.rx2.asCoroutineDispatcher

class AppDispatchersImpl(
  private val appSchedulers: AppSchedulers,
  override val main: CoroutineDispatcher = appSchedulers.main.asCoroutineDispatcher(),
  override val io: CoroutineDispatcher = appSchedulers.io.asCoroutineDispatcher(),
  override val computation: CoroutineDispatcher = appSchedulers.computation.asCoroutineDispatcher(),
) : AppDispatchers