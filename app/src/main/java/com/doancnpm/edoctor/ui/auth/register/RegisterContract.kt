package com.doancnpm.edoctor.ui.auth.register

import com.doancnpm.edoctor.domain.dispatchers.AppDispatchers
import com.doancnpm.edoctor.domain.entity.AppError
import com.doancnpm.edoctor.domain.entity.User.RoleId
import com.doancnpm.edoctor.domain.repository.UserRepository
import com.doancnpm.edoctor.ui.auth.register.RegisterContract.PartialChange
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.rx3.rxObservable
import java.util.Date

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

  data class Changed(
    val phone: Boolean = false,
    val password: Boolean = false,
    val fullName: Boolean = false,
  )

  enum class ValidationError {
    INVALID_PHONE_NUMBER,
    TOO_SHORT_PASSWORD,
    TOO_SHORT_FULL_NAME,
    INVALID_BIRTH_DAY,
  }

  data class ViewState(
    val errors: Set<ValidationError>,
    val isLoading: Boolean,
    val formData: FormData, // keep latest state when replace fragment,
    val changed: Changed,
  ) {
    companion object Factory {
      @JvmStatic
      fun initial() = ViewState(
        isLoading = false,
        errors = emptySet(),
        formData = FormData.Initial,
        changed = Changed(),
      )
    }
  }

  sealed class ViewIntent {
    data class PhoneChanged(val phone: String) : ViewIntent()
    data class PasswordChanged(val password: String) : ViewIntent()
    data class RoleIdChanged(val roleId: RoleId) : ViewIntent()
    data class FullNameChanged(val fullName: String) : ViewIntent()
    data class BirthdayChanged(val date: Date?) : ViewIntent()
    object Submit : ViewIntent()

    object PhoneChangedFirstTime : ViewIntent()
    object PasswordChangedFirstTime : ViewIntent()
    object FullNameChangedFirstTime : ViewIntent()
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
        PhoneChangedFirstTime -> state.copy(changed = state.changed.copy(phone = true))
        PasswordChangedFirstTime -> state.copy(changed = state.changed.copy(password = true))
        FullNameChangedFirstTime -> state.copy(changed = state.changed.copy(fullName = true))
      }
    }

    data class ErrorsAndFormDataChanged(
      val errors: Set<ValidationError>,
      val formData: FormData.Data,
    ) : PartialChange()

    object Loading : PartialChange()
    data class Success(val phone: String) : PartialChange()
    data class Failure(val error: AppError) : PartialChange()

    object PhoneChangedFirstTime : PartialChange()
    object PasswordChangedFirstTime : PartialChange()
    object FullNameChangedFirstTime : PartialChange()
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