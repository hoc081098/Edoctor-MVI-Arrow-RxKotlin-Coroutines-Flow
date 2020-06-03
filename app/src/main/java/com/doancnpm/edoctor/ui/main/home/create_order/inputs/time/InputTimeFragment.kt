package com.doancnpm.edoctor.ui.main.home.create_order.inputs.time

import android.os.Bundle
import android.view.View
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.core.BaseFragment
import com.doancnpm.edoctor.databinding.FragmentInputTimeBinding
import com.doancnpm.edoctor.utils.viewBinding

class InputTimeFragment : BaseFragment(R.layout.fragment_input_time) {
  private val binding by viewBinding<FragmentInputTimeBinding>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.root
  }
}