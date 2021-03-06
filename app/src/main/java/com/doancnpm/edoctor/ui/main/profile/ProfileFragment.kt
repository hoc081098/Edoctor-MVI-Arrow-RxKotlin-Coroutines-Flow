package com.doancnpm.edoctor.ui.main.profile

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
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
import timber.log.Timber

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
            ifLeft = {
              imageAvatar.setImageResource(R.drawable.icons8_person_96)

              textFullName.text = NOT_LOGGED_IN
              textPhone.text = NOT_LOGGED_IN
              textBirthday.text = NOT_LOGGED_IN
              textStatus.text = NOT_LOGGED_IN

              fabUpdateProfile.invisible()
            },
            ifRight = { user ->
              user.avatar
                ?.let {
                  glide
                    .load(it)
                    .placeholder(R.drawable.icons8_person_96)
                    .error(R.drawable.icons8_person_96)
                    .dontAnimate()
                    .into(imageAvatar)
                }
                ?: when (val firstLetter = user.fullName.firstOrNull()) {
                  null -> ColorDrawable(Color.parseColor("#fafafa"))
                  else -> {
                    val size = requireContext()
                      .dpToPx(96)
                      .also { Timber.d("96dp = ${it}px") }
                    TextDrawable
                      .builder()
                      .beginConfig()
                      .width(size)
                      .height(size)
                      .endConfig()
                      .buildRect(
                        firstLetter.toUpperCase().toString(),
                        ColorGenerator.MATERIAL.getColor(user.phone),
                      )
                  }
                }.let(imageAvatar::setImageDrawable)

              textFullName.text = user.fullName
              textPhone.text = user.phone
              textBirthday.text = user.birthday?.toString_yyyyMMdd() ?: "N/A"
              textStatus.text = when (user.status) {
                User.Status.INACTIVE -> "Inactive"
                User.Status.ACTIVE -> "Active"
                User.Status.PENDING -> "Pending"
              }

              fabUpdateProfile.visible()
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

    viewModel.isLoggingOut.observe(owner = viewLifecycleOwner) {
      binding.run {
        if (it) {
          logoutButton.invisible()
          progressBar.visible()
        } else {
          logoutButton.visible()
          progressBar.invisible()
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
    binding.fabUpdateProfile
      .clicks()
      .withLatestFrom(viewModel.userObservable) { _, user -> user }
      .mapNotNull { it.orNull() }
      .subscribe { user ->
        ProfileFragmentDirections
          .actionProfileFragmentToUpdateProfileFragment(user)
          .let { findNavController().navigate(it) }
      }
      .addTo(compositeDisposable)
  }

  private companion object {
    const val NOT_LOGGED_IN = "Not logged in"
  }
}