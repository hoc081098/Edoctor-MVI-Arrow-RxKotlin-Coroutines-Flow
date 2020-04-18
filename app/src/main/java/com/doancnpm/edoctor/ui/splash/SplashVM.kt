package com.doancnpm.edoctor.ui.splash

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doancnpm.edoctor.core.BaseVM
import com.doancnpm.edoctor.domain.repository.UserRepository
import com.doancnpm.edoctor.utils.Event
import com.doancnpm.edoctor.utils.asLiveData
import kotlinx.coroutines.launch

class SplashVM(private val userRepository: UserRepository) : BaseVM() {
  private val authEventD = MutableLiveData<Event<Boolean>>()
  val authEvent get() = authEventD.asLiveData()

  init {
    viewModelScope.launch {
      val auth = userRepository.checkAuth().fold(ifLeft = { false }, ifRight = { it })
      authEventD.value = Event(auth)
    }
  }
}