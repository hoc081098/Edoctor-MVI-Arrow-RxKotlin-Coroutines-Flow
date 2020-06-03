package com.doancnpm.edoctor.ui.main.home.create_order

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.core.BaseFragment
import com.doancnpm.edoctor.databinding.FragmentCreateOrderBinding
import com.doancnpm.edoctor.databinding.FragmentHistoryBinding
import com.doancnpm.edoctor.utils.toast
import com.doancnpm.edoctor.utils.viewBinding

class CreateOrderFragment : BaseFragment(R.layout.fragment_create_order) {
  private val binding by viewBinding<FragmentCreateOrderBinding>()
  private val navArgs by navArgs<CreateOrderFragmentArgs>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.root
    navArgs.service
    requireContext().toast("Binding: $binding")
  }
}