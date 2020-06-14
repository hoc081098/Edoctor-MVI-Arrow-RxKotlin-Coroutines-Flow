package com.doancnpm.edoctor.ui.auth.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.core.BaseFragment
import com.doancnpm.edoctor.databinding.FragmentLoginBinding
import com.doancnpm.edoctor.domain.entity.getMessage
import com.doancnpm.edoctor.ui.auth.login.LoginContract.*
import com.doancnpm.edoctor.ui.main.MainActivity
import com.doancnpm.edoctor.utils.*
import com.jakewharton.rxbinding4.view.clicks
import com.jakewharton.rxbinding4.widget.textChanges
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.kotlin.addTo
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class LoginFragment : BaseFragment(R.layout.fragment_login) {
  private val binding by viewBinding<FragmentLoginBinding>()
  private val viewModel by viewModel<LoginVM>()
  private val navArgs by navArgs<LoginFragmentArgs>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    setupViews(savedInstanceState == null)
    bindVM()
  }

  private fun setupViews(isFirstConstruction: Boolean) {
    val onClickListener = View.OnClickListener {
      findNavController().navigate(
        when (it.id) {
          R.id.sign_up_button -> LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
          R.id.verify_button -> LoginFragmentDirections.actionLoginFragmentToResendCodeFragment()
          else -> error("Missing case ${it.id}")
        }
      )
    }
    binding.signUpButton.setOnClickListener(onClickListener)
    binding.verifyButton.setOnClickListener(onClickListener)

    if (isFirstConstruction) {
      navArgs.phone?.let {
        binding.editPhone.editText!!.setText(it)
        return
      }
    }

    val state = viewModel.stateLiveData.value
    binding.editPhone.editText!!.setText(state?.phone)
    binding.editPassword.editText!!.setText(state?.password)
  }

  private fun bindVM() {
    viewModel.stateLiveData.observe(owner = viewLifecycleOwner) { (emailErrors, passwordErrors, isLoading) ->
      binding.run {

        if (ValidationError.INVALID_PHONE_NUMBER in emailErrors) {
          "Invalid phone number"
        } else {
          null
        }.let { if (editPhone.error != it) editPhone.error = it }

        if (ValidationError.TOO_SHORT_PASSWORD in passwordErrors) {
          "Too short password"
        } else {
          null
        }.let { if (editPassword.error != it) editPassword.error = it }

        if (isLoading) {
          progressBar.visible()
          buttonLogin.invisible()
        } else {
          progressBar.invisible()
          buttonLogin.visible()
        }
      }
    }

    viewModel.eventLiveData.observeEvent(owner = viewLifecycleOwner) { event ->
      when (event) {
        SingleEvent.LoginSuccess -> {
          view?.snack("Login successfully")

          startActivity(
            Intent(
              requireContext(),
              MainActivity::class.java
            )
          )
          requireActivity().finish()
        }
        is SingleEvent.LoginFailure -> {
          Timber.d("Login error: ${event.error}")
          view?.snack("Login error: ${event.error.getMessage()}")
        }
      }
    }

    viewModel.processIntents(
      Observable.mergeArray(
        binding.editPhone.editText!!.textChanges()
          .map { ViewIntent.PhoneChanged(it.toString()) },
        binding.editPassword.editText!!.textChanges()
          .map { ViewIntent.PasswordChange(it.toString()) },
        binding.buttonLogin.clicks()
          .map { ViewIntent.SubmitLogin },
      )
    ).addTo(compositeDisposable)
  }
}