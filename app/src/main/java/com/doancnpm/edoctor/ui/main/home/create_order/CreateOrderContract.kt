package com.doancnpm.edoctor.ui.main.home.create_order

import com.doancnpm.edoctor.domain.entity.AppError
import java.util.*

interface CreateOrderContract {
  data class Location(val lat: Double, val lng: Double)

  data class Times(
    val startTime: Date? = null,
    val endTime: Date? = null,
  )

  sealed class SingleEvent {
    data class Error(val appError: AppError) : SingleEvent()

    sealed class Times : SingleEvent() {
      object PastStartTime : Times()
      object StartTimeIsLaterThanEndTime : Times()

      object PastEndTime : Times()
      object EndTimeIsEarlierThanStartTime : Times()
    }
  }
}