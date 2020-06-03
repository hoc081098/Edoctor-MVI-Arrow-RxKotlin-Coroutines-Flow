package com.doancnpm.edoctor.ui.main.home.create_order.inputs.promotion

import android.os.Bundle
import android.view.View
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.core.BaseFragment
import com.doancnpm.edoctor.databinding.FragmentInputPromotionBinding
import com.doancnpm.edoctor.utils.viewBinding

class InputPromotionFragment : BaseFragment(R.layout.fragment_input_promotion) {
  private val binding by viewBinding<FragmentInputPromotionBinding>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.root
  }
}