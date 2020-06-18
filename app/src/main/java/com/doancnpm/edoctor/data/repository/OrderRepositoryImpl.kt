package com.doancnpm.edoctor.data.repository

import arrow.core.Either
import com.doancnpm.edoctor.data.ErrorMapper
import com.doancnpm.edoctor.data.remote.ApiService
import com.doancnpm.edoctor.data.remote.body.CreateOrderBody
import com.doancnpm.edoctor.data.toInt
import com.doancnpm.edoctor.data.toOrderDomain
import com.doancnpm.edoctor.domain.entity.*
import com.doancnpm.edoctor.domain.repository.OrderRepository
import com.doancnpm.edoctor.utils.UTCTimeZone
import com.doancnpm.edoctor.utils.toString_yyyyMMdd
import com.doancnpm.edoctor.utils.toString_yyyyMMdd_HHmmss
import kotlinx.coroutines.delay
import timber.log.Timber
import java.util.Date

class OrderRepositoryImpl(
  private val apiService: ApiService,
  private val errorMapper: ErrorMapper,
  private val baseUrl: String,
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

  override suspend fun getOrders(
    page: Int,
    perPage: Int,
    serviceName: String?,
    date: Date?,
    orderId: Long?,
    statuses: Set<Order.Status>?
  ): DomainResult<List<Order>> {
    return Either.catch(errorMapper::map) {
      val statusesMap = statuses
        ?.withIndex()
        ?.associate { (index, value) -> "statuses[$index]" to value.toInt().toString() }
        ?: emptyMap()

      apiService
        .getOrders(
          page = page,
          perPage = perPage,
          serviceName = serviceName,
          date = date?.toString_yyyyMMdd(),
          orderId = orderId,
          statuses = statusesMap,
        )
        .unwrap()
        .orders
        .map { it.toOrderDomain(baseUrl) }
        .also { delay(200) }
        .also {
          Timber.d("""{ size: ${it.size} } getOrders {
              |page: $page, 
              |perPage: $perPage, 
              |serviceName: $serviceName, 
              |date: $date,
              |orderId: $orderId, 
              |statuses: $statuses }""".trimMargin())
        }
    }
  }

  override suspend fun cancel(order: Order): DomainResult<Unit> =
    Either.catch(errorMapper::map) { apiService.cancelOrder(order.id).unwrap() }

  override suspend fun findDoctor(order: Order): DomainResult<Unit> =
    Either.catch(errorMapper::map) { apiService.findDoctor(order.id).unwrap() }
}
