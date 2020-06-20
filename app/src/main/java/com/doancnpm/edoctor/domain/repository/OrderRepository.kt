package com.doancnpm.edoctor.domain.repository

import androidx.annotation.IntRange
import com.doancnpm.edoctor.domain.entity.*
import java.util.Date

interface OrderRepository {
  suspend fun createOrder(
    service: Service,
    location: Location,
    note: String?,
    promotion: Promotion?,
    card: Card,
    startTime: Date,
    endTime: Date,
  ): DomainResult<Order>

  suspend fun getOrders(
    @IntRange(from = 1) page: Int,
    @IntRange(from = 1) perPage: Int,
    serviceName: String?,
    date: Date?,
    orderId: Long?,
    statuses: Set<Order.Status>?,
  ): DomainResult<List<Order>>

  suspend fun cancel(order: Order): DomainResult<Unit>

  suspend fun findDoctor(order: Order): DomainResult<Unit>
}