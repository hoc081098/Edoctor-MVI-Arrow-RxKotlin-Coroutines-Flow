package com.doancnpm.edoctor.ui.auth.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class LoginFragment : BaseFragment(R.layout.fragment_login) {
  private val binding by viewBinding(FragmentLoginBinding::bind)
  private val viewModel by viewModel<LoginVM>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    setupViews()
    bindVM()
  }

  private fun setupViews() {
    binding.signUpButton.setOnClickListener {
      findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToRegisterFragment())
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
          view?.snack("Login success") {
            onDismissed {
              startActivity(
                Intent(
                  requireContext(),
                  MainActivity::class.java
                )
              )
            }
          }
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