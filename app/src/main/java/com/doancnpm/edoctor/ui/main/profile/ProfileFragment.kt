package com.doancnpm.edoctor.ui.main.profile

import android.os.Bundle
import android.view.View
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.core.BaseFragment
import com.doancnpm.edoctor.databinding.FragmentProfileBinding
import com.doancnpm.edoctor.domain.entity.getMessage
import com.doancnpm.edoctor.ui.main.profile.ProfileContract.ViewIntent
import com.doancnpm.edoctor.utils.*
import com.jakewharton.rxbinding4.view.clicks
import io.reactivex.rxjava3.kotlin.addTo
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProfileFragment : BaseFragment(R.layout.fragment_profile) {
  private val binding by viewBinding { FragmentProfileBinding.bind(it) }
  private val viewModel by viewModel<ProfileVM>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    setupViews()
    bindVM()
  }

  private fun bindVM() {
    viewModel.eventLiveData.observeEvent(owner = viewLifecycleOwner) {
      when (it) {
        ProfileContract.SingleEvent.LogoutSucess -> {
          view?.snack("Logout success")
        }
        is ProfileContract.SingleEvent.LogoutFailure -> {
          view?.snack("Logout failure: ${it.error.getMessage()}")
        }
      }.exhaustive
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
}