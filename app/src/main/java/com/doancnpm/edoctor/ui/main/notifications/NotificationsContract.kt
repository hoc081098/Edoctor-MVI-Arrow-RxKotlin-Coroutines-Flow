package com.doancnpm.edoctor.ui.main.notifications

import com.doancnpm.edoctor.domain.entity.AppError

interface NotificationsContract {
  sealed class PlaceholderState {
    data class Idle(val isInitial: Boolean = true) : PlaceholderState()
    object Loading : PlaceholderState()
    data class Error(val error: AppError) : PlaceholderState()
  }
}