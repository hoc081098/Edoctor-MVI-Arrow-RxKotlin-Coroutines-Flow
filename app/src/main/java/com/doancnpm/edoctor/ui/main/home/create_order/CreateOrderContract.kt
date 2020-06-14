package com.doancnpm.edoctor.ui.main.home.create_order

import com.doancnpm.edoctor.domain.entity.AppError
import com.doancnpm.edoctor.domain.entity.Card
import com.doancnpm.edoctor.domain.entity.Location
import com.doancnpm.edoctor.domain.entity.Promotion
import java.util.Date

interface CreateOrderContract {
  data class Location(val lat: Double, val lng: Double) {
    fun toDomain() = Location(
      latitude = lat,
      longitude = lng
    )
  }

  data class Times(
    val startTime: Date? = null,
    val endTime: Date? = null,
  )

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

  interface SelectCardContract {
    data class ViewState(
      val items: List<CardItem> = emptyList(),
      val isLoading: Boolean = true,
      val error: AppError? = null,
    )

    data class CardItem(
      val card: Card,
      val isSelected: Boolean,
    )
  }

  sealed class SingleEvent {
    data class AddressError(val locationError: AppError.LocationError) : SingleEvent()

    sealed class Times : SingleEvent() {
      object PastStartTime : Times()
      object StartTimeIsLaterThanEndTime : Times()

      object PastEndTime : Times()
      object EndTimeIsEarlierThanStartTime : Times()
    }

    // Create order
    object MissingRequiredInput : SingleEvent()
    object Success : SingleEvent()
    data class Error(val appError: AppError) : SingleEvent()
  }
}