package com.doancnpm.edoctor.ui.main.profile.update_profile

import android.net.Uri
import com.doancnpm.edoctor.domain.entity.AppError
import com.doancnpm.edoctor.domain.entity.User
import java.util.Date

interface UpdateProfileContract {
  data class ViewState(
    val fullName: String,
    val birthday: Date?,
    val avatar: Uri?,
    val isLoading: Boolean,
    val tooShortFullName: Boolean,
  ) {
    companion object {
      fun initial(user: User): ViewState {
        return ViewState(
          fullName = user.fullName,
          birthday = user.birthday,
          avatar = null,
          isLoading = false,
          tooShortFullName = user.fullName.length < 6
        )
      }
    }
  }

  sealed class ViewIntent {
    data class AvatarChanged(val avatar: Uri?) : ViewIntent()
    data class FullNameChanged(val name: String) : ViewIntent()
    data class BirthdayChanged(val date: Date?) : ViewIntent()

    object Submit : ViewIntent()
  }

  sealed class PartialChange {
    data class AvatarChanged(val avatar: Uri?) : PartialChange()
    data class FullNameChanged(
      val name: String,
      val tooShortFullName: Boolean
    ) : PartialChange()

    data class BirthdayChanged(val date: Date?) : PartialChange()

    object Loading : PartialChange()
    object Success : PartialChange()
    data class Failure(val error: AppError) : PartialChange()

    fun reduce(vs: ViewState): ViewState {
      return when (this) {
        is AvatarChanged -> vs.copy(avatar = avatar)
        is FullNameChanged -> vs.copy(
          fullName = name,
          tooShortFullName = tooShortFullName
        )
        is BirthdayChanged -> vs.copy(birthday = date)
        Loading -> vs.copy(isLoading = true)
        Success -> vs.copy(isLoading = false)
        is Failure -> vs.copy(isLoading = false)
      }
    }
  }

  sealed class SingleEvent {
    object Success : SingleEvent()
    data class Failure(val error: AppError) : SingleEvent()
  }
}