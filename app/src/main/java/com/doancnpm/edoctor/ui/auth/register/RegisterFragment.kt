package com.doancnpm.edoctor.ui.auth.register

import android.os.Bundle
import android.text.InputType
import android.view.View
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.core.BaseFragment
import com.doancnpm.edoctor.databinding.FragmentRegisterBinding
import com.doancnpm.edoctor.domain.entity.User
import com.doancnpm.edoctor.domain.entity.getMessage
import com.doancnpm.edoctor.ui.auth.register.RegisterContract.*
import com.doancnpm.edoctor.utils.*
import com.jakewharton.rxbinding4.view.clicks
import com.jakewharton.rxbinding4.widget.checkedChanges
import com.jakewharton.rxbinding4.widget.textChanges
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.kotlin.addTo
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.util.*

class RegisterFragment : BaseFragment(R.layout.fragment_register) {
  private val binding by viewBinding(FragmentRegisterBinding::bind)
  private val viewModel by viewModel<RegisterVM>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    setupViews()
    bindVM()
  }

  private fun bindVM() {
    viewModel
      .stateLiveData
      .observe(owner = viewLifecycleOwner, observer = ::render)

    viewModel.eventLiveData.observeEvent(owner = viewLifecycleOwner, observer = ::handleEvent)

    viewModel.processIntents(
      Observable.mergeArray(
        binding.editPhone.editText!!
          .textChanges()
          .map { ViewIntent.PhoneChanged(it.toString()) },
        binding.editPassword.editText!!
          .textChanges()
          .map { ViewIntent.PasswordChanged(it.toString()) },
        binding.editFullName.editText!!
          .textChanges()
          .map { ViewIntent.FullNameChanged(it.toString()) },
        Observable.just(
          ViewIntent.BirthdayChanged(
            parse_yyyyMMdd("1998-10-08")!!
          )
        ),
        binding.editBirthday
          .editText!!
          .clicks()
          .exhaustMap { requireActivity().pickDateObservable(1998, Calendar.OCTOBER, 8) }
          .map { ViewIntent.BirthdayChanged(it) },
        binding.radioRole
          .checkedChanges()
          .map {
            when (it) {
              R.id.radioCustomer -> User.RoleId.CUSTOMER
              R.id.radioDoctor -> User.RoleId.DOCTOR
              else -> error("Invalid checked id")
            }
          }
          .map { ViewIntent.RoleIdChanged(it) },
        binding.buttonRegister
          .clicks()
          .map { ViewIntent.Submit },
      )
    ).addTo(compositeDisposable)
  }

  private fun handleEvent(event: SingleEvent) {
    when (event) {
      is SingleEvent.Success -> {
        view?.snack("Register success") {

        }
      }
      is SingleEvent.Failure -> {
        Timber.d("Register error: ${event.error}")
        view?.snack("Register error: ${event.error.getMessage()}")
      }
    }
  }

  private fun render(state: ViewState) {
    val (errors, isLoading, formData) = state
    Timber.d("State: $state")

    binding.run {
      if (ValidationError.INVALID_PHONE_NUMBER in errors) {
        "Invalid phone number"
      } else {
        null
      }.let { if (it != editPhone.error) editPhone.error = it }

      if (ValidationError.TOO_SHORT_PASSWORD in errors) {
        "Too short password"
      } else {
        null
      }.let { if (editPassword.error != it) editPassword.error = it }

      if (ValidationError.TOO_SHORT_FULL_NAME in errors) {
        "Too short full name"
      } else {
        null
      }.let { if (editFullName.error != it) editFullName.error = it }

      if (ValidationError.INVALID_BIRTH_DAY in errors) {
        "Invalid birthday"
      } else {
        null
      }.let { if (editBirthday.error != it) editBirthday.error = it }

      editBirthday.editText!!.setText(
        formData.birthday?.toString_yyyyMMdd()
      )

      if (isLoading) {
        progressBar.visible()
        buttonRegister.invisible()
      } else {
        progressBar.invisible()
        buttonRegister.visible()
      }
    }
  }

  private fun setupViews() {
    binding.editBirthday.editText!!.inputType = InputType.TYPE_NULL
    binding.editBirthday.editText!!.onFocusChangeListener = null

    val (phone, password, roleId, fullName, birthday) =
      viewModel.stateLiveData.value?.formData as? RegisterContract.FormData.Data ?: return

    binding.run {
      editPhone.editText!!.setText(phone)
      editPassword.editText!!.setText(password)
      editFullName.editText!!.setText(fullName)
      radioRole.check(
        when (roleId) {
          User.RoleId.CUSTOMER -> R.id.radioCustomer
          User.RoleId.DOCTOR -> R.id.radioDoctor
        }
      )
      editBirthday.editText!!.setText(
        birthday?.toString_yyyyMMdd()
      )
    }
  }
}