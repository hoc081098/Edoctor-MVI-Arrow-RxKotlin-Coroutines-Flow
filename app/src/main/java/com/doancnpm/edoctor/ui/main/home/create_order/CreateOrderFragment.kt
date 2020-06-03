package com.doancnpm.edoctor.ui.main.home.create_order

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.core.BaseFragment
import com.doancnpm.edoctor.databinding.FragmentCreateOrderBinding
import com.doancnpm.edoctor.ui.main.home.create_order.inputs.address.InputAddressFragment
import com.doancnpm.edoctor.ui.main.home.create_order.inputs.promotion.InputPromotionFragment
import com.doancnpm.edoctor.ui.main.home.create_order.inputs.time.InputTimeFragment
import com.doancnpm.edoctor.utils.viewBinding
import com.jakewharton.rxbinding4.viewpager2.pageSelections
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import kotlin.LazyThreadSafetyMode.NONE

class CreateOrderFragment : BaseFragment(R.layout.fragment_create_order) {
  private val binding by viewBinding<FragmentCreateOrderBinding>()
  private val navArgs by navArgs<CreateOrderFragmentArgs>()
  private val fragments by lazy(NONE) {
    listOf(
      InputAddressFragment(),
      InputTimeFragment(),
      InputPromotionFragment(),
    )
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    requireActivity()
      .onBackPressedDispatcher
      .addCallback(owner = viewLifecycleOwner) {
        if (binding.viewPager.currentItem - 1 in fragments.indices) {
          binding.viewPager.currentItem--
        } else {
          findNavController().popBackStack()
        }
      }
    setupViews()
  }

  private fun setupViews() {
    val viewPager = binding.viewPager

    viewPager.adapter = CreateOrderViewPagerAdapter(this@CreateOrderFragment, fragments)
    viewPager
      .pageSelections()
      .subscribeBy {
        when (it) {
          0 -> binding.prevButton.isEnabled = false
          fragments.lastIndex -> binding.nextButton.isEnabled = false
          else -> {
            binding.prevButton.isEnabled = true
            binding.nextButton.isEnabled = true
          }
        }
      }
      .addTo(compositeDisposable)

    val onClickListener = View.OnClickListener {
      when (it) {
        binding.nextButton -> {
          if (viewPager.currentItem + 1 in fragments.indices) {
            viewPager.currentItem++
          }
        }
        binding.prevButton -> {
          if (viewPager.currentItem - 1 in fragments.indices) {
            viewPager.currentItem--
          }
        }
      }
    }
    binding.nextButton.setOnClickListener(onClickListener)
    binding.prevButton.setOnClickListener(onClickListener)
  }
}

class CreateOrderViewPagerAdapter(
  fragment: Fragment,
  private val fragments: List<Fragment>
) : FragmentStateAdapter(fragment) {
  override fun getItemCount() = fragments.size
  override fun createFragment(position: Int) = fragments[position]
}