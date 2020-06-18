package com.doancnpm.edoctor.ui.main.history

import androidx.annotation.IntRange
import androidx.annotation.LayoutRes
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.domain.dispatchers.AppDispatchers
import com.doancnpm.edoctor.domain.entity.AppError
import com.doancnpm.edoctor.domain.entity.Order
import com.doancnpm.edoctor.domain.repository.OrderRepository
import com.doancnpm.edoctor.ui.main.history.HistoryContract.HistoryType
import com.doancnpm.edoctor.ui.main.history.HistoryContract.PartialChange
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.rx3.rxObservable
import java.util.Date

interface HistoryContract {
  object PlaceholderStates {
    val idle = listOf(PlaceholderState.Idle)
    val loading = listOf(PlaceholderState.Loading)
  }

  data class ViewState(
    val orders: List<Order> = emptyList(),
    val firstPageState: PlaceholderState = PlaceholderState.Idle,
    val nextPageState: List<PlaceholderState> = emptyList(),
    val page: Int = 0,
    val type: HistoryType = HistoryType.WAITING,
  ) {
    val canLoadNextPage: Boolean
      get() {
        return page > 0
          && firstPageState is PlaceholderState.Success
          && nextPageState.firstOrNull() is PlaceholderState.Idle
      }
  }

  enum class HistoryType(val statuses: Set<Order.Status>) {
    WAITING(
      setOf(
        Order.Status.PENDING_STATUS,
        Order.Status.DOCTOR_CANCELED_STATUS,
      )
    ),
    UP_COMING(setOf(Order.Status.ACCEPTED_STATUS)),
    PROCESSING(setOf(Order.Status.PROCESSING_STATUS)),
    DONE(setOf(Order.Status.DONE_STATUS));

    companion object Helper {
      private val cache = mapOf(
        Order.Status.PENDING_STATUS to R.layout.item_recycler_order_waiting,
        Order.Status.DOCTOR_CANCELED_STATUS to R.layout.item_recycler_order_waiting,
        Order.Status.ACCEPTED_STATUS to R.layout.item_recycler_order_upcoming,
        Order.Status.PROCESSING_STATUS to R.layout.item_recycler_order_processing,
        Order.Status.DONE_STATUS to R.layout.item_recycler_order_done,
      )

      @LayoutRes
      fun layoutIdFor(status: Order.Status): Int = cache[status]!!
    }
  }

  sealed class PlaceholderState {
    object Idle : PlaceholderState()
    object Loading : PlaceholderState()
    data class Success(val isEmpty: Boolean) : PlaceholderState()
    data class Error(val error: AppError) : PlaceholderState()
  }

  sealed class ViewIntent {
    data class ChangeType(val historyType: HistoryType) : ViewIntent()
    object LoadNextPage : ViewIntent()

    data class Cancel(val order: Order) : ViewIntent()
    data class FindDoctor(val order: Order) : ViewIntent()
  }

  sealed class SingleEvent {
    sealed class Cancel : SingleEvent() {
      data class Success(val order: Order) : Cancel()
      data class Failure(val order: Order, val error: AppError) : Cancel()
    }

    sealed class FindDoctor : SingleEvent() {
      data class Success(val order: Order) : FindDoctor()
      data class Failure(val order: Order, val error: AppError) : FindDoctor()
    }
  }

  sealed class PartialChange {
    abstract fun reduce(vs: ViewState): ViewState

    sealed class FirstPage : PartialChange() {
      object Loading : FirstPage()
      data class Error(val error: AppError, val type: HistoryType) : FirstPage()
      data class Data(val orders: List<Order>, val type: HistoryType) : FirstPage()

