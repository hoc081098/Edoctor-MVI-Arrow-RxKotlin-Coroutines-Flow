package com.doancnpm.edoctor.domain.dispatchers

import io.reactivex.rxjava3.core.Scheduler

interface AppSchedulers {
  val main: Scheduler
  val io: Scheduler
  val computation: Scheduler
}