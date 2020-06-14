package com.doancnpm.edoctor.ui.main.home.create_order.select_card.add_card

import com.doancnpm.edoctor.domain.dispatchers.AppDispatchers
import com.doancnpm.edoctor.domain.entity.AppError
import com.doancnpm.edoctor.domain.repository.CardRepository
import com.doancnpm.edoctor.ui.main.home.create_order.select_card.add_card.AddCardContract.FormData.Data
import com.doancnpm.edoctor.ui.main.home.create_order.select_card.add_card.AddCardContract.FormData.Initial
import com.doancnpm.edoctor.ui.main.home.create_order.select_card.add_card.AddCardContract.Interactor
import com.doancnpm.edoctor.ui.main.home.create_order.select_card.add_card.AddCardContract.PartialChange
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.rx3.rxObservable

interface AddCardContract {
  sealed class FormData {
    abstract val holderName: String?
    abstract val number: String?
    abstract val expiredMonth: Int?
    abstract val expiredYear: Int?
    abstract val cvc: Int?

    object Initial : FormData() {
      override val holderName: String? = null
      override val number: String? = null
      override val expiredMonth: Int? = null
      override val expiredYear: Int? = null
      override val cvc: Int? = null
    }

    data class Data(
      override val holderName: String,
      override val number: String,
      override val expiredMonth: Int,
      override val expiredYear: Int,
      override val cvc: Int
    ) : FormData()

    data class Invalid(
      override val holderName: String,
      override val number: String,
      override val expiredMonth: Int?,
      override val expiredYear: Int?,
      override val cvc: Int?
    ) : FormData()
  }

  enum class ValidationError {
    EMPTY_HOLDER_NAME,
    INVALID_NUMBER,
    INVALID_EXPIRED_DATE,
    INVALID_CVC,
  }

  data class ViewState(
    val errors: Set<ValidationError>,
    val isLoading: Boolean,
    val formData: FormData, // keep latest state when replace fragment
  ) {
    companion object Factory {
      @JvmStatic
      fun initial() = ViewState(
        isLoading = false,
        errors = emptySet(),
        formData = Initial,
      )
    }
  }

  sealed class ViewIntent {
    data class HolderNameChanged(val name: String) : ViewIntent()
    data class NumberChanged(val number: String) : ViewIntent()
    data class ExpiredDateChanged(val date: String) : ViewIntent()
    data class CvcChanged(val cvc: String) : ViewIntent()

    object Submit : ViewIntent()
  }

  sealed class SingleEvent {
    object Success : SingleEvent()
    data class Failure(val error: AppError) : SingleEvent()
  }

  sealed class PartialChange {
    fun reduce(state: ViewState): ViewState {
      return when (this) {
        Loading -> state.copy(isLoading = true)
        is Success -> state.copy(isLoading = false)
        is Failure -> state.copy(isLoading = false)
        is ErrorsAndFormDataChanged -> state.copy(
          errors = errors,
          formData = formData,
        )
      }
    }

    data class ErrorsAndFormDataChanged(
      val errors: Set<ValidationError>,
      val formData: FormData,
    ) : PartialChange()

    object Loading : PartialChange()
    object Success : PartialChange()
    data class Failure(val error: AppError) : PartialChange()
  }

  interface Interactor {
    fun addCard(formData: Data): Observable<PartialChange>
  }
}

@ExperimentalCoroutinesApi
class AddCardInteractor(
  private val cardRepository: CardRepository,
  private val dispatchers: AppDispatchers,
) : Interactor {
  override fun addCard(formData: Data): Observable<PartialChange> {
    return rxObservable(dispatchers.main) {
      send(PartialChange.Loading)

      cardRepository
        .addCard(
          holderName = formData.holderName,
          number = formData.number,
          expiredMonth = formData.expiredMonth,
          expiredYear = formData.expiredYear,
          cvc = formData.cvc,
        )
        .fold(
          ifLeft = { PartialChange.Failure(it) },
          ifRight = { PartialChange.Success },
        )
        .let { send(it) }
    }
  }
}