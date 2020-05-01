package com.doancnpm.edoctor.ui.auth.login

import com.doancnpm.edoctor.domain.dispatchers.AppDispatchers
import com.doancnpm.edoctor.domain.entity.AppError
import com.doancnpm.edoctor.domain.repository.UserRepository
import io.reactivex.Observable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.rx2.rxObservable

interface LoginContract {
  enum class ValidationError {
    INVALID_EMAIL_ADDRESS,
    TOO_SHORT_PASSWORD,
  }

  data class ViewState(
    val emailErrors: Set<ValidationError>,
    val passwordErrors: Set<ValidationError>,
    val isLoading: Boolean,
    // keep latest state when replace fragment
    val email: String?,
    val password: String?,
  ) {
    companion object {
      @JvmStatic
      fun initial() = ViewState(
        isLoading = false,
        emailErrors = emptySet(),
        passwordErrors = emptySet(),
        email = null,
        password = null
      )
    }
  }

  sealed class ViewIntent {
    data class EmailChanged(val email: String) : ViewIntent()
    data class PasswordChange(val password: String) : ViewIntent()
    object SubmitLogin : ViewIntent()
  }

  sealed class SingleEvent {
    object LoginSuccess : SingleEvent()
    data class LoginFailure(val error: AppError) : SingleEvent()
  }

  sealed class PartialChange {
    fun reduce(state: ViewState): ViewState {
      return when (this) {
        is EmailError -> state.copy(emailErrors = errors)
        is PasswordError -> state.copy(passwordErrors = errors)
        Loading -> state.copy(isLoading = true)
        LoginSuccess -> state.copy(isLoading = false)
        is LoginFailure -> state.copy(isLoading = false)
        is EmailChanged -> state.copy(email = email)
        is PasswordChanged -> state.copy(password = password)
      }
    }

    data class EmailError(val errors: Set<ValidationError>) : PartialChange()
    data class PasswordError(val errors: Set<ValidationError>) : PartialChange()

    data class EmailChanged(val email: String) : PartialChange()
    data class PasswordChanged(val password: String) : PartialChange()

    object Loading : PartialChange()
    object LoginSuccess : PartialChange()
    data class LoginFailure(val error: AppError) : PartialChange()
  }

  interface Interactor {
    fun login(email: String, password: String): Observable<PartialChange>
  }
}

@ExperimentalCoroutinesApi
class LoginInteractor(
  private val dispatchers: AppDispatchers,
  private val userRepository: UserRepository,
) : LoginContract.Interactor {
  override fun login(email: String, password: String): Observable<LoginContract.PartialChange> {
    return rxObservable(dispatchers.main) {
      send(LoginContract.PartialChange.Loading)
      userRepository
        .login(email, password)
        .fold(
          ifLeft = { LoginContract.PartialChange.LoginFailure(it) },
          ifRight = { LoginContract.PartialChange.LoginSuccess }
        )
        .let { send(it) }
    }
  }
}