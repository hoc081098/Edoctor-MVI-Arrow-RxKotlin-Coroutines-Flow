package com.doancnpm.edoctor.ui.auth.verify

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.core.BaseFragment
import com.doancnpm.edoctor.databinding.FragmentVerifyBinding
import com.doancnpm.edoctor.domain.entity.getMessage
import com.doancnpm.edoctor.ui.auth.verify.VerifyContract.*
import com.doancnpm.edoctor.utils.*
import com.jakewharton.rxbinding4.view.clicks
import com.jakewharton.rxbinding4.widget.textChanges
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.kotlin.addTo
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class VerifyFragment : BaseFragment(R.layout.fragment_verify) {
  private val viewModel by viewModel<VerifyVM> { parametersOf(navArgs.phone) }
  private val binding by viewBinding { FragmentVerifyBinding.bind(it) }
  private val navArgs by navArgs<VerifyFragmentArgs>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    setupViews()
    bindVM()
  }

  private fun bindVM() {
    viewModel.viewState.observe(owner = viewLifecycleOwner) { (_, errors, isLoading) ->
      binding.run {
        if (ValidationError.EMPTY_CODE in errors) {
          "Empty code"
        } else {
          null
        }.let { if (editCode.error != it) editCode.error = it }

        if (isLoading) {
          progressBar.visible()
          buttonVerify.invisible()
        } else {
          progressBar.invisible()
          buttonVerify.visible()
        }
      }
    }
    viewModel.singleEvent.observeEvent(owner = viewLifecycleOwner) { event ->
      when (event) {
        SingleEvent.Success -> {
          view?.snack("Verify successfully")

          VerifyFragmentDirections
            .actionVerifyFragmentToLoginFragment()
            .apply { phone = navArgs.phone }
            .let { findNavController().navigate(it) }
        }
        is SingleEvent.Failure -> {
          view?.snack(event.error.getMessage())
        }
      }
    }

    viewModel.process(
      Observable.mergeArray(
        binding.editCode.editText!!
          .textChanges()
          .map { ViewIntent.CodeChanged(it.toString()) },
        binding.buttonVerify
          .clicks()
          .map { ViewIntent.Submit },
      )
    ).addTo(compositeDisposable)
  }

  private fun setupViews() {
    binding.editCode.editText!!.setText(viewModel.viewState.value?.code)
  }
}