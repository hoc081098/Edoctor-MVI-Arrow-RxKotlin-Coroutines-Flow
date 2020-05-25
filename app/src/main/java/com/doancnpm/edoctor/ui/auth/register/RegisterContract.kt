package com.doancnpm.edoctor.ui.auth.register

import com.doancnpm.edoctor.domain.dispatchers.AppDispatchers
import com.doancnpm.edoctor.domain.entity.AppError
import com.doancnpm.edoctor.domain.entity.User.RoleId
import com.doancnpm.edoctor.domain.repository.UserRepository
import com.doancnpm.edoctor.ui.auth.register.RegisterContract.PartialChange
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.rx3.rxObservable
import java.util.*

interface RegisterContract {
  sealed class FormData {
    abstract val phone: String?
    abstract val password: String?
    abstract val roleId: RoleId?
    abstract val fullName: String?
    abstract val birthday: Date?

    object Initial : FormData() {
      override val phone: String? = null
      override val password: String? = null
      override val roleId: RoleId? = null
      override val fullName: String? = null
      override val birthday: Date? = null
    }

    data class Data(
      override val phone: String,
      override val password: String,
      override val roleId: RoleId,
      override val fullName: String,
      override val birthday: Date?,
    ) : FormData()
  }

  enum class ValidationError {
    INVALID_PHONE_NUMBER,
    TOO_SHORT_PASSWORD,
    MISSING_ROLE,
    TOO_SHORT_FULL_NAME,
    INVALID_BIRTH_DAY,
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
        formData = FormData.Initial,
      )
    }
  }

  sealed class ViewIntent {
    data class PhoneChanged(val phone: String) : ViewIntent()
    data class PasswordChanged(val password: String) : ViewIntent()
    data class RoleIdChanged(val roleId: RoleId) : ViewIntent()
    data class FullNameChanged(val fullName: String) : ViewIntent()
    data class BirthdayChanged(val date: Date) : ViewIntent()
    object Submit : ViewIntent()
  }

  sealed class SingleEvent {
    data class Success(val phone: String) : SingleEvent()
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
      val formData: FormData.Data,
    ) : PartialChange()

    object Loading : PartialChange()
    data class Success(val phone: String) : PartialChange()
    data class Failure(val error: AppError) : PartialChange()
  }

  interface Interactor {
    fun register(formData: FormData.Data): Observable<PartialChange>
  }
}

@ExperimentalCoroutinesApi
class RegisterInteractor(
  private val dispatchers: AppDispatchers,
  private val userRepository: UserRepository,
) : RegisterContract.Interactor {
  override fun register(formData: RegisterContract.FormData.Data): Observable<PartialChange> {
    return rxObservable(dispatchers.main) {
      send(PartialChange.Loading)

      userRepository.register(
        phone = formData.phone,
        password = formData.password,
        roleId = formData.roleId,
        fullName = formData.fullName,
        birthday = formData.birthday,
      ).fold(
        ifLeft = { PartialChange.Failure(it) },
        ifRight = { PartialChange.Success(it) }
      ).let { send(it) }
    }
  }
}