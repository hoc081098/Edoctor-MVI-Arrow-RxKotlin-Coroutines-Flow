package com.doancnpm.edoctor.ui.main.home.create_order.select_card

import com.doancnpm.edoctor.domain.entity.AppError
import com.doancnpm.edoctor.domain.entity.Card

interface SelectCardContract {
  data class ViewState(
    val cards: List<Card> = emptyList(),
    val isLoading: Boolean = true,
    val error: AppError? = null,
  )
}