package com.doancnpm.edoctor.ui.main.notifications

import com.doancnpm.edoctor.domain.entity.AppError

interface NotificationsContract {
  sealed class PlaceholderState {
    object Idle : PlaceholderState()
    object Loading : PlaceholderState()
    data class Success(val isEmpty: Boolean) : PlaceholderState()
    data class Error(val error: AppError) : PlaceholderState()
  }
}