package com.doancnpm.edoctor.ui.main.home

import com.doancnpm.edoctor.domain.entity.AppError
import com.doancnpm.edoctor.domain.entity.Category

@ExperimentalStdlibApi
interface HomeContract {
  data class ViewState(
    val categories: List<Category> = emptyList(),
    val isLoadingFirstPage: Boolean = false,
    val firstPageError: AppError? = null,
    val placeholderState: PlaceholderState = PlaceholderState.Idle,
    val page: Int = 0,
    val loadedAll: Boolean = false,
  ) {
    val items: List<Item>
      get() = buildList(capacity = categories.size) {
        addAll(categories.map { Item.CategoryItem(it) })

        if (!loadedAll) {
          add(Item.Placeholder(placeholderState))
        }
      }
  }

  sealed class PlaceholderState {
    object Idle : PlaceholderState()
    object Loading : PlaceholderState()
    data class Error(val error: AppError) : PlaceholderState()
  }

  sealed class Item {
    data class CategoryItem(val category: Category) : Item()
    data class Placeholder(val state: PlaceholderState) : Item()
  }
}