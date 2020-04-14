package com.doancnpm.edoctor.domain.dispatchers

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class AppSchedulersImpl(
  override val main: Scheduler = AndroidSchedulers.mainThread(),
  override val io: Scheduler = Schedulers.io(),
  override val computation: Scheduler = Schedulers.computation(),
) : AppSchedulers