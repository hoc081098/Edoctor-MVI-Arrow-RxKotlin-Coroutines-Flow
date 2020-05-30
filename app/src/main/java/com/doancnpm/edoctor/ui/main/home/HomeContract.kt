package com.doancnpm.edoctor.ui.main.home

import com.doancnpm.edoctor.domain.entity.AppError
import com.doancnpm.edoctor.domain.entity.Category

interface HomeContract {
  data class ViewState(
    val categories: List<Category> = emptyList(),
    val isLoadingFirstPage: Boolean = false,
    val firstPageError: AppError? = null,
    val placeholderState: PlaceholderState = PlaceholderState.Idle,
    val page: Int = 0,
  )

  sealed class PlaceholderState {
    object Idle : PlaceholderState()
    object Loading : PlaceholderState()
    data class Error(val error: AppError) : PlaceholderState()
  }
}