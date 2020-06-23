package com.doancnpm.edoctor.ui.auth.resendcode

import com.doancnpm.edoctor.domain.dispatchers.AppDispatchers
import com.doancnpm.edoctor.domain.entity.AppError
import com.doancnpm.edoctor.domain.repository.UserRepository
import com.doancnpm.edoctor.ui.auth.resendcode.ResendCodeContract.PartialStateChange
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.rx3.rxObservable

interface ResendCodeContract {
  //region Outputs
  enum class ValidationError { INVALID_PHONE_NUMBER }

  data class ViewState(
    val phone: String? = null,
    val errors: Set<ValidationError> = emptySet(),
    val isLoading: Boolean = false,
    val phoneChanged: Boolean = false,
  )

  sealed class SingleEvent {
    data class Success(val phone: String) : SingleEvent()
    data class Failure(val phone: String, val error: AppError) : SingleEvent()
  }
  //endregion

  //region Inputs
  sealed class ViewIntent {
    data class PhoneChanged(val phone: String) : ViewIntent()
    object Submit : ViewIntent()

    object PhoneChangedFirstTime : ViewIntent()
  }

  sealed class PartialStateChange {
    fun reduce(state: ViewState): ViewState {
      return when (this) {
        is PhoneChanged -> state.copy(phone = phone)
        is PhoneErrors -> state.copy(errors = errors)
        Loading -> state.copy(isLoading = true)
        is Success -> state.copy(isLoading = false)
        is Failure -> state.copy(isLoading = false)
        PhoneChangedFirstTime -> state.copy(phoneChanged = true)
      }
    }

    data class PhoneChanged(val phone: String) : PartialStateChange()
    data class PhoneErrors(val errors: Set<ValidationError>) : PartialStateChange()

    object Loading : PartialStateChange()
    data class Success(val phone: String) : PartialStateChange()
    data class Failure(val phone: String, val error: AppError) : PartialStateChange()

    object PhoneChangedFirstTime : PartialStateChange()
  }
  //endregion

  interface Interactor {
    fun resendCode(phone: String): Observable<PartialStateChange>
  }
}

@ExperimentalCoroutinesApi
class ResendCodeInteractor(
  private val dispatchers: AppDispatchers,
  private val userRepository: UserRepository,
) : ResendCodeContract.Interactor {
  override fun resendCode(phone: String): Observable<PartialStateChange> {
    return rxObservable(dispatchers.main) {
      send(PartialStateChange.Loading)
      userRepository
        .resendCode(phone)
        .fold(
          ifLeft = { PartialStateChange.Failure(phone, it) },
          ifRight = { PartialStateChange.Success(phone) },
        )
        .let { send(it) }
    }
  }
}