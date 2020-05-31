package com.doancnpm.edoctor.ui.main.home.services

import androidx.annotation.MainThread
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.doancnpm.edoctor.core.BaseVM
import com.doancnpm.edoctor.domain.entity.AppError
import com.doancnpm.edoctor.domain.entity.Category
import com.doancnpm.edoctor.domain.entity.Service
import com.doancnpm.edoctor.domain.repository.ServiceRepository
import com.doancnpm.edoctor.ui.main.home.services.ServicesContract.LoadingState
import com.doancnpm.edoctor.utils.asLiveData
import kotlinx.coroutines.launch

class ServicesVM(
  private val serviceRepository: ServiceRepository,
  private val category: Category,
) : BaseVM() {
  private var currentPage = 0
  private var loadedAll = false

  private val usersD = MutableLiveData<List<Service>>().apply { value = emptyList() }
  private val placeholderStateD =
    MutableLiveData<PlaceholderState>().apply { value = PlaceholderState.Idle }

  val userLiveData get() = usersD.asLiveData()

  val loadingStateLiveData
    get() = placeholderStateD.map {
      when (it) {
        null -> emptyList()
        PlaceholderState.Idle -> emptyList()
        PlaceholderState.Loading -> listOf(LoadingState.Loading)
        is PlaceholderState.Error -> listOf(LoadingState.Error(it.error))
      }
    }

  init {
    loadNextPage()
  }

  @MainThread
  fun loadNextPage() {
    val state = placeholderStateD.value
    if (state === null || (state is PlaceholderState.Idle && !loadedAll)) {
      _loadNextPage()
    }
  }

  @MainThread
  fun retryNextPage() {
    if (placeholderStateD.value is PlaceholderState.Error) {
      _loadNextPage()
    }
  }

  @Suppress("FunctionName")
  private fun _loadNextPage() {
    viewModelScope.launch {
      placeholderStateD.value = PlaceholderState.Loading

      val currentList = usersD.value ?: emptyList()
      serviceRepository
        .getServicesByCategory(
          category = category,
          perPage = PER_PAGE,
          page = currentPage + 1,
        )
        .fold(
          ifLeft = {
            placeholderStateD.value = PlaceholderState.Error(it)
          },
          ifRight = {
            loadedAll = it.isEmpty()
            currentPage += 1
            usersD.value = currentList + it
            placeholderStateD.value = PlaceholderState.Idle
          }
        )
    }
  }

  private sealed class PlaceholderState {
    object Idle : PlaceholderState()
    object Loading : PlaceholderState()
    data class Error(val error: AppError) : PlaceholderState()
  }

  private companion object {
    const val PER_PAGE = 6
  }
}