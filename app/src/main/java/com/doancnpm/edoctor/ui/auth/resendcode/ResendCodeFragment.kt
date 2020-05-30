package com.doancnpm.edoctor.ui.auth.resendcode

import android.os.Bundle
import android.view.View
import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import androidx.navigation.fragment.findNavController
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.core.BaseFragment
import com.doancnpm.edoctor.databinding.FragmentResendCodeBinding
import com.doancnpm.edoctor.domain.entity.getMessage
import com.doancnpm.edoctor.ui.auth.resendcode.ResendCodeContract.ValidationError
import com.doancnpm.edoctor.ui.auth.resendcode.ResendCodeContract.ViewIntent
import com.doancnpm.edoctor.utils.*
import com.jakewharton.rxbinding4.view.clicks
import com.jakewharton.rxbinding4.widget.textChanges
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.kotlin.addTo
import org.koin.androidx.viewmodel.ext.android.viewModel

class ResendCodeFragment : BaseFragment(R.layout.fragment_resend_code) {
  private val binding by viewBinding { FragmentResendCodeBinding.bind(it) }
  private val viewModel by viewModel<ResendCodeVM>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    setupViews()
    bindVM()
  }

  private fun setupViews() {
    binding.textView2.text = HtmlCompat.fromHtml(
      "We will send you a <b>One time code</b> on your phone number",
      FROM_HTML_MODE_LEGACY
    )

    binding.editPhone.editText!!.setText(viewModel.viewState.value?.phone)
  }

  private fun bindVM() {
    viewModel.viewState.observe(owner = viewLifecycleOwner) { (_, errors, isLoading) ->
      binding.run {
        if (ValidationError.INVALID_PHONE_NUMBER in errors) {
          "Invalid phone number"
        } else {
          null
        }.let { if (editPhone.error != it) editPhone.error = it }

        if (isLoading) {
          progressBar.visible()
          buttonGetOtp.invisible()
        } else {
          progressBar.invisible()
          buttonGetOtp.visible()
        }
      }
    }
    viewModel.singleEvent.observeEvent(owner = viewLifecycleOwner) { event ->
      when (event) {
        is ResendCodeContract.SingleEvent.Success -> {
          view?.snack("Resend code successfully")

          ResendCodeFragmentDirections
            .actionResendCodeFragmentToVerifyFragment(event.phone)
            .let { findNavController().navigate(it) }
        }
        is ResendCodeContract.SingleEvent.Failure -> {
          view?.snack(event.error.getMessage())
        }
      }
    }

    viewModel.process(
      Observable.mergeArray(
        binding.editPhone.editText!!
          .textChanges()
          .map { ViewIntent.PhoneChanged(it.toString()) },
        binding.buttonGetOtp.clicks()
          .map { ViewIntent.Submit },
      )
    ).addTo(compositeDisposable)
  }
}