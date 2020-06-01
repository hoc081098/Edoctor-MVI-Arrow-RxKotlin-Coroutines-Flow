package com.doancnpm.edoctor.ui.main.home.services

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.doancnpm.edoctor.core.BaseVM
import com.doancnpm.edoctor.domain.entity.Category
import com.doancnpm.edoctor.domain.entity.Service
import com.doancnpm.edoctor.domain.repository.ServiceRepository
import com.doancnpm.edoctor.ui.main.home.services.ServicesContract.PlaceholderState
import com.doancnpm.edoctor.utils.asLiveData
import kotlinx.coroutines.launch

class ServicesVM(
  private val serviceRepository: ServiceRepository,
  private val category: Category,
) : BaseVM() {
  //region Private
  private var currentPage = 0
  private var loadedAll = false

  private val servicesD = MutableLiveData<List<Service>>().apply { value = emptyList() }

  private val refreshingD = MutableLiveData<Boolean>().apply { value = false }

  private val placeholderStateD =
    MutableLiveData<PlaceholderState>().apply { value = PlaceholderState.Idle }
  private val firstPagePlaceholderStateD =
    MutableLiveData<PlaceholderState>().apply { value = PlaceholderState.Idle }

  private val shouldLoadNextPage
    get() = if (currentPage == 0) {
      firstPagePlaceholderStateD.value == PlaceholderState.Idle
    } else {
      placeholderStateD.value == PlaceholderState.Idle
    } && !loadedAll

  private val shouldRetry
    get() = if (currentPage == 0) {
      firstPagePlaceholderStateD.value!! is PlaceholderState.Error
    } else {
      placeholderStateD.value!! is PlaceholderState.Error
    }

  //endregion

  //region Public
  val servicesLiveData get() = servicesD.asLiveData()

  val placeholderStateLiveData: LiveData<List<PlaceholderState>>
    get() = placeholderStateD.map {
      if (it == PlaceholderState.Idle) emptyList()
      else listOf(it)
    }

  val firstPagePlaceholderStateLiveData get() = firstPagePlaceholderStateD.asLiveData()

  val refreshingLiveData get() = refreshingD.asLiveData()
  //endregion

  init {
    loadNextPage()
  }

  @MainThread
  fun loadNextPage() {
    if (shouldLoadNextPage) {
      _loadNextPage()
    }
  }

  @MainThread
  fun retryNextPage() {
    if (shouldRetry) {
      _loadNextPage()
    }
  }

  @MainThread
  fun refresh() {
    refreshingD.value = true

    viewModelScope.launch {
      serviceRepository
        .getServicesByCategory(
          category = category,
          perPage = PER_PAGE,
          page = 1,
        )
        .also { refreshingD.value = false }
        .fold(
          ifLeft = {},
          ifRight = { services ->
            if (services.isNotEmpty()) {
              placeholderStateD.value = PlaceholderState.Idle
              firstPagePlaceholderStateD.value = PlaceholderState.Idle

              loadedAll = false
              currentPage = 1

              servicesD.value = services
            }
          }
        )
    }
  }

  @Suppress("FunctionName")
  private fun _loadNextPage() {
    viewModelScope.launch {

      if (currentPage == 0) {
        firstPagePlaceholderStateD.value = PlaceholderState.Loading
      } else {
        placeholderStateD.value = PlaceholderState.Loading
      }

      serviceRepository
        .getServicesByCategory(
          category = category,
          perPage = PER_PAGE,
          page = currentPage + 1,
        )
        .fold(
          ifLeft = {
            if (currentPage == 0) {
              firstPagePlaceholderStateD.value = PlaceholderState.Error(it)
            } else {
              placeholderStateD.value = PlaceholderState.Error(it)
            }
          },
          ifRight = { services ->
            if (currentPage == 0) {
              firstPagePlaceholderStateD.value = PlaceholderState.Idle
            } else {
              placeholderStateD.value = PlaceholderState.Idle
            }

            loadedAll = services.isEmpty()
            currentPage += 1

            if (services.isNotEmpty()) {
              servicesD.value = (servicesD.value!! + services).distinctBy { it.id }
            }
          }
        )
    }
  }

  private companion object {
    const val PER_PAGE = 4
  }
}