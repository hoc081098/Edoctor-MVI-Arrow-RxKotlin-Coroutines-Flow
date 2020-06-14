package com.doancnpm.edoctor.ui.main.profile

import android.os.Bundle
import android.view.View
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.doancnpm.edoctor.GlideApp
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.core.BaseFragment
import com.doancnpm.edoctor.databinding.FragmentProfileBinding
import com.doancnpm.edoctor.domain.entity.User
import com.doancnpm.edoctor.domain.entity.getMessage
import com.doancnpm.edoctor.ui.main.profile.ProfileContract.ViewIntent
import com.doancnpm.edoctor.utils.*
import com.jakewharton.rxbinding4.view.clicks
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProfileFragment : BaseFragment(R.layout.fragment_profile) {
  private val binding by viewBinding<FragmentProfileBinding>()
  private val viewModel by viewModel<ProfileVM>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    setupViews()
    bindVM()
  }

  private fun bindVM() {
    val glide = GlideApp.with(this)

    viewModel.userObservable
      .subscribeBy {
        binding.run {
          it.fold(
            ifEmpty = {
              imageAvatar.setImageResource(R.drawable.ic_person_black_24dp)

              textFullName.text = NOT_LOGGED_IN
              textPhone.text = NOT_LOGGED_IN
              textBirthday.text = NOT_LOGGED_IN
              textStatus.text = NOT_LOGGED_IN
            },
            ifSome = { user ->
              user.avatar
                ?.let {
                  glide
                    .load(it)
                    .placeholder(R.drawable.ic_person_black_24dp)
                    .error(R.drawable.ic_person_black_24dp)
                    .transition(withCrossFade())
                    .into(imageAvatar)
                }
                ?: imageAvatar.setImageResource(R.drawable.ic_person_black_24dp)

              textFullName.text = user.fullName
              textPhone.text = user.phone
              textBirthday.text = user.birthday ?: "N/A"
              textStatus.text = when (user.status) {
                User.Status.INACTIVE -> "Inactive"
                User.Status.ACTIVE -> "Active"
                User.Status.PENDING -> "Pending"
              }
            }
          )
        }
      }
      .addTo(compositeDisposable)

    viewModel.eventLiveData.observeEvent(owner = viewLifecycleOwner) {
      when (it) {
        ProfileContract.SingleEvent.LogoutSucess -> {
          requireContext().toast("Logout success")
        }
        is ProfileContract.SingleEvent.LogoutFailure -> {
          view?.snack("Logout failure: ${it.error.getMessage()}")
        }
      }
    }

    viewModel.process(
      binding
        .logoutButton
        .clicks()
        .exhaustMap {
          requireActivity().showAlertDialogAsObservable {
            title("Logout")
            message("Are you sure you want to logout?")
            iconId(R.drawable.ic_exit_to_app_black_24dp)
            cancelable(true)
          }
        }
        .map { ViewIntent.Logout }
    ).addTo(compositeDisposable)
  }

  private fun setupViews() {

  }

  private companion object {
    const val NOT_LOGGED_IN = "Not logged in"
  }
}