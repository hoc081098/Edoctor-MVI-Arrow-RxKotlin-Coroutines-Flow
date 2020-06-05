package com.doancnpm.edoctor.ui.main.home.create_order

import com.doancnpm.edoctor.domain.entity.AppError

interface CreateOrderContract {
  data class Location(val lat: Double, val lng: Double)

  sealed class SingleEvent {
    data class Error(val appError: AppError) : SingleEvent()
  }
}