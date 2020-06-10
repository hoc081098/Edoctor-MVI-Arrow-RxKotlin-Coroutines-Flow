package com.doancnpm.edoctor.ui.main.home.create_order.inputs.promotion

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doancnpm.edoctor.core.BaseVM
import com.doancnpm.edoctor.domain.repository.PromotionRepository
import com.doancnpm.edoctor.ui.main.home.create_order.inputs.promotion.InputPromotionContract.PromotionItem
import com.doancnpm.edoctor.ui.main.home.create_order.inputs.promotion.InputPromotionContract.ViewState
import com.doancnpm.edoctor.utils.asLiveData
import com.doancnpm.edoctor.utils.setValueNotNull
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.LazyThreadSafetyMode.NONE

class InputPromotionVM(
  private val promotionRepository: PromotionRepository,
) : BaseVM() {
  private val selectedItemD = MutableLiveData<PromotionItem>()

  private val viewStateD by lazy(NONE) {
    MutableLiveData<ViewState>()
      .apply { value = ViewState() }
      .also { fetchPromotion() }
  }

  val viewState get() = viewStateD.asLiveData()
  val selectedItem get() = selectedItemD.asLiveData()

  fun retry() {
    val value = viewStateD.value!!
    if (value.error !== null && !value.isLoading) {
      fetchPromotion()
    }
  }

  fun select(item: PromotionItem) {
    val old = selectedItemD.value
    selectedItemD.value = if (old === null || old.promotion.id != item.promotion.id) item else null

    viewStateD.setValueNotNull { state ->
      state.copy(
        isLoading = false,
        error = null,
        promotions = state.promotions.map {
          it.copy(
            isSelected = it.promotion.id == selectedItemD.value?.promotion?.id
          )
        }
      )
    }
  }

  private fun fetchPromotion() {
    viewModelScope.launch {
      viewStateD.setValueNotNull { it.copy(isLoading = true, error = null) }

      promotionRepository
        .getPromotions()
        .fold(
          ifLeft = { error ->
            Timber.d(error, "Error: $error")
            viewStateD.setValueNotNull { it.copy(isLoading = false, error = error) }
          },
          ifRight = { promotions ->
            viewStateD.setValueNotNull { state ->
              state.copy(
                isLoading = false,
                error = null,
                promotions = promotions.map {
                  PromotionItem(
                    promotion = it,
                    isSelected = selectedItemD.value?.promotion?.id == it.id,
                  )
                }
              )
            }
          }
        )
    }
  }
}