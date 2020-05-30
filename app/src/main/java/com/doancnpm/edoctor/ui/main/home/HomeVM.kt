package com.doancnpm.edoctor.ui.main.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doancnpm.edoctor.core.BaseVM
import com.doancnpm.edoctor.domain.repository.CategoryRepository
import com.doancnpm.edoctor.ui.main.home.HomeContract.*
import com.doancnpm.edoctor.utils.asLiveData
import com.doancnpm.edoctor.utils.setValue
import kotlinx.coroutines.launch

@ExperimentalStdlibApi
class HomeVM(private val categoryRepository: CategoryRepository) : BaseVM() {
  private val ViewState.shouldLoadNextPage: Boolean
    get() = when (page) {
      0 -> !isLoadingFirstPage && firstPageError === null && categories.isEmpty()
      else -> placeholderState === PlaceholderState.Idle && !loadedAll
    }
  private val ViewState.shouldRetryNextPage: Boolean
    get() = when (page) {
      0 -> !isLoadingFirstPage && firstPageError !== null
      else -> placeholderState is PlaceholderState.Error
    }

  private val stateD = MutableLiveData<ViewState>().apply { value = ViewState() }

  val stateLiveData = stateD.asLiveData()

  init {
    loadNextPageInternal()
  }

  fun loadNextPage() {
    if (stateD.value!!.shouldLoadNextPage) {
      loadNextPageInternal()
    }
  }

  fun retryNextPage() {
    if (stateD.value!!.shouldRetryNextPage) {
      loadNextPageInternal()
    }
  }

  /**
   * Must be guarded
   */
  private fun loadNextPageInternal() {
    viewModelScope.launch {
      val state = stateD.value!!

      stateD.setValue {
        if (state.page == 0) {
          state.copy(
            isLoadingFirstPage = true,
            firstPageError = null
          )
        } else {
          state.copy(
            isLoadingFirstPage = false,
            firstPageError = null,
            placeholderState = PlaceholderState.Loading,
            items = state.categories.map { Item.CategoryItem(it) } + Item.Placeholder(
              PlaceholderState.Loading)
          )
        }
      }

      categoryRepository
        .getCategories(
          page = state.page + 1,
          perPage = PER_PAGE
        )
        .fold(
          ifRight = { result ->
            val categories = (state.categories + result).distinctBy { it.id }

            state.copy(
              categories = categories,
              isLoadingFirstPage = false,
              firstPageError = null,
              placeholderState = PlaceholderState.Idle,
              page = state.page + if (result.isEmpty()) 0 else 1,
              loadedAll = result.isEmpty(),
              items = buildList(capacity = categories.size) {
                addAll(categories.map { Item.CategoryItem(it) })
                if (result.isNotEmpty()) {
                  add(Item.Placeholder(PlaceholderState.Loading))
                }
              }
            )
          },
          ifLeft = {
            if (state.page == 0) {
              state.copy(
                isLoadingFirstPage = false,
                firstPageError = it
              )
            } else {
              state.copy(
                isLoadingFirstPage = false,
                firstPageError = null,
                placeholderState = PlaceholderState.Error(it),
                items = state.categories.map { Item.CategoryItem(it) } + Item.Placeholder(
                  PlaceholderState.Error(it))
              )
            }
          }
        )
        .let { newState -> stateD.setValue { newState } }
    }
  }

  fun refresh() = Unit

  private companion object {
    const val PER_PAGE = 4
  }
}