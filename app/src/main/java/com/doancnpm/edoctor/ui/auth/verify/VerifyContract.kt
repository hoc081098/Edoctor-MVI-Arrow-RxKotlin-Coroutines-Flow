package com.doancnpm.edoctor.ui.auth.verify

import com.doancnpm.edoctor.domain.dispatchers.AppDispatchers
import com.doancnpm.edoctor.domain.entity.AppError
import com.doancnpm.edoctor.domain.repository.UserRepository
import com.doancnpm.edoctor.ui.auth.verify.VerifyContract.PartialStateChange
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.rx3.rxObservable

interface VerifyContract {
  //region Outputs
  enum class ValidationError { EMPTY_CODE }

  data class ViewState(
    val code: String? = null,
    val errors: Set<ValidationError> = emptySet(),
    val isLoading: Boolean = false,
    val codeChanged: Boolean = false
  )

  sealed class SingleEvent {
    object Success : SingleEvent()
    data class Failure(val error: AppError) : SingleEvent()
  }
  //endregion

  //region Inputs
  sealed class ViewIntent {
    data class CodeChanged(val code: String) : ViewIntent()
    object Submit : ViewIntent()

    object CodeChangedFirstTime : ViewIntent()
  }

  sealed class PartialStateChange {
    fun reduce(state: ViewState): ViewState {
      return when (this) {
        is CodeChanged -> state.copy(code = code)
        is CodeErrors -> state.copy(errors = errors)
        Loading -> state.copy(isLoading = true)
        is Success -> state.copy(isLoading = false)
        is Failure -> state.copy(isLoading = false)
        CodeChangedFirstTime -> state.copy(codeChanged = true)
      }
    }

    data class CodeChanged(val code: String) : PartialStateChange()
    data class CodeErrors(val errors: Set<ValidationError>) : PartialStateChange()

    object Loading : PartialStateChange()
    object Success : PartialStateChange()
    data class Failure(val error: AppError) : PartialStateChange()

    object CodeChangedFirstTime : PartialStateChange()
  }
  //endregion

  interface Interactor {
    fun verify(phone: String, code: String): Observable<PartialStateChange>
  }
}

@ExperimentalCoroutinesApi
class VerifyInteractor(
  private val dispatchers: AppDispatchers,
  private val userRepository: UserRepository,
) : VerifyContract.Interactor {
  override fun verify(phone: String, code: String): Observable<PartialStateChange> {
    return rxObservable(dispatchers.main) {
      send(PartialStateChange.Loading)

      userRepository
        .verify(phone = phone, code = code)
        .fold(
          ifLeft = { PartialStateChange.Failure(it) },
          ifRight = { PartialStateChange.Success }
        )
        .let { send(it) }
    }
  }
}