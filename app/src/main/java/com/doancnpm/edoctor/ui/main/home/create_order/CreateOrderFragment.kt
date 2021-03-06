package com.doancnpm.edoctor.ui.main.home.create_order

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.core.BaseFragment
import com.doancnpm.edoctor.databinding.FragmentCreateOrderBinding
import com.doancnpm.edoctor.domain.entity.getMessage
import com.doancnpm.edoctor.ui.main.MainActivity
import com.doancnpm.edoctor.ui.main.history.HistoryContract
import com.doancnpm.edoctor.ui.main.home.create_order.inputs.address.InputAddressFragment
import com.doancnpm.edoctor.ui.main.home.create_order.inputs.confirmation.OrderConfirmationFragment
import com.doancnpm.edoctor.ui.main.home.create_order.inputs.note.InputNoteFragment
import com.doancnpm.edoctor.ui.main.home.create_order.inputs.promotion.InputPromotionFragment
import com.doancnpm.edoctor.ui.main.home.create_order.inputs.select_card.SelectCardFragment
import com.doancnpm.edoctor.ui.main.home.create_order.inputs.time.InputTimeFragment
import com.doancnpm.edoctor.utils.*
import com.jakewharton.rxbinding4.viewpager2.pageSelections
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.awaitFirst
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import timber.log.Timber
import kotlin.LazyThreadSafetyMode.NONE

@ExperimentalCoroutinesApi
class CreateOrderFragment : BaseFragment(R.layout.fragment_create_order), () -> Boolean {
  private val binding by viewBinding<FragmentCreateOrderBinding>()
  private val viewModel by viewModel<CreateOrderVM>() { parametersOf(navArgs.service) }
  private val navArgs by navArgs<CreateOrderFragmentArgs>()

  private val fragments by lazy(NONE) {
    listOf(
      { InputAddressFragment() },
      { InputTimeFragment() },
      { InputPromotionFragment() },
      { InputNoteFragment() },
      { SelectCardFragment() },
      { OrderConfirmationFragment() },
    )
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    setupBackPressed()
    setupViews()
    bindVM()
  }

  private fun bindVM() {
    viewModel.singleEventObservable
      .subscribeBy { event ->
        when (event) {
          is CreateOrderContract.SingleEvent.Error -> {
            Timber.d("Error: ${event.appError}")
            context?.toast("Submit failure: ${event.appError.getMessage()}")
          }
          CreateOrderContract.SingleEvent.MissingRequiredInput -> {
            context?.toast("Missing required input. Please fill before submitting!")
          }
          is CreateOrderContract.SingleEvent.Success -> {
            context?.toast("Submit successfully")

            findNavController().popBackStack(R.id.servicesFragment, false)
            (requireActivity() as MainActivity).navigateToHistory(
              type = HistoryContract.HistoryType.WAITING,
              orderId = event.order.id
            )
          }
        }
      }
      .addTo(compositeDisposable)
    viewModel.isSubmittingLiveData.observe(owner = viewLifecycleOwner) {
      if (it) {
        binding.progressBar.show()
      } else {
        binding.progressBar.hide()
      }
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    (requireActivity() as MainActivity).onSupportNavigateUp = null
  }

  private fun setupBackPressed() {
    requireActivity()
      .onBackPressedDispatcher
      .addCallback(owner = viewLifecycleOwner) {
        when {
          binding.viewPager.currentItem - 1 in fragments.indices -> {
            binding.viewPager.currentItem--
          }
          else -> showExitAlert()
        }
      }
  }

  private fun setupViews() {
    val viewPager = binding.viewPager

    val mainActivity = requireActivity() as MainActivity
    // Viewpager
    viewPager.run {
      adapter = CreateOrderViewPagerAdapter(this@CreateOrderFragment, fragments)
      isUserInputEnabled = false

      pageSelections()
        .distinctUntilChanged()
        .doOnNext { Timber.d("setupViews: { index: $it }") }
        .switchMap { index ->
          viewModel
            .canGoNextObservables[index]
            .map { it to index }
        }
        .subscribeBy { (nextEnabled, index) ->
          Timber.d("setupViews: { nextEnabled: $nextEnabled, index: $index }")

          binding.run {
            prevButton.isEnabled = index > 0
            nextButton.isEnabled = nextEnabled
            nextButton.text = if (index == fragments.lastIndex) "Finish" else "Next"
          }

          mainActivity.supportActionBar!!.setDisplayHomeAsUpEnabled(index == 0)
          mainActivity.onSupportNavigateUp = if (index == 0) this@CreateOrderFragment else null
        }
        .addTo(compositeDisposable)
    }

    // Previous and next button
    val onClickListener = View.OnClickListener {
      hideKeyboard()

      when (it) {
        binding.nextButton -> if (viewPager.currentItem + 1 in fragments.indices) {
          viewPager.currentItem++
        } else {
          requireActivity().showAlertDialog {
            title("Submit order")
            message("Are you sure you want to submit this order?")
            cancelable(true)
            positiveAction("OK") { _, _ -> viewModel.submit() }
            negativeAction("Cancel") { _, _ -> }
          }
        }
        binding.prevButton -> if (viewPager.currentItem - 1 in fragments.indices) {
          viewPager.currentItem--
        }
      }
    }
    binding.nextButton.setOnClickListener(onClickListener)
    binding.prevButton.setOnClickListener(onClickListener)
  }

  @Suppress("DEPRECATION")
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    childFragmentManager.fragments.forEach { it.onActivityResult(requestCode, resultCode, data) }
  }

  override fun invoke(): Boolean {
    showExitAlert()
    return false
  }

  private fun showExitAlert() {
    lifecycleScope.launch {
      if (viewModel.canGoNextObservables.first().awaitFirst() == true) {
        requireActivity().showAlertDialog {
          title("Cancel creating new order")
          message("All information will be not saved")
          cancelable(false)
          positiveAction("OK") { _, _ -> findNavController().popBackStack() }
          negativeAction("Cancel") { _, _ -> }
        }
      } else {
        findNavController().popBackStack()
      }
    }
  }
}

class CreateOrderViewPagerAdapter(
  fragment: Fragment,
  private val fragments: List<() -> Fragment>
) : FragmentStateAdapter(fragment) {
  override fun getItemCount() = fragments.size
  override fun createFragment(position: Int) = fragments[position]()
}