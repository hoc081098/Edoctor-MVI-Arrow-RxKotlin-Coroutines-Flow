package com.doancnpm.edoctor.domain.repository

import com.doancnpm.edoctor.domain.entity.DomainResult
import com.doancnpm.edoctor.domain.entity.Location
import com.doancnpm.edoctor.domain.entity.Promotion
import com.doancnpm.edoctor.domain.entity.Service
import java.util.*

interface OrderRepository {
  suspend fun createOrder(
    service: Service,
    location: Location,
    note: String?,
    originalPrice: Double,
    promotion: Promotion?,
    total: Double,
    payCardId: String,
    startTime: Date,
    endTime: Date,
  ): DomainResult<Unit>
}