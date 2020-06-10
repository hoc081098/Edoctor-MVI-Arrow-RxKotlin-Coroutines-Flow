package com.doancnpm.edoctor.ui.main.home.create_order.inputs.promotion

import com.doancnpm.edoctor.domain.entity.AppError
import com.doancnpm.edoctor.domain.entity.Promotion

interface InputPromotionContract {
  data class ViewState(
    val promotions: List<PromotionItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: AppError? = null,
  )

  data class PromotionItem(
    val promotion: Promotion,
    val isSelected: Boolean
  )
}