package com.doancnpm.edoctor.ui.main.home

import androidx.lifecycle.viewModelScope
import com.doancnpm.edoctor.core.BaseVM
import com.doancnpm.edoctor.domain.repository.CategoryRepository
import kotlinx.coroutines.launch
import timber.log.Timber

class HomeVM(private val categoryRepository: CategoryRepository) : BaseVM() {
  init {
    viewModelScope.launch {
      categoryRepository
        .getCategories(page = 1, perPage = 10)
        .let { Timber.d("Categories: $it") }
    }
  }
}