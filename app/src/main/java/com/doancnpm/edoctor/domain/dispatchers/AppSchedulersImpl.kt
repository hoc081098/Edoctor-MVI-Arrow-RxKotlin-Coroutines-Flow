package com.doancnpm.edoctor.domain.dispatchers

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers

class AppSchedulersImpl(
  override val main: Scheduler = AndroidSchedulers.mainThread(),
  override val io: Scheduler = Schedulers.io(),
  override val computation: Scheduler = Schedulers.computation(),
) : AppSchedulers