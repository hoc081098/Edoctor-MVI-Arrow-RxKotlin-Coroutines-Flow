package com.doancnpm.edoctor.ui.main.home.create_order

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.core.BaseFragment
import com.doancnpm.edoctor.databinding.FragmentCreateOrderBinding
import com.doancnpm.edoctor.ui.main.home.create_order.inputs.address.InputAddressFragment
import com.doancnpm.edoctor.ui.main.home.create_order.inputs.promotion.InputPromotionFragment
import com.doancnpm.edoctor.ui.main.home.create_order.inputs.time.InputTimeFragment
import com.doancnpm.edoctor.utils.viewBinding
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
    setupViews()
  }

  private fun setupViews() {
    binding.viewPager.run {
      adapter = CreateOrderViewPagerAdapter(this@CreateOrderFragment, fragments)
    }
  }
}

class CreateOrderViewPagerAdapter(
  fragment: Fragment,
  private val fragments: List<Fragment>
) : FragmentStateAdapter(fragment) {
  override fun getItemCount() = fragments.size
  override fun createFragment(position: Int) = fragments[position]
}