package com.doancnpm.edoctor.domain.dispatchers

import kotlinx.coroutines.CoroutineDispatcher

interface AppDispatchers {
  val main: CoroutineDispatcher
  val io: CoroutineDispatcher
  val computation: CoroutineDispatcher
}