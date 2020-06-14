package com.doancnpm.edoctor.domain.repository

import com.doancnpm.edoctor.domain.entity.*
import java.util.*

interface OrderRepository {
  suspend fun createOrder(
    service: Service,
    location: Location,
    note: String?,
    promotion: Promotion?,
    card: Card,
    startTime: Date,
    endTime: Date,
  ): DomainResult<Unit>
}