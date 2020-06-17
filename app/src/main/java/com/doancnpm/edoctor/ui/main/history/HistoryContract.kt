package com.doancnpm.edoctor.ui.main.history

import androidx.annotation.IntRange
import com.doancnpm.edoctor.domain.entity.AppError
import com.doancnpm.edoctor.domain.entity.Order
import com.doancnpm.edoctor.domain.repository.OrderRepository
import com.doancnpm.edoctor.ui.main.history.HistoryContract.HistoryType
import com.doancnpm.edoctor.ui.main.history.HistoryContract.PartialChange
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.rx3.asObservable
import java.util.Date

interface HistoryContract {
  data class ViewState(
    val orders: List<Order> = emptyList(),
    val firstPageState: PlaceholderState = PlaceholderState.Idle,
    val nextPageState: List<PlaceholderState> = PlaceholderState.idle,
    val page: Int = 0,
    val type: HistoryType = HistoryType.WAITING,
  )

  enum class HistoryType(val statuses: Set<Order.Status>) {
    WAITING(
      setOf(
        Order.Status.PENDING_STATUS,
        Order.Status.DOCTOR_CANCELED_STATUS,
      )
    ),
    UP_COMING(setOf(Order.Status.ACCEPTED_STATUS)),
    PROCESSING(setOf(Order.Status.PROCESSING_STATUS)),
    DONE(setOf(Order.Status.DONE_STATUS))
  }

  sealed class PlaceholderState {
    object Idle : PlaceholderState()
    object Loading : PlaceholderState()
    data class Success(val isEmpty: Boolean) : PlaceholderState()
    data class Error(val error: AppError) : PlaceholderState()

    companion object Factory {
      val idle = listOf(Idle)
      val loading = listOf(Loading)
    }
  }

  sealed class ViewIntent {
    data class ChangeType(val historyType: HistoryType) : ViewIntent()
  }

  sealed class SingleEvent

  sealed class PartialChange {
    abstract fun reduce(vs: ViewState): ViewState

    sealed class FirstPage : PartialChange() {
      object Loading : FirstPage()
      data class Error(val error: AppError) : FirstPage()
      data class Data(val orders: List<Order>, val type: HistoryType) : FirstPage()

      override fun reduce(vs: ViewState): ViewState {
        return when (this) {
          Loading -> vs.copy(
            orders = emptyList(),
            firstPageState = PlaceholderState.Loading,
            nextPageState = PlaceholderState.idle,
            page = 0
          )
          is Error -> vs.copy(
            orders = emptyList(),
            firstPageState = PlaceholderState.Error(error),
            nextPageState = PlaceholderState.idle,
            page = 0
          )
          is Data -> vs.copy(
            orders = orders,
            firstPageState = PlaceholderState.Success(orders.isEmpty()),
            nextPageState = PlaceholderState.idle,
            page = 1,
            type = type,
          )
        }
      }
    }

    sealed class NextPage : PartialChange() {
      object Loading : NextPage()
      data class Error(val error: AppError) : NextPage()
      data class Data(val orders: List<Order>, val type: HistoryType) : NextPage()

      override fun reduce(vs: ViewState): ViewState {
        TODO("Not yet implemented")
      }
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
  }
}

@ExperimentalCoroutinesApi
class HistoryInteractor(
  private val orderRepository: OrderRepository,
) : HistoryContract.Interactor {
  override fun load(page: Int, perPage: Int, serviceName: String?, date: Date?, orderId: Long?, type: HistoryType): Observable<PartialChange> {
    return flow {
      emit(if (page == 1) PartialChange.FirstPage.Loading else PartialChange.NextPage.Loading)

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
              PartialChange.FirstPage.Error(it)
            } else {
              PartialChange.NextPage.Error(it)
            }
          },
          ifRight = {
            if (page == 1) {
              PartialChange.FirstPage.Data(it, type)
            } else {
              PartialChange.NextPage.Data(it, type)
            }
          }
        )
        .let { emit(it) }
    }.asObservable()
  }
}