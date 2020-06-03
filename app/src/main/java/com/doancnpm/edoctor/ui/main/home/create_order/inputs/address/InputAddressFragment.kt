package com.doancnpm.edoctor.ui.main.home.create_order.inputs.address

import android.os.Bundle
import android.view.View
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.core.BaseFragment
import com.doancnpm.edoctor.databinding.FragmentInputAddressBinding
import com.doancnpm.edoctor.utils.viewBinding

class InputAddressFragment : BaseFragment(R.layout.fragment_input_address) {
  private val binding by viewBinding<FragmentInputAddressBinding>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.root
  }
}