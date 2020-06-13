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
import com.doancnpm.edoctor.ui.main.home.create_order.inputs.address.InputAddressFragment
import com.doancnpm.edoctor.ui.main.home.create_order.inputs.confirmation.OrderConfirmationFragment
import com.doancnpm.edoctor.ui.main.home.create_order.inputs.note.InputNoteFragment
import com.doancnpm.edoctor.ui.main.home.create_order.inputs.promotion.InputPromotionFragment
import com.doancnpm.edoctor.ui.main.home.create_order.inputs.time.InputTimeFragment
import com.doancnpm.edoctor.utils.hideKeyboard
import com.doancnpm.edoctor.utils.showAlertDialog
import com.doancnpm.edoctor.utils.snack
import com.doancnpm.edoctor.utils.viewBinding
import com.jakewharton.rxbinding4.viewpager2.pageSelections
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.awaitFirst
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import timber.log.Timber
import kotlin.LazyThreadSafetyMode.NONE

class CreateOrderFragment : BaseFragment(R.layout.fragment_create_order) {
  private val binding by viewBinding<FragmentCreateOrderBinding>()
  private val viewModel by viewModel<CreateOrderVM>() { parametersOf(navArgs.service) }
  private val navArgs by navArgs<CreateOrderFragmentArgs>()

  private val fragments by lazy(NONE) {
    listOf(
      { InputAddressFragment() },
      { InputTimeFragment() },
      { InputPromotionFragment() },
      { InputNoteFragment() },
      { OrderConfirmationFragment() },
    )
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    setupBackPressed()
    setupViews()
  }

  private fun setupBackPressed() {
    requireActivity()
      .onBackPressedDispatcher
      .addCallback(owner = viewLifecycleOwner) {
        when {
          binding.viewPager.currentItem - 1 in fragments.indices -> {
            binding.viewPager.currentItem--
          }
          else -> lifecycleScope.launch {
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
  }

  private fun setupViews() {
    val viewPager = binding.viewPager

    // Viewpager
    viewPager.run {
      adapter = CreateOrderViewPagerAdapter(this@CreateOrderFragment, fragments)
      isUserInputEnabled = false

      pageSelections()
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
          CreateOrderFragmentDirections
            .actionCreateOrderFragmentToSelectCardFragment()
            .let { findNavController().navigate(it) }
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
}

class CreateOrderViewPagerAdapter(
  fragment: Fragment,
  private val fragments: List<() -> Fragment>
) : FragmentStateAdapter(fragment) {
  override fun getItemCount() = fragments.size
  override fun createFragment(position: Int) = fragments[position]()
}