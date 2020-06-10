package com.doancnpm.edoctor.data.repository

import arrow.core.Either
import com.doancnpm.edoctor.data.ErrorMapper
import com.doancnpm.edoctor.data.remote.ApiService
import com.doancnpm.edoctor.data.remote.body.CreateOrderBody
import com.doancnpm.edoctor.domain.entity.DomainResult
import com.doancnpm.edoctor.domain.entity.Location
import com.doancnpm.edoctor.domain.entity.Promotion
import com.doancnpm.edoctor.domain.entity.Service
import com.doancnpm.edoctor.domain.repository.OrderRepository
import com.doancnpm.edoctor.utils.UTCTimeZone
import com.doancnpm.edoctor.utils.toString_yyyyMMdd_HHmmss
import java.util.*

class OrderRepositoryImpl(
  private val apiService: ApiService,
  private val errorMapper: ErrorMapper,
) : OrderRepository {
  override suspend fun createOrder(
    service: Service,
    location: Location,
    note: String?,
    originalPrice: Double,
    promotion: Promotion?,
    total: Double,
    payCardId: String,
    startTime: Date,
    endTime: Date
  ): DomainResult<Unit> {
    return Either.catch(errorMapper::map) {
      apiService
        .createOrder(
          CreateOrderBody(
            endTime = endTime.toString_yyyyMMdd_HHmmss(UTCTimeZone),
            lat = location.latitude,
            lng = location.longitude,
            note = note,
            originalPrice = originalPrice,
            payCardId = payCardId,
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