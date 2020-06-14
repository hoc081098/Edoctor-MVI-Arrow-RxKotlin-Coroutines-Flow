package com.doancnpm.edoctor.ui.main.home.create_order.inputs.select_card.add_card

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.core.BaseFragment
import com.doancnpm.edoctor.databinding.FragmentAddCardBinding
import com.doancnpm.edoctor.domain.entity.getMessage
import com.doancnpm.edoctor.ui.main.home.create_order.inputs.select_card.add_card.AddCardContract.*
import com.doancnpm.edoctor.utils.*
import com.jakewharton.rxbinding4.view.clicks
import com.jakewharton.rxbinding4.widget.textChanges
import io.reactivex.rxjava3.core.Observable.mergeArray
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class AddCardFragment : BaseFragment(R.layout.fragment_add_card) {
  private val binding by viewBinding<FragmentAddCardBinding>()
  private val viewModel by viewModel<AddCardVM>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    setupViews()
    bindVM()
  }

  private fun bindVM() {
    viewModel.stateLiveData.observe(owner = viewLifecycleOwner, observer = ::render)

    viewModel.eventLiveData.observeEvent(owner = viewLifecycleOwner, observer = ::handleEvent)

    viewModel.processIntents(
      binding.run {
        mergeArray(
          editHolderName.editText!!.textChanges().map { ViewIntent.HolderNameChanged(it.toString()) },
          editCardNumber.editText!!.textChanges().map { ViewIntent.NumberChanged(it.toString()) },
          editExpiredDate.editText!!.textChanges().map { ViewIntent.ExpiredDateChanged(it.toString()) },
          editCvc.editText!!.textChanges().map { ViewIntent.CvcChanged(it.toString()) },
          buttonAdd.clicks().map { ViewIntent.Submit },
        )
      }
    )
  }

  private fun handleEvent(event: SingleEvent) {
    when (event) {
      SingleEvent.Success -> {
        view?.snack("Add card successfully")
        findNavController().popBackStack()
      }
      is SingleEvent.Failure -> {
        Timber.d("Add card error: ${event.error}")
        view?.snack("Add card error: ${event.error.getMessage()}")
      }
    }
  }

  private fun render(state: ViewState) {
    val (errors, isLoading, formData) = state
    Timber.d("State: $state")

    binding.run {
      if (ValidationError.EMPTY_HOLDER_NAME in errors) {
        "Blank holder name"
      } else {
        null
      }.let { if (it != editHolderName.error) editHolderName.error = it }

      if (ValidationError.INVALID_NUMBER in errors) {
        "Invalid card number"
      } else {
        null
      }.let { if (editCardNumber.error != it) editCardNumber.error = it }

      if (ValidationError.INVALID_EXPIRED_DATE in errors) {
        "Invalid expired date"
      } else {
        null
      }.let { if (editExpiredDate.error != it) editExpiredDate.error = it }

      if (ValidationError.INVALID_CVC in errors) {
        "Invalid cvc"
      } else {
        null
      }.let { if (editCvc.error != it) editCvc.error = it }

      if (isLoading) {
        progressBar.visible()
        buttonAdd.invisible()
      } else {
        progressBar.invisible()
        buttonAdd.visible()
      }
    }
  }

  private fun setupViews() {
    fun bindInitial(holderName: String, number: String, expiredMonth: Int?, expiredYear: Int?, cvc: Int?) {
      binding.run {
        editHolderName.editText!!.setText(holderName)
        editCardNumber.editText!!.setText(number)
        editExpiredDate.editText!!.setText(
          expiredMonth?.let { month ->
            expiredYear?.let { "$month/$it" }
          }
        )
        editCvc.editText!!.setText(cvc?.toString())
      }
    }

    when (val formData = viewModel.stateLiveData.value?.formData) {
      FormData.Initial -> Unit
      is FormData.Data -> {
        val (holderName, number, expiredMonth, expiredYear, cvc) = formData
        bindInitial(holderName, number, expiredMonth, expiredYear, cvc)
      }
      is FormData.Invalid -> {
        val (holderName, number, expiredMonth, expiredYear, cvc) = formData
        bindInitial(holderName, number, expiredMonth, expiredYear, cvc)
      }
    }
  }
}