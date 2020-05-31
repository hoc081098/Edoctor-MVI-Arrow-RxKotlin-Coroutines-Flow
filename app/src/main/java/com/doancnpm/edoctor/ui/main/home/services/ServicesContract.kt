package com.doancnpm.edoctor.ui.main.home.services

import com.doancnpm.edoctor.domain.entity.AppError

interface ServicesContract {
  sealed class PlaceholderState {
    object Idle : PlaceholderState()
    object Loading : PlaceholderState()
    data class Error(val error: AppError) : PlaceholderState()
  }
}