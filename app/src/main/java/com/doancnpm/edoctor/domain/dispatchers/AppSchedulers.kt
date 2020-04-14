package com.doancnpm.edoctor.domain.dispatchers

import io.reactivex.Scheduler

interface AppSchedulers {
  val main: Scheduler
  val io: Scheduler
  val computation: Scheduler
}