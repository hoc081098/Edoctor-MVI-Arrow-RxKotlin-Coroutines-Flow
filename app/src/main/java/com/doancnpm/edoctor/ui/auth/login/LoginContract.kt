package com.doancnpm.edoctor.ui.auth.login

import com.doancnpm.edoctor.domain.dispatchers.AppDispatchers
import com.doancnpm.edoctor.domain.entity.AppError
import com.doancnpm.edoctor.domain.repository.UserRepository
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.rx3.rxObservable

interface LoginContract {
  enum class ValidationError {
    INVALID_PHONE_NUMBER,
    TOO_SHORT_PASSWORD,
  }

  data class ViewState(
    val phoneErrors: Set<ValidationError>,
    val passwordErrors: Set<ValidationError>,
    val isLoading: Boolean,
    // keep latest state when replace fragment
    val phone: String?,
    val password: String?,
    val phoneChanged: Boolean,
    val passwordChanged: Boolean,
  ) {
    companion object {
      @JvmStatic
      fun initial() = ViewState(
        isLoading = false,
        phoneErrors = emptySet(),
        passwordErrors = emptySet(),
        phone = null,
        password = null,
        phoneChanged = false,
        passwordChanged = false,
      )
    }
  }

  sealed class ViewIntent {
    data class PhoneChanged(val phone: String) : ViewIntent()
    data class PasswordChange(val password: String) : ViewIntent()
    object SubmitLogin : ViewIntent()

    object PhoneChangedFirstTime : ViewIntent()
    object PasswordChangedFirstTime : ViewIntent()
  }

  sealed class SingleEvent {
    object LoginSuccess : SingleEvent()
    data class LoginFailure(val error: AppError) : SingleEvent()
  }

  sealed class PartialChange {
    fun reduce(state: ViewState): ViewState {
      return when (this) {
        is PhoneError -> state.copy(phoneErrors = errors)
        is PasswordError -> state.copy(passwordErrors = errors)
        Loading -> state.copy(isLoading = true)
        LoginSuccess -> state.copy(isLoading = false)
        is LoginFailure -> state.copy(isLoading = false)
        is PhoneChanged -> state.copy(phone = phone)
        is PasswordChanged -> state.copy(password = password)
        PhoneChangedFirstTime -> state.copy(phoneChanged = true)
        PasswordChangedFirstTime -> state.copy(passwordChanged = true)
      }
    }

    data class PhoneError(val errors: Set<ValidationError>) : PartialChange()
    data class PasswordError(val errors: Set<ValidationError>) : PartialChange()

    data class PhoneChanged(val phone: String) : PartialChange()
    data class PasswordChanged(val password: String) : PartialChange()

    object Loading : PartialChange()
    object LoginSuccess : PartialChange()
    data class LoginFailure(val error: AppError) : PartialChange()

    object PhoneChangedFirstTime : PartialChange()
    object PasswordChangedFirstTime : PartialChange()
  }

  interface Interactor {
    fun login(phone: String, password: String): Observable<PartialChange>
  }
}

@ExperimentalCoroutinesApi
class LoginInteractor(
  private val dispatchers: AppDispatchers,
  private val userRepository: UserRepository,
) : LoginContract.Interactor {
  override fun login(phone: String, password: String): Observable<LoginContract.PartialChange> {
    return rxObservable(dispatchers.main) {
      send(LoginContract.PartialChange.Loading)
      userRepository
        .login(phone, password)
        .fold(
          ifLeft = { LoginContract.PartialChange.LoginFailure(it) },
          ifRight = { LoginContract.PartialChange.LoginSuccess }
        )
        .let { send(it) }
    }
  }
}