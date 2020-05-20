package com.doancnpm.edoctor.ui.auth.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.core.BaseFragment
import com.doancnpm.edoctor.databinding.FragmentLoginBinding
import com.doancnpm.edoctor.domain.entity.getMessage
import com.doancnpm.edoctor.ui.auth.login.LoginContract.*
import com.doancnpm.edoctor.ui.main.MainActivity
import com.doancnpm.edoctor.utils.*
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.Observable
import io.reactivex.rxkotlin.addTo
import org.koin.androidx.viewmodel.ext.android.viewModel

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
    binding.editEmail.editText!!.setText(state?.email)
    binding.editPassword.editText!!.setText(state?.password)
  }

  private fun bindVM() {
    viewModel.stateLiveData.observe(owner = viewLifecycleOwner) { (emailErrors, passwordErrors, isLoading) ->
      if (binding.editEmail.error != emailErrors) {
        binding.editEmail.error = if (ValidationError.INVALID_EMAIL_ADDRESS in emailErrors) {
          "Invalid email address"
        } else {
          null
        }
      }
      if (binding.editPassword.error != passwordErrors) {
        binding.editPassword.error = if (ValidationError.TOO_SHORT_PASSWORD in passwordErrors) {
          "Too short password"
        } else {
          null
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
          view?.snack("Login error: ${event.error.getMessage()}")
        }
      }
    }

    viewModel.processIntents(
      Observable.mergeArray(
        binding.editEmail.editText!!.textChanges()
          .map { ViewIntent.EmailChanged(it.toString()) },
        binding.editPassword.editText!!.textChanges()
          .map { ViewIntent.PasswordChange(it.toString()) },
        binding.buttonLogin.clicks()
          .map { ViewIntent.SubmitLogin }
      )
    ).addTo(compositeDisposable)
  }
}