package com.doancnpm.edoctor.ui.main.home.services

import com.doancnpm.edoctor.domain.entity.AppError

interface ServicesContract {
  sealed class LoadingState {
    object Loading : LoadingState()
    data class Error(val error: AppError) : LoadingState()
  }
}