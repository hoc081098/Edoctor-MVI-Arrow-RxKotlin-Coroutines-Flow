package com.doancnpm.edoctor.ui.main.home.create_order.inputs.address

import android.os.Bundle
import android.view.View
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.core.BaseFragment
import com.doancnpm.edoctor.databinding.FragmentInputAddressBinding
import com.doancnpm.edoctor.ui.main.home.create_order.CreateOrderVM
import com.doancnpm.edoctor.utils.viewBinding
import org.koin.androidx.viewmodel.ext.android.getViewModel
import kotlin.LazyThreadSafetyMode.NONE

class InputAddressFragment : BaseFragment(R.layout.fragment_input_address) {
  private val binding by viewBinding<FragmentInputAddressBinding>()
  private val viewModel by lazy(NONE) { requireParentFragment().getViewModel<CreateOrderVM>() }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding
    viewModel
  }
}