package com.doancnpm.edoctor.ui.main

import arrow.core.extensions.option.monad.flatten
import com.doancnpm.edoctor.core.BaseVM
import com.doancnpm.edoctor.domain.repository.UserRepository
import com.doancnpm.edoctor.ui.main.history.HistoryContract.HistoryType

class MainVM(userRepository: UserRepository) : BaseVM() {
  private var historyType: HistoryType? = null

  fun setHistoryType(type: HistoryType) {
    historyType = type
  }

  fun getHistoryType(): HistoryType? {
    return historyType?.let { it.also { historyType = null } }
  }

  val logoutEvent = userRepository
    .userObservable()
    .map { it.toOption().flatten() }
    .distinctUntilChanged()
    .filter { it.isEmpty() }
    .map { Unit }!!
}