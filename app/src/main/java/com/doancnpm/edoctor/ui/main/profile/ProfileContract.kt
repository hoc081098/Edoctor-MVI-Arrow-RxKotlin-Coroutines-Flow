package com.doancnpm.edoctor.ui.main.profile

import com.doancnpm.edoctor.domain.entity.AppError

interface ProfileContract {
  sealed class ViewIntent {
    object Logout : ViewIntent()
  }

  sealed class SingleEvent {
    object LogoutSucess : SingleEvent()
    data class LogoutFailure(val error: AppError) : SingleEvent()
  }
}