      override fun reduce(vs: ViewState): ViewState {
        return when (this) {
          Loading -> vs.copy(
            orders = emptyList(),
            firstPageState = PlaceholderState.Loading,
            nextPageState = emptyList(),
            page = 0
          )
          is Error -> vs.copy(
            orders = emptyList(),
            firstPageState = PlaceholderState.Error(error),
            nextPageState = emptyList(),
            page = 0,
            type = type,
          )
          is Data -> vs.copy(
            orders = orders,
            firstPageState = PlaceholderState.Success(orders.isEmpty()),
            nextPageState = PlaceholderStates.idle,
            page = 1,
            type = type,
          )
        }
      }
    }

    sealed class NextPage : PartialChange() {
      object Loading : NextPage()
      data class Error(val error: AppError) : NextPage()
      data class Data(val orders: List<Order>) : NextPage()

      override fun reduce(vs: ViewState): ViewState {
        return when (this) {
          Loading -> vs.copy(nextPageState = PlaceholderStates.loading)
          is Error -> vs.copy(nextPageState = listOf(PlaceholderState.Error(error)))
          is Data -> vs.copy(
            orders = (vs.orders + orders).distinctBy { it.id },
            nextPageState = if (orders.isNotEmpty()) PlaceholderStates.idle else emptyList(),
            page = vs.page + 1,
          )
        }
      }
    }

    sealed class Cancel : PartialChange() {
      override fun reduce(vs: ViewState): ViewState {
        return when (this) {
          is Success -> vs.copy(
            orders = vs.orders.filter { it.id != order.id }
          )
          is Failure -> vs
        }
      }

      data class Success(val order: Order) : Cancel()
      data class Failure(val order: Order, val error: AppError) : Cancel()
    }

    sealed class FindDoctor : PartialChange() {
      override fun reduce(vs: ViewState): ViewState = vs

      data class Success(val order: Order) : FindDoctor()
      data class Failure(val order: Order, val error: AppError) : FindDoctor()
    }
  }

  interface Interactor {
    fun load(
      @IntRange(from = 1) page: Int,
      @IntRange(from = 1) perPage: Int,
      serviceName: String?,
      date: Date?,
      orderId: Long?,
      type: HistoryType
    ): Observable<PartialChange>

    fun cancel(order: Order): Observable<PartialChange.Cancel>

    fun findDoctor(order: Order): Observable<PartialChange.FindDoctor>
  }
}

@ExperimentalCoroutinesApi
class HistoryInteractor(
  private val orderRepository: OrderRepository,
  private val dispatchers: AppDispatchers,
) : HistoryContract.Interactor {
  override fun load(
    page: Int,
    perPage: Int,
    serviceName: String?,
    date: Date?,
    orderId: Long?,
    type: HistoryType
  ): Observable<PartialChange> {
    return rxObservable(dispatchers.main) {
      send(if (page == 1) PartialChange.FirstPage.Loading else PartialChange.NextPage.Loading)

      orderRepository
        .getOrders(
          page = page,
          perPage = perPage,
          serviceName = serviceName,
          date = date,
          orderId = orderId,
          statuses = type.statuses,
        )
        .fold(
          ifLeft = {
            if (page == 1) {
              PartialChange.FirstPage.Error(it, type)
            } else {
              PartialChange.NextPage.Error(it)
            }
          },
          ifRight = {
            if (page == 1) {
              PartialChange.FirstPage.Data(it, type)
            } else {
              PartialChange.NextPage.Data(it)
            }
          }
        )
        .let { send(it) }
    }
  }

  override fun cancel(order: Order): Observable<PartialChange.Cancel> {
    return rxObservable(dispatchers.main) {
      orderRepository
        .cancel(order)
        .fold(
          ifLeft = { PartialChange.Cancel.Failure(order, it) },
          ifRight = { PartialChange.Cancel.Success(order) },
        )
        .let { send(it) }
    }
  }

  override fun findDoctor(order: Order): Observable<PartialChange.FindDoctor> {
    return rxObservable(dispatchers.main) {
      orderRepository
        .findDoctor(order)
        .fold(
          ifLeft = { PartialChange.FindDoctor.Failure(order, it) },
          ifRight = { PartialChange.FindDoctor.Success(order) },
        )
        .let { send(it) }
    }
  }
}