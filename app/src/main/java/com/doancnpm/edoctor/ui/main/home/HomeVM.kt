package com.doancnpm.edoctor.ui.main.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doancnpm.edoctor.core.BaseVM
import com.doancnpm.edoctor.domain.entity.Category
import com.doancnpm.edoctor.domain.repository.CategoryRepository
import com.doancnpm.edoctor.ui.main.home.HomeContract.*
import com.doancnpm.edoctor.utils.asLiveData
import com.doancnpm.edoctor.utils.setValueNotNull
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
      stateD.setValueNotNull { state ->
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
            items = state.categories.map { Item.CategoryItem(it) } +
                Item.Placeholder(PlaceholderState.Loading)
          )
        }
      }

      categoryRepository
        .getCategories(
          page = stateD.value!!.page + 1,
          perPage = PER_PAGE
        )
        .fold(
          ifLeft = {
            stateD.setValueNotNull { state ->
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
                  items = state.categories.map { Item.CategoryItem(it) } +
                      Item.Placeholder(PlaceholderState.Error(it))
                )
              }
            }
          },
          ifRight = { result -> stateD.setValueNotNull { it.copyWithSuccessResult(result) } }
        )
    }
  }

  fun refresh() {
    viewModelScope.launch {
      stateD.setValueNotNull { it.copy(isRefreshing = true) }

      categoryRepository
        .getCategories(
          page = 1,
          perPage = PER_PAGE
        )
        .fold(
          ifLeft = { stateD.setValueNotNull { it.copy(isRefreshing = false) } },
          ifRight = { result ->
            stateD.setValueNotNull {
              it.copyWithSuccessResult(result, refresh = true)
            }
          }
        )
    }
  }

  private fun ViewState.copyWithSuccessResult(
    result: List<Category>,
    refresh: Boolean = false,
  ): ViewState {
    val newCategories = buildList(capacity = result.size + if (!refresh) categories.size else 0) {
      if (!refresh) {
        addAll(categories)
      }
      addAll(result)
    }.distinctBy { it.id }
    val newPage = page + if (result.isEmpty()) 0 else 1

    return copy(
      categories = newCategories,
      isLoadingFirstPage = false,
      firstPageError = null,
      placeholderState = PlaceholderState.Idle,
      page = if (refresh) 1 else newPage,
      loadedAll = result.isEmpty(),
      items = buildList(capacity = newCategories.size) {
        addAll(newCategories.map { Item.CategoryItem(it) })

        if (result.isNotEmpty() && newPage > 0) {
          add(Item.Placeholder(PlaceholderState.Idle))
        }
      },
      isRefreshing = if (refresh) false else isRefreshing,
    )
  }

  private companion object {
    const val PER_PAGE = 4
  }
}