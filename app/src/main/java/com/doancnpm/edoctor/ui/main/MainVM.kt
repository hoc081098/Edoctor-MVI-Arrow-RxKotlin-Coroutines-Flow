package com.doancnpm.edoctor.ui.main

import arrow.core.extensions.option.monad.flatten
import com.doancnpm.edoctor.core.BaseVM
import com.doancnpm.edoctor.domain.repository.UserRepository

class MainVM(userRepository: UserRepository) : BaseVM() {
  val logoutEvent = userRepository
    .userObservable()
    .map { it.toOption().flatten() }
    .distinctUntilChanged()
    .filter { it.isEmpty() }
    .map { Unit }!!
}