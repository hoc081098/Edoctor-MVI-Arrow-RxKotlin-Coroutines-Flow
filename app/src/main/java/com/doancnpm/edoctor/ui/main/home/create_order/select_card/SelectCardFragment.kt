package com.doancnpm.edoctor.ui.main.home.create_order.select_card

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.core.BaseFragment
import com.doancnpm.edoctor.databinding.FragmentSelectCardBinding
import com.doancnpm.edoctor.utils.viewBinding

class SelectCardFragment : BaseFragment(R.layout.fragment_select_card) {
  private val binding by viewBinding<FragmentSelectCardBinding>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.button.setOnClickListener {
      SelectCardFragmentDirections
        .actionSelectCardFragmentToAddCardFragment()
        .let { findNavController().navigate(it) }
    }
  }
}