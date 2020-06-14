package com.doancnpm.edoctor.data.repository

import arrow.core.Either
import com.doancnpm.edoctor.data.ErrorMapper
import com.doancnpm.edoctor.data.remote.ApiService
import com.doancnpm.edoctor.data.remote.body.CreateOrderBody
import com.doancnpm.edoctor.domain.entity.*
import com.doancnpm.edoctor.domain.repository.OrderRepository
import com.doancnpm.edoctor.utils.UTCTimeZone
import com.doancnpm.edoctor.utils.toString_yyyyMMdd_HHmmss
import timber.log.Timber
import java.util.Date

class OrderRepositoryImpl(
  private val apiService: ApiService,
  private val errorMapper: ErrorMapper,
) : OrderRepository {
  override suspend fun createOrder(
    service: Service,
    location: Location,
    note: String?,
    promotion: Promotion?,
    card: Card,
    startTime: Date,
    endTime: Date
  ): DomainResult<Unit> {
    return Either.catch(errorMapper::map) {
      val originalPrice = service.price.toDouble()
      val total = promotion?.discount?.let { originalPrice * (1 - it) }
        ?: originalPrice

      Timber.d("originalPrice=$originalPrice, total=$total")

      apiService
        .createOrder(
          CreateOrderBody(
            endTime = endTime.toString_yyyyMMdd_HHmmss(UTCTimeZone),
            lat = location.latitude,
            lng = location.longitude,
            note = note,
            originalPrice = originalPrice,
            payCardId = card.id,
            promotionId = promotion?.id,
            serviceId = service.id,
            startTime = startTime.toString_yyyyMMdd_HHmmss(UTCTimeZone),
            total = total,
          )
        )
        .unwrap()
    }
  }
